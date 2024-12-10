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

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.slytechs.jnet.jnetruntime.pipeline.InputTransformer.InputMapper;
import com.slytechs.jnet.jnetruntime.util.Named;
import com.slytechs.jnet.jnetruntime.util.Registration;

/**
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public class Head<T> extends Processor<T> {

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[IN "
				+ inputsById.values().stream()
						.map(InputTransformer::toString)
						.collect(Collectors.joining(" OR "))
				+ "]";
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Processor#getOutput()
	 */
	@Override
	public T getOutput() {
		if (outputData == null)
			throw new IllegalStateException("no output for pipeline");

		return outputData;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Processor#getInput()
	 */
	@Override
	public final T getInput() {
		return getOutput();
	}

	private final Map<Object, InputTransformer<?, T>> inputsById = new HashMap<>();

	Head(Pipeline<T> pipeline) {
		super(-1, "head");

		super.initialize(pipeline);
	}

	public Registration registerInput(InputTransformer<?, T> newInput) {

		var id = newInput.id();
		newInput.head = this;

		inputsById.put(id, newInput);

		return () -> inputsById.remove(id);
	}

	private static final class DefaultInputTransformer<IN, OUT> extends InputTransformer<IN, OUT> {

		/**
		 * @param id
		 * @param mapper
		 */
		public DefaultInputTransformer(Object id, InputMapper<IN, OUT> mapper) {
			super(id, mapper);

			String name = Named.toName(id);
			setName(name);
		}

	}

	public <IN> Registration addInput(String name, InputMapper<IN, T> mapper) {

		var input = new DefaultInputTransformer<IN, T>(name, mapper);

		return registerInput(input);
	}

	@SuppressWarnings("unchecked")
	public <IN> IN connector(Object id) {
		readLock.lock();

		try {
			var p = inputsById.get(id);
			if (p == null)
				throw pipeline.inputNotFoundException(id);

			return (IN) p.getInput();
		} finally {
			readLock.unlock();
		}
	}

}
