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

import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.OutputEndPoint;
import com.slytechs.jnet.jnetruntime.util.Registration;

/**
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 *
 */
public class AbstractOutput<T_IN, T_OUT, T_BASE extends DataTransformer<T_IN, T_OUT, T_BASE>>
		extends AbstractTransformer<T_IN, T_OUT, T_BASE>
		implements OutputEndPoint<T_OUT> {

	/**
	 * @param name
	 * @param inputType
	 * @param outputType
	 */
	public AbstractOutput(String name, DataType inputType, DataType outputType) {
		super(name, inputType, outputType);

		this.outputDataList = new DataList<>(outputType);
		this.outputDataList.addChangeListener(super::outputData);

	}

	/**
	 * @param name
	 * @param input
	 * @param inputType
	 * @param outputType
	 */
	public AbstractOutput(String name, T_IN input, DataType inputType, DataType outputType) {
		super(name, input, inputType, outputType);

		this.outputDataList = new DataList<>(outputType);
		this.outputDataList.addChangeListener(super::outputData);
	}

	private AbstractProcessor<T_IN, ?> parentProcessor;
	private final DataList<T_OUT> outputDataList;

	/**null
	 * @see com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.OutputEndPoint#registerOutputData(java.lang.Object)
	 */
	@Override
	public Registration registerOutputData(T_OUT data) {
		outputDataList.add(data);

		return () -> outputDataList.remove(data);
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.OutputEndPoint#addOutputData(java.lang.Object)
	 */
	@Override
	public T_OUT addOutputData(T_OUT data) {

		registerOutputData(data);

		return data;
	}

}
