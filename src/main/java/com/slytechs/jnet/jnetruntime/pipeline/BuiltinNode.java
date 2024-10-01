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
 * Abstract class representing a built-in node in a data processing pipeline.
 * Built-in nodes are fundamental components of the pipeline with fixed
 * priorities.
 *
 * @param <T>      The type of data processed by this node
 * @param <T_BASE> The specific type of the data processor implementation
 * @author Mark Bednarczyk
 */
abstract class BuiltinNode<T, T_BASE extends DataProcessor<T, T_BASE>>
		extends AbstractProcessor<T, T_BASE> {

	/**
	 * Constructs a new BuiltinNode with the specified parameters.
	 *
	 * @param pipeline The pipeline this node belongs to
	 * @param priority The priority of this node
	 * @param name     The name of this node
	 * @param type     The data type processed by this node
	 */
	public BuiltinNode(Pipeline<T, ?> pipeline, int priority, String name, DataType type) {
		super(pipeline, priority, name, type);
	}

	/**
	 * Constructs a new BuiltinNode with the specified parameters and initial output
	 * data.
	 *
	 * @param pipeline The pipeline this node belongs to
	 * @param priority The priority of this node
	 * @param name     The name of this node
	 * @param type     The data type processed by this node
	 * @param dataOut  The initial output data
	 */
	public BuiltinNode(Pipeline<T, ?> pipeline, int priority, String name, DataType type, T dataOut) {
		super(pipeline, priority, name, type, dataOut);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws UnsupportedOperationException always, as built-in nodes have fixed
	 *                                       priorities
	 */
	@Override
	public final T_BASE priority(int newPriority) {
		throw new UnsupportedOperationException("builtin node's priority is read-only");
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return true, as this is a built-in node
	 */
	@Override
	public boolean isBuiltin() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return A string representation of this BuiltinNode
	 */
	@Override
	public String toString() {
		return ""
				+ getClass().getSimpleName()
				+ " ["
				+ "" + dataType()
				+ "," + PipelineUtils.enableFlagLabel(isEnabled())
				+ "," + PipelineUtils.bypassFlagLabel(isBypassed())
				+ "]";
	}
}