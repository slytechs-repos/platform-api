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

import java.util.Optional;
import java.util.concurrent.Callable;
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
	 * Creates a new failed Try with the given exception.
	 *
	 * @param <T>       the type of the success value
	 * @param exception the failure exception, must not be null
	 * @return a new failed Try
	 */
	static <T> Try<T> failure(Exception exception) {
		return new TryFailureRecord<>(exception);
	}

	/**
	 * Lifts a throwing function into a function that returns a Try.
	 *
	 * @param <T> the input type
	 * @param <R> the result type
	 * @param f   the function that may throw
	 * @return a function that returns a Try
	 */
	static <T, R> Function<T, Try<R>> lift(ThrowingFunction<T, R> f) {
		return t -> Try.of(() -> f.apply(t));
	}

	/**
	 * Lifts a throwing consumer into a safe consumer that handles exceptions.
	 *
	 * @param <T> the input type
	 * @param c   the consumer that may throw
	 * @return a consumer that handles exceptions
	 */
	static <T> Consumer<T> liftConsumer(ThrowingConsumer<T> c) {
		return t -> {
			try {
				c.accept(t);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	/**
	 * Lifts a throwing consumer into a safe consumer with custom error handling.
	 *
	 * @param <T>          the input type
	 * @param c            the consumer that may throw
	 * @param errorHandler handles any exceptions
	 * @return a consumer that safely handles exceptions
	 */
	static <T> Consumer<T> liftConsumer(ThrowingConsumer<T> c, Consumer<Exception> errorHandler) {
		return t -> {
			try {
				c.accept(t);
			} catch (Exception e) {
				errorHandler.accept(e);
			}
		};
	}

	/**
	 * Lifts a throwing runnable into a safe runnable that handles exceptions.
	 *
	 * @param r the runnable that may throw
	 * @return a runnable that handles exceptions
	 */
	static Runnable liftRunnable(ThrowingRunnable r) {
		return () -> {
			try {
				r.run();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	/**
	 * Lifts a throwing runnable into a safe runnable with custom error handling.
	 *
	 * @param r            the runnable that may throw
	 * @param errorHandler handles any exceptions
	 * @return a runnable that safely handles exceptions
	 */
	static Runnable liftRunnable(ThrowingRunnable r, Consumer<Exception> errorHandler) {
		return () -> {
			try {
				r.run();
			} catch (Exception e) {
				errorHandler.accept(e);
			}
		};
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
	 * Attempts to execute a {@link Callable} task and wraps its result in a
	 * {@code Try}. This method provides integration with the {@link Callable}
	 * interface commonly used in concurrent programming.
	 * 
	 * <p>
	 * The task will be executed immediately and synchronously. Any exception thrown
	 * during execution will be caught and wrapped in a failed {@code Try}.
	 * </p>
	 * 
	 * <p>
	 * Example usage:
	 * </p>
	 * 
	 * <pre>{@code
	 * Callable<String> task = () -> {
	 * 	// Some potentially failing operation
	 * 	return processData();
	 * };
	 * 
	 * Try<String> result = Try.ofCallable(task);
	 * result.ifSuccess(data -> System.out.println("Success: " + data))
	 * 		.ifFailure(ex -> System.err.println("Failed: " + ex.getMessage()));
	 * }</pre>
	 *
	 * @param <T>  the type of value that the {@code Callable} returns
	 * @param task the {@code Callable} task to execute, must not be null
	 * @return a {@code Try} containing either the task's result or any thrown
	 *         exception
	 * @throws NullPointerException if the task is null
	 */
	static <T> Try<T> ofCallable(Callable<T> task) {
		try {
			return success(task.call());
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
	 * Attempts to execute a {@link Runnable} task and wraps the execution result in
	 * a {@code Try}. Since {@code Runnable} tasks don't return values, the success
	 * case will contain the original {@code Runnable} instance.
	 * 
	 * <p>
	 * The task will be executed immediately and synchronously. Any
	 * {@code RuntimeException} thrown during execution will be caught and wrapped
	 * in a failed {@code Try}. Note that unlike other methods in this class, this
	 * method only catches {@code RuntimeException}s as {@code Runnable} is not
	 * designed to throw checked exceptions.
	 * </p>
	 * 
	 * <p>
	 * Example usage:
	 * </p>
	 * 
	 * <pre>{@code
	 * Runnable task = () -> {
	 * 	// Some potentially failing operation
	 * 	processData();
	 * };
	 * 
	 * Try<Void> result = Try.ofRunnable(task);
	 * result.ifSuccess(r -> System.out.println("Task completed successfully"))
	 * 		.ifFailure(ex -> System.err.println("Task failed: " + ex.getMessage()));
	 * }</pre>
	 *
	 * @param task the {@code Runnable} task to execute, must not be null
	 * @return a {@code Try} containing either null (on success) or the
	 *         thrown {@code RuntimeException} (on failure)
	 * @throws NullPointerException if the task is null
	 */
	static Try<Void> ofRunnable(Runnable task) {
		try {
			task.run();
			return success(null);
		} catch (RuntimeException e) {
			return failure(e);
		}
	}

	/**
	 * Creates a new successful Try with the given value.
	 *
	 * @param <T>   the type of the success value
	 * @param value the success value, must not be null
	 * @return a new successful Try
	 */
	static <T> Try<T> success(T value) {
		return new TrySuccessRecord<>(value);
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
	 * Returns the failure exception if present.
	 *
	 * @return the failure exception, null if this is a success
	 */
	@Nullable
	Exception failure();

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
	 * Executes a throwing consumer if this is a success.
	 *
	 * @param c the consumer that may throw
	 * @return this Try for chaining
	 */
	default Try<T> ifSuccessThrowing(ThrowingConsumer<? super T> c) {
		if (isSuccess()) {
			try {
				c.accept(success());
			} catch (Exception e) {
				return Try.failure(e);
			}
		}
		return this;
	}

	/**
	 * Returns whether this is a failed Try.
	 *
	 * @return true if this is a failure, false if it's a success
	 */
	boolean isFailure();

	/**
	 * Returns whether this is a successful Try.
	 *
	 * @return true if this is a success, false if it's a failure
	 */
	boolean isSuccess();

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
	 * Maps this try using a throwing function.
	 *
	 * @param <R> the result type
	 * @param f   the function that may throw
	 * @return a new Try with the mapped value
	 */
	default <R> Try<R> mapThrowing(ThrowingFunction<? super T, ? extends R> f) {
		if (isFailure()) {
			return Try.failure(failure());
		}
		try {
			return Try.success(f.apply(success()));
		} catch (Exception e) {
			return Try.failure(e);
		}
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
	 * Converts this Try to a Stream.
	 *
	 * @return a Stream containing the success value, or an empty Stream if this is
	 *         a failure
	 */
	default Stream<T> stream() {
		return isSuccess() ? Stream.of(success()) : Stream.empty();
	}

	/**
	 * Returns the success value if present.
	 *
	 * @return the success value, null if this is a failure
	 */
	@Nullable
	T success();

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

	/**
	 * Converts this Try to an Optional.
	 *
	 * @return an Optional containing the success value, or empty if this is a
	 *         failure
	 */
	default Optional<T> toOptional() {
		return Optional.ofNullable(success());
	}
}