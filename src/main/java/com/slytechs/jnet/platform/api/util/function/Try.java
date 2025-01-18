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

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Represents the result of an operation that may either succeed with a value or
 * fail with an exception.
 *
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * // Parse message
 * Try<Message> result = Try.of(() -> parseMessage(data));
 * 
 * // Basic error handling
 * if (result.isFailure()) {
 * 	log.error("Parse failed: {}", result.failure().getMessage());
 * 	return Try.failure(result.failure());
 * }
 * 
 * // Transform message
 * Try<Response> response = result
 * 		.map(Message::process)
 * 		.map(Response::new);
 * 
 * // Combine host and port
 * Try<String> host = Try.of(() -> getHost());
 * Try<Integer> port = Try.of(() -> getPort());
 * 
 * Try<Connection> conn = host.combineWith(
 * 		port,
 * 		(h, p) -> connect(h, p));
 * 
 * // Convert to primitive
 * Try<Integer> size = Try.of(() -> message.getSize());
 * IntTry intSize = size.toIntTry();
 * 
 * // Get value or default
 * Connection connection = conn.orElse(defaultConnection);
 * }</pre>
 *
 * @param <T> the type of the success value
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public interface Try<T> {

	/**
	 * Record implementation of Try that stores either a success value or failure
	 * exception.
	 */
	record TryRecord<T>(@Nullable T success, @Nullable Exception failure) implements Try<T> {

		/**
		 * Constructs a Try instance, ensuring exactly one of success or failure is
		 * present.
		 *
		 * @throws IllegalArgumentException if both success and failure are null or
		 *                                  non-null
		 */
		public TryRecord {
			if ((success == null) == (failure == null)) {
				throw new IllegalArgumentException("Try must have either success or failure, not both or neither");
			}
		}

		@Override
		public String toString() {
			return isSuccess()
					? "Success[" + String.valueOf(success) + "]"
					: "Failure[" + failure + "]";
		}
	}

	/**
	 * Creates a new successful Try with the given value.
	 *
	 * @param <T>   the type of the success value
	 * @param value the success value, must not be null
	 * @return a new successful Try
	 * @throws IllegalArgumentException if value is null
	 */
	static <T> Try<T> success(T value) {
		Objects.requireNonNull(value, "Success value must not be null");
		return new TryRecord<>(value, null);
	}

	/**
	 * Creates a new failed Try with the given exception.
	 *
	 * @param <T>       the type of the success value
	 * @param exception the failure exception, must not be null
	 * @return a new failed Try
	 * @throws IllegalArgumentException if exception is null
	 */
	static <T> Try<T> failure(Exception exception) {
		Objects.requireNonNull(exception, "Failure exception must not be null");
		return new TryRecord<>(null, exception);
	}

	/**
	 * Attempts to execute the given supplier and wrap its result in a Try.
	 *
	 * @param <T>      the type of the success value
	 * @param supplier the operation to attempt
	 * @return a Try containing either the operation's result or any thrown
	 *         exception
	 */
	static <T> Try<T> of(ThrowingSupplier<T> supplier) {
		try {
			return success(supplier.get());
		} catch (Exception e) {
			return failure(e);
		}
	}

	/**
	 * Attempts to execute the given checked supplier and wrap its result in a Try.
	 *
	 * @param <T>      the type of the success value
	 * @param <E>      the type of the checked exception
	 * @param supplier the operation to attempt
	 * @return a Try containing either the operation's result or any thrown
	 *         exception
	 */
	static <T, E extends Exception> Try<T> ofChecked(CheckedSupplier<T, E> supplier) {
		try {
			return success(supplier.get());
		} catch (Exception e) {
			return failure(e);
		}
	}

	/**
	 * Returns whether this is a successful Try.
	 *
	 * @return true if this is a success, false if it's a failure
	 */
	default boolean isSuccess() {
		return success() != null;
	}

	/**
	 * Returns whether this is a failed Try.
	 *
	 * @return true if this is a failure, false if it's a success
	 */
	default boolean isFailure() {
		return !isSuccess();
	}

	/**
	 * Returns the success value if present.
	 *
	 * @return the success value, null if this is a failure
	 */
	@Nullable
	T success();

	/**
	 * Returns the failure exception if present.
	 *
	 * @return the failure exception, null if this is a success
	 */
	@Nullable
	Exception failure();

	/**
	 * Executes the given action if this is a success.
	 *
	 * @param action the action to execute with the success value
	 * @return this Try for method chaining
	 */
	default Try<T> ifSuccess(Consumer<? super T> action) {
		if (isSuccess()) {
			action.accept(success());
		}
		return this;
	}

	/**
	 * Executes the given action if this is a failure.
	 *
	 * @param action the action to execute with the failure exception
	 * @return this Try for method chaining
	 */
	default Try<T> ifFailure(Consumer<? super Exception> action) {
		if (isFailure()) {
			action.accept(failure());
		}
		return this;
	}

	/**
	 * Maps the success value to a new value if this is a success.
	 *
	 * @param <U>    the type of the mapped value
	 * @param mapper the function to apply to the success value
	 * @return a new Try with either the mapped value or the original failure
	 */
	default <U> Try<U> map(Function<? super T, ? extends U> mapper) {
		return isSuccess()
				? Try.success(mapper.apply(success()))
				: Try.failure(failure());
	}

	/**
	 * Maps the success value to a new Try if this is a success.
	 *
	 * @param <U>    the type of the mapped value
	 * @param mapper the function to apply to the success value
	 * @return the mapped Try or the original failure
	 */
	default <U> Try<U> flatMap(Function<? super T, Try<U>> mapper) {
		return isSuccess()
				? mapper.apply(success())
				: Try.failure(failure());
	}

	/**
	 * Filters the success value using the given predicate.
	 *
	 * @param predicate the predicate to test the success value
	 * @return this Try if it's a failure or the predicate returns true, otherwise a
	 *         failure with IllegalStateException
	 */
	default Try<T> filter(Predicate<? super T> predicate) {
		if (isSuccess() && !predicate.test(success())) {
			return Try.failure(new IllegalStateException("Predicate does not match for: " + success()));
		}
		return this;
	}

	/**
	 * Returns the success value or throws the failure exception.
	 *
	 * @return the success value if present
	 * @throws Exception the failure exception if this is a failure
	 */
	default T get() throws Exception {
		if (isSuccess()) {
			return success();
		}
		throw failure();
	}

	/**
	 * Returns the success value or the given default value.
	 *
	 * @param defaultValue the value to use if this is a failure
	 * @return the success value or the default value
	 */
	default T orElse(T defaultValue) {
		return isSuccess() ? success() : defaultValue;
	}

	/**
	 * Returns the success value or gets a value from the supplier.
	 *
	 * @param supplier the supplier to provide a value if this is a failure
	 * @return the success value or the supplied value
	 */
	default T orElseGet(Supplier<? extends T> supplier) {
		return isSuccess() ? success() : supplier.get();
	}

	/**
	 * Returns the success value or throws the given exception.
	 *
	 * @param <X>               the type of exception to throw
	 * @param exceptionSupplier the supplier of the exception to throw
	 * @return the success value if present
	 * @throws X if this is a failure
	 */
	default <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
		if (isSuccess()) {
			return success();
		}
		throw exceptionSupplier.get();
	}

	/**
	 * Recovers from a failure by providing an alternative value.
	 *
	 * @param recovery the function to provide an alternative value from the failure
	 * @return the success value or the recovered value
	 */
	default T recover(Function<? super Exception, ? extends T> recovery) {
		return isSuccess() ? success() : recovery.apply(failure());
	}

	/**
	 * Converts this Try to an Optional.
	 *
	 * @return an Optional containing the success value, or empty if this is a
	 *         failure
	 */
	default Optional<T> toOptional() {
		return Optional.ofNullable(success());
	}

	/**
	 * Converts this Try to a Stream.
	 *
	 * @return a Stream containing the success value, or an empty Stream if this is
	 *         a failure
	 */
	default Stream<T> stream() {
		return isSuccess() ? Stream.of(success()) : Stream.empty();
	}

	/**
	 * A supplier that may throw an unchecked exception.
	 *
	 * @param <T> the type of value supplied
	 */
	@FunctionalInterface
	interface ThrowingSupplier<T> {
		T get() throws Exception;
	}

	/**
	 * A supplier that may throw a checked exception.
	 *
	 * @param <T> the type of value supplied
	 * @param <E> the type of exception that may be thrown
	 */
	@FunctionalInterface
	interface CheckedSupplier<T, E extends Exception> {
		T get() throws E;
	}

	/**
	 * Maps a failure to a different exception type.
	 *
	 * @param mapper the function to convert the exception
	 * @return this Try if success, or a new failure with the mapped exception
	 */
	default Try<T> mapFailure(Function<Exception, Exception> mapper) {
		if (isSuccess()) {
			return this;
		}
		return Try.failure(mapper.apply(failure()));
	}

	/**
	 * Combines multiple Try instances into a single result.
	 *
	 * @param <U>      type of the other Try
	 * @param <R>      type of the result
	 * @param other    the other Try to combine with
	 * @param combiner function to combine both success values
	 * @return a new Try with the combined result or the first failure encountered
	 */
	default <U, R> Try<R> combineWith(Try<U> other, BiFunction<T, U, R> combiner) {
		if (isFailure()) {
			return Try.failure(failure());
		}
		if (other.isFailure()) {
			return Try.failure(other.failure());
		}
		return Try.success(combiner.apply(success(), other.success()));
	}

	/**
	 * Converts the Try to a primitive IntTry if the value is an Integer.
	 *
	 * @return an IntTry containing the int value or the original failure
	 * @throws IllegalStateException if the value is not an Integer
	 */
	default IntTry toIntTry() {
		if (isFailure()) {
			return IntTry.failure(failure());
		}

		T value = success();
		if (!(value instanceof Integer i)) {
			return IntTry.failure(new IllegalStateException("Value is not an Integer"));
		}

		return IntTry.success(i);
	}

	/**
	 * Converts the Try to a primitive LongTry if the value is a Long.
	 *
	 * @return a LongTry containing the long value or the original failure
	 * @throws IllegalStateException if the value is not a Long
	 */
	default LongTry toLongTry() {
		if (isFailure()) {
			return LongTry.failure(failure());
		}

		T value = success();
		if (!(value instanceof Long l)) {
			return LongTry.failure(new IllegalStateException("Value is not a Long"));
		}

		return LongTry.success(l);
	}
}