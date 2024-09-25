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

import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.InputEntryPoint;

/**
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 *
 */
public class AbstractInput<T_IN, T_OUT, T_BASE extends DataTransformer<T_IN, T_OUT, T_BASE>>
		extends AbstractTransformer<T_IN, T_OUT, T_BASE>
		implements InputEntryPoint<T_IN> {

	AbstractProcessor<T_OUT, ?> headNode;

	/**
	 * @param name
	 * @param inputType
	 * @param outputType
	 */
	public AbstractInput(String name, DataType inputType, DataType outputType) {
		super(name, inputType, outputType);
	}

	/**
	 * @param name
	 * @param input
	 * @param inputType
	 * @param outputType
	 */
	public AbstractInput(String name, T_IN input, DataType inputType, DataType outputType) {
		super(name, input, inputType, outputType);
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.AbstractTransformer#outputData()
	 */
	@Override
	public T_OUT outputData() {
		return headNode.outputData();
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.AbstractTransformer#outputData(java.lang.Object)
	 */
	@Override
	T_OUT outputData(T_OUT output) {
		throw new UnsupportedOperationException("not implemented yet");
	}

}
