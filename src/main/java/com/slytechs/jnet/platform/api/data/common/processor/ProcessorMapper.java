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
package com.slytechs.jnet.platform.api.data.common.processor;

import java.util.function.Supplier;

/**
 * Interface for mapping processor data between different representations. Used
 * to transform data as it flows through the processing chain.
 *
 * @param <T> Type of data being mapped
 */
public interface ProcessorMapper<T> {

	/**
	 * Simplified processor mapper for basic transformations that don't require full
	 * processor context.
	 *
	 * @param <T> Type of mapped data
	 */
	interface SimpleProcessorMapper<T> extends ProcessorMapper<T> {

		/**
		 * Creates mapped processor data from sink supplier.
		 *
		 * @param sink Supplies downstream data
		 * @return Mapped processor data
		 */
		T createMappedProcessor(Supplier<T> sink);

		/**
		 * @see com.slytechs.jnet.platform.api.data.common.processor.ProcessorMapper#createMappedProcessor(java.util.function.Supplier,
		 *      com.slytechs.jnet.platform.api.data.common.processor.Processor)
		 */
		@Override
		default T createMappedProcessor(Supplier<T> sink, Processor<T> processor) {
			return createMappedProcessor(sink);
		}

	}

	/**
	 * Creates mapped processor data with full processor context.
	 *
	 * @param sink      Supplies downstream data
	 * @param processor Parent processor for context
	 * @return Mapped processor data
	 */

	T createMappedProcessor(Supplier<T> sink, Processor<T> processor);
}