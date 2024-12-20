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
package com.slytechs.jnet.platform.api.pipeline;

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

import com.slytechs.jnet.platform.api.internal.util.function.FunctionalProxies;

/**
 * A generic data type literal that determines raw and generic data types at
 * runtime. This class implements the {@link DataType} interface and provides
 * functionality similar to Guice's TypeLiteral.
 * 
 * <p>
 * The DataLiteral class works with the FunctionalProxies utility to create proxy
 * implementations when actual method references are not provided. This ensures
 * seamless operation with data type handlers in the pipeline platform.
 * </p>
 * 
 * <p>
 * Key features:
 * <ul>
 * <li>Runtime type information extraction</li>
 * <li>Automatic proxy generation for missing components</li>
 * <li>Support for generic type parameters</li>
 * <li>Flexible initialization options</li>
 * </ul>
 * </p>
 *
 * @param <T> The type parameter representing the data type
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc
 * @see DataType
 * @see FunctionalProxies
 */
public class DataLiteral<T> implements DataType<T> {

	/**
	 * Creates a new DataType instance from a given object by extracting its generic
	 * type information.
	 * 
	 * <p>
	 * This factory method:
	 * <ul>
	 * <li>Extracts generic type information from the object</li>
	 * <li>Creates proxy implementations for required functionality</li>
	 * <li>Constructs a fully initialized DataLiteral instance</li>
	 * </ul>
	 * </p>
	 *
	 * @param <T> The type parameter
	 * @param obj The object to extract type information from
	 * @return A new DataType instance
	 * @throws IllegalArgumentException if generic type information is not available
	 */
	public static <T> DataType<T> from(Object obj) {
		Type superclass = obj.getClass().getGenericSuperclass();
		if (!(superclass instanceof ParameterizedType paramType)) {
			throw new IllegalArgumentException("DataLiteral must be created with generic type information [%s]"
					.formatted(obj));
		}

		var type = paramType.getActualTypeArguments()[0];

		Class<T> dataClass = getDataType(type);

		var empty = FunctionalProxies.createNoOpProxy(dataClass);
		var arrayWrapper = FunctionalProxies.createArrayWrapper(dataClass);
		var arrayAllocator = FunctionalProxies.createArrayAllocator(dataClass);
		var name = getSimpleTypeName(type);

		return new DataLiteral<>(name, dataClass, empty, arrayWrapper, arrayAllocator);
	}

	/**
	 * Recursively builds a string representation of a Type object.
	 */
	private static <T> Class<T> getDataType(Type type) {
		if (type instanceof Class<?>) {
			return ((Class<T>) type);
		}

		if (type instanceof ParameterizedType) {
			ParameterizedType paramType = (ParameterizedType) type;
			return getDataType(paramType.getRawType());
		}

		if (type instanceof WildcardType) {
			throw new IllegalArgumentException("WildcardTypes are not supported");
		}

		return (Class<T>) type;
	}

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
					.map(DataLiteral::getSimpleTypeName)
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
					.map(DataLiteral::getTypeName)
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

	/**
	 * Default constructor that infers generic type information.
	 * 
	 * <p>
	 * The following components are automatically proxy-generated:
	 * <ul>
	 * <li>Empty instance - via FunctionalProxies.createNoOpProxy()</li>
	 * <li>Array wrapper - via FunctionalProxies.createArrayWrapper()</li>
	 * <li>Array allocator - via FunctionalProxies.createArrayAllocator()</li>
	 * </ul>
	 * </p>
	 *
	 * @throws IllegalArgumentException if generic type information is not available
	 */
	protected DataLiteral() {
		this((String) null);
	}

	/**
	 * Constructor with explicit data class specification.
	 * 
	 * <p>
	 * Proxy-generated components:
	 * <ul>
	 * <li>Empty instance - via FunctionalProxies.createNoOpProxy()</li>
	 * <li>Array wrapper - via FunctionalProxies.createArrayWrapper()</li>
	 * <li>Array allocator - via FunctionalProxies.createArrayAllocator()</li>
	 * </ul>
	 * </p>
	 *
	 * @param dataClass The Class object representing the data type
	 */
	public DataLiteral(Class<T> dataClass) {
		this(null, dataClass);
	}

	/**
	 * Constructor with generic information.
	 * 
	 * <p>
	 * Proxy-generated components:
	 * <ul>
	 * <li>Empty instance - via FunctionalProxies.createNoOpProxy()</li>
	 * <li>Array wrapper - via FunctionalProxies.createArrayWrapper()</li>
	 * <li>Array allocator - via FunctionalProxies.createArrayAllocator()</li>
	 * </ul>
	 * </p>
	 *
	 * @param name the name
	 */
	protected DataLiteral(String name) {
		Type superclass = getClass().getGenericSuperclass();
		if (!(superclass instanceof ParameterizedType paramType)) {
			throw new IllegalArgumentException("DataLiteral must be created with generic type information");
		}

		this.type = paramType.getActualTypeArguments()[0];
		this.dataClass = getDataType(type);

		this.empty = FunctionalProxies.createNoOpProxy(dataClass);
		this.arrayWrapper = FunctionalProxies.createArrayWrapper(dataClass);
		this.arrayAllocator = FunctionalProxies.createArrayAllocator(dataClass);
		this.name = (name == null) ? getSimpleTypeName(type) : name;
	}

	/**
	 * Constructor with name and data class.
	 * 
	 * <p>
	 * Proxy-generated components:
	 * <ul>
	 * <li>Empty instance - via FunctionalProxies.createNoOpProxy()</li>
	 * <li>Array wrapper - via FunctionalProxies.createArrayWrapper()</li>
	 * <li>Array allocator - via FunctionalProxies.createArrayAllocator()</li>
	 * </ul>
	 * </p>
	 *
	 * @param name      The name of the data type
	 * @param dataClass The Class object representing the data type
	 */
	public DataLiteral(String name, Class<T> dataClass) {
		this.dataClass = dataClass;
		this.type = dataClass;
		this.empty = FunctionalProxies.createNoOpProxy(dataClass);
		this.arrayWrapper = FunctionalProxies.createArrayWrapper(dataClass);
		this.arrayAllocator = FunctionalProxies.createArrayAllocator(dataClass);
		this.name = (name == null) ? getSimpleTypeName(dataClass) : name;
	}

	/**
	 * Constructor with name and data class.
	 * 
	 * <p>
	 * Proxy-generated components:
	 * <ul>
	 * <li>Empty instance - via FunctionalProxies.createNoOpProxy()</li>
	 * <li>Array allocator - via FunctionalProxies.createArrayAllocator()</li>
	 * </ul>
	 * </p>
	 *
	 * @param name      The name of the data type
	 * @param dataClass The Class object representing the data type
	 */
	public DataLiteral(String name, Class<T> dataClass, Function<T[], T> arrayWrapper) {
		this.dataClass = dataClass;
		this.type = dataClass;
		this.empty = FunctionalProxies.createNoOpProxy(dataClass);
		this.arrayWrapper = arrayWrapper;
		this.arrayAllocator = FunctionalProxies.createArrayAllocator(dataClass);
		this.name = (name == null) ? getSimpleTypeName(dataClass) : name;
	}

	/**
	 * Constructor with name and data class.
	 * 
	 * <p>
	 * Proxy-generated components:
	 * <ul>
	 * <li>Array wrapper - via FunctionalProxies.createArrayWrapper()</li>
	 * <li>Array allocator - via FunctionalProxies.createArrayAllocator()</li>
	 * </ul>
	 * </p>
	 *
	 * @param name      The name of the data type
	 * @param dataClass The Class object representing the data type
	 */
	public DataLiteral(String name, Class<T> dataClass, T empty) {
		this.dataClass = dataClass;
		this.type = dataClass;
		this.empty = empty;
		this.arrayWrapper = FunctionalProxies.createArrayWrapper(dataClass);
		this.arrayAllocator = FunctionalProxies.createArrayAllocator(dataClass);
		this.name = (name == null) ? getSimpleTypeName(dataClass) : name;
	}

	/**
	 * Constructor with name and data class.
	 * 
	 * @param name      The name of the data type
	 * @param dataClass The Class object representing the data type
	 */
	public DataLiteral(String name, Class<T> dataClass, T empty, Function<T[], T> arrayWrapper,
			IntFunction<T[]> arrayAllocator) {
		this.dataClass = dataClass;
		this.type = dataClass;
		this.empty = empty;
		this.arrayWrapper = arrayWrapper;
		this.arrayAllocator = arrayAllocator;
		this.name = (name == null) ? getSimpleTypeName(dataClass) : name;
	}

	/**
	 * Constructor with name and data class.
	 * 
	 * <p>
	 * Proxy-generated components:
	 * <ul>
	 * <li>Array wrapper - via FunctionalProxies.createArrayWrapper()</li>
	 * </ul>
	 * </p>
	 *
	 * @param name      The name of the data type
	 * @param dataClass The Class object representing the data type
	 */
	public DataLiteral(String name, Class<T> dataClass, T empty, IntFunction<T[]> arrayAllocator) {
		this.dataClass = dataClass;
		this.type = dataClass;
		this.empty = empty;
		this.arrayWrapper = FunctionalProxies.createArrayWrapper(dataClass);
		this.arrayAllocator = arrayAllocator;
		this.name = (name == null) ? getSimpleTypeName(dataClass) : name;
	}

	/**
	 * Constructor with generic information.
	 * 
	 * <p>
	 * Proxy-generated components:
	 * <ul>
	 * <li>Array wrapper - via FunctionalProxies.createArrayWrapper()</li>
	 * <li>Array allocator - via FunctionalProxies.createArrayAllocator()</li>
	 * </ul>
	 * </p>
	 *
	 * @param name  the name
	 * @param empty the empty
	 */
	protected DataLiteral(String name, T empty) {
		Type superclass = getClass().getGenericSuperclass();
		if (!(superclass instanceof ParameterizedType paramType)) {
			throw new IllegalArgumentException("DataLiteral must be created with generic type information");
		}

		this.type = paramType.getActualTypeArguments()[0];
		this.dataClass = getDataType(type);

		this.empty = empty;
		this.arrayWrapper = FunctionalProxies.createArrayWrapper(dataClass);
		this.arrayAllocator = FunctionalProxies.createArrayAllocator(dataClass);
		this.name = (name == null) ? getSimpleTypeName(type) : name;
	}

	/**
	 * Constructor with generic information.
	 * 
	 * <p>
	 * Proxy-generated components:
	 * <ul>
	 * <li>Array allocator - via FunctionalProxies.createArrayAllocator()</li>
	 * </ul>
	 * </p>
	 *
	 * @param name         the name
	 * @param empty        the empty
	 * @param arrayWrapper the array wrapper
	 */
	protected DataLiteral(String name, T empty, Function<T[], T> arrayWrapper) {
		Type superclass = getClass().getGenericSuperclass();
		if (!(superclass instanceof ParameterizedType paramType)) {
			throw new IllegalArgumentException("DataLiteral must be created with generic type information");
		}

		this.type = paramType.getActualTypeArguments()[0];
		this.dataClass = getDataType(type);

		this.empty = empty;
		this.arrayWrapper = arrayWrapper;
		this.arrayAllocator = FunctionalProxies.createArrayAllocator(dataClass);
		this.name = (name == null) ? getSimpleTypeName(type) : name;
	}

	/**
	 * Constructor with generic information.
	 * 
	 * @param name           the name
	 * @param empty          the empty
	 * @param arrayWrapper   the array wrapper
	 * @param arrayAllocator the array allocator
	 */
	protected DataLiteral(String name, T empty, Function<T[], T> arrayWrapper, IntFunction<T[]> arrayAllocator) {
		Type superclass = getClass().getGenericSuperclass();
		if (!(superclass instanceof ParameterizedType paramType)) {
			throw new IllegalArgumentException("DataLiteral must be created with generic type information");
		}

		this.type = paramType.getActualTypeArguments()[0];
		this.dataClass = getDataType(type);

		this.empty = empty;
		this.arrayWrapper = arrayWrapper;
		this.arrayAllocator = arrayAllocator;
		this.name = (name == null) ? getSimpleTypeName(type) : name;
	}

	/**
	 * Constructor with generic information.
	 * 
	 * <p>
	 * Proxy-generated components:
	 * <ul>
	 * <li>Empty instance - via FunctionalProxies.createNoOpProxy()</li>
	 * <li>Array allocator - via FunctionalProxies.createArrayAllocator()</li>
	 * </ul>
	 * </p>
	 *
	 * @param name the name
	 */
	protected DataLiteral(String name, T empty, IntFunction<T[]> arrayAllocator) {
		Type superclass = getClass().getGenericSuperclass();
		if (!(superclass instanceof ParameterizedType paramType)) {
			throw new IllegalArgumentException("DataLiteral must be created with generic type information");
		}

		this.type = paramType.getActualTypeArguments()[0];
		this.dataClass = getDataType(type);

		this.empty = empty;
		this.arrayWrapper = FunctionalProxies.createArrayWrapper(dataClass);
		this.arrayAllocator = arrayAllocator;
		this.name = (name == null) ? getSimpleTypeName(type) : name;
	}

	/**
	 * Constructor with custom empty instance.
	 * 
	 * <p>
	 * Proxy-generated components:
	 * <ul>
	 * <li>Array wrapper - via FunctionalProxies.createArrayWrapper()</li>
	 * <li>Array allocator - via FunctionalProxies.createArrayAllocator()</li>
	 * </ul>
	 * </p>
	 *
	 * @param empty The custom empty instance
	 */
	protected DataLiteral(T empty) {
		this(null, empty);
	}

	/**
	 * Constructor with custom array wrapper function.
	 * 
	 * <p>
	 * Proxy-generated components:
	 * <ul>
	 * <li>Array allocator - via FunctionalProxies.createArrayAllocator()</li>
	 * </ul>
	 * </p>
	 *
	 * @param empty        The custom empty instance
	 * @param arrayWrapper The function to wrap arrays
	 */
	protected DataLiteral(T empty, Function<T[], T> arrayWrapper) {
		this(null, empty, arrayWrapper);
	}

	/**
	 * Constructor with generic information.
	 * 
	 * <p>
	 * Proxy-generated components:
	 * <ul>
	 * <li>Array allocator - via FunctionalProxies.createArrayAllocator()</li>
	 * </ul>
	 * </p>
	 *
	 * @param name the name
	 */
	protected DataLiteral(T empty, IntFunction<T[]> arrayAllocator) {
		Type superclass = getClass().getGenericSuperclass();
		if (!(superclass instanceof ParameterizedType paramType)) {
			throw new IllegalArgumentException("DataLiteral must be created with generic type information");
		}

		this.type = paramType.getActualTypeArguments()[0];
		this.dataClass = getDataType(type);

		this.empty = empty;
		this.arrayWrapper = FunctionalProxies.createArrayWrapper(dataClass);
		this.arrayAllocator = arrayAllocator;
		this.name = getSimpleTypeName(type);
	}

	/**
	 * @see com.slytechs.jnet.platform.api.pipeline.DataType#arrayAllocator()
	 */
	@Override
	public IntFunction<T[]> arrayAllocator() {
		return arrayAllocator;
	}

	/**
	 * @see com.slytechs.jnet.platform.api.pipeline.DataType#dataClass()
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
	 * @see com.slytechs.jnet.platform.api.pipeline.DataType#empty()
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
	 * @see com.slytechs.jnet.platform.api.pipeline.DataType#name()
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
	 * @see com.slytechs.jnet.platform.api.pipeline.DataType#wrapArray(java.lang.Object[])
	 */
	@Override
	public T wrapArray(T[] array) {
		return arrayWrapper.apply(array);
	}
}
