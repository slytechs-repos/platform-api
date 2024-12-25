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
package com.slytechs.jnet.platform.api.data.pipeline.transform;

import java.util.function.Supplier;

import com.slytechs.jnet.platform.api.data.pipeline.Transformer;

public interface InputMapper<IN, OUT> {
	public static abstract class GenericInputMapper<IN, T> implements InputMapper<IN, T> {

		private InputMapper<IN, T> proxy;

		public GenericInputMapper(InputMapper<IN, T> proxy) {
			this.proxy = proxy;
		}

		/**
		 * @see InputMapper#createMappedInput(java.util.function.Supplier)
		 */
		@Override
		public IN createMappedInput(Supplier<T> sink, Transformer<IN, T> input) {
			return proxy.createMappedInput(sink, input);
		}

	}

	interface SimpleInputMapper<IN, OUT> extends InputMapper<IN, OUT> {
		IN createMappedInput(Supplier<OUT> sink);

		@Override
		default IN createMappedInput(Supplier<OUT> sink, Transformer<IN, OUT> input) {
			return createMappedInput(sink);
		}

	}

	IN createMappedInput(Supplier<OUT> sink, Transformer<IN, OUT> input);
}