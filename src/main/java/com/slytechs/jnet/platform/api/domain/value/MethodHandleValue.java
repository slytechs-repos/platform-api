package com.slytechs.jnet.platform.api.domain.value;

import java.lang.invoke.MethodHandle;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;

import com.slytechs.jnet.platform.api.domain.Domain;
import com.slytechs.jnet.platform.api.domain.impl.DomainLockAccessor;

public class MethodHandleValue implements Value {

	private final String name;
	private final Domain domain;
	private final MethodHandle getter;
	private final MethodHandle setter;
	private final Object target;

	public MethodHandleValue(String name, Domain domain, Object target, MethodHandle getter) {
		this.name = Objects.requireNonNull(name, "name cannot be null");
		this.domain = Objects.requireNonNull(domain, "domain cannot be null");
		this.target = target;
		this.getter = Objects.requireNonNull(getter, "getter cannot be null");
		this.setter = null;
	}

	public MethodHandleValue(String name, Domain domain, Object target, MethodHandle getter, MethodHandle setter) {
		this.name = Objects.requireNonNull(name, "name cannot be null");
		this.domain = Objects.requireNonNull(domain, "domain cannot be null");
		this.target = target;
		this.getter = Objects.requireNonNull(getter, "getter cannot be null");
		this.setter = setter;
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
			return getter.invoke(target);
		} catch (Throwable e) {
			throw new RuntimeException("Error invoking getter", e);
		} finally {
			getLock().readLock().unlock();
		}
	}

	@Override
	public void set(Object newValue) {
		if (setter == null)
			throw new UnsupportedOperationException("This value is read-only");

		getLock().writeLock().lock();
		try {
			setter.invoke(target, newValue);
		} catch (Throwable e) {
			throw new RuntimeException("Error invoking setter", e);
		} finally {
			getLock().writeLock().unlock();
		}
	}

	@Override
	public boolean compareAndSet(Object expectedValue, Object newValue) {
		if (setter == null)
			throw new UnsupportedOperationException("This value is read-only");

		getLock().writeLock().lock();
		try {
			Object currentValue = getter.invoke(target);
			if (Objects.equals(currentValue, expectedValue)) {
				setter.invoke(target, newValue);
				return true;
			}
			return false;
		} catch (Throwable e) {
			throw new RuntimeException("Error during compareAndSet operation", e);
		} finally {
			getLock().writeLock().unlock();
		}
	}

	@Override
	public Object getAndSet(Object newValue) {
		if (setter == null)
			throw new UnsupportedOperationException("This value is read-only");

		getLock().writeLock().lock();
		try {
			Object oldValue = getter.invoke(target);
			setter.invoke(target, newValue);
			return oldValue;
		} catch (Throwable e) {
			throw new RuntimeException("Error during getAndSet operation", e);
		} finally {
			getLock().writeLock().unlock();
		}
	}
}