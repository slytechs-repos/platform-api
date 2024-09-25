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
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.OutputEndPoint;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.OutputFactory;
import com.slytechs.jnet.jnetruntime.pipeline.Pipeline.Tail;
import com.slytechs.jnet.jnetruntime.util.Registration;

final class TailNode<T>
		extends BuiltinNode<T, TailNode<T>>
		implements Tail<T, TailNode<T>> {

	private final Map<Object, AbstractOutput<T, ?, ?>> outputMap = new HashMap<>();
	private final Map<DataType, OutputFactory<?, ? extends OutputEndPoint<?>>> factoryMap = new HashMap<>();

	/**
	 * @param priority
	 * @param name     public Registration addInput(InputEntryPoint<?> input) {
	 * 
	 * @param type
	 */
	public TailNode(Pipeline<T, ?> parent, String name, DataType type) {
		super(parent, Pipeline.TAIL_BUILTIN_PRIORITY, name, type, type.empty());

		enable(true);
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Pipeline.Tail#getOutput(com.slytechs.jnet.jnetruntime.pipeline.DataType)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T_OUT> OutputEndPoint<T_OUT> getOutput(DataType type) throws NotFound {
		if (!outputMap.containsKey(type)) {
			AbstractOutput<T, Object, ?> out = getOutputFromFactory(type);

			outputMap.put(type, out);

			Registration r2 = registerLocalOutput(out.inputData());

			Registration r1 = () -> {
				r2.unregister();
				outputMap.remove(out);
			};

			out.setRegistration(r1);

		}

		return (OutputEndPoint<T_OUT>) outputMap.get(type);
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Pipeline.Tail#getOutput(com.slytechs.jnet.jnetruntime.pipeline.DataType,
	 *      java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T_OUT> OutputEndPoint<T_OUT> getOutput(DataType type, String id) throws NotFound {
		if (!outputMap.containsKey(id)) {
			AbstractOutput<T, Object, ?> out = getOutputFromFactory(type);

			outputMap.put(id, out);

			Registration r2 = registerLocalOutput(out.inputData());

			Registration r1 = () -> {
				r2.unregister();
				outputMap.remove(out);
			};

			out.setRegistration(r1);
		}

		return (OutputEndPoint<T_OUT>) outputMap.get(type);
	}

	@SuppressWarnings("unchecked")
	private <T_OUT> AbstractOutput<T, T_OUT, ?> getOutputFromFactory(DataType type) throws NotFound {
		var factory = factoryMap.get(type);
		if (factory == null)
			throw new NotFound("input type [%s] not found"
					.formatted(type));

		return (AbstractOutput<T, T_OUT, ?>) factory.newInstance();
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Pipeline.Tail#registerOutput(com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.OutputEndPoint)
	 */
	@Override
	public <T_OUT> Registration registerOutput(DataType type, OutputFactory<T_OUT, OutputEndPoint<T_OUT>> factory) {

		factoryMap.put(type, factory);

		return () -> {
			factoryMap.remove(type);
		};
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Pipeline.Tail#registerOutput(com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.OutputEndPoint)
	 */
	@Override
	public <T_OUT> Registration registerOutput(OutputEndPoint<T_OUT> output) {
		return registerOutput(output.outputType(), () -> output);
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.AbstractProcessor#register()
	 */
	@Override
	void register() {
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.AbstractProcessor#inputData()
	 */
	@Override
	public T inputData() {
		return outputData();
	}

}