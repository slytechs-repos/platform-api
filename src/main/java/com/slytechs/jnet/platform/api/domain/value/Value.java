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
package com.slytechs.jnet.platform.api.domain.value;

import com.slytechs.jnet.platform.api.util.Named;

/**
 * The Value interface provides a unified API for managing and accessing value
 * instances within a domain hierarchy. It supports both simple property-style
 * values and more complex event-backed or computed values, offering thread-safe
 * operations and type-safe access patterns.
 * 
 * <p>
 * Values can represent various types of data:
 * </p>
 * <ul>
 * <li>Simple properties or attributes</li>
 * <li>Computed or derived values</li>
 * <li>Event-backed fields</li>
 * <li>Cached results with invalidation support</li>
 * </ul>
 * 
 * <p>
 * All value operations are atomic and thread-safe when used within a domain
 * hierarchy. The interface provides both type-safe and raw object operations,
 * with the type-safe variants being preferred for compile-time safety.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * // Simple value operations
 * Value counter = Values.of("counter", domain, 0);
 * counter.set(42);
 * int value = counter.getValue(Integer.class);
 * 
 * // Type-safe atomic operations
 * boolean updated = counter.compareAndSetValue(42, 100);
 * int previous = counter.getAndSetValue(200);
 * 
 * // Using with computed values
 * Value computed = Values.computed("timestamp", domain, System::currentTimeMillis);
 * long time = computed.getValue(Long.class);
 * computed.invalidate(); // Force recomputation
 * }</pre>
 */
public interface Value extends Named {

	/**
	 * Retrieves the current value. This operation is atomic and thread-safe within
	 * the domain hierarchy.
	 * 
	 * <p>
	 * For computed or cached values, this method may trigger computation or
	 * validation of the cached value. The behavior depends on the specific
	 * implementation and whether the value has been invalidated.
	 * </p>
	 *
	 * @return the current value
	 * @throws IllegalStateException if the value is in an invalid state or cannot
	 *                               be computed
	 * @see #getValue(Class)
	 */
	Object get();

	/**
	 * Retrieves the current value with type safety. This is the preferred method
	 * when working with known value types as it provides compile-time type
	 * checking.
	 *
	 * @param <T>        the expected type of the value
	 * @param valueClass the class representing the expected type
	 * @return the current value cast to the specified type
	 * @throws IllegalStateException if the value is in an invalid state
	 * @throws ClassCastException    if the value cannot be cast to the specified
	 *                               type
	 */
	@SuppressWarnings("unchecked")
	default <T> T getValue(Class<T> valueClass) {
		return (T) get();
	}

	/**
	 * Sets a new value. This operation is atomic and thread-safe within the domain
	 * hierarchy.
	 * 
	 * <p>
	 * Some value implementations may be read-only or computed, in which case this
	 * method will throw an UnsupportedOperationException.
	 * </p>
	 *
	 * @param newValue the new value to set
	 * @throws UnsupportedOperationException if this value is read-only
	 * @throws IllegalArgumentException      if the new value is invalid
	 * @throws ClassCastException            if the new value is of an incompatible
	 *                                       type
	 */
	void set(Object newValue);

	/**
	 * Sets a new value with type safety. This is the preferred method when working
	 * with known value types as it provides compile-time type checking.
	 *
	 * @param <T>      the type of the value
	 * @param newValue the new value to set
	 * @throws UnsupportedOperationException if this value is read-only
	 * @throws IllegalArgumentException      if the new value is invalid
	 * @throws ClassCastException            if the new value is of an incompatible
	 *                                       type
	 */
	default <T> void setValue(T newValue) {
		set(newValue);
	}

	/**
	 * Atomically sets a new value if the current value matches the expected value.
	 * This operation is atomic and thread-safe within the domain hierarchy.
	 *
	 * @param expectedValue the value that must be present for the update to occur
	 * @param newValue      the new value to set if the comparison succeeds
	 * @return true if the update was successful, false otherwise
	 * @throws UnsupportedOperationException if this value is read-only
	 * @throws IllegalArgumentException      if either value is invalid
	 * @throws ClassCastException            if either value is of an incompatible
	 *                                       type
	 */
	boolean compareAndSet(Object expectedValue, Object newValue);

	/**
	 * Type-safe version of compareAndSet. This is the preferred method when working
	 * with known value types as it provides compile-time type checking.
	 *
	 * @param <T>           the type of the values
	 * @param expectedValue the value that must be present for the update to occur
	 * @param newValue      the new value to set if the comparison succeeds
	 * @return true if the update was successful, false otherwise
	 * @throws UnsupportedOperationException if this value is read-only
	 * @throws IllegalArgumentException      if either value is invalid
	 * @throws ClassCastException            if either value is of an incompatible
	 *                                       type
	 */
	default <T> boolean compareAndSetValue(T expectedValue, T newValue) {
		return compareAndSet(expectedValue, newValue);
	}

	/**
	 * Atomically retrieves the current value and sets a new one. This operation is
	 * atomic and thread-safe within the domain hierarchy.
	 *
	 * @param newValue the new value to set
	 * @return the previous value
	 * @throws UnsupportedOperationException if this value is read-only
	 * @throws IllegalArgumentException      if the new value is invalid
	 * @throws ClassCastException            if the new value is of an incompatible
	 *                                       type
	 */
	Object getAndSet(Object newValue);

	/**
	 * Type-safe version of getAndSet. This is the preferred method when working
	 * with known value types as it provides compile-time type checking.
	 *
	 * @param <T>      the type of the value
	 * @param newValue the new value to set
	 * @return the previous value cast to the specified type
	 * @throws UnsupportedOperationException if this value is read-only
	 * @throws IllegalArgumentException      if the new value is invalid
	 * @throws ClassCastException            if the new value is of an incompatible
	 *                                       type
	 */
	@SuppressWarnings("unchecked")
	default <T> T getAndSetValue(T newValue) {
		return (T) getAndSet(newValue);
	}

	/**
	 * Invalidates any cached state of this value. After invalidation, the next
	 * access will trigger recomputation or revalidation as appropriate.
	 * 
	 * <p>
	 * This operation is particularly useful for:
	 * </p>
	 * <ul>
	 * <li>Cached computed values that need refreshing</li>
	 * <li>Event-backed values that need to clear their cache</li>
	 * <li>Values that maintain derived state that needs recalculation</li>
	 * </ul>
	 * 
	 * <p>
	 * For simple values that don't maintain cached state, this operation has no
	 * effect. The operation is guaranteed to be thread-safe within the domain
	 * hierarchy.
	 * </p>
	 */
	default void invalidate() {
		// Default implementation does nothing
	}
}