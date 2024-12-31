package com.slytechs.jnet.platform.api.domain.value;

import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;

import com.slytechs.jnet.platform.api.domain.Domain;
import com.slytechs.jnet.platform.api.domain.impl.DomainLockAccessor;

public class PojoValue implements Value {

	private final String name;
	private final Domain domain;
	private Object value;

	public PojoValue(String name, Domain domain, Object initialValue) {
		this.name = Objects.requireNonNull(name, "name cannot be null");
		this.domain = Objects.requireNonNull(domain, "domain cannot be null");
		this.value = initialValue;
	}

	protected ReadWriteLock getLock() {
		return ((DomainLockAccessor) domain).getDomainLock();
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public Object get() {
		getLock().readLock().lock();
		try {
			return value;
		} finally {
			getLock().readLock().unlock();
		}
	}

	@Override
	public void set(Object newValue) {
		getLock().writeLock().lock();
		try {
			value = newValue;
		} finally {
			getLock().writeLock().unlock();
		}
	}

	@Override
	public boolean compareAndSet(Object expectedValue, Object newValue) {
		getLock().writeLock().lock();
		try {
			if (Objects.equals(value, expectedValue)) {
				value = newValue;
				return true;
			}
			return false;
		} finally {
			getLock().writeLock().unlock();
		}
	}

	@Override
	public Object getAndSet(Object newValue) {
		getLock().writeLock().lock();
		try {
			Object oldValue = value;
			value = newValue;
			return oldValue;
		} finally {
			getLock().writeLock().unlock();
		}
	}

	@Override
	public String toString() {
		getLock().readLock().lock();
		try {
			return String.format("Value[name=%s, value=%s]", name, value);
		} finally {
			getLock().readLock().unlock();
		}
	}
}