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

import static java.util.function.Predicate.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.slytechs.jnet.jnetruntime.pipeline.DataProcessor.ProcessorFactory;
import com.slytechs.jnet.jnetruntime.pipeline.DataProcessor.ProcessorFactory.Named;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.InputFactory;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.InputFactory.Arg2;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.InputTransformer;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.OutputFactory;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.OutputTransformer;

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
 * 
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 */
public class AbstractPipeline<T, T_PIPE extends Pipeline<T, T_PIPE>>
		extends AbstractComponent<T_PIPE>
		implements Pipeline<T, T_PIPE> {

	private final DataType dataType;
	private final HeadNode<T> head;
	private final TailNode<T> tail;

	private final List<AbstractProcessor<T, ?>> initializedList = new ArrayList<>();
	private final List<AbstractProcessor<T, ?>> activeList = new ArrayList<>();

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

		initializedList.add(head);
		initializedList.add(tail);
		Collections.sort(initializedList);
	}

	/**
	 * Adds a new processor to the pipeline.
	 *
	 * @param newProcessor The processor to add
	 * @throws IllegalStateException if the processor is already initialized in this
	 *                               pipeline
	 */
	private synchronized void addNewProcessor0(AbstractProcessor<T, ?> newProcessor) {
		if (initializedList.contains(newProcessor))
			throw new IllegalStateException("processor [%s] already initialized in pipeline [%s]"
					.formatted(newProcessor.name(), name()));

		newProcessor.setRegistration(() -> removeProcessor0(newProcessor));

		initializedList.add(newProcessor);
		Collections.sort(initializedList);

		if (newProcessor.isEnabled())
			linkProcessor(newProcessor);
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized HeadNode<T> head() {
		return head;
	}

	/**
	 * Links a processor to the active list of processors in the pipeline.
	 *
	 * @param processor The processor to link
	 * @throws IllegalStateException if the processor is already active in this
	 *                               pipeline
	 */
	synchronized void linkProcessor(AbstractProcessor<T, ?> processor) {
		if (activeList.contains(processor))
			throw new IllegalStateException("processor [%s] already active in pipeline [%s]"
					.formatted(processor.name(), name()));

		activeList.add(processor);
		Collections.sort(activeList);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onEnable(boolean newValue) {
		initializedList.stream()
				.filter(PipeComponent::isEnabled)
				.filter(not(activeList::contains))
				.forEach(activeList::add);

		relinkActiveNodes();
	}

	/**
	 * Handles the enabling or disabling of a processor node.
	 *
	 * @param processor The processor that has been enabled or disabled
	 */
	synchronized void onNodeEnable(DataProcessor<T, ?> processor) {
		if (!isEnabled())
			return; // Not enabled, nothing to do

		if (!(processor instanceof AbstractProcessor<T, ?> aprocessor))
			throw new IllegalStateException("invalid processor implementation [%s]"
					.formatted(processor.name()));

		boolean b = aprocessor.isEnabled();

		if (b == activeList.contains(aprocessor))
			return;

		if (b)
			linkProcessor(aprocessor);
		else
			unlinkProcessor(aprocessor);

		// List modified, need to relink nodes
		relinkActiveNodes();
	}

	/**
	 * Handles changes in the output of a processor.
	 *
	 * @param processor The processor whose output has changed
	 */
	synchronized void onOutputChange(AbstractProcessor<T, ?> processor) {
		// Implementation not provided in the original code
	}

	/**
	 * Relinks all active nodes in the pipeline. This method is called when the
	 * pipeline structure changes.
	 */
	private synchronized void relinkActiveNodes() {
		Collections.sort(activeList);

		AbstractProcessor<T, ?> prev = null;

		for (AbstractProcessor<T, ?> p : activeList) {
			if (prev != null)
				prev.nextProcessor(p);

			prev = p;
		}

		reRegisterActiveNodes();
	}

	/**
	 * Removes a processor from the pipeline.
	 *
	 * @param processor The processor to remove
	 */
	private synchronized void removeProcessor0(AbstractProcessor<T, ?> processor) {
		if (activeList.remove(processor))
			relinkActiveNodes();

		initializedList.remove(processor);
	}

	/**
	 * Re-registers all active nodes in the pipeline. This method is called after
	 * relinking to ensure proper data flow.
	 */
	private void reRegisterActiveNodes() {
		for (AbstractProcessor<T, ?> p : activeList.reversed()) {
			p.reLinkData();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized TailNode<T> tail() {
		return tail;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
//		String initStr = initializedList.stream()
//				.map(p -> (p.isEnabled() ? "(%s)" : "(!%s)").formatted(p.name()))
//				.collect(Collectors.joining(",", "[", "]"));

		String activeStr = activeList.stream()
				.filter(not(AbstractComponent::isBuiltin))
				.map(p -> (p.isEnabled() ? "(%s)" : "(!%s)").formatted(p.name()))
				.collect(Collectors.joining("->", "[", "]"));

		String inputStr = head.inputsToString();
		String outStr = tail.outputsToString();

		return ""
				+ getClass().getSimpleName()
				+ " ["
				+ "==>" + inputStr
				+ "==>" + activeStr
				+ "==>" + outStr
				+ "]";
	}

	/**
	 * Unlinks a processor from the active list of processors in the pipeline.
	 *
	 * @param processor The processor to unlink
	 */
	synchronized void unlinkProcessor(AbstractProcessor<T, ?> processor) {
		processor.nextProcessor(null);
		activeList.remove(processor);

		relinkActiveNodes();
	}

	/**
	 * Unregisters a processor from the pipeline.
	 *
	 * @param processor The processor to unregister
	 * @throws IllegalStateException if the processor is not registered in this
	 *                               pipeline
	 */
	void unregisterProcessor(AbstractProcessor<T, ?> processor) {
		if (!initializedList.contains(processor) || processor.registration().isEmpty())
			throw new IllegalStateException("Processor [%s] is not registered"
					.formatted(name()));

		processor.enable(false);
		initializedList.remove(processor);

		// Reset processor registration
		processor.setRegistration(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T_INPUT extends InputTransformer<?>> T_INPUT addInput(InputFactory<T, T_INPUT> factory) {
		T_INPUT input = factory.newInstance(head);

		head.addInput((AbstractInput<?, T, ?>) input, input.inputType());

		return input;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T_INPUT extends InputTransformer<?>> T_INPUT addInput(String id, InputFactory<T, T_INPUT> factory) {
		T_INPUT input = factory.newInstance(head);

		head.addInput((AbstractInput<?, T, ?>) input, id);

		return input;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T_OUTPUT extends OutputTransformer<?>> T_OUTPUT addOutput(String id, OutputFactory<T, T_OUTPUT> factory) {
		T_OUTPUT output = factory.newInstance(tail);

		tail.addOutput((AbstractOutput<T, ?, ?>) output, id);

		return output;
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
	public <T_INPUT extends InputTransformer<?>, T1> T_INPUT addInput(
			String id,
			T1 arg1,
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
	public <T_INPUT extends InputTransformer<?>, T1, T2> T_INPUT addInput(
			String id,
			T1 arg1,
			T2 arg2,
			Arg2<T, T_INPUT, T1, T2> factory) {

		T_INPUT input = factory.newInstance2Args(head, arg1, arg2);

		head.addInput((AbstractInput<?, T, ?>) input, id);

		return input;
	}
}