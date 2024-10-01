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
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import com.slytechs.jnet.jnetruntime.util.HasId;
import com.slytechs.jnet.jnetruntime.util.HasName;

/**
 * Represents a type of data in the pipeline system. This interface extends
 * HasId and HasName, providing identification and naming capabilities.
 *
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 */
public interface DataType extends HasId, HasName {

	/**
	 * Support class for DataType implementations. Provides utility methods for
	 * handling data of a specific type.
	 *
	 * @param <T> The type of data supported
	 */
	public class DataSupport<T> {

		private final DataType dataType;
		private final Class<T> dataClass;
		private final Function<T[], T> arrayWrapper;
		private final BiFunction<T, ? super Object, T> opaqueWrapper;
		private final T empty;

		/**
		 * Constructs a DataSupport instance without array or opaque wrapping
		 * capabilities.
		 *
		 * @param dataType  The DataType instance
		 * @param dataClass The class of the data type
		 */
		public DataSupport(DataType dataType, Class<T> dataClass) {
			this.dataType = dataType;
			this.dataClass = dataClass;
			this.arrayWrapper = null;
			this.opaqueWrapper = (t, obj) -> t;
			this.empty = null;
		}

		/**
		 * Constructs a DataSupport instance with array and opaque wrapping
		 * capabilities.
		 *
		 * @param dataType      The DataType instance
		 * @param dataClass     The class of the data type
		 * @param opaqueWrapper Function to wrap data with opaque object
		 * @param arrayWrapper  Function to wrap array of data
		 */
		public DataSupport(
				DataType dataType,
				Class<T> dataClass,
				BiFunction<T, ? super Object, T> opaqueWrapper,
				Function<T[], T> arrayWrapper) {

			this.dataType = dataType;
			this.dataClass = dataClass;
			this.arrayWrapper = arrayWrapper;
			this.opaqueWrapper = opaqueWrapper;
			this.empty = wrapCollection(Collections.emptyList());
		}

		/**
		 * @return The class of the data type
		 */
		public Class<T> dataClass() {
			return dataClass;
		}

		/**
		 * @return The DataType instance
		 */
		public DataType dataType() {
			return dataType;
		}

		/**
		 * @return An empty instance of the data type
		 */
		public T empty() {
			return empty;
		}

		/**
		 * Wraps an array of data into a single instance.
		 *
		 * @param array The array to wrap
		 * @return A single instance representing the array
		 */
		public T wrapArray(T[] array) {
			if (array.length == 1)
				return array[0];

			return arrayWrapper.apply(array);
		}

		/**
		 * Wraps a collection of data into a single instance.
		 *
		 * @param list The collection to wrap
		 * @return A single instance representing the collection
		 */
		public T wrapCollection(Collection<T> list) {
			T[] array = list.toArray(size -> PipelineUtils.newArray(dataClass, size));
			return wrapArray(array);
		}

		/**
		 * Wraps data with an opaque object.
		 *
		 * @param data   The data to wrap
		 * @param opaque The opaque object to wrap with
		 * @return The wrapped data
		 */
		public T wrapOpaque(T data, Object opaque) {
			return opaqueWrapper.apply(data, opaque);
		}

		/**
		 * Creates a new array of the data type.
		 *
		 * @param size The size of the array
		 * @return A new array of the data type
		 */
		public T[] newArray(int size) {
			return PipelineUtils.newArray(dataClass, size);
		}
	}

	/**
	 * Interface for objects that have a DataType.
	 */
	public interface HasDataType {
		/**
		 * @return The DataType of this object
		 */
		DataType dataType();
	}

	/**
	 * Interface for integer-to-string conversion operations.
	 */
	public interface IntString extends Consumer<String> {

		/**
		 * Opaque wrapper for IntString.
		 *
		 * @param <U> The type of the opaque object
		 */
		public static class OpaqueIntString<U> extends OpaqueData<IntString, U> implements IntString {

			/**
			 * Constructs an OpaqueIntString.
			 *
			 * @param data   The IntString data
			 * @param opaque The opaque object
			 */
			public OpaqueIntString(IntString data, U opaque) {
				super(data, opaque);
			}

			@Override
			public void accept(String t) {
				data().accept(t);
			}
		}

		/**
		 * Wraps an array of IntString into a single IntString.
		 *
		 * @param array The array to wrap
		 * @return A single IntString representing the array
		 */
		static IntString wrapArray(IntString[] array) {
			return str -> {
				for (var i : array)
					i.accept(str);
			};
		}

		/**
		 * Wraps an IntString with an opaque object.
		 *
		 * @param <U>    The type of the opaque object
		 * @param data   The IntString to wrap
		 * @param opaque The opaque object
		 * @return The wrapped IntString
		 */
		static <U> IntString wrapOpaque(IntString data, U opaque) {
			return new OpaqueIntString<>(data, opaque);
		}
	}

	/**
	 * Class for wrapping data with an opaque object.
	 *
	 * @param <T> The type of the data
	 * @param <U> The type of the opaque object
	 */
	public class OpaqueData<T, U> {

		private U opaque;
		private final T data;

		/**
		 * Constructs an OpaqueData instance with data only.
		 *
		 * @param data The data to wrap
		 */
		public OpaqueData(T data) {
			this.data = data;
		}

		/**
		 * Constructs an OpaqueData instance with data and opaque object.
		 *
		 * @param data   The data to wrap
		 * @param opaque The opaque object
		 */
		public OpaqueData(T data, U opaque) {
			this.data = data;
			this.opaque = opaque;
		}

		/**
		 * @return The wrapped data
		 */
		public T data() {
			return data;
		}

		/**
		 * @return The opaque object
		 */
		public U opaque() {
			return opaque;
		}

		/**
		 * Sets the opaque object.
		 *
		 * @param u The new opaque object
		 */
		public void opaque(U u) {
			this.opaque = u;
		}
	}

	/**
	 * Enumeration of primitive data types.
	 */
	public enum Primitives implements DataType {
		INT_STRING(IntString.class, IntString::wrapOpaque, IntString::wrapArray);

		private final DataSupport<?> dataSupport;

		/**
		 * Constructs a Primitives enum instance.
		 *
		 * @param <T>           The type of the data
		 * @param dataClass     The class of the data type
		 * @param opaqueWrapper Function to wrap data with opaque object
		 * @param arrayWrapper  Function to wrap array of data
		 */
		<T> Primitives(
				Class<T> dataClass,
				BiFunction<T, ? super Object, T> opaqueWrapper,
				Function<T[], T> arrayWrapper) {

			dataSupport = new DataSupport<T>(this, dataClass, opaqueWrapper, arrayWrapper);
		}

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		@Override
		public <T> DataSupport<T> dataSupport() {
			return (DataSupport<T>) this.dataSupport;
		}
	}

	/**
	 * Checks if this DataType is compatible with the given class.
	 *
	 * @param dataClass The class to check compatibility with
	 * @return true if compatible, false otherwise
	 */
	default boolean isCompatibleWith(Class<?> dataClass) {
		return dataSupport().dataClass().isAssignableFrom(dataClass);
	}

	/**
	 * @param <T> The type of data
	 * @return The DataSupport for this DataType
	 */
	<T> DataSupport<T> dataSupport();

	/**
	 * @param <T> The type of data
	 * @return The class of the data type
	 */
	@SuppressWarnings("unchecked")
	default <T> Class<T> dataClass() {
		return (Class<T>) dataSupport().dataClass();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	String name();

	/**
	 * @param <T> The type of data
	 * @return An empty instance of the data type
	 */
	@SuppressWarnings("unchecked")
	default <T> T empty() {
		return (T) dataSupport().empty();
	}
}