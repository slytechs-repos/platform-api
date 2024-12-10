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

import java.util.Collection;
import java.util.function.IntFunction;

import com.slytechs.jnet.jnetruntime.util.Named;

public interface DataTypeInfo extends Named {

	<T> DataType<T> dataType();

	@Override
	default String name() {
		return dataType().name();
	}

	@SuppressWarnings("unchecked")
	default <T> Class<T> dataClass() {
		return (Class<T>) dataType().dataClass();
	}

	@SuppressWarnings("unchecked")
	default <T> T empty() {
		return (T) dataType().empty();
	}

	@SuppressWarnings("unchecked")
	default <T> T wrapArray(T[] array) {
		return (T) dataType().optimizeArray(array);
	}

	@SuppressWarnings({
			"unchecked",
			"rawtypes"
	})
	default <T> T wrapCollection(Collection<T> collection) {
		return (T) dataType().optimizeCollection((Collection) collection);
	}

	@SuppressWarnings({ "rawtypes",
			"unchecked"
	})
	default <T> IntFunction<T[]> arrayAllocator() {
		return (IntFunction) dataType().arrayAllocator();
	}
}