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
public class AbstractNode<T, T_BASE extends PipelineNode<T, T_BASE>> implements PipelineNode<T, T_BASE> {

	public static class Dummy<T> extends AbstractNode<T, Dummy<T>> {

		Dummy(DataType type) {
			super(type);
		}

	}

	private int priority;
	private boolean enabled = true;

	PipelineNode<T, T_BASE> next;
	private final DataType dataType;
	private final T data;
	private String name;

	Registration registration;
	private final AbstractPipeline<T, ?> pipeline;

	private AbstractNode(DataType dataType) {
		this.pipeline = null;
		this.name = "dummy";
		this.dataType = dataType;
		this.data = null;
	}

	@SuppressWarnings("unchecked")
	public AbstractNode(Pipeline<T, ?> pipeline, int priority, String name, DataType type) {
		if (!(pipeline instanceof AbstractPipeline<T, ?> ap))
			throw new IllegalArgumentException("only AbstractPipeline types are supported");

		this.pipeline = ap;
		this.priority = priority;
		this.data = (T) this;
		this.dataType = type;
		this.name = name;
	}

	AbstractNode(Pipeline<T, ?> pipeline, int priority, String name, T data, DataType type) {
		if (!(pipeline instanceof AbstractPipeline<T, ?> ac))
			throw new IllegalArgumentException("only AbstractPipeline types are supported");

		this.pipeline = ac;
		this.priority = priority;
		this.data = data;
		this.dataType = type;
		this.name = name;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.PipelineNode#enable(boolean)
	 */
	@Override
	public PipelineNode<T, T_BASE> enable(boolean b) {
		this.enabled = b;

		// Notify the parent pipe of node state changes
		pipeline.onNodeEnable(this);

		return this;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.PipelineNode#getData()
	 */
	@Override
	public T getData() {
		return data;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.PipelineNode#getDataType()
	 */
	@Override
	public DataType getDataType() {
		return dataType;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.PipelineNode#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return this.enabled;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.PipelineNode#name()
	 */
	@Override
	public String name() {
		return name;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.PipelineNode#nextNode()
	 */
	@Override
	public PipelineNode<T, T_BASE> nextNode() {
		return next;
	}

	AbstractPipeline<T, ?> parent() {
		return pipeline;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.PipelineNode#priority()
	 */
	@Override
	public int priority() {
		return priority;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.PipelineNode#priority(int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T_BASE priority(int newPriority) {
		this.priority = newPriority;

		return (T_BASE) this;
	}

}
