package com.slytechs.jnet.platform.api.domain.value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Consumer;

import com.slytechs.jnet.platform.api.domain.Domain;
import com.slytechs.jnet.platform.api.domain.DomainFolder;
import com.slytechs.jnet.platform.api.domain.impl.DomainLockAccessor;
import com.slytechs.jnet.platform.api.util.Registration;

public class ValueFolderBase implements ValueFolder {

	private final String name;
	private final Domain domain;
	private final DomainFolder parent;
	private final Map<String, Value> values;
	private final Map<String, DomainFolder> folders;

	public ValueFolderBase(String name, Domain domain, DomainFolder parent) {
		this.name = Objects.requireNonNull(name, "name cannot be null");
		this.domain = Objects.requireNonNull(domain, "domain cannot be null");
		this.parent = parent;
		this.values = new LinkedHashMap<>();
		this.folders = new LinkedHashMap<>();
	}

	protected ReadWriteLock getLock() {
		return ((DomainLockAccessor) domain).getDomainLock();
	}

	@Override
	public <T extends Value> T addValue(T newValue) {
		Objects.requireNonNull(newValue, "newValue cannot be null");
		getLock().writeLock().lock();
		try {
			values.put(newValue.name(), newValue);
			return newValue;
		} finally {
			getLock().writeLock().unlock();
		}
	}

	@Override
	public <T extends Value> T addValue(T newValue, Consumer<Registration> registration) {
		Objects.requireNonNull(registration, "registration cannot be null");
		getLock().writeLock().lock();
		try {
			registration.accept(() -> removeValue(newValue.name()));
			return addValue(newValue);
		} finally {
			getLock().writeLock().unlock();
		}
	}

	@Override
	public boolean containsValue(String name) {
		getLock().readLock().lock();
		try {
			return values.containsKey(name);
		} finally {
			getLock().readLock().unlock();
		}
	}

	@Override
	public List<? extends Value> listValues() {
		getLock().readLock().lock();
		try {
			return Collections.unmodifiableList(new ArrayList<>(values.values()));
		} finally {
			getLock().readLock().unlock();
		}
	}

	@Override
	public Value removeValue(String name) {
		getLock().writeLock().lock();
		try {
			return values.remove(name);
		} finally {
			getLock().writeLock().unlock();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Value> T value(String name) {
		getLock().readLock().lock();
		try {
			return (T) values.get(name);
		} finally {
			getLock().readLock().unlock();
		}
	}

	@Override
	public DomainFolder addFolder(DomainFolder newFolder) {
		Objects.requireNonNull(newFolder, "newFolder cannot be null");
		getLock().writeLock().lock();
		try {
			folders.put(newFolder.name(), newFolder);
			return newFolder;
		} finally {
			getLock().writeLock().unlock();
		}
	}

	@Override
	public DomainFolder addFolder(DomainFolder newFolder, Consumer<Registration> registration) {
		Objects.requireNonNull(registration, "registration cannot be null");
		getLock().writeLock().lock();
		try {
			registration.accept(() -> removeFolder(newFolder.name()));
			return addFolder(newFolder);
		} finally {
			getLock().writeLock().unlock();
		}
	}

	@Override
	public boolean containsFoder(String name) { // Note: method name matches interface typo
		getLock().readLock().lock();
		try {
			return folders.containsKey(name);
		} finally {
			getLock().readLock().unlock();
		}
	}

	@Override
	public Domain domain() {
		return domain;
	}

	@Override
	public List<DomainFolder> folders() {
		getLock().readLock().lock();
		try {
			return Collections.unmodifiableList(new ArrayList<>(folders.values()));
		} finally {
			getLock().readLock().unlock();
		}
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public DomainFolder parent() {
		return parent;
	}

	@Override
	public DomainFolder removeFolder(String name) {
		getLock().writeLock().lock();
		try {
			return folders.remove(name);
		} finally {
			getLock().writeLock().unlock();
		}
	}

	@Override
	public String toString() {
		getLock().readLock().lock();
		try {
			return String.format("ValueFolder[name=%s, values=%d, folders=%d]",
					name, values.size(), folders.size());
		} finally {
			getLock().readLock().unlock();
		}
	}
}