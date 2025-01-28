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
package com.slytechs.jnet.platform.api.incubator;

import java.util.function.Supplier;

import com.slytechs.jnet.platform.api.util.function.ThrowingSupplier;
import com.slytechs.jnet.platform.api.util.function.Try;

/**
 * An implementation of a stable value container that represents immutable data
 * with flexible initialization timing. Unlike {@code final} fields which must
 * be initialized eagerly during construction, StableValue allows lazy
 * initialization while maintaining similar performance characteristics through
 * JVM optimizations.
 * 
 * <p>
 * Key features:
 * <ul>
 * <li>Thread-safe initialization guaranteed to occur at most once
 * <li>Enables JVM constant-folding optimizations similar to final fields
 * <li>Supports lazy initialization patterns
 * <li>Safe for sharing across multiple threads after initialization
 * <li>Integration with Try for safe exception handling
 * </ul>
 * 
 * <p>
 * Example usage:
 * 
 * <pre>{@code
 * // Basic usage with non-throwing operations
 * StableValue<String> stable1 = StableValue.of(); // Empty
 * StableValue<String> stable2 = StableValue.of("initialized"); // Pre-initialized
 * StableValue<String> stable3 = StableValue.ofSupplier( // Lazy with supplier
 * 		() -> "computed");
 * 
 * // Using with operations that may throw
 * StableValue<FileReader> reader = StableValue.ofThrowing(
 * 		() -> new FileReader("config.txt")); // May throw IOException
 * 
 * // Safe access with exception handling
 * Try<FileReader> result = reader.getThrowing();
 * result.ifSuccess(r -> {
 * 	// Use the reader
 * 	processConfig(r);
 * }).ifFailure(ex -> {
 * 	// Handle specific exceptions
 * 	if (ex instanceof FileNotFoundException) {
 * 		createDefaultConfig();
 * 	} else {
 * 		logError("Config error", ex);
 * 	}
 * });
 * 
 * // Combining StableValue with Try transformations
 * StableValue<Config> config = StableValue.ofThrowing(() -> {
 * 	Try<String> content = Try.of(() -> readFile("config.txt"))
 * 			.map(str -> str.trim()) // Transform content
 * 			.filter(str -> !str.isEmpty()); // Validate
 * 
 * 	return content.map(Config::parse) // Parse config
 * 			.orElseGet(Config::getDefaults); // Use defaults if needed
 * });
 * 
 * // Resource cleanup with Try
 * Try<Void> cleanup = reader.close(); // Safe resource cleanup
 * cleanup.ifFailure(ex -> logError("Cleanup failed", ex));
 * }</pre>
 *
 * <p>
 * This implementation is particularly useful for:
 * <ul>
 * <li>Breaking up monolithic initialization of application state
 * <li>Implementing lazy initialization patterns without performance penalties
 * <li>Managing immutable values that require complex or costly initialization
 * <li>Ensuring thread-safe single initialization of shared resources
 * <li>Safely handling resources that require cleanup
 * </ul>
 *
 * @param {@literal <T>} the type of value being stored
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 * @see java.lang.reflect.Field#isFinal()
 * @see Try
 */
public final class StableValue<T> {

	/**
	 * Creates a new empty StableValue container.
	 *
	 * @param <T> the type of value to be stored
	 * @return a new empty StableValue instance
	 */
	public static <T> StableValue<T> of() {
		return new StableValue<>();
	}

	/**
	 * Creates a new StableValue container pre-initialized with the given value.
	 *
	 * @param <T>   the type of value to be stored
	 * @param value the initial value to store
	 * @return a new pre-initialized StableValue instance
	 */
	public static <T> StableValue<T> of(T value) {
		return new StableValue<>(value);
	}

	/**
	 * Creates a new StableValue container with a supplier for lazy initialization.
	 * The supplier will be used to initialize the value on first access.
	 *
	 * @param <T>     the type of value to be stored
	 * @param factory the supplier that will provide the value when needed
	 * @return a new StableValue instance with lazy initialization
	 */
	public static <T> StableValue<T> ofSupplier(Supplier<T> factory) {
		return new StableValue<>(() -> Try.success(factory.get()));
	}

	/**
	 * Creates a new StableValue container with a supplier that may throw
	 * exceptions. The supplier will be used to initialize the value on first
	 * access.
	 * 
	 * <p>
	 * Any exceptions thrown by the supplier during initialization will be wrapped
	 * in a Try instance, allowing for proper exception handling at the point of
	 * value access rather than creation.
	 * 
	 * <p>
	 * Example usage:
	 * 
	 * <pre>{@code
	 * // Create with a supplier that may throw IOException
	 * StableValue<FileReader> value = StableValue.ofThrowing(
	 * 		() -> new FileReader("config.txt"));
	 * 
	 * // Get value with exception handling
	 * Try<FileReader> result = value.getThrowing();
	 * if (result.isSuccess()) {
	 * 	FileReader reader = result.success();
	 * 	// Use reader...
	 * } else {
	 * 	// Handle exception
	 * 	handleError(result.failure());
	 * }
	 * }</pre>
	 *
	 * @param <T>     the type of value to be stored
	 * @param factory the supplier that may throw exceptions
	 * @return a new StableValue instance with exception-aware lazy initialization
	 */
	public static <T> StableValue<T> ofThrowing(ThrowingSupplier<T> factory) {
		return new StableValue<T>(Try.wrapSupplier(factory));
	}

	private volatile T value = null;
	private final Supplier<Try<T>> valueSupplier;

	private StableValue() {
		this.valueSupplier = null;
	}

	private StableValue(T value) {
		this.value = value;
		this.valueSupplier = null;
	}

	private StableValue(Supplier<Try<T>> factory) {
		this.valueSupplier = factory;
	}

	/**
	 * Atomically initializes this stable value if it hasn't been set yet. If the
	 * value is already initialized, returns the existing value. The supplier is
	 * guaranteed to be called at most once, even in multi-threaded scenarios.
	 *
	 * <p>
	 * This method provides similar guarantees to final fields regarding visibility
	 * and immutability after initialization, while allowing flexible initialization
	 * timing.
	 *
	 * @param supplier the supplier to compute the value if not already set
	 * @return the initialized value (either existing or newly computed)
	 * @throws NullPointerException if the supplier is null or returns null
	 */
	public T computeIfUnset(Supplier<T> supplier) {
		if (value == null) {
			synchronized (this) {
				if (value == null) {
					value = supplier.get();
				}
			}
		}
		return value;
	}

	/**
	 * Atomically initializes this stable value using a supplier that may throw
	 * exceptions. The result is wrapped in a Try instance for safe handling of
	 * failures.
	 *
	 * @param supplier the supplier to compute the value if not already set
	 * @return a Try containing either the initialized value or any exception that
	 *         occurred
	 */
	public Try<T> computeIfUnsetThrowing(ThrowingSupplier<T> supplier) {
		if (value == null) {
			synchronized (this) {
				if (value == null) {
					Try<T> result = Try.ofSupplier(supplier);
					if (result.isSuccess()) {
						value = result.success();
					}
					return result;
				}
			}
		}
		return Try.success(value);
	}

	/**
	 * Retrieves the stored value, initializing it if necessary using the supplier
	 * provided during construction.
	 *
	 * <p>
	 * If this StableValue was created with {@code ofSupplier()}, the supplier will
	 * be used to initialize the value on first access. Otherwise, if no value has
	 * been set, an IllegalStateException is thrown.
	 * </p>
	 *
	 * @return the stored value
	 * @throws IllegalStateException if no value is set and no supplier was provided
	 * @throws RuntimeException      if the supplier throws an exception during
	 *                               initialization
	 */
	public T get() {
		if (value == null) {
			if (valueSupplier == null)
				throw new IllegalStateException("no value");

			Try<T> result = valueSupplier.get();
			if (result.isFailure()) {
				throw new RuntimeException(result.failure());
			}
			return computeIfUnset(() -> result.success());
		}

		return value;
	}

	/**
	 * Retrieves the stored value wrapped in a Try instance, allowing for safe
	 * handling of any initialization exceptions.
	 *
	 * @return a Try containing either the value or any exception that occurred
	 */
	public Try<T> getThrowing() {
		if (value == null) {
			if (valueSupplier == null)
				return Try.failure(new IllegalStateException("no value"));

			Try<T> result = valueSupplier.get();
			if (result.isSuccess()) {
				value = result.success();
			}
			return result;
		}

		return Try.success(value);
	}

	/**
	 * Clears the stored value. This method does not invoke close() on the value if
	 * it implements AutoCloseable.
	 */
	public void clear() {
		if (value != null) {
			synchronized (this) {
				value = null;
			}
		}
	}

	/**
	 * Closes and clears the stored value if it implements AutoCloseable or
	 * Closeable. The result is wrapped in a Try instance for safe handling of any
	 * exceptions that occur during closure.
	 *
	 * @return a Try containing either success (null) or any exception that occurred
	 */
	public Try<Void> close() {
		if (value instanceof AutoCloseable closeable) {
			synchronized (this) {
				if (value != null) {
					Try<Void> result = Try.ofRunnable(() -> {
						closeable.close();
					});

					if (result.isSuccess()) {
						value = null;
					}

					return result;
				}
			}
		}
		return Try.success(null);
	}
}