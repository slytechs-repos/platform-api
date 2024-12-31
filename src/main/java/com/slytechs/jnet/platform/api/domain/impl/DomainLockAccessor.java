package com.slytechs.jnet.platform.api.domain.impl;

import java.util.concurrent.locks.ReadWriteLock;

/**
 * Extension to Domain interface to support lock access.
 */
public interface DomainLockAccessor {
    /**
     * Gets the read-write lock associated with this domain.
     * @return the domain's lock
     */
    ReadWriteLock getDomainLock();
}