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
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 */
public class AbstractProcessor<T, T_BASE extends DataProcessor<T, T_BASE>>
		extends AbstractComponent<T_BASE>
		implements DataProcessor<T, T_BASE>, Comparable<DataProcessor<?, ?>> {

	static class Dummy<T> extends AbstractProcessor<T, Dummy<T>> {

		Dummy(DataType type) {
			super(type);
		}

	}

	private int priority;

	private final DataType dataType;
	private final T inputData;
	private T outputData; // Auto maintained and updated by DataList object
	private final DataList<T> outputList;

	private final AbstractPipeline<T, ?> pipeline;

	private AbstractProcessor<T, ?> nextProcessor;
	private Registration nextProcessorRegistration;

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.HasOutputData#outputData()
	 */
	@Override
	public T outputData() {
		assert outputData != null : "[%s].outputData() data is null".formatted(name());
		return outputData;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.DataProcessor#inputData()
	 */
	@Override
	public T inputData() {
		assert inputData != null : "[%s].inputData() data is null".formatted(name());
		return inputData;
	}

	void register() {
		if (nextProcessorRegistration != null)
			throw new IllegalStateException("regstration already exists for processor [%s]"
					.formatted(toString()));

		final T nextData = nextProcessorNotBypassed()
				.inputData();

		assert nextData != null : "[%s].nextProcessorNotBypassed() returns null".formatted(name());

		// DataList automatically maintains the outputData field integrity
		outputList.addLast(nextData);
		assert outputData != null : "[%s].register() output data is null".formatted(name());

		nextProcessorRegistration = () -> {
			outputList.remove(nextData);
		};
	}

	final protected Registration registerLocalOutput(T localOutput) {
		outputList.addFirst(localOutput);

		return () -> outputList.remove(localOutput);
	}

	final void unregister() {
		if (nextProcessorRegistration != null)
			nextProcessorRegistration.unregister();

		this.nextProcessorRegistration = null;
	}

	private AbstractProcessor(DataType dataType) {
		super("dummy");

		this.pipeline = null;
		this.dataType = dataType;
		this.inputData = null;
		this.outputList = new DataList<>(dataType, this::updateOutput);
	}

	@SuppressWarnings("unchecked")
	public AbstractProcessor(Pipeline<T, ?> pipeline, int priority, String name, DataType type) {
		super(name);

		if (!(pipeline instanceof AbstractPipeline<T, ?> apipeline))
			throw new IllegalArgumentException("only AbstractPipeline types are supported");

		if (!type.isCompatibleWith(getClass()))
			throw new IllegalArgumentException("processor subclass must implement data interface [%s]"
					.formatted(type.dataClass()));

		this.pipeline = apipeline;
		this.priority = priority;
		this.inputData = (T) this;
		this.dataType = type;
		this.outputList = new DataList<>(type, this::updateOutput);
	}

	AbstractProcessor(Pipeline<T, ?> pipeline, int priority, String name, DataType type, T data) {
		super(name);

		if (!(pipeline instanceof AbstractPipeline<T, ?> ac))
			throw new IllegalArgumentException("only AbstractPipeline types are supported");

		this.pipeline = ac;
		this.priority = priority;
		this.inputData = data;
		this.dataType = type;
		this.outputList = new DataList<>(type, this::updateOutput);
	}

	private void updateOutput(T newOutput) {
		this.outputData = newOutput;
	}

	/**
	 * For sorting, lowest priority value to the highest.
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(DataProcessor<?, ?> o) {

		// To prevent integer precision rollover, use comparison method instead of
		// subtraction of the 2.

		if (this.priority == o.priority())
			return 0;

		return (this.priority < o.priority())
				? -1
				: 1;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.DataProcessor#dataType()
	 */
	@Override
	public DataType dataType() {
		return dataType;
	}

	final AbstractProcessor<T, ?> nextProcessor() {
		return nextProcessor;
	}

	void nextProcessor(AbstractProcessor<T, ?> next) {
		unregister();
		this.nextProcessor = next;
	}

	final AbstractProcessor<T, ?> nextProcessorNotBypassed() {
		AbstractProcessor<T, ?> p = nextProcessor();

		while (p.isBypassed())
			p = p.nextProcessor();

		return p;
	}

	@Override
	protected void onEnable(boolean newValue) {
		// Notify the parent pipe of node state changes
		pipeline.onNodeEnable(this);
	}

	AbstractPipeline<T, ?> parent() {
		return pipeline;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.DataProcessor#priority()
	 */
	@Override
	public int priority() {
		return priority;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.DataProcessor#priority(int)
	 */
	@Override
	public T_BASE priority(int newPriority) {
		this.priority = newPriority;

		return us();
	}

	/**
	 * @see java.lang.Object#toString()
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
	 * @see com.slytechs.jnet.jnetruntime.pipeline.HasOutputData#outputType()
	 */
	@Override
	public DataType outputType() {
		return this.dataType;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.HasInputData#inputType()
	 */
	@Override
	public DataType inputType() {
		return this.dataType;
	}

}
