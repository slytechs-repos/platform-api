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
package com.slytechs.jnet.jnetruntime.internal.util;

import java.util.function.Supplier;

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
		return new StableValue<>(factory);
	}

	private volatile T value = null;
	private final Supplier<T> valueSupplier;

	private StableValue() {
		this.valueSupplier = null;
	}

	private StableValue(T value) {
		this.value = value;
		this.valueSupplier = null;
	}

	private StableValue(Supplier<T> valueFactory) {
		this.valueSupplier = valueFactory;
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
	 * Clears the stored value, safely releasing any associated resources.
	 */
	public void clear() {
		if (value != null) {
			synchronized (this) {
				value = null;
			}
		}
	}
}