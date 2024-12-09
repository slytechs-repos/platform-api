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

import com.slytechs.jnet.jnetruntime.pipeline.InputTransformer.InputMapper;
import com.slytechs.jnet.jnetruntime.util.Registration;

/**
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public class Head<T> {

	private final Pipeline<T> pipeline;

	Head(Pipeline<T> pipeline) {
		this.pipeline = pipeline;
	}

	public Registration registerInputTransformer(InputTransformer<?, T> newInput) {
		return null;
	}

	public Registration registerInputTransformer(InputTransformer.InputFactory<?, T, ?> newInputFactory) {
		return null;
	}

	public <IN> Registration addInputTransformer(String name, InputMapper<IN, T> mapper) {

		var input = new InputTransformer<IN, T>(name, mapper) {};

		return registerInputTransformer(input);
	}

	public T getOutput() {
		return null;
	}

	public <IN> IN getInputTransformer(String name) {
		return null;
	}

	public <IN> IN getInputTransformer(DataType inType) {
		return null;
	}

	public <IN> IN getInputTransformer(Class<IN> dataClass) {
		return null;
	}

}
