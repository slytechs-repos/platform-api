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

import com.slytechs.jnet.jnetruntime.util.Registration;

/**
 * Abstract base class for data processors in a pipeline.
 * 
 * <p>
 * This class implements the core functionality of a data processor, including
 * input and output data management, priority handling, and pipeline
 * integration. It serves as a foundation for creating specific data processor
 * implementations.
 * </p>
 *
 * @param <T>      The type of data processed by this processor
 * @param <T_BASE> The specific type of the processor implementation
 *
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 */
public class AbstractProcessor<T, T_BASE extends DataProcessor<T, T_BASE>>
		extends AbstractComponent<T_BASE>
		implements DataProcessor<T, T_BASE>, Comparable<DataProcessor<?, ?>> {

	/**
	 * A dummy processor used for internal operations.
	 *
	 * @param <T> The type of data processed by this dummy processor
	 */
	static class Dummy<T> extends AbstractProcessor<T, Dummy<T>> {
		Dummy(DataType type) {
			super(type);
		}
	}

	private int priority;
	private final DataType dataType;
	private T inputData;
	private T outputData; // Auto maintained and updated by DataList object
	private final DataList<T> outputList;
	private final AbstractPipeline<T, ?> pipeline;
	private AbstractProcessor<T, ?> nextProcessor;
	private Registration nextLinkRegistration;

	/**
	 * Constructs a dummy processor with the specified data type.
	 *
	 * @param dataType The data type for this processor
	 */
	private AbstractProcessor(DataType dataType) {
		super("dummy");
		this.pipeline = null;
		this.dataType = dataType;
		this.inputData = null;
		this.outputList = new DataList<>(dataType, this::updateOutput);
		enable(true);
	}

	/**
	 * Constructs a new processor with the specified parameters.
	 *
	 * @param pipeline The pipeline this processor belongs to
	 * @param priority The priority of this processor
	 * @param name     The name of this processor
	 * @param type     The data type this processor handles
	 * @throws IllegalArgumentException if the pipeline is invalid or the processor
	 *                                  class is incompatible with the data type
	 */
	@SuppressWarnings("unchecked")
	public AbstractProcessor(Pipeline<T, ?> pipeline, int priority, String name, DataType type) {
		super(name);
		if (!(pipeline instanceof AbstractPipeline<T, ?> apipeline))
			throw new IllegalArgumentException("invalid pipeline [%s]".formatted(pipeline.name()));
		if (!type.isCompatibleWith(getClass()))
			throw new IllegalArgumentException("processor subclass must implement data interface [%s]"
					.formatted(type.dataClass()));
		this.pipeline = apipeline;
		this.priority = priority;
		this.inputData = (T) this;
		this.dataType = type;
		this.outputList = new DataList<>(type, this::updateOutput);
		enable(true);
	}

	/**
	 * Constructs a new processor with the specified parameters and initial data.
	 *
	 * @param pipeline The pipeline this processor belongs to
	 * @param priority The priority of this processor
	 * @param name     The name of this processor
	 * @param type     The data type this processor handles
	 * @param data     The initial data for this processor
	 * @throws IllegalArgumentException if the pipeline is invalid
	 */
	AbstractProcessor(Pipeline<T, ?> pipeline, int priority, String name, DataType type, T data) {
		super(name);
		if (!(pipeline instanceof AbstractPipeline<T, ?> ac))
			throw new IllegalArgumentException("invalid pipeline [%s]".formatted(pipeline.name()));
		this.pipeline = ac;
		this.priority = priority;
		this.inputData = data;
		this.dataType = type;
		this.outputList = new DataList<>(type, this::updateOutput);
		enable(true);
	}

	/**
	 * Adds a local output to this processor's output list.
	 *
	 * @param localOutput The local output to add
	 * @return A Registration object for unregistering the output
	 */
	final protected Registration addOutputToNode(T localOutput) {
		outputList.addFirst(localOutput);
		return () -> outputList.remove(localOutput);
	}

	/**
	 * Compares this processor to another based on priority. Processors are sorted
	 * from lowest to highest priority value.
	 *
	 * @param o The other processor to compare to
	 * @return A negative integer, zero, or a positive integer as this processor has
	 *         lower, equal to, or higher priority than the specified processor
	 */
	@Override
	public int compareTo(DataProcessor<?, ?> o) {
		if (this.priority == o.priority())
			return 0;
		return (this.priority < o.priority()) ? -1 : 1;
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
	public T inputData() {
		assert inputData != null : "[%s].inputData() data is null".formatted(name());
		return inputData;
	}

	/**
	 * Sets the input data for this processor.
	 *
	 * @param newInputData The new input data
	 */
	void inputData(T newInputData) {
		this.inputData = newInputData;
		if (inputData == null)
			inputData = inputType().empty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataType inputType() {
		return this.dataType;
	}

	/**
	 * Gets the next processor in the pipeline.
	 *
	 * @return The next processor
	 */
	final AbstractProcessor<T, ?> nextProcessor() {
		return nextProcessor;
	}

	/**
	 * Sets the next processor in the pipeline.
	 *
	 * @param next The next processor to set
	 */
	void nextProcessor(AbstractProcessor<T, ?> next) {
		unregister();
		this.nextProcessor = next;
	}

	/**
	 * Gets the next non-bypassed processor in the pipeline.
	 *
	 * @return The next non-bypassed processor
	 */
	final AbstractProcessor<T, ?> nextProcessorNotBypassed() {
		AbstractProcessor<T, ?> p = nextProcessor();
		while (p.isBypassed())
			p = p.nextProcessor();
		return p;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onEnable(boolean newValue) {
		pipeline.onNodeEnable(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T outputData() {
		assert outputData != null : "[%s].outputData() data is null".formatted(name());
		return outputData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataType outputType() {
		return this.dataType;
	}

	/**
	 * Gets the parent pipeline of this processor.
	 *
	 * @return The parent pipeline
	 */
	AbstractPipeline<T, ?> parent() {
		return pipeline;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int priority() {
		return priority;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T_BASE priority(int newPriority) {
		this.priority = newPriority;
		return us();
	}

	/**
	 * Re-links the data connections in the pipeline.
	 */
	void reLinkData() {
		if (nextLinkRegistration != null)
			unregister();
		final T nextData = nextProcessorNotBypassed().inputData();
		assert nextData != null : "[%s].nextProcessorNotBypassed() returns null".formatted(name());
		outputList.addLast(nextData);
		assert outputData != null : "[%s].register() output data is null".formatted(name());
		nextLinkRegistration = () -> {
			outputList.remove(nextData);
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return ""
				+ getClass().getSimpleName()
				+ " [priority=" + priority
				+ ", enabled=" + isEnabled()
				+ ", dataType=" + dataType
				+ ", name=" + name()
				+ "]";
	}

	/**
	 * Unregisters this processor from the pipeline.
	 */
	final void unregister() {
		if (nextLinkRegistration != null)
			nextLinkRegistration.unregister();
		this.nextLinkRegistration = null;
	}

	/**
	 * Updates the output data of this processor.
	 *
	 * @param newOutput The new output data
	 */
	private void updateOutput(T newOutput) {
		this.outputData = newOutput;
	}
}