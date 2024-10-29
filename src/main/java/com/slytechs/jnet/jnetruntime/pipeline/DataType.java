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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import com.slytechs.jnet.jnetruntime.NotFound;
import com.slytechs.jnet.jnetruntime.util.HasId;
import com.slytechs.jnet.jnetruntime.util.HasName;
import com.slytechs.jnet.jnetruntime.util.Registration;

/**
 * Represents a type of data in the pipeline system. This interface extends
 * HasId and HasName, providing identification and naming capabilities.
 *
 * @author Mark Bednarczyk
 */
public interface DataType extends HasId, HasName {

	public class DefaultDataType<T> implements DataType {

		private final String name;
		private final DataSupport<T> dataSupport;

		public DefaultDataType(String name, Class<T> dataClass) {
			this.name = name;
			this.dataSupport = new DataSupport<>(this, dataClass);
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.pipeline.DataType#dataSupport()
		 */
		@Override
		public <Q> DataSupport<Q> dataSupport() {
			return (DataSupport<Q>) dataSupport;
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.pipeline.DataType#name()
		 */
		@Override
		public String name() {
			return name;
		}

	}

	/**
	 * Support class for DataType implementations. Provides utility methods for
	 * handling data of a specific type.
	 *
	 * @param <T> The type of data supported
	 * @author Mark Bednarczyk
	 */
	public class DataSupport<T> {

		/**
		 * Creates the array wrapper.
		 *
		 * @param <T>       the generic type
		 * @param dataClass the data class
		 * @return the function
		 */
		private static <T> Function<T[], T> createArrayWrapper(Class<T> dataClass) {
			return new Function<T[], T>() {
				/**
				 * @see java.util.function.Function#apply(java.lang.Object)
				 */
				@Override
				public T apply(T[] arr) {
					try {
						return DataArrayMethodHandle.wrapArray(arr, dataClass);
					} catch (IllegalAccessException e) {
						throw new IllegalStateException(e);
					}
				}
			};
		}

		/**
		 * Looks up a data class in currently registered data type registry and return a
		 * data type.
		 *
		 * @param dataClass java class for that the data type describves
		 * @return data type
		 * @throws NotFound thrown if the data type for the specified class is not
		 *                  registered
		 */
		static DataType lookupClass(Class<?> dataClass) throws NotFound {
			return DataTypeRegistry.global()
					.findType(dataClass)
					.orElseThrow(() -> new NotFound("data class [%s] not found in global data type registry"
							.formatted(dataClass.getSimpleName())));
		}

		/** The data type. */
		private final DataType dataType;

		/** The data class. */
		private final Class<T> dataClass;

		/** The array wrapper. */
		private final Function<T[], T> arrayWrapper;

		/** The empty. */
		private final T empty;

		private final Registration registration;

		/**
		 * Constructs a DataSupport instance without array or opaque wrapping
		 * capabilities.
		 *
		 * @param dataType  The DataType instance
		 * @param dataClass The class of the data type
		 */
		public DataSupport(DataType dataType, Class<T> dataClass) {
			this(dataType, dataClass, createArrayWrapper(dataClass));
		}

		/**
		 * Constructs a DataSupport instance with array and opaque wrapping
		 * capabilities.
		 *
		 * @param <U>          the generic type
		 * @param dataType     The DataType instance
		 * @param dataClass    The class of the data type
		 * @param arrayWrapper Function to wrap array of data
		 */
		public <U> DataSupport(
				DataType dataType,
				Class<T> dataClass,
				Function<T[], T> arrayWrapper) {

			this.dataType = dataType;
			this.dataClass = dataClass;
			this.arrayWrapper = arrayWrapper;
			this.empty = wrapCollection(Collections.emptyList());

			this.registration = DataTypeRegistry.global()
					.register(dataType, dataClass);
		}

		/**
		 * Data class.
		 *
		 * @return The class of the data type
		 */
		public Class<T> dataClass() {
			return dataClass;
		}

		/**
		 * Data type.
		 *
		 * @return The DataType instance
		 */
		public DataType dataType() {
			return dataType;
		}

		/**
		 * Empty.
		 *
		 * @return An empty instance of the data type
		 */
		public T empty() {
			return empty;
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

		public Registration registration() {
			return registration;
		}

		/**
		 * Wraps an array of data into a single instance.
		 *
		 * @param array The array to wrap
		 * @return A single instance representing the array
		 */
		public T wrapArray(T[] array) {
			if (array.length == 1) {
				return array[0];
			}

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
	}

	class DataTypeRegistry {
		private static final DataTypeRegistry global = new DataTypeRegistry();

		public static synchronized DataTypeRegistry global() {
			return global;
		}

		private final Map<Class<?>, List<DataType>> registry = new HashMap<>();

		public synchronized Optional<DataType> findType(Class<?> dataClass) throws IllegalStateException {
			var list = registry.get(dataClass);
			if (list == null || list.isEmpty()) {
				return Optional.empty();
			}

			if (list.size() > 1) {
				throw new IllegalStateException(
						"multiple data types %s registered for data class [%s], not allowed to pick one randomly"
								.formatted(list, dataClass.getSimpleName()));
			}

			return Optional.of(list.get(0));
		}

		public synchronized List<DataType> listTypes(Class<?> dataClass) throws NotFound {
			if (registry.containsKey(dataClass)) {
				throw new NotFound(dataClass.getSimpleName());
			}

			return Collections.unmodifiableList(registry.get(dataClass));
		}

		public synchronized Registration register(DataType dataType, Class<?> dataClass) {
			var list = registry.computeIfAbsent(dataClass, (key) -> new ArrayList<DataType>());
			list.add(dataType);

			return () -> {
				var listValue = registry.get(dataClass);
				if (listValue != null) {
					listValue.remove(dataType);
				}
			};
		}

		@Override
		public synchronized String toString() {
			return "DataTypeRegistry [registry=" + registry + "]";
		}

		public synchronized void unregister(DataType dataType) {
			dataType.dataSupport()
					.registration()
					.unregister();
		}
	}

	/**
	 * Interface for objects that have a DataType.
	 *
	 * @author Mark Bednarczyk
	 */
	public interface HasDataType {

		/**
		 * Data type.
		 *
		 * @return The DataType of this object
		 */
		DataType dataType();
	}

	/**
	 * Interface for integer-to-string conversion operations.
	 *
	 * @author Mark Bednarczyk
	 */
	public interface IntString extends Consumer<String> {

		/**
		 * Opaque wrapper for IntString.
		 *
		 * @param <U> The type of the opaque object
		 * @author Mark Bednarczyk
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

			/**
			 * Accept.
			 *
			 * @param t the t
			 * @see java.util.function.Consumer#accept(java.lang.Object)
			 */
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
				for (var i : array) {
					i.accept(str);
				}
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
	 * @author Mark Bednarczyk
	 */
	public class OpaqueData<T, U> {

		/** The opaque. */
		private U opaque;

		/** The data. */
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
		 * Data.
		 *
		 * @return The wrapped data
		 */
		public T data() {
			return data;
		}

		/**
		 * Opaque.
		 *
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
	 *
	 * @author Mark Bednarczyk
	 */
	public enum Primitives implements DataType {

		/** The int string. */
		INT_STRING(IntString.class, IntString::wrapOpaque, IntString::wrapArray);

		/** The data support. */
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

			dataSupport = new DataSupport<T>(this, dataClass, arrayWrapper);
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
	 * Data class.
	 *
	 * @param <T> The type of data
	 * @return The class of the data type
	 */
	@SuppressWarnings("unchecked")
	default <T> Class<T> dataClass() {
		return (Class<T>) dataSupport().dataClass();
	}

	/**
	 * Data support.
	 *
	 * @param <T> The type of data
	 * @return The DataSupport for this DataType
	 */
	<T> DataSupport<T> dataSupport();

	/**
	 * Empty.
	 *
	 * @param <T> The type of data
	 * @return An empty instance of the data type
	 */
	@SuppressWarnings("unchecked")
	default <T> T empty() {
		return (T) dataSupport().empty();
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
	 * {@inheritDoc}
	 */
	@Override
	String name();
}
