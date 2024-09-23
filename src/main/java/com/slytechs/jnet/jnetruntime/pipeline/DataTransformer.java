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

import com.slytechs.jnet.jnetruntime.util.HasName;

public interface DataTransformer<T_IN, T_OUT, T_BASE extends DataTransformer<T_IN, T_OUT, T_BASE>>
		extends HasName, PipeComponent<T_BASE> {

	interface PipelineInput<T_DATA> extends HasDataInput<T_DATA> {

		interface Factory<T_DATA, T_BASE extends PipelineInput<T_DATA>> {
			T_BASE newInputBuilder();
		}

		interface Factory1Arg<T_DATA, T_ARG1> {
			PipelineInput<T_DATA> newInputBuilder(T_ARG1 arg1);
		}

		interface Factory2Args<T_DATA, T_ARG1, T_ARG2> {
			PipelineInput<T_DATA> newInputBuilder(T_ARG1 arg1, T_ARG2 arg2);
		}

		@Override
		DataType inputType();

		@Override
		T_DATA inputData();

	}

	interface PipelineOutput<T_OUT, T_BASE extends PipelineOutput<T_OUT, T_BASE>>
			extends HasDataOutput<T_OUT>, PipeComponent<T_BASE> {

		@Override
		DataType outputType();

		@Override
		T_OUT outputData();
	}

	@Override
	T_BASE enable(boolean b);

	@Override
	boolean isEnabled();

	DataType inputType();

	DataType outputType();

	@Override
	T_BASE name(String newName);

	@Override
	T_BASE enable(BooleanSupplier b);
}