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

/**
 * @author Mark Bednarczyk
 *
 */
public abstract class PassthroughProcessor<T, T_BASE extends PassthroughProcessor<T, T_BASE>>
		extends AbstractProcessor<T, T_BASE>
		implements DataPassthrough<T, T_BASE> {

	/**
	 * @param priority
	 * @param name
	 * @param dataType
	 */
	public PassthroughProcessor(int priority, String name, DataType dataType) {
		super(priority, name, dataType);
	}

	/**
	 * @param pipeline
	 * @param priority
	 * @param name
	 * @param type
	 * @param data
	 */
	public PassthroughProcessor(Pipeline<T, ?> pipeline, int priority, String name, DataType type, T data) {
		super(pipeline, priority, name, type, data);
	}

	/**
	 * @param pipeline
	 * @param priority
	 * @param name
	 * @param type
	 */
	public PassthroughProcessor(Pipeline<T, ?> pipeline, int priority, String name, DataType type) {
		super(pipeline, priority, name, type);
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.AbstractProcessor#inputData()
	 */
	@Override
	public final T inputData() {
		return outputData();
	}

	@Override
	public String dataToString() {
		return "%s".formatted(ID(outputData));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return ""
				+ getClass().getSimpleName()
				+ " [priority=" + priority()
				+ ", enabled=" + isEnabled()
				+ ", output=" + ID(outputData())
				+ ", input=" + ID(inputData())
				+ ", dataType=" + dataType()
				+ ", name=" + name()
				+ "]";
	}
}
