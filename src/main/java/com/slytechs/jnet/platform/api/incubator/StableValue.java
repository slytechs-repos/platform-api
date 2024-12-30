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

import java.io.Closeable;
import java.util.function.Supplier;

import com.slytechs.jnet.platform.api.util.function.CheckedUtils;
import com.slytechs.jnet.platform.api.util.function.ThrowableSupplier;
import com.slytechs.jnet.platform.api.util.function.UncheckedSupplier;

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
 * </ul>
 * 
 * <p>
 * Example usage:
 * 
 * <pre>{@code
 * // Create empty stable value
 * StableValue<ExpensiveObject> stable1 = StableValue.of();
 * 
 * // Create pre-initialized stable value
 * StableValue<String> stable2 = StableValue.of("initialized");
 * 
 * // Create lazy-initialized stable value with supplier
 * StableValue<ExpensiveObject> stable3 = StableValue.ofSupplier(() -> new ExpensiveObject());
 * 
 * // Later, possibly in a different thread:
 * ExpensiveObject obj = stable1.computeIfUnset(() -> new ExpensiveObject());
 * }</pre>
 *
 * <p>
 * This implementation is particularly useful for:
 * <ul>
 * <li>Breaking up monolithic initialization of application state
 * <li>Implementing lazy initialization patterns without performance penalties
 * <li>Managing immutable values that require complex or costly initialization
 * <li>Ensuring thread-safe single initialization of shared resources
 * </ul>
 *
 * @param <T> the type of value being stored
 * @author Mark Bednarczyk
 * @see java.lang.reflect.Field#isFinal()
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
		return new StableValue<>(UncheckedSupplier.of(factory));
	}

	/**
	 * Creates a new StableValue container with a supplier that may throw checked
	 * exceptions. The supplier will be used to initialize the value on first
	 * access.
	 * 
	 * <p>
	 * Any exceptions thrown by the supplier during initialization will be
	 * propagated to the caller when the value is accessed through
	 * {@link #getOrThrow()}, {@link #getOrThrow(Class)}, or
	 * {@link #computeIfUnset(ThrowableSupplier)}. This allows for proper exception
	 * handling at the point of value access rather than creation.
	 * 
	 * <p>
	 * Example usage:
	 * 
	 * <pre>{@code
	 * // Create with a supplier that may throw IOException
	 * StableValue<FileReader> value = StableValue.ofThrowableSupplier(
	 * 		() -> new FileReader("config.txt"));
	 * 
	 * try {
	 * 	// Exception may be thrown here during initialization
	 * 	FileReader reader = value.getOrThrow();
	 * 	// Use reader...
	 * } catch (IOException e) {
	 * 	// Handle file access exception
	 * }
	 * }</pre>
	 *
	 * @param <T>     the type of value to be stored
	 * @param factory the supplier that will provide the value when needed
	 * @return a new StableValue instance with exception-aware lazy initialization
	 * @see #getOrThrow()
	 * @see #getOrThrow(Class)
	 * @see #computeIfUnset(ThrowableSupplier)
	 */
	public static <T> StableValue<T> ofThrowableSupplier(ThrowableSupplier<T> factory) {
		return new StableValue<>(factory);
	}

	private volatile T value = null;
	private final UncheckedSupplier<T> valueSupplier;
	private final ThrowableSupplier<T> valueThrowableSupplier;

	/**
	 * Creates an empty StableValue container with no initial value or supplier.
	 * This constructor is used by the {@link #of()} factory method.
	 * 
	 * @see #of()
	 */
	private StableValue() {
		this.valueSupplier = null;
		this.valueThrowableSupplier = null;
	}

	/**
	 * Creates a StableValue container pre-initialized with the given value. This
	 * constructor is used by the {@link #of(T)} factory method.
	 *
	 * @param value the initial value to store, may be null
	 * @see #of(T)
	 */
	private StableValue(T value) {
		this.value = value;
		this.valueSupplier = null;
		this.valueThrowableSupplier = null;
	}

	/**
	 * Creates a StableValue container with a supplier that may throw checked
	 * exceptions. The supplier is converted to both checked and unchecked variants
	 * for flexible access. This constructor is used by the
	 * {@link #ofThrowableSupplier(ThrowableSupplier)} factory method.
	 *
	 * @param <E>     the type of throwable that the supplier may throw
	 * @param factory the supplier that will provide the value and may throw checked
	 *                exceptions
	 * @see #ofThrowableSupplier(ThrowableSupplier)
	 * @see #getOrThrow()
	 * @see #get()
	 */
	private <E extends Throwable> StableValue(ThrowableSupplier<T> factory) {
		this.valueSupplier = factory.asUnchecked();
		this.valueThrowableSupplier = factory;
	}

	/**
	 * Creates a StableValue container with an unchecked supplier. The supplier is
	 * converted to both checked and unchecked variants for consistent interface.
	 * This constructor is used by the {@link #ofSupplier(Supplier)} factory method.
	 *
	 * @param valueFactory the unchecked supplier that will provide the value
	 * @see #ofSupplier(Supplier)
	 * @see #get()
	 */
	private StableValue(UncheckedSupplier<T> valueFactory) {
		this.valueSupplier = valueFactory;
		this.valueThrowableSupplier = valueFactory.asThrowable();
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
	 * checked exceptions.
	 *
	 * @param supplier the supplier to compute the value if not already set
	 * @return the initialized value (either existing or newly computed)
	 * @throws Exception if the supplier throws an exception during value
	 *                   computation
	 */
	public T computeIfUnset(ThrowableSupplier<T> supplier) throws Exception {
		if (value == null) {
			synchronized (this) {
				if (value == null) {
					try {
						value = supplier.getOrThrow();
					} catch (RuntimeException e) {
						throw e;
					} catch (Throwable e) {
						throw (Exception) e;
					}
				}
			}
		}
		return value;
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
	 */
	public T get() {
		if (value == null) {
			if (valueSupplier == null)
				throw new IllegalStateException("no value");
			return computeIfUnset(valueSupplier);
		}

		return value;
	}

	/**
	 * Retrieves the stored value, potentially throwing checked exceptions during
	 * initialization.
	 *
	 * @return the stored value
	 * @throws Exception             if initialization fails with a checked
	 *                               exception
	 * @throws IllegalStateException if no value is set and no supplier was provided
	 */
	public T getOrThrow() throws Exception {
		if (value == null) {
			if (valueThrowableSupplier == null)
				throw new IllegalStateException("no value");
			return computeIfUnset(valueThrowableSupplier);
		}

		return value;
	}

	/**
	 * Retrieves the stored value, throwing only the specified exception type.
	 *
	 * @param <E>            the type of exception to throw
	 * @param exceptionClass the class object of the exception type
	 * @return the stored value
	 * @throws E                     if initialization fails with the specified
	 *                               exception type
	 * @throws IllegalStateException if no value is set and no supplier was provided
	 * @throws RuntimeException      if initialization fails with an unexpected
	 *                               exception type
	 */
	public <E extends Exception> T getOrThrow(Class<E> exceptionClass) throws E {
		if (value == null) {
			if (valueThrowableSupplier == null)
				throw new IllegalStateException("no value");

			try {
				return computeIfUnset(valueThrowableSupplier);
			} catch (Exception e) {
				throw CheckedUtils.castAsCheckedOrThrowRuntime(e, exceptionClass);
			}
		}

		return value;
	}

	/**
	 * Clears the stored value, safely releasing any associated resources. This
	 * method does not invoke close() on the value if it implements AutoCloseable.
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
	 * Closeable. If the value doesn't implement either interface, this method has
	 * no effect.
	 *
	 * @throws Exception if closing the resource throws an exception
	 */
	public void close() throws Exception {
		if (value instanceof AutoCloseable closeable) {
			synchronized (this) {
				if (value != null) {
					closeable.close();

					value = null;
				}
			}
		} else if (value instanceof Closeable closeable) {
			synchronized (this) {
				if (value != null) {
					closeable.close();

					value = null;
				}
			}

		}

	}

	/**
	 * Closes and clears the stored value, throwing any exceptions that occur during
	 * closure.
	 *
	 * @param <E> the type of throwable that may be thrown
	 * @throws E if closing the resource throws an exception
	 */
	@SuppressWarnings("unchecked")
	public <E extends Throwable> void closeOrThrow() throws E {
		if (value instanceof AutoCloseable closeable) {
			synchronized (this) {
				if (value != null) {
					try {
						closeable.close();
					} catch (Exception e) {
						throw (E) e;
					}

					value = null;
				}
			}
		} else if (value instanceof Closeable closeable) {
			synchronized (this) {
				if (value != null) {
					try {
						closeable.close();
					} catch (Exception e) {
						throw (E) e;
					}

					value = null;
				}
			}
		}
	}
}