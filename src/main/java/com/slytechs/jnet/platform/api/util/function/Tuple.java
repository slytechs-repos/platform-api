/*
 * Sly Technologies Free License
 *
 * Copyright 2025 Sly Technologies Inc.
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
package com.slytechs.jnet.platform.api.util.function;

/**
 * A generic interface for tuple types that represent a fixed-size collection of
 * heterogeneous values. Each element in a tuple can be of a different type and
 * may be null.
 * 
 * <p>
 * The record implementations provide standard {@code equals()},
 * {@code hashCode()} and {@code toString()} behavior where:
 * <ul>
 * <li>equals() performs value-based comparison of all components, correctly
 * handling nulls
 * <li>hashCode() combines component hash codes consistently with equals()
 * <li>toString() formats as "TupleN [value1, value2, ...]"
 * </ul>
 * </p>
 * 
 * <p>
 * This interface defines tuple types from 2 to 5 values. Each tuple type is
 * immutable and thread-safe. Tuples provide convenient static factory methods
 * through their respective {@code of} methods.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * // Create tuple with a String and Integer
 * Tuple2<String, Integer> person = Tuple2.of("John", 25);
 * 
 * // Access values (may return null)
 * String name = person.value1(); // "John"
 * Integer age = person.value2(); // 25
 * Object[] all = person.values(); // ["John", 25]
 * }</pre>
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 * @see Tuple2
 * @see Tuple3
 * @see Tuple4
 * @see Tuple5
 */
public interface Tuple {

	/**
	 * A tuple of 2 elements.
	 *
	 * @param <T1> the type of the first value
	 * @param <T2> the type of the second value
	 */
	interface Tuple2<T1, T2> extends Tuple {

		/**
		 * Record implementation of Tuple2.
		 */
		record Tuple2Record<T1, T2>(@Nullable T1 value1, @Nullable T2 value2) implements Tuple2<T1, T2> {
			@Override
			public Object[] values() {
				return new Object[] {
						value1,
						value2
				};
			}

			@Override
			public String toString() {
				return "Tuple2 [" + String.valueOf(value1) + ", " + String.valueOf(value2) + "]";
			}
		}

		/**
		 * Creates a new tuple with two values.
		 *
		 * @param <T1>   the type of the first value
		 * @param <T2>   the type of the second value
		 * @param value1 the first value, may be null
		 * @param value2 the second value, may be null
		 * @return a new tuple containing the values
		 */
		static <T1, T2> Tuple2<T1, T2> of(@Nullable T1 value1, @Nullable T2 value2) {
			return new Tuple2Record<>(value1, value2);
		}

		/**
		 * Returns the first value.
		 *
		 * @return the first value, may be null
		 */
		@Nullable
		T1 value1();

		/**
		 * Returns the second value.
		 *
		 * @return the second value, may be null
		 */
		@Nullable
		T2 value2();
	}

	/**
	 * A tuple of 3 elements.
	 *
	 * @param <T1> the type of the first value
	 * @param <T2> the type of the second value
	 * @param <T3> the type of the third value
	 */
	interface Tuple3<T1, T2, T3> extends Tuple {

		/**
		 * Record implementation of Tuple3.
		 */
		record Tuple3Record<T1, T2, T3>(@Nullable T1 value1, @Nullable T2 value2, @Nullable T3 value3)
				implements Tuple3<T1, T2, T3> {
			@Override
			public Object[] values() {
				return new Object[] {
						value1,
						value2,
						value3
				};
			}

			@Override
			public String toString() {
				return "Tuple3 [" + String.valueOf(value1) + ", " + String.valueOf(value2) + ", " +
						String.valueOf(value3) + "]";
			}
		}

		/**
		 * Creates a new tuple with three values.
		 *
		 * @param <T1>   the type of the first value
		 * @param <T2>   the type of the second value
		 * @param <T3>   the type of the third value
		 * @param value1 the first value, may be null
		 * @param value2 the second value, may be null
		 * @param value3 the third value, may be null
		 * @return a new tuple containing the values
		 */
		static <T1, T2, T3> Tuple3<T1, T2, T3> of(
				@Nullable T1 value1,
				@Nullable T2 value2,
				@Nullable T3 value3) {
			return new Tuple3Record<>(value1, value2, value3);
		}

		/**
		 * Returns the first value.
		 *
		 * @return the first value, may be null
		 */
		@Nullable
		T1 value1();

		/**
		 * Returns the second value.
		 *
		 * @return the second value, may be null
		 */
		@Nullable
		T2 value2();

		/**
		 * Returns the third value.
		 *
		 * @return the third value, may be null
		 */
		@Nullable
		T3 value3();
	}

	/**
	 * A tuple of 4 elements.
	 *
	 * @param <T1> the type of the first value
	 * @param <T2> the type of the second value
	 * @param <T3> the type of the third value
	 * @param <T4> the type of the fourth value
	 */
	interface Tuple4<T1, T2, T3, T4> extends Tuple {

		/**
		 * Record implementation of Tuple4.
		 */
		record Tuple4Record<T1, T2, T3, T4>(
				@Nullable T1 value1,
				@Nullable T2 value2,
				@Nullable T3 value3,
				@Nullable T4 value4) implements Tuple4<T1, T2, T3, T4> {

			@Override
			public Object[] values() {
				return new Object[] {
						value1,
						value2,
						value3,
						value4
				};
			}

			@Override
			public String toString() {
				return "Tuple4 [" +
						String.valueOf(value1) + ", " +
						String.valueOf(value2) + ", " +
						String.valueOf(value3) + ", " +
						String.valueOf(value4) + "]";
			}
		}

		/**
		 * Creates a new tuple with four values.
		 *
		 * @param <T1>   the type of the first value
		 * @param <T2>   the type of the second value
		 * @param <T3>   the type of the third value
		 * @param <T4>   the type of the fourth value
		 * @param value1 the first value, may be null
		 * @param value2 the second value, may be null
		 * @param value3 the third value, may be null
		 * @param value4 the fourth value, may be null
		 * @return a new tuple containing the values
		 */
		static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4> of(
				@Nullable T1 value1,
				@Nullable T2 value2,
				@Nullable T3 value3,
				@Nullable T4 value4) {
			return new Tuple4Record<>(value1, value2, value3, value4);
		}

		/**
		 * Returns the first value.
		 *
		 * @return the first value, may be null
		 */
		@Nullable
		T1 value1();

		/**
		 * Returns the second value.
		 *
		 * @return the second value, may be null
		 */
		@Nullable
		T2 value2();

		/**
		 * Returns the third value.
		 *
		 * @return the third value, may be null
		 */
		@Nullable
		T3 value3();

		/**
		 * Returns the fourth value.
		 *
		 * @return the fourth value, may be null
		 */
		@Nullable
		T4 value4();
	}

	/**
	 * A tuple of 5 elements.
	 *
	 * @param <T1> the type of the first value
	 * @param <T2> the type of the second value
	 * @param <T3> the type of the third value
	 * @param <T4> the type of the fourth value
	 * @param <T5> the type of the fifth value
	 */
	interface Tuple5<T1, T2, T3, T4, T5> extends Tuple {

		/**
		 * Record implementation of Tuple5.
		 */
		record Tuple5Record<T1, T2, T3, T4, T5>(
				@Nullable T1 value1,
				@Nullable T2 value2,
				@Nullable T3 value3,
				@Nullable T4 value4,
				@Nullable T5 value5) implements Tuple5<T1, T2, T3, T4, T5> {

			@Override
			public Object[] values() {
				return new Object[] {
						value1,
						value2,
						value3,
						value4,
						value5
				};
			}

			@Override
			public String toString() {
				return "Tuple5 [" +
						String.valueOf(value1) + ", " +
						String.valueOf(value2) + ", " +
						String.valueOf(value3) + ", " +
						String.valueOf(value4) + ", " +
						String.valueOf(value5) + "]";
			}
		}

		/**
		 * Creates a new tuple with five values.
		 *
		 * @param <T1>   the type of the first value
		 * @param <T2>   the type of the second value
		 * @param <T3>   the type of the third value
		 * @param <T4>   the type of the fourth value
		 * @param <T5>   the type of the fifth value
		 * @param value1 the first value, may be null
		 * @param value2 the second value, may be null
		 * @param value3 the third value, may be null
		 * @param value4 the fourth value, may be null
		 * @param value5 the fifth value, may be null
		 * @return a new tuple containing the values
		 */
		static <T1, T2, T3, T4, T5> Tuple5<T1, T2, T3, T4, T5> of(
				@Nullable T1 value1,
				@Nullable T2 value2,
				@Nullable T3 value3,
				@Nullable T4 value4,
				@Nullable T5 value5) {
			return new Tuple5Record<>(value1, value2, value3, value4, value5);
		}

		/**
		 * Returns the first value.
		 *
		 * @return the first value, may be null
		 */
		@Nullable
		T1 value1();

		/**
		 * Returns the second value.
		 *
		 * @return the second value, may be null
		 */
		@Nullable
		T2 value2();

		/**
		 * Returns the third value.
		 *
		 * @return the third value, may be null
		 */
		@Nullable
		T3 value3();

		/**
		 * Returns the fourth value.
		 *
		 * @return the fourth value, may be null
		 */
		@Nullable
		T4 value4();

		/**
		 * Returns the fifth value.
		 *
		 * @return the fifth value, may be null
		 */
		@Nullable
		T5 value5();
	}

	/**
	 * Returns all values of this tuple as an array of Objects.
	 *
	 * @return an array containing all values in this tuple, elements may be null
	 */
	Object[] values();
}