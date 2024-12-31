package com.slytechs.jnet.platform.api.domain.value;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Base implementation of the Value interface that provides atomic operations on
 * values using AtomicReference.
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public class ValueBase implements Value {

	private final String name;
	private final AtomicReference<Object> valueRef;

	/**
	 * Constructs a new ValueBase with the specified name and initial value.
	 *
	 * @param name         the name of this value
	 * @param initialValue the initial value to store
	 * @throws IllegalArgumentException if name is null
	 */
	public ValueBase(String name, Object initialValue) {
		if (name == null)
			throw new IllegalArgumentException("name cannot be null");

		this.name = name;
		this.valueRef = new AtomicReference<>(initialValue);
	}

	/**
	 * Constructs a new ValueBase with the specified name and null initial value.
	 *
	 * @param name the name of this value
	 * @throws IllegalArgumentException if name is null
	 */
	public ValueBase(String name) {
		this(name, null);
	}

	@Override
	public Object get() {
		return valueRef.get();
	}

	@Override
	public void set(Object newValue) {
		valueRef.set(newValue);
	}

	@Override
	public boolean compareAndSet(Object expectedValue, Object newValue) {
		return valueRef.compareAndSet(expectedValue, newValue);
	}

	@Override
	public Object getAndSet(Object newValue) {
		return valueRef.getAndSet(newValue);
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String toString() {
		Object value = get();
		return String.format("Value[name=%s, value=%s]",
				name,
				value != null ? value.toString() : "null");
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Value))
			return false;

		Value other = (Value) obj;
		Object thisValue = get();
		Object otherValue = other.get();

		return name.equals(other.name()) &&
				(thisValue == null ? otherValue == null : thisValue.equals(otherValue));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + name.hashCode();
		Object value = get();
		result = prime * result + (value == null ? 0 : value.hashCode());
		return result;
	}
}