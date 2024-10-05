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
 * @author Mark Bednarczyk
 *
 */
public class PeekProcessor<T>
		extends AbstractProcessor<T, PeekProcessor<T>> {

//	private final DataProxyMethodHandle<T> proxyHandle;

	/**
	 * @param pipeline
	 * @param priority
	 * @param name
	 * @param type
	 */
	public PeekProcessor(Pipeline<T, ?> pipeline, int priority) {
		super(pipeline, priority, "peek", pipeline.dataType(), null);

//		this.proxyHandle = new DataProxyMethodHandle<>(pipeline.dataType().dataClass(), null);
//		inputData(proxyHandle.getProxy());
	}

	public PeekProcessor<T> peek(T peekAction) {
		addExternalOutput(peekAction);

		return this;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.AbstractProcessor#inputData()
	 */
	@Override
	public T inputData() {
		return outputData();
	}

}
