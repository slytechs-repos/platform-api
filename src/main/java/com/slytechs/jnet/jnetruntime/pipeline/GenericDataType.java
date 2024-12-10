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
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import com.slytechs.jnet.jnetruntime.internal.util.function.FunctionalProxies;

/**
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public abstract class GenericDataType<T> implements DataType<T> {

	/**
	 * Recursively builds a string representation of a Type object.
	 */
	private static String getSimpleTypeName(Type type) {
		if (type instanceof Class<?>) {
			return ((Class<?>) type).getSimpleName();
		}

		if (type instanceof ParameterizedType) {
			ParameterizedType paramType = (ParameterizedType) type;
			String rawType = getSimpleTypeName(paramType.getRawType());
			String args = Arrays.stream(paramType.getActualTypeArguments())
					.map(GenericDataType::getSimpleTypeName)
					.collect(Collectors.joining(", "));

			return rawType + "<" + args + ">";
		}

		if (type instanceof WildcardType) {
			WildcardType wildcardType = (WildcardType) type;
			Type[] upperBounds = wildcardType.getUpperBounds();
			Type[] lowerBounds = wildcardType.getLowerBounds();

			if (lowerBounds.length > 0) {
				return "? super " + getSimpleTypeName(lowerBounds[0]);

			} else if (upperBounds.length > 0 && !upperBounds[0].equals(Object.class)) {
				return "? extends " + getSimpleTypeName(upperBounds[0]);
			}

			return "?";
		}

		if (type instanceof TypeVariable)
			return ((TypeVariable<?>) type).getName();

		if (type instanceof GenericArrayType)
			return getSimpleTypeName(((GenericArrayType) type).getGenericComponentType()) + "[]";

		return type.toString();
	}

	/**
	 * Recursively builds a string representation of a Type object.
	 */
	private static String getTypeName(Type type) {
		if (type instanceof Class<?>)
			return ((Class<?>) type).getName();

		if (type instanceof ParameterizedType) {
			ParameterizedType paramType = (ParameterizedType) type;
			String rawType = getTypeName(paramType.getRawType());
			String args = Arrays.stream(paramType.getActualTypeArguments())
					.map(GenericDataType::getTypeName)
					.collect(Collectors.joining(", "));

			return rawType + "<" + args + ">";
		}

		if (type instanceof WildcardType) {
			WildcardType wildcardType = (WildcardType) type;
			Type[] upperBounds = wildcardType.getUpperBounds();
			Type[] lowerBounds = wildcardType.getLowerBounds();

			if (lowerBounds.length > 0) {
				return "? super " + getTypeName(lowerBounds[0]);

			} else if (upperBounds.length > 0 && !upperBounds[0].equals(Object.class)) {
				return "? extends " + getTypeName(upperBounds[0]);
			}

			return "?";
		}

		if (type instanceof TypeVariable)
			return ((TypeVariable<?>) type).getName();

		if (type instanceof GenericArrayType)
			return getTypeName(((GenericArrayType) type).getGenericComponentType()) + "[]";

		return type.toString();
	}

	private final Class<T> dataClass;

	private final T empty;

	private final Function<T[], T> arrayWrapper;

	private final IntFunction<T[]> arrayAllocator;
	private final String name;
	private final Type type;

	protected GenericDataType() {
		this((String) null);
	}

	@SuppressWarnings("unchecked")
	protected GenericDataType(String name) {
		Type superclass = getClass().getGenericSuperclass();
		if (!(superclass instanceof ParameterizedType paramType)) {
			throw new IllegalArgumentException("GenericDataType must be created with generic type information");
		}

		this.type = paramType.getActualTypeArguments()[0];
		this.dataClass = (Class<T>) ((ParameterizedType) type).getRawType();

		this.empty = FunctionalProxies.createNoOpProxy(dataClass);
		this.arrayWrapper = FunctionalProxies.createArrayWrapper(dataClass);
		this.arrayAllocator = size -> (T[]) Array.newInstance(dataClass, size);
		this.name = (name == null) ? getSimpleTypeName(type) : name;
	}

	@SuppressWarnings("unchecked")
	protected GenericDataType(String name, T empty) {
		Type superclass = getClass().getGenericSuperclass();
		if (!(superclass instanceof ParameterizedType paramType)) {
			throw new IllegalArgumentException("GenericDataType must be created with generic type information");
		}

		this.type = paramType.getActualTypeArguments()[0];
		this.dataClass = (Class<T>) ((ParameterizedType) type).getRawType();

		this.empty = empty;
		this.arrayWrapper = FunctionalProxies.createArrayWrapper(dataClass);
		this.arrayAllocator = size -> (T[]) Array.newInstance(dataClass, size);
		this.name = (name == null) ? getSimpleTypeName(type) : name;
	}

	@SuppressWarnings("unchecked")
	protected GenericDataType(String name, T empty, Function<T[], T> arrayWrapper) {
		Type superclass = getClass().getGenericSuperclass();
		if (!(superclass instanceof ParameterizedType paramType)) {
			throw new IllegalArgumentException("GenericDataType must be created with generic type information");
		}

		this.type = paramType.getActualTypeArguments()[0];
		this.dataClass = (Class<T>) ((ParameterizedType) type).getRawType();

		this.empty = empty;
		this.arrayWrapper = arrayWrapper;
		this.arrayAllocator = size -> (T[]) Array.newInstance(dataClass, size);
		this.name = (name == null) ? getSimpleTypeName(type) : name;
	}

	@SuppressWarnings("unchecked")
	protected GenericDataType(String name, T empty, Function<T[], T> arrayWrapper, IntFunction<T[]> arrayAllocator) {
		Type superclass = getClass().getGenericSuperclass();
		if (!(superclass instanceof ParameterizedType paramType)) {
			throw new IllegalArgumentException("GenericDataType must be created with generic type information");
		}

		this.type = paramType.getActualTypeArguments()[0];
		this.dataClass = (Class<T>) ((ParameterizedType) type).getRawType();

		this.empty = empty;
		this.arrayWrapper = arrayWrapper;
		this.arrayAllocator = arrayAllocator;
		this.name = (name == null) ? getSimpleTypeName(type) : name;
	}

	protected GenericDataType(T empty) {
		this(null, empty);
	}

	protected GenericDataType(T empty, Function<T[], T> arrayWrapper) {
		this(null, empty, arrayWrapper);
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.DataType#arrayAllocator()
	 */
	@Override
	public IntFunction<T[]> arrayAllocator() {
		return arrayAllocator;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.DataType#dataClass()
	 */
	@Override
	public Class<T> dataClass() {
		return dataClass;
	}

	@Override
	public Type dataParameterizedType() {
		return type;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.DataType#empty()
	 */
	@Override
	public T empty() {
		return empty;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null)
			return false;

		if (!(obj instanceof DataType<?> other))
			return false;

		return Objects.equals(name, other.name()) && Objects.equals(type, other.dataParameterizedType());
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(name, type);
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.DataType#name()
	 */
	@Override
	public String name() {
		return name;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DataType [name=" + name + ", dataClass=" + getTypeName(type) + "]";
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.DataType#wrapArray(java.lang.Object[])
	 */
	@Override
	public T wrapArray(T[] array) {
		return arrayWrapper.apply(array);
	}
}
