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
import com.slytechs.jnet.jnetruntime.util.Registration;

public interface DataTransformer<T_IN, T_OUT, T_BASE extends DataTransformer<T_IN, T_OUT, T_BASE>>
		extends HasName, PipeComponent<T_BASE> {

	interface InputFactory<T, T_BASE extends InputEntryPoint<T>> {
		T_BASE newInstance();
	}

	interface InputEntryPoint<T> extends HasInputData<T> {

	}

	interface OutputFactory<T, T_BASE extends OutputEndPoint<T>> {
		T_BASE newInstance();
	}

	interface OutputEndPoint<T> extends HasOutputData<T> {
		T addOutputData(T data);

		Registration registerOutputData(T data);
	}

	@Override
	T_BASE enable(boolean b);

	@Override
	T_BASE enable(BooleanSupplier b);

	DataType inputType();

	@Override
	boolean isEnabled();

	@Override
	T_BASE name(String newName);

	DataType outputType();

}