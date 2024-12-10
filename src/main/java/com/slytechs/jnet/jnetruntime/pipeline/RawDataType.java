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

import java.lang.reflect.Array;
import java.util.function.Function;
import java.util.function.IntFunction;

import com.slytechs.jnet.jnetruntime.internal.util.function.FunctionalProxies;

public record RawDataType<T>(
		String name,
		Class<T> dataClass, T empty,
		Function<T[], T> arrayWrapper,
		IntFunction<T[]> arrayAllocator)
		implements DataType<T> {

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name;
	}

	private static <T> Class<T> dataClass(T emptyDataLambda) {
		Class<? extends Object> cl = emptyDataLambda.getClass();
		Class<?>[] ints = FunctionalProxies.filterFunctionalInterfaces(cl.getInterfaces());
		assert ints.length == 1 : "data must must implement exactly 1 functional interface";

		@SuppressWarnings({
				"unchecked",
				"rawtypes",
		})
		Class<T> dataClass = (Class) ints[0]; // Only 1 functional interface expected
		assert dataClass.isAssignableFrom(cl);

		return dataClass;
	}

	public RawDataType(Class<T> dataClass) {
		this(dataClass, FunctionalProxies.createNoOpProxy(dataClass));
	}

	public RawDataType(String name, Class<T> dataClass) {
		this(name, dataClass,
				FunctionalProxies.createNoOpProxy(dataClass),
				FunctionalProxies.createArrayWrapper(dataClass),
				FunctionalProxies.createArrayAllocator(dataClass));
	}

	public RawDataType(T empty) {
		this(dataClass(empty), empty);
	}

	public RawDataType(Class<T> dataClass, T empty) {
		this(dataClass, empty, FunctionalProxies.createArrayWrapper(dataClass));
	}

	public RawDataType(T empty, Function<T[], T> arrayWrapper) {
		this(dataClass(empty), empty, arrayWrapper);
	}

	public RawDataType(String name, T empty, Function<T[], T> arrayWrapper) {
		this(name, dataClass(empty), empty, arrayWrapper);
	}

	public RawDataType(String name, Class<T> dataClass, Function<T[], T> arrayWrapper) {
		this(name, dataClass,
				FunctionalProxies.createNoOpProxy(dataClass),
				arrayWrapper,
				FunctionalProxies.createArrayAllocator(dataClass));
	}

	@SuppressWarnings("unchecked")
	public RawDataType(String name, Class<T> dataClass, T empty, Function<T[], T> arrayWrapper) {
		this(name, dataClass, empty, arrayWrapper, size -> (T[]) Array.newInstance(dataClass(empty), size));
	}

	public RawDataType(Class<T> dataClass, T empty, Function<T[], T> arrayWrapper) {
		this(dataClass.getSimpleName(), dataClass, empty, arrayWrapper);
	}

	public RawDataType(Class<T> dataClass, T empty, Function<T[], T> arrayWrapper,
			IntFunction<T[]> arrayAllocator) {
		this(dataClass.getSimpleName(), dataClass, empty, arrayWrapper, arrayAllocator);
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.DataType#wrapArray(java.lang.Object[])
	 */
	@Override
	public T wrapArray(T[] array) {
		return arrayWrapper.apply(array);
	}

}