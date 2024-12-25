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
package com.slytechs.jnet.platform.api.data;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.function.IntFunction;

import com.slytechs.jnet.platform.api.util.Named;

/**
 * A core interface that represents a data type, providing essential type
 * information and utility methods for managing type-specific operations in the
 * pipeline platform.
 * 
 * <p>
 * The {@code DataType} interface serves as a foundational abstraction that
 * offers key functionalities such as:
 * <ul>
 * <li>Array allocation and management</li>
 * <li>Data class information retrieval</li>
 * <li>Type parameterization support</li>
 * <li>Array wrapping and optimization</li>
 * </ul>
 * </p>
 *
 * <p>
 * This interface enables efficient handling of various data types in a
 * type-safe manner by providing a consistent API for type-specific operations.
 * </p>
 * 
 * @param <T> The type parameter representing the specific data type being
 *            managed
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc
 */
public interface DataType<T> extends Named {

	/**
	 * Returns an array allocator function for creating new arrays of type T.
	 * 
	 * <p>
	 * This function allocates arrays of the specific data type with a given length.
	 * It is used internally for array creation operations.
	 * </p>
	 *
	 * @return An IntFunction that creates arrays of type T with the specified
	 *         length
	 */
	IntFunction<T[]> arrayAllocator();

	/**
	 * Returns the Class object representing the raw data type.
	 * 
	 * <p>
	 * Provides access to the runtime Class information of the data type for
	 * reflection and type manipulation purposes.
	 * </p>
	 *
	 * @return The Class object representing type T
	 */
	Class<T> dataClass();

	/**
	 * Returns the parameterized Type information for the data type.
	 * 
	 * <p>
	 * By default, returns the same as dataClass(). Implementations can override
	 * this to provide specific generic type information.
	 * </p>
	 *
	 * @return The Type object representing the complete type information including
	 *         generics
	 */
	default Type dataParameterizedType() {
		return dataClass();
	}

	/**
	 * Returns an empty or default instance of the data type.
	 * 
	 * <p>
	 * Provides a way to obtain a canonical empty representation of the data type.
	 * This is often used as a default value or placeholder.
	 * </p>
	 *
	 * @return An empty instance of type T
	 */
	T empty();

	/**
	 * Returns the name identifier for this data type.
	 * 
	 * <p>
	 * Inherited from Named interface, provides a string identifier that can be used
	 * to reference this data type.
	 * </p>
	 *
	 * @return The string name of this data type
	 */
	@Override
	String name();

	/**
	 * Optimizes an array based on its contents, providing a more efficient
	 * representation.
	 * 
	 * <p>
	 * This method implements the following optimization rules:
	 * <ul>
	 * <li>Returns empty() for null or empty arrays</li>
	 * <li>Returns the single element for arrays of size 1</li>
	 * <li>Returns a wrapped array for multiple elements</li>
	 * </ul>
	 * </p>
	 *
	 * @param array The array to optimize
	 * @return An optimized representation of the array contents
	 */
	default T optimizeArray(T[] array) {
		if ((array == null) || (array.length == 0) || (array.length == 1) && (array[0] == null))
			return empty();

		if (array.length == 1)
			return array[0];

		return wrapArray(array);
	}

	/**
	 * Optimizes a collection by converting it to an optimized array representation.
	 * 
	 * <p>
	 * Converts the collection to an array using the arrayAllocator and applies
	 * array optimization rules.
	 * </p>
	 *
	 * @param col The collection to optimize
	 * @return An optimized representation of the collection contents
	 */
	default T optimizeCollection(Collection<T> col) {
		var array = col.toArray(arrayAllocator());
		return optimizeArray(array);
	}

	/**
	 * Wraps an array into a single instance representation.
	 * 
	 * <p>
	 * This method provides a way to represent multiple instances as a single
	 * aggregate instance of the data type.
	 * </p>
	 *
	 * @param array The array to wrap
	 * @return A single instance representing the wrapped array
	 */
	T wrapArray(T[] array);
}