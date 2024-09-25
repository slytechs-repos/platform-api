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

import java.util.function.BooleanSupplier;

import com.slytechs.jnet.jnetruntime.NotFound;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.InputEntryPoint;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.InputFactory;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.OutputEndPoint;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.OutputFactory;
import com.slytechs.jnet.jnetruntime.util.HasName;
import com.slytechs.jnet.jnetruntime.util.Registration;

/**
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 *
 */
public interface Pipeline<T, T_BASE extends Pipeline<T, T_BASE>> extends HasName, PipeComponent<T_BASE> {

	interface Head<T, T_HEAD extends Head<T, T_HEAD>> extends DataProcessor<T, T_HEAD> {

		<T_IN> Registration registerInput(DataType type, InputFactory<T_IN, InputEntryPoint<T_IN>> factory);

		<T_IN> Registration registerInput(InputEntryPoint<T_IN> input);

		<T_IN> InputEntryPoint<T_IN> getInput(DataType type) throws NotFound;

		<T_IN> InputEntryPoint<T_IN> getInput(DataType type, String id) throws NotFound;

		@SuppressWarnings("unchecked")
		default <T_IN> T_IN getInputData(DataType type) throws NotFound {
			return (T_IN) getInput(type).inputData();
		}

		@SuppressWarnings("unchecked")
		default <T_IN> T_IN getInputData(DataType type, String id) throws NotFound {
			return (T_IN) getInput(type, id).inputData();
		}

	}

	interface Tail<T, T_BASE extends Tail<T, T_BASE>> extends DataProcessor<T, T_BASE> {
		<T_OUT> Registration registerOutput(DataType type, OutputFactory<T_OUT, OutputEndPoint<T_OUT>> factory);

		<T_OUT> Registration registerOutput(OutputEndPoint<T_OUT> output);

		<T_OUT> OutputEndPoint<T_OUT> getOutput(DataType type) throws NotFound;

		<T_OUT> OutputEndPoint<T_OUT> getOutput(DataType type, String id) throws NotFound;

	}

	int HEAD_BUILTIN_PRIORITY = -1;
	int TAIL_BUILTIN_PRIORITY = Integer.MAX_VALUE;

	default <T_IN> Registration registerInput(DataType type, InputFactory<T_IN, InputEntryPoint<T_IN>> factory) {
		return head().registerInput(type, factory);
	}

	default <T_IN> Registration registerInput(InputEntryPoint<T_IN> input) {
		return head().registerInput(input);
	}

	default <T_OUT> Registration registerOutput(DataType type, OutputFactory<T_OUT, OutputEndPoint<T_OUT>> factory) {
		return tail().registerOutput(type, factory);
	}

	default <T_OUT> Registration registerOutput(OutputEndPoint<T_OUT> output) {
		return tail().registerOutput(output);
	}

	default <T_OUT> OutputEndPoint<T_OUT> getOutput(DataType type) throws NotFound {
		return tail().getOutput(type);
	}

	Registration registerProcessor(int priority, DataProcessor<T, ? extends DataProcessor<T, ?>> processor);

	@Override
	T_BASE bypass(boolean b);

	@Override
	default T_BASE bypass(BooleanSupplier b) {
		return bypass(b.getAsBoolean());
	}

	DataType dataType();

	Head<T, ?> head();

	@Override
	boolean isBypassed();

	Tail<T, ?> tail();
}
