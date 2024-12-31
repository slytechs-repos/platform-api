package com.slytechs.jnet.platform.api.domain.impl;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Manages domain-wide read-write locks. Each domain instance gets its own lock
 * which is shared by all its folders and values.
 */
public final class DomainLock {
	private final ReadWriteLock lock;

	public DomainLock() {
		this.lock = new ReentrantReadWriteLock();
	}

	public ReadWriteLock getLock() {
		return lock;
	}
}