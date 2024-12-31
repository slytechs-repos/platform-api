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
 * A Value implementation that requires non-null values.
 */
public class NonNullValue implements Value {

    private final Value delegate;

    /**
     * Constructs a new NonNullValue with the specified name and initial value.
     *
     * @param name         the name of this value
     * @param initialValue the initial value to store
     * @throws IllegalArgumentException if name or initialValue is null
     */
    public NonNullValue(String name, Object initialValue) {
        if (initialValue == null)
            throw new IllegalArgumentException("initialValue cannot be null");
            
        this.delegate = new ValueBase(name, initialValue);
    }

    @Override
    public Object get() {
        Object value = delegate.get();
        if (value == null) {
            throw new IllegalStateException("Value cannot be null");
        }
        return value;
    }

    @Override
    public void set(Object newValue) {
        if (newValue == null) {
            throw new IllegalArgumentException("New value cannot be null");
        }
        delegate.set(newValue);
    }

    @Override
    public boolean compareAndSet(Object expectedValue, Object newValue) {
        if (newValue == null) {
            throw new IllegalArgumentException("New value cannot be null");
        }
        return delegate.compareAndSet(expectedValue, newValue);
    }

    @Override
    public Object getAndSet(Object newValue) {
        if (newValue == null) {
            throw new IllegalArgumentException("New value cannot be null");
        }
        Object oldValue = delegate.getAndSet(newValue);
        if (oldValue == null) {
            throw new IllegalStateException("Previous value was null");
        }
        return oldValue;
    }

    @Override
    public String name() {
        return delegate.name();
    }
}