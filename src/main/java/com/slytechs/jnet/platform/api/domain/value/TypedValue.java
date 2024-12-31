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

/**
 * A type-safe implementation of Value that enforces value type constraints.
 *
 * @param <T> the type of value this implementation handles
 */
public class TypedValue<T> implements Value {

    private final Value delegate;
    private final Class<T> type;

    /**
     * Constructs a new TypedValue with the specified name and type.
     *
     * @param name the name of this value
     * @param type the class representing the type constraint
     * @throws IllegalArgumentException if name or type is null
     */
    public TypedValue(String name, Class<T> type) {
        this(name, type, null);
    }

    /**
     * Constructs a new TypedValue with the specified name, type, and initial value.
     *
     * @param name         the name of this value
     * @param type         the class representing the type constraint
     * @param initialValue the initial value to store
     * @throws IllegalArgumentException if name or type is null
     * @throws ClassCastException if initialValue is not of the specified type
     */
    public TypedValue(String name, Class<T> type, T initialValue) {
        if (type == null)
            throw new IllegalArgumentException("type cannot be null");
            
        this.delegate = new ValueBase(name, initialValue);
        this.type = type;
        
        // Validate initial value if provided
        if (initialValue != null && !type.isInstance(initialValue)) {
            throw new ClassCastException("Initial value is not of type " + type.getName());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get() {
        Object value = delegate.get();
        if (value != null && !type.isInstance(value)) {
            throw new ClassCastException("Stored value is not of type " + type.getName());
        }
        return (T) value;
    }

    @Override
    public void set(Object newValue) {
        if (newValue != null && !type.isInstance(newValue)) {
            throw new ClassCastException("New value is not of type " + type.getName());
        }
        delegate.set(newValue);
    }

    @Override
    public boolean compareAndSet(Object expectedValue, Object newValue) {
        if (expectedValue != null && !type.isInstance(expectedValue)) {
            throw new ClassCastException("Expected value is not of type " + type.getName());
        }
        if (newValue != null && !type.isInstance(newValue)) {
            throw new ClassCastException("New value is not of type " + type.getName());
        }
        return delegate.compareAndSet(expectedValue, newValue);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getAndSet(Object newValue) {
        if (newValue != null && !type.isInstance(newValue)) {
            throw new ClassCastException("New value is not of type " + type.getName());
        }
        Object oldValue = delegate.getAndSet(newValue);
        if (oldValue != null && !type.isInstance(oldValue)) {
            throw new ClassCastException("Stored value is not of type " + type.getName());
        }
        return (T) oldValue;
    }

    @Override
    public String name() {
        return delegate.name();
    }

    /**
     * Gets the type constraint for this TypedValue.
     *
     * @return the class representing the type constraint
     */
    public Class<T> getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("TypedValue[name=%s, type=%s, value=%s]", 
                name(), 
                type.getSimpleName(), 
                get());
    }
}