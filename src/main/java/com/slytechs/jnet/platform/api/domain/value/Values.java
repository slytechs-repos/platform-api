/*
 * Sly Technologies Free License
 * 
 * Copyright 2023 Sly Technologies Inc.
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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import com.slytechs.jnet.platform.api.domain.Domain;

/**
 * A utility class providing factory methods for creating different types of
 * Value instances. This class simplifies the creation and composition of values
 * within a domain hierarchy, supporting various patterns such as constant
 * values, computed values, transformed values, and reflective access.
 * 
 * <p>
 * All created values are thread-safe and properly integrated with the domain's
 * locking mechanism. The factory methods support several value patterns:
 * </p>
 * <ul>
 * <li>Simple POJO values</li>
 * <li>Immutable constant values</li>
 * <li>Dynamically computed values</li>
 * <li>Cached values with invalidation</li>
 * <li>Type-safe value wrappers</li>
 * <li>Transformed values with custom conversion</li>
 * <li>Reflection-based field and method access</li>
 * </ul>
 * 
 * <p>
 * Example usage patterns:
 * </p>
 * 
 * <pre>{@code
 * // Simple value
 * Value simple = Values.of("counter", domain, 0);
 * 
 * // Constant value
 * Value constant = Values.constant("pi", domain, Math.PI);
 * 
 * // Computed value
 * Value timestamp = Values.computed("time", domain, System::currentTimeMillis);
 * 
 * // Cached computed value
 * Value cached = Values.cached("expensive", domain, () -> computeExpensiveValue());
 * 
 * // Type-safe value
 * Value intValue = Values.typed(simple, Integer.class);
 * 
 * // Value with transformation
 * Value hex = Values.transform(simple,
 * 		Object::toString, // getter transformation
 * 		s -> Integer.parseInt(s, 16) // setter transformation
 * );
 * }</pre>
 */
public final class Values {

	private Values() {
		// Prevent instantiation
	}

	/**
	 * Creates a new mutable value with basic POJO semantics. The created value
	 * provides thread-safe access using the domain's locking mechanism.
	 *
	 * @param name   the name of the value
	 * @param domain the domain this value belongs to
	 * @param value  the initial value
	 * @return a new mutable Value instance
	 * @throws IllegalArgumentException if name or domain is null
	 */
	public static Value of(String name, Domain domain, Object value) {
		return new PojoValue(name, domain, value);
	}

	/**
	 * Creates an immutable value that always returns the same constant. Any
	 * attempts to modify the value will throw UnsupportedOperationException.
	 * 
	 * <p>
	 * Constants are particularly useful for configuration values or mathematical
	 * constants that should never change.
	 * </p>
	 *
	 * @param name   the name of the constant
	 * @param domain the domain this constant belongs to
	 * @param value  the constant value
	 * @return an immutable Value instance
	 * @throws IllegalArgumentException if name or domain is null
	 */
	public static Value constant(String name, Domain domain, Object value) {
		return new PojoValue(name, domain, value) {
			@Override
			public void set(Object newValue) {
				throw new UnsupportedOperationException("Cannot modify a constant value");
			}

			@Override
			public boolean compareAndSet(Object expectedValue, Object newValue) {
				throw new UnsupportedOperationException("Cannot modify a constant value");
			}

			@Override
			public Object getAndSet(Object newValue) {
				throw new UnsupportedOperationException("Cannot modify a constant value");
			}
		};
	}

	/**
	 * Creates a computed value that dynamically generates its value using the
	 * provided supplier. The value is recomputed on each access and cannot be
	 * modified directly.
	 * 
	 * <p>
	 * Computed values are useful for:
	 * </p>
	 * <ul>
	 * <li>System metrics that need real-time values</li>
	 * <li>Derived values that depend on other state</li>
	 * <li>Integration with external data sources</li>
	 * </ul>
	 *
	 * @param name     the name of the computed value
	 * @param domain   the domain this value belongs to
	 * @param supplier the function that computes the value
	 * @return a computed Value instance
	 * @throws IllegalArgumentException if any parameter is null
	 */
	public static Value computed(String name, Domain domain, Supplier<?> supplier) {
		Objects.requireNonNull(supplier, "supplier cannot be null");
		return new PojoValue(name, domain, null) {
			@Override
			public Object get() {
				return supplier.get();
			}

			@Override
			public void set(Object newValue) {
				throw new UnsupportedOperationException("Cannot modify a computed value");
			}

			@Override
			public boolean compareAndSet(Object expectedValue, Object newValue) {
				throw new UnsupportedOperationException("Cannot modify a computed value");
			}

			@Override
			public Object getAndSet(Object newValue) {
				throw new UnsupportedOperationException("Cannot modify a computed value");
			}
		};
	}

	/**
	 * Creates a value that transforms its inputs and outputs using the provided
	 * functions. This allows for automatic conversion between different
	 * representations of the same data.
	 * 
	 * <p>
	 * Common use cases include:
	 * </p>
	 * <ul>
	 * <li>Unit conversions (e.g., meters to feet)</li>
	 * <li>Format conversions (e.g., hex to decimal)</li>
	 * <li>Type adaptations (e.g., string to enum)</li>
	 * </ul>
	 *
	 * @param <T>      the type of the underlying value
	 * @param <R>      the type of the transformed value
	 * @param delegate the base value to transform
	 * @param getter   the function to transform the value when reading
	 * @param setter   the function to transform the value when writing
	 * @return a transformed Value instance
	 * @throws IllegalArgumentException if any parameter is null
	 */
	public static <T, R> Value transform(Value delegate, Function<T, R> getter, Function<R, T> setter) {
		Objects.requireNonNull(delegate, "delegate cannot be null");
		Objects.requireNonNull(getter, "getter cannot be null");
		Objects.requireNonNull(setter, "setter cannot be null");

		return new Value() {
			@Override
			public String name() {
				return delegate.name();
			}

			@SuppressWarnings("unchecked")
			@Override
			public R get() {
				return getter.apply((T) delegate.get());
			}

			@SuppressWarnings("unchecked")
			@Override
			public void set(Object newValue) {
				delegate.set(setter.apply((R) newValue));
			}

			@SuppressWarnings("unchecked")
			@Override
			public boolean compareAndSet(Object expectedValue, Object newValue) {
				T transformedExpected = setter.apply((R) expectedValue);
				T transformedNew = setter.apply((R) newValue);
				return delegate.compareAndSet(transformedExpected, transformedNew);
			}

			@SuppressWarnings("unchecked")
			@Override
			public R getAndSet(Object newValue) {
				T transformedNew = setter.apply((R) newValue);
				T oldValue = (T) delegate.getAndSet(transformedNew);
				return getter.apply(oldValue);
			}
		};
	}

	/**
	 * Creates a value that provides access to a field or property through method
	 * handle reflection. This is more efficient than traditional reflection for
	 * repeated access.
	 *
	 * @param name   the name of the value
	 * @param domain the domain this value belongs to
	 * @param target the target object containing the getter
	 * @param getter the getter method to access
	 * @return a reflection-based Value instance
	 * @throws IllegalAccessException if the method is not accessible
	 */
	public static Value fromGetter(String name, Domain domain, Object target, Method getter)
			throws IllegalAccessException {
		Objects.requireNonNull(getter, "getter cannot be null");
		MethodHandle handle = MethodHandles.lookup().unreflect(getter);
		return new MethodHandleValue(name, domain, target, handle);
	}

	/**
	 * Creates a value that provides read/write access to a property through method 
	 * handle reflection.
	 *
	 * @param name   the name of the value
	 * @param domain the domain this value belongs to
	 * @param target the target object containing the accessors
	 * @param getter the getter method
	 * @param setter the setter method
	 * @return a reflection-based Value instance
	 * @throws IllegalAccessException if either method is not accessible
	 */
	public static Value fromAccessors(String name, Domain domain, Object target, Method getter, Method setter)
			throws IllegalAccessException {
		Objects.requireNonNull(getter, "getter cannot be null");
		Objects.requireNonNull(setter, "setter cannot be null");

		MethodHandle getterHandle = MethodHandles.lookup().unreflect(getter);
		MethodHandle setterHandle = MethodHandles.lookup().unreflect(setter);
		return new MethodHandleValue(name, domain, target, getterHandle, setterHandle);
	}

	/**
	 * Creates a value that provides direct access to a field through VarHandle
	 * reflection. This provides atomic access operations directly on the field.
	 *
	 * @param name   the name of the value
	 * @param domain the domain this value belongs to
	 * @param target the target object containing the field
	 * @param field  the field to access
	 * @return a reflection-based Value instance
	 * @throws IllegalAccessException if the field is not accessible
	 */
	public static Value fromField(String name, Domain domain, Object target, Field field)
			throws IllegalAccessException {
		Objects.requireNonNull(field, "field cannot be null");
		VarHandle handle = MethodHandles.lookup().unreflectVarHandle(field);
		return new VarHandleValue(name, domain, target, handle);
	}

	/**
	 * Creates a value that caches its computed result until explicitly invalidated.
	 * This is useful for expensive computations that don't need to be performed on
	 * every access.
	 * 
	 * <p>
	 * The cached value provides:
	 * </p>
	 * <ul>
	 * <li>Lazy initialization</li>
	 * <li>Thread-safe access</li>
	 * <li>Manual invalidation control</li>
	 * <li>Automatic cache population</li>
	 * </ul>
	 *
	 * @param name     the name of the cached value
	 * @param domain   the domain this value belongs to
	 * @param supplier the function that computes the value
	 * @return a cached Value instance
	 * @throws IllegalArgumentException if any parameter is null
	 */
	public static Value cached(String name, Domain domain, Supplier<?> supplier) {
		Objects.requireNonNull(supplier, "supplier cannot be null");
		return new PojoValue(name, domain, null) {
			private volatile boolean invalid = true;
			private volatile Object cachedValue;

			@Override
			public Object get() {
				if (invalid) {
					getLock().writeLock().lock();
					try {
						if (invalid) {
							cachedValue = supplier.get();
							invalid = false;
						}
					} finally {
						getLock().writeLock().unlock();
					}
				}
				getLock().readLock().lock();
				try {
					return cachedValue;
				} finally {
					getLock().readLock().unlock();
				}
			}

			@Override
			public void set(Object newValue) {
				getLock().writeLock().lock();
				try {
					cachedValue = newValue;
					invalid = false;
				} finally {
					getLock().writeLock().unlock();
				}
			}

			@Override
			public void invalidate() {
				getLock().writeLock().lock();
				try {
					invalid = true;
				} finally {
					getLock().writeLock().unlock();
				}
			}
		};
	}

	/**
	 * Creates a value that enforces type safety on all operations. This wraps an
	 * existing value and ensures that all values read from or written to it are of
	 * the specified type.
	 * 
	 * <p>
	 * Type checking is performed on:
	 * </p>
	 * <ul>
	 * <li>Get operations (result type)</li>
	 * <li>Set operations (new value type)</li>
	 * <li>Compare-and-set operations (both values)</li>
	 * <li>Get-and-set operations (new value type)</li>
	 * </ul>
	 *
	 * @param <T>      the type to enforce
	 * @param delegate the value to wrap
	 * @param type     the class representing the enforced type
	 * @return a type-safe Value instance
	 * @throws IllegalArgumentException if delegate or type is null
	 */
	public static <T> Value typed(Value delegate, Class<T> type) {
		return new Value() {
			@Override
			public String name() {
				return delegate.name();
			}

			@SuppressWarnings("unchecked")
			@Override
			public T get() {
				Object value = delegate.get();
				if (value == null || type.isInstance(value)) {
					return (T) value;
				}
				throw new ClassCastException(
						String.format("Value of type %s cannot be cast to %s",
								value.getClass().getName(), type.getName()));
			}

			@Override
			public void set(Object newValue) {
				if (newValue != null && !type.isInstance(newValue)) {
					throw new ClassCastException(
							String.format("Value of type %s cannot be cast to %s",
									newValue.getClass().getName(), type.getName()));
				}
				delegate.set(newValue);
			}

			@Override
			public boolean compareAndSet(Object expectedValue, Object newValue) {
				if (expectedValue != null && !type.isInstance(expectedValue) ||
						newValue != null && !type.isInstance(newValue)) {
					throw new ClassCastException("Values must be of type " + type.getName());
				}
				return delegate.compareAndSet(expectedValue, newValue);
			}

			@Override
			public Object getAndSet(Object newValue) {
				if (newValue != null && !type.isInstance(newValue)) {
					throw new ClassCastException("New value must be of type " + type.getName());
				}
				return delegate.getAndSet(newValue);
			}
		};
	}
}