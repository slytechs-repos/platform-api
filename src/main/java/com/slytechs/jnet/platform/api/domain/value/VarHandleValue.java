package com.slytechs.jnet.platform.api.domain.value;

import java.lang.invoke.VarHandle;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;

import com.slytechs.jnet.platform.api.domain.Domain;
import com.slytechs.jnet.platform.api.domain.impl.DomainLockAccessor;
import com.slytechs.jnet.platform.api.domain.value.Value;

public class VarHandleValue implements Value {

    private final String name;
    private final Domain domain;
    private final VarHandle handle;
    private final Object target;

    public VarHandleValue(String name, Domain domain, Object target, VarHandle handle) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.domain = Objects.requireNonNull(domain, "domain cannot be null");
        this.handle = Objects.requireNonNull(handle, "handle cannot be null");
        this.target = target;
    }

    public VarHandleValue(String name, Domain domain, VarHandle handle) {
        this(name, domain, null, handle);
    }

    protected ReadWriteLock getLock() {
        return ((DomainLockAccessor)domain).getDomainLock();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Object get() {
        getLock().readLock().lock();
        try {
            return target == null ? handle.get() : handle.get(target);
        } finally {
            getLock().readLock().unlock();
        }
    }

    @Override
    public void set(Object newValue) {
        getLock().writeLock().lock();
        try {
            if (target == null)
                handle.set(newValue);
            else
                handle.set(target, newValue);
        } finally {
            getLock().writeLock().unlock();
        }
    }

    @Override
    public boolean compareAndSet(Object expectedValue, Object newValue) {
        getLock().writeLock().lock();
        try {
            return target == null ?
                    handle.compareAndSet(expectedValue, newValue) :
                    handle.compareAndSet(target, expectedValue, newValue);
        } finally {
            getLock().writeLock().unlock();
        }
    }

    @Override
    public Object getAndSet(Object newValue) {
        getLock().writeLock().lock();
        try {
            return target == null ?
                    handle.getAndSet(newValue) :
                    handle.getAndSet(target, newValue);
        } finally {
            getLock().writeLock().unlock();
        }
    }
}