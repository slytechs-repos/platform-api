/*
 * Sly Technologies Free License
 * 
 * Copyright 2024 Sly Technologies Inc.
 *
 * Licensed under the Sly Technologies Free License (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.slytechs.com/free-license-text
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.slytechs.jnet.jnetruntime.pipeline;

import static com.slytechs.jnet.jnetruntime.pipeline.PipelineUtils.*;
import static java.util.function.Predicate.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import com.slytechs.jnet.jnetruntime.pipeline.DataProcessor.ProcessorFactory;
import com.slytechs.jnet.jnetruntime.pipeline.DataProcessor.ProcessorFactory.Named;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.InputFactory;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.InputFactory.Arg2;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.InputTransformer;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.OutputFactory;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.OutputTransformer;
import com.slytechs.jnet.jnetruntime.util.DoublyLinkedPriorityQueue;

/**
 * Abstract implementation of a data processing pipeline.
 * 
 * <p>
 * This class provides a foundation for creating data processing pipelines with
 * customizable inputs, processors, and outputs. It manages the lifecycle of
 * pipeline components, including initialization, activation, and deactivation
 * of processors.
 * </p>
 * 
 * <p>
 * The pipeline consists of three main parts:
 * <ul>
 * <li>A head node for managing inputs</li>
 * <li>A list of processors for data transformation</li>
 * <li>A tail node for managing outputs</li>
 * </ul>
 * </p>
 *
 * @param <T>      The type of data processed by this pipeline
 * @param <T_PIPE> The specific type of the pipeline implementation
 * @author Mark Bednarczyk
 */
public class AbstractPipeline<T, T_PIPE extends Pipeline<T, T_PIPE>>
		extends AbstractComponent<T_PIPE>
		implements Pipeline<T, T_PIPE> {

	/** The data type. */
	private final DataType dataType;

	/** The head. */
	private final HeadNode<T> head;

	/** The tail. */
	private final TailNode<T> tail;

	/** The initialized list. */
	private final List<AbstractProcessor<T, ?>> initializedProcessors = new ArrayList<>();

	/** The active list. */
	private final Queue<AbstractProcessor<T, ?>> activeProcessors = new DoublyLinkedPriorityQueue<>();

	/**
	 * Constructs a new AbstractPipeline with the specified name and data type.
	 *
	 * @param name     The name of the pipeline
	 * @param dataType The type of data processed by this pipeline
	 */
	public AbstractPipeline(String name, DataType dataType) {
		super(name);

		this.dataType = dataType;
		this.head = new HeadNode<>(this, "head".formatted(name), dataType);
		this.tail = new TailNode<>(this, "tail".formatted(name), dataType);

		activeProcessors.add(head);
		activeProcessors.add(tail);
	}

	private void activateAllProcessorsStillNotActive() {
		initializedProcessors.stream()
				.filter(PipeComponent::isEnabled)
				.filter(not(activeProcessors::contains))
				.forEach(this::activateProcessor);
	}

	/**
	 * Links a processor to the active list of processors in the pipeline.
	 *
	 * @param processor The processor to link
	 * @throws IllegalStateException if the processor is already active in this
	 *                               pipeline
	 */
	void activateProcessor(AbstractProcessor<T, ?> processor) {
		try {
			writeLock.lock();

			var isAdded = activeProcessors.add(processor);
			if (!isAdded)
				throw new IllegalStateException("processor [%s] already active in pipeline [%s]"
						.formatted(processor.name(), name()));

			var prevProcessor = processor.prevProcessor;
			prevProcessor.calculateOutput();

		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T_INPUT extends InputTransformer<?>> T_INPUT addInput(InputFactory<T, T_INPUT> factory) {
		T_INPUT input = factory.newInstance(head);

		addNewInput0((AbstractInput<?, T, ?>) input, input.inputType());

		return input;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T_INPUT extends InputTransformer<?>> T_INPUT addInput(String id,
			InputFactory<T, T_INPUT> factory) {
		T_INPUT input = factory.newInstance(head);

		addNewInput0((AbstractInput<?, T, ?>) input, id);

		return input;
	}

	private void addNewInput0(AbstractInput<?, T, ?> input, Object id) {
		head.addInput(input, id);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T_INPUT extends InputTransformer<?>, T1> T_INPUT addInput(String id, T1 arg1,
			InputFactory.Arg1<T, T_INPUT, T1> factory) {
		T_INPUT input = factory.newInstance1Arg(head, arg1);

		head.addInput((AbstractInput<?, T, ?>) input, id);

		return input;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T_INPUT extends InputTransformer<?>, T1, T2> T_INPUT addInput(String id, T1 arg1, T2 arg2,
			Arg2<T, T_INPUT, T1, T2> factory) {

		T_INPUT input = factory.newInstance2Args(head, arg1, arg2);

		addNewInput0((AbstractInput<?, T, ?>) input, id);

		return input;
	}

	/**
	 * Adds a new processor to the pipeline.
	 *
	 * @param newProcessor The processor to add
	 * @throws IllegalStateException if the processor is already initialized in this
	 *                               pipeline
	 */
	private void addNewProcessor0(AbstractProcessor<T, ?> newProcessor) {
		try {
			writeLock.lock();

			if (initializedProcessors.contains(newProcessor))
				throw new IllegalStateException("processor [%s] already initialized in pipeline [%s]"
						.formatted(newProcessor.name(), name()));

			newProcessor.setRegistration(() -> unregisterProcessor(newProcessor));

			initializedProcessors.add(newProcessor);
			Collections.sort(initializedProcessors);

			if (newProcessor.isEnabled())
				activateProcessor(newProcessor);

		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T_OUTPUT extends OutputTransformer<?>> T_OUTPUT addOutput(OutputFactory<T, T_OUTPUT> factory) {
		T_OUTPUT output = factory.newInstance(tail);

		tail.addOutput((AbstractOutput<T, ?, ?>) output, output.outputType());

		return output;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T_OUTPUT extends OutputTransformer<?>> T_OUTPUT addOutput(String id,
			OutputFactory<T, T_OUTPUT> factory) {
		T_OUTPUT output = factory.newInstance(tail);

		tail.addOutput((AbstractOutput<T, ?, ?>) output, id);

		return output;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T_PROC extends DataProcessor<T, T_PROC>> T_PROC addProcessor(int priority,
			ProcessorFactory<T, T_PROC> processorFactory) {

		T_PROC p = processorFactory.newProcessor(this, priority);

		addNewProcessor0((AbstractProcessor<T, ?>) p);

		return p;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T_PROC extends DataProcessor<T, T_PROC>> T_PROC addProcessor(int priority,
			String name,
			Named<T, T_PROC> processorFactory) {

		T_PROC p = processorFactory.newProcessor(this, priority, name);

		addNewProcessor0((AbstractProcessor<T, ?>) p);

		return p;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataType dataType() {
		return dataType;
	}

	void deactivateProcessor(AbstractProcessor<T, ?> processor) {
		try {
			writeLock.lock();

			var prevProcessor = processor.prevProcessor;

			var isRemoved = activeProcessors.remove(processor);
			if (!isRemoved)
				throw new IllegalStateException("processor [%s] already inactive in pipeline [%s]"
						.formatted(processor.name(), name()));

			prevProcessor.calculateOutput();

		} finally {
			writeLock.unlock();
		}
	}

	void resortProcessor(AbstractProcessor<T, ?> processor) {
		try {
			writeLock.lock();

			if (processor.isEnabled()) {
				deactivateProcessor(processor);
				activateProcessor(processor);
			}

		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HeadNode<T> head() {
		return head;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onEnable(boolean newValue) {
		activateAllProcessorsStillNotActive();
	}

	/**
	 * Handles changes in the output of a processor.
	 *
	 * @param processor The processor whose output has changed
	 */
	void onOutputChange(AbstractProcessor<T, ?> processor) {
		// Implementation not provided in the original code
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TailNode<T> tail() {
		return tail;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
//		String initStr = initializedList.stream()
//				.map(p -> (p.isEnabled() ? "(%s)" : "(!%s)").formatted(p.name()))
//				.collect(Collectors.joining(",", "[", "]"));deactivateProcessor

		String activeStr = activeProcessors.stream()
//				.filter(not(AbstractComponent::isBuiltin))
				.map(p -> (p.isEnabled() ? "%s(%s=>%s)" : "!%s(%s=>%s)")
						.formatted(p.name(), ID(p.inputData()), ID(p.outputData())))
				.collect(Collectors.joining(", ", "P[", "]"));

		String inputStr = head.inputsToString();
		String outStr = tail.outputsToString();

		return ""
				+ getClass().getSimpleName()
				+ " ["
				+ inputStr
				+ "=>" + activeStr
				+ "=>" + outStr
				+ "]";
	}

	/**
	 * Unregisters a processor from the pipeline.
	 *
	 * @param processor The processor to Unregisters
	 */
	private void unregisterProcessor(AbstractProcessor<T, ?> processor) {

		try {
			writeLock.lock();

			deactivateProcessor(processor);
			initializedProcessors.remove(processor);

			// Reset processor registration
			processor.setRegistration(null);

		} finally {
			writeLock.unlock();
		}

		assert initializedProcessors.contains(processor) == false;
		assert activeProcessors.contains(processor) == false;
	}
}