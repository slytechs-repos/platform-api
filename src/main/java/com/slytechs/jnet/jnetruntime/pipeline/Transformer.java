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

import com.slytechs.jnet.jnetruntime.util.HasName;

public interface Transformer<T_IN, T_OUT, T_BASE extends Transformer<T_IN, T_OUT, T_BASE>> extends HasName {
	interface TransformerFactory<T_IN, T_OUT, T_TRANSFORMER extends Transformer<T_IN, T_OUT, T_TRANSFORMER>> {

		T_TRANSFORMER newInstance();
	}

	interface NamedTransformerFactory<T_IN, T_OUT, T_TRANSFORMER extends Transformer<T_IN, T_OUT, T_TRANSFORMER>> {
		T_TRANSFORMER newInstance(String name);
	}

	T_BASE enable(boolean b);

	boolean isEnabled();

	T_IN getInput();

	T_OUT getOutput();

	DataType outputType();

	DataType inputType();
}