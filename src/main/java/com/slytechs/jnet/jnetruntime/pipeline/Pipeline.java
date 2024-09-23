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
import java.util.function.Supplier;

import com.slytechs.jnet.jnetruntime.pipeline.DataProcessor.ProcessorFactory;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.PipelineInput;
import com.slytechs.jnet.jnetruntime.util.HasName;
import com.slytechs.jnet.jnetruntime.util.Id;
import com.slytechs.jnet.jnetruntime.util.Registration;

/**
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 *
 */
public interface Pipeline<T, T_PIPE extends Pipeline<T, T_PIPE>> extends HasName, PipeComponent<T_PIPE> {

	interface Head<T, T_HEAD extends Head<T, T_HEAD>> extends DataProcessor<T, T_HEAD> {

		<T_IN extends DataTransformer<T_IN, T, ?>> T_IN addInputGet(Supplier<T_IN> input, Id id);

		default <T_IN extends DataTransformer<T_IN, T, ?>> T_IN addInput(T_IN input, Id id) {
			return addInputGet(() -> input, id);
		}

		<T_IN extends DataTransformer<T_IN, T, ?>> T_IN getInput(Id id);

		<T_IN extends DataTransformer<T_IN, T, ?>> Registration registerInputGet(Supplier<T_IN> input, Id id);

		default <T_IN extends DataTransformer<T_IN, T, ?>> Registration registerInput(T_IN input, Id id) {
			return registerInputGet(() -> input, id);
		}

		Registration addInput(PipelineInput<?> input);
	}

	interface Tail<T, T_TAIL extends Tail<T, T_TAIL>> extends DataProcessor<T, T_TAIL> {
		<T_OUT extends DataTransformer<T_OUT, T, ?>> Registration registerOutputGet(Supplier<T_OUT> output, Id id);

		default <T_OUT extends DataTransformer<T_OUT, T, ?>> Registration registerOutput(T_OUT output, Id id) {
			return registerOutputGet(() -> output, id);
		}
	}

	int SOURCE_NODE_PRIORITY = -1;
	int SINK_NODE_PRIORITY = Integer.MAX_VALUE;

	<T_PROC extends DataProcessor<T, T_PROC>> T_PROC addProcessor(int priority, String name,
			ProcessorFactory<T, T_PROC> factory);

	@Override
	T_PIPE bypass(boolean b);

	@Override
	default T_PIPE bypass(BooleanSupplier b) {
		return bypass(b.getAsBoolean());
	}

	DataType dataType();

	Head<T, ?> head();

	@Override
	boolean isBypassed();

	<T_DATA, T_BUILDER extends PipelineInput<T_DATA>> T_BUILDER newInputBuilder(
			PipelineInput.Factory<T_DATA, T_BUILDER> factory);

	<T_DATA, T_ARG1> T_DATA newInputData(T_ARG1 arg1, PipelineInput.Factory1Arg<T_DATA, T_ARG1> factory);

	<T_DATA, T_ARG1, T_ARG2> T_DATA newInputData(T_ARG1 arg1, T_ARG2 arg2, PipelineInput.Factory2Args<T_DATA, T_ARG1, T_ARG2> factory);

	Registration addInput(PipelineInput<?> input);
	
	Tail<T, ?> tail();
}
