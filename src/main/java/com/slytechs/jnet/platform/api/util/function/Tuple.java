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
 * heterogeneous values. Tuples provide a way to group multiple values together
 * into a single compound value.
 * 
 * <p>
 * This interface defines a collection of tuple types, from 2-value tuples up to
 * 5-value tuples ({@link Tuple2}, {@link Tuple3}, {@link Tuple4}, and
 * {@link Tuple5}). Each tuple type represents an immutable ordered sequence of
 * elements where each element can be of a different type.
 * 
 * <p>
 * Use tuples when you need to:
 * <ul>
 * <li>Return multiple values from a method without creating a dedicated class
 * <li>Group related values together in a type-safe way
 * <li>Create composite keys or values for collections
 * <li>Pass multiple parameters as a single unit
 * </ul>
 * 
 * <p>
 * Example usage for Tuple2:
 * 
 * <pre>{@code
 * // Create a tuple containing a String and Integer
 * Tuple2<String, Integer> person = Tuple2.of("John", 25);
 * 
 * // Access individual values
 * String name = person.value1(); // "John"
 * int age = person.value2(); // 25
 * 
 * // Use in a method return
 * public static Tuple2<String, Integer> parseNameAndAge(String input) {
 * 	String[] parts = input.split(",");
 * 	return Tuple2.of(parts[0], Integer.parseInt(parts[1]));
 * }
 * }</pre>
 * 
 * Example usage for Tuple3:
 * 
 * <pre>{@code
 * // Create a tuple containing String, Integer, and Boolean values
 * Tuple3<String, Integer, Boolean> employee = Tuple3.of("Alice", 30, true);
 * 
 * // Access values
 * String name = employee.value1(); // "Alice"
 * int age = employee.value2(); // 30
 * boolean active = employee.value3(); // true
 * 
 * // Get all values as an array
 * Object[] values = employee.values(); // ["Alice", 30, true]
 * }</pre>
 * 
 * <p>
 * All tuple implementations are immutable and thread-safe. The values stored in
 * a tuple can be accessed through their respective value methods (value1(),
 * value2(), etc.) or collectively through the {@link #values()} method which
 * returns all values as an array.
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
	 * A tuple of 2 elements. Represents an ordered pair of values of different
	 * types.
	 *
	 * @param <T1> the type of the first value
	 * @param <T2> the type of the second value
	 */
	interface Tuple2<T1, T2> extends Tuple {

		/**
		 * Record implementation of Tuple2.
		 *
		 * @param <T1>   the type of the first value
		 * @param <T2>   the type of the second value
		 * @param value1 first value
		 * @param value2 second value
		 */
		record Tuple2Impl<T1, T2>(T1 value1, T2 value2) implements Tuple2<T1, T2> {
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
		 * Creates a new {@code Tuple2} instance containing two values.
		 *
		 * @param <T1>   the type of the first value
		 * @param <T2>   the type of the second value
		 * @param value1 the first value
		 * @param value2 the second value
		 * @return a new {@code Tuple2} containing the specified values
		 */
		static <T1, T2> Tuple2<T1, T2> of(T1 value1, T2 value2) {
			return new Tuple2Impl<>(value1, value2);
		}

		/**
		 * Returns the first value of this tuple.
		 *
		 * @return the first value
		 */
		T1 value1();

		/**
		 * Returns the second value of this tuple.
		 *
		 * @return the second value
		 */
		T2 value2();
	}

	/**
	 * A tuple of 3 elements. Represents an ordered triplet of values of different
	 * types.
	 *
	 * @param <T1> the type of the first value
	 * @param <T2> the type of the second value
	 * @param <T3> the type of the third value
	 */
	interface Tuple3<T1, T2, T3> extends Tuple {

		/**
		 * Record implementation of Tuple3.
		 *
		 * @param <T1>   the type of the first value
		 * @param <T2>   the type of the second value
		 * @param <T3>   the type of the third value
		 * @param value1 first value
		 * @param value2 second value
		 * @param value3 third value
		 */
		record Tuple3Impl<T1, T2, T3>(T1 value1, T2 value2, T3 value3) implements Tuple3<T1, T2, T3> {
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
				return "Tuple3 [" + String.valueOf(value1) + ", " + String.valueOf(value2) + ", " + String.valueOf(
						value3) + "]";
			}
		}

		/**
		 * Creates a new {@code Tuple3} instance containing three values.
		 *
		 * @param <T1>   the type of the first value
		 * @param <T2>   the type of the second value
		 * @param <T3>   the type of the third value
		 * @param value1 the first value
		 * @param value2 the second value
		 * @param value3 the third value
		 * @return a new {@code Tuple3} containing the specified values
		 */
		static <T1, T2, T3> Tuple3<T1, T2, T3> of(T1 value1, T2 value2, T3 value3) {
			return new Tuple3Impl<>(value1, value2, value3);
		}

		/**
		 * Returns the first value of this tuple.
		 *
		 * @return the first value
		 */
		T1 value1();

		/**
		 * Returns the second value of this tuple.
		 *
		 * @return the second value
		 */
		T2 value2();

		/**
		 * Returns the third value of this tuple.
		 *
		 * @return the third value
		 */
		T3 value3();
	}

	/**
	 * A tuple of 4 elements. Represents an ordered quadruplet of values of
	 * different types.
	 *
	 * @param <T1> the type of the first value
	 * @param <T2> the type of the second value
	 * @param <T3> the type of the third value
	 * @param <T4> the type of the fourth value
	 */
	interface Tuple4<T1, T2, T3, T4> extends Tuple {

		/**
		 * Record implementation of Tuple4.
		 *
		 * @param <T1>   the type of the first value
		 * @param <T2>   the type of the second value
		 * @param <T3>   the type of the third value
		 * @param <T4>   the type of the fourth value
		 * @param value1 first value
		 * @param value2 second value
		 * @param value3 third value
		 * @param value4 fourth value
		 */
		record Tuple4Impl<T1, T2, T3, T4>(T1 value1, T2 value2, T3 value3, T4 value4) implements
				Tuple4<T1, T2, T3, T4> {
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
		 * Creates a new {@code Tuple4} instance containing four values.
		 *
		 * @param <T1>   the type of the first value
		 * @param <T2>   the type of the second value
		 * @param <T3>   the type of the third value
		 * @param <T4>   the type of the fourth value
		 * @param value1 the first value
		 * @param value2 the second value
		 * @param value3 the third value
		 * @param value4 the fourth value
		 * @return a new {@code Tuple4} containing the specified values
		 */
		static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4> of(T1 value1, T2 value2, T3 value3, T4 value4) {
			return new Tuple4Impl<>(value1, value2, value3, value4);
		}

		/**
		 * Returns the first value of this tuple.
		 *
		 * @return the first value
		 */
		T1 value1();

		/**
		 * Returns the second value of this tuple.
		 *
		 * @return the second value
		 */
		T2 value2();

		/**
		 * Returns the third value of this tuple.
		 *
		 * @return the third value
		 */
		T3 value3();

		/**
		 * Returns the fourth value of this tuple.
		 *
		 * @return the fourth value
		 */
		T4 value4();
	}

	/**
	 * A tuple of 5 elements. Represents an ordered quintuplet of values of
	 * different types.
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
		 *
		 * @param <T1>   the type of the first value
		 * @param <T2>   the type of the second value
		 * @param <T3>   the type of the third value
		 * @param <T4>   the type of the fourth value
		 * @param <T5>   the type of the fifth value
		 * @param value1 first value
		 * @param value2 second value
		 * @param value3 third value
		 * @param value4 fourth value
		 * @param value5 fifth value
		 */
		record Tuple5Impl<T1, T2, T3, T4, T5>(T1 value1, T2 value2, T3 value3, T4 value4, T5 value5)
				implements Tuple5<T1, T2, T3, T4, T5> {
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
		 * Creates a new {@code Tuple5} instance containing five values.
		 *
		 * @param <T1>   the type of the first value
		 * @param <T2>   the type of the second value
		 * @param <T3>   the type of the third value
		 * @param <T4>   the type of the fourth value
		 * @param <T5>   the type of the fifth value
		 * @param value1 the first value
		 * @param value2 the second value
		 * @param value3 the third value
		 * @param value4 the fourth value
		 * @param value5 the fifth value
		 * @return a new {@code Tuple5} containing the specified values
		 */
		static <T1, T2, T3, T4, T5> Tuple5<T1, T2, T3, T4, T5> of(T1 value1, T2 value2, T3 value3, T4 value4,
				T5 value5) {
			return new Tuple5Impl<>(value1, value2, value3, value4, value5);
		}

		/**
		 * Returns the first value of this tuple.
		 *
		 * @return the first value
		 */
		T1 value1();

		/**
		 * Returns the second value of this tuple.
		 *
		 * @return the second value
		 */
		T2 value2();

		/**
		 * Returns the third value of this tuple.
		 *
		 * @return the third value
		 */
		T3 value3();

		/**
		 * Returns the fourth value of this tuple.
		 *
		 * @return the fourth value
		 */
		T4 value4();

		/**
		 * Returns the fifth value of this tuple.
		 *
		 * @return the fifth value
		 */
		T5 value5();
	}

	/**
	 * Returns all values of this tuple as an array of Objects.
	 *
	 * @return an array containing all values in this tuple
	 */
	Object[] values();
}