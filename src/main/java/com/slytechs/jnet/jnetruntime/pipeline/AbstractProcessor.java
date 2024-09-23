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

/**
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 */
public class AbstractProcessor<T, T_BASE extends DataProcessor<T, T_BASE>>
		extends AbstractComponent<T_BASE>
		implements DataProcessor<T, T_BASE> {

	public static class Dummy<T> extends AbstractProcessor<T, Dummy<T>> {

		Dummy(DataType type) {
			super(type);
		}

	}

	private int priority;

	private final DataType dataType;
	private final T data;

	private final AbstractPipeline<T, ?> pipeline;
	private AbstractProcessor<T, ?> nextProcessor;

	void nextProcessor(AbstractProcessor<T, ?> next) {
		this.nextProcessor = next;
	}

	AbstractProcessor<T, ?> nextProcessor() {
		return nextProcessor;
	}

	private AbstractProcessor(DataType dataType) {
		super("dummy");

		this.pipeline = null;
		this.dataType = dataType;
		this.data = null;
	}

	@SuppressWarnings("unchecked")
	public AbstractProcessor(Pipeline<T, ?> pipeline, int priority, String name, DataType type) {
		super(name);

		if (!(pipeline instanceof AbstractPipeline<T, ?> ap))
			throw new IllegalArgumentException("only AbstractPipeline types are supported");

		this.pipeline = ap;
		this.priority = priority;
		this.data = (T) this;
		this.dataType = type;
	}

	AbstractProcessor(Pipeline<T, ?> pipeline, int priority, String name, T data, DataType type) {
		super(name);

		if (!(pipeline instanceof AbstractPipeline<T, ?> ac))
			throw new IllegalArgumentException("only AbstractPipeline types are supported");

		this.pipeline = ac;
		this.priority = priority;
		this.data = data;
		this.dataType = type;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.DataProcessor#data()
	 */
	@Override
	public T data() {
		return data;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.DataProcessor#dataType()
	 */
	@Override
	public DataType dataType() {
		return dataType;
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

}
