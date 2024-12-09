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

import com.slytechs.jnet.jnetruntime.pipeline.OutputTransformer.OutputMapper;
import com.slytechs.jnet.jnetruntime.util.Registration;

/**
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public class Tail<T> {

	private final Pipeline<T> pipeline;

	Tail(Pipeline<T> pipeline) {
		this.pipeline = pipeline;
	}

	public Registration registerOutputTransformer(OutputTransformer<?, T> newOutput) {
		return null;
	}

	public Registration registerOutputTransformer(OutputTransformer.OutputFactory<?, T, ?> newOutput) {
		return null;
	}

	public <OUT> Registration addOutputTransformer(int priority, String name, OutputMapper<OUT, T> sink) {

		OutputTransformer<?, T> output = new OutputTransformer<OUT, T>(priority, name) {};

		return registerOutputTransformer(output);
	}

	public <OUT> OutputTransformer<OUT, T> getOutputTransformer(String name) {
		return null;
	}

	public <OUT> OutputTransformer<OUT, T> getOutputTransformer(DataType inType) {
		return null;
	}

}
