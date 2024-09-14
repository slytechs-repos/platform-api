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

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import com.slytechs.jnet.jnetruntime.util.HasId;
import com.slytechs.jnet.jnetruntime.util.HasName;

/**
 * The Interface DataType.
 *
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 */
public interface DataType extends HasId, HasName {

	public interface HasDataType {
		DataType dataType();
	}

	/**
	 * The Class DataSupport.
	 *
	 * @param <T> the generic type
	 */
	public class DataSupport<T> {

		/** The data type. */
		private final DataType dataType;

		/** The data class. */
		private final Class<T> dataClass;

		/** The array wrapper. */
		private final Function<T[], T> arrayWrapper;

		/** The opaque wrapper. */
		private final BiFunction<T, ? super Object, T> opaqueWrapper;

		/**
		 * Instantiates a new data support.
		 *
		 * @param dataType      the data type
		 * @param dataClass     the data class
		 * @param arrayWrapper  the array wrapper
		 * @param opaqueWrapper the opaque wrapper
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
		}
		
		/**
		 * Instantiates a new data support.
		 *
		 * @param dataType      the data type
		 * @param dataClass     the data class
		 * @param arrayWrapper  the array wrapper
		 * @param opaqueWrapper the opaque wrapper
		 */
		public DataSupport(
				DataType dataType,
				Class<T> dataClass) {

			this.dataType = dataType;
			this.dataClass = dataClass;
			this.arrayWrapper = null;
			this.opaqueWrapper = (t, obj) -> t;
		}


		/**
		 * Data class.
		 *
		 * @return the class
		 */
		public Class<T> dataClass() {
			return dataClass;
		}

		/**
		 * Wrap array.
		 *
		 * @param array the array
		 * @return the t
		 */
		public T wrapArray(T[] array) {
			return arrayWrapper.apply(array);
		}

		/**
		 * Wrap opaque.
		 *
		 * @param data   the data
		 * @param opaque the opaque
		 * @return the t
		 */
		public T wrapOpaque(T data, Object opaque) {
			return opaqueWrapper.apply(data, opaque);
		}

		/**
		 * Data type.
		 *
		 * @return the data type
		 */
		public DataType dataType() {
			return dataType;
		}
	}

	public class OpaqueData<T, U> {

		private U opaque;
		private final T data;

		public OpaqueData(T data) {
			this.data = data;
		}

		public OpaqueData(T data, U opaque) {
			this.data = data;
			this.opaque = opaque;
		}

		public U opaque() {
			return opaque;
		}

		public void opaque(U u) {
			this.opaque = u;
		}

		public T data() {
			return data;
		}

	}

	/**
	 * Data support.
	 *
	 * @param <T> the generic type
	 * @return the data support
	 */
	<T> DataSupport<T> dataSupport();

	/**
	 * Name.
	 *
	 * @return the string
	 */
	@Override
	String name();

	public interface IntString extends Consumer<String> {

		public static class OpaqueIntString<U> extends OpaqueData<IntString, U> implements IntString {

			/**
			 * @param opaque
			 */
			public OpaqueIntString(IntString data, U opaque) {
				super(data, opaque);
			}

			/**
			 * @see java.util.function.Consumer#accept(java.lang.Object)
			 */
			@Override
			public void accept(String t) {
				data().accept(t);
			}

		}

		static <U> IntString wrapOpaque(IntString data, U opaque) {
			return new OpaqueIntString<>(data, opaque);
		}

		static IntString wrapArray(IntString[] array) {
			return str -> {
				for (var i : array)
					i.accept(str);
			};
		}
	}

	/**
	 * The Enum Primitives.
	 */
	public enum Primitives implements DataType {
		INT_STRING(IntString.class, IntString::wrapOpaque, IntString::wrapArray),

		;

		/** The data support. */
		private final DataSupport<?> dataSupport;

		/**
		 * Instantiates a new primitives.
		 *
		 * @param <T>           the generic type
		 * @param dataClass     the data class
		 * @param arrayWrapper  the array wrapper
		 * @param opaqueWrapper the opaque wrapper
		 */
		<T> Primitives(
				Class<T> dataClass,
				BiFunction<T, ? super Object, T> opaqueWrapper,
				Function<T[], T> arrayWrapper) {

			dataSupport = new DataSupport<T>(this, dataClass, opaqueWrapper, arrayWrapper);
		}

		/**
		 * Data support.
		 *
		 * @param <T> the generic type
		 * @return the data support
		 * @see com.slytechs.jnet.jnetruntime.pipeline.DataType#dataSupport()
		 */
		@SuppressWarnings("unchecked")
		@Override
		public <T> DataSupport<T> dataSupport() {
			return (DataSupport<T>) this.dataSupport;
		}

	}
}
