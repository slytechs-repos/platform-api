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

abstract class BuiltinNode<T, T_BASE extends DataProcessor<T, T_BASE>>
		extends AbstractProcessor<T, T_BASE> {

	/**
	 * @param pipeline
	 * @param priority
	 * @param name
	 * @param type
	 */
	public BuiltinNode(Pipeline<T, ?> pipeline, int priority, String name, DataType type) {
		super(pipeline, priority, name, type);
	}

	public BuiltinNode(Pipeline<T, ?> pipeline, int priority, String name, DataType type, T dataOut) {
		super(pipeline, priority, name, type, dataOut);
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.AbstractProcessor#priority(int)
	 */
	@Override
	public final T_BASE priority(int newPriority) {
		throw new UnsupportedOperationException("builtin node's priority is read-only");
	}

	/**
	 * @see java.lang.Object#toString()
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