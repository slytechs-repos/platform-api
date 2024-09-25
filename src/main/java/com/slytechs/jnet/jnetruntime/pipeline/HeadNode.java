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

import com.slytechs.jnet.jnetruntime.NotFound;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.InputEntryPoint;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.InputFactory;
import com.slytechs.jnet.jnetruntime.pipeline.Pipeline.Head;
import com.slytechs.jnet.jnetruntime.util.Registration;

final class HeadNode<T>
		extends BuiltinNode<T, HeadNode<T>>
		implements Head<T, HeadNode<T>> {

	private final Map<Object, AbstractInput<?, T, ?>> inputMap = new HashMap<>();
	private final Map<DataType, InputFactory<?, ? extends InputEntryPoint<?>>> factoryMap = new HashMap<>();

	/**
	 * @param priority
	 * @param name
	 * @param type
	 */
	public HeadNode(Pipeline<T, ?> parent, String name, DataType type) {
		super(parent, Pipeline.HEAD_BUILTIN_PRIORITY, name, type, null);

		enable(true);
	}

	/**
	 * @throws NotFound
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Pipeline.Head#getInput(com.slytechs.jnet.jnetruntime.pipeline.DataType)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T_IN> InputEntryPoint<T_IN> getInput(DataType type) throws NotFound {
		if (!inputMap.containsKey(type)) {
			var t = getInputFactory(type);

			inputMap.put(type, t);
		}

		return (InputEntryPoint<T_IN>) inputMap.get(type);
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Pipeline.Head#getInput(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T_IN> InputEntryPoint<T_IN> getInput(DataType type, String id) throws NotFound {
		if (!inputMap.containsKey(id)) {
			var t = getInputFactory(type);

			inputMap.put(id, t);
		}

		return (InputEntryPoint<T_IN>) inputMap.get(type);
	}

	@SuppressWarnings("unchecked")
	private <T_IN> AbstractInput<T_IN, T, ?> getInputFactory(DataType type) throws NotFound {
		var factory = factoryMap.get(type);
		if (factory == null)
			throw new NotFound("input type [%s] not found"
					.formatted(type));

		var in = (AbstractInput<T_IN, T, ?>) factory.newInstance();

		in.headNode = this;

		return in;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.AbstractProcessor#nextProcessor(com.slytechs.jnet.jnetruntime.pipeline.AbstractProcessor)
	 */
	@Override
	void nextProcessor(AbstractProcessor<T, ?> next) {
		super.nextProcessor(next);

		T out = (next == null)
				? null
				: next.inputData();

		inputMap.values().forEach(t -> t.outputData(out));
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Pipeline.Head#registerInput(com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.InputFactory)
	 */
	@Override
	public <T_IN> Registration registerInput(DataType type, InputFactory<T_IN, InputEntryPoint<T_IN>> factory) {

		factoryMap.put(type, factory);

		return () -> {
			factoryMap.remove(type);
		};
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Pipeline.Head#registerInput(com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.InputEntryPoint)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T_IN> Registration registerInput(InputEntryPoint<T_IN> input) {
		if (!(input instanceof AbstractInput ainput))
			throw new IllegalArgumentException("unsupported input entry point implementation");

		ainput.headNode = this;

		var r = registerInput(input.inputType(), () -> input);

		return () -> {
			r.unregister();
			ainput.headNode = null;
		};
	}

}