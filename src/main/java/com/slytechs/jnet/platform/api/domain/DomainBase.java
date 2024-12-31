package com.slytechs.jnet.platform.api.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;

import com.slytechs.jnet.platform.api.domain.impl.DomainLockAccessor;

/**
 * Base implementation for extensible domains.
 */
public abstract class DomainBase implements Domain, DomainLockAccessor {

	private final String name;
	private final Domain parent;
	private final Map<String, Domain> domains;
	private final Map<String, DomainFolder> folders;

	protected DomainBase(String name, Domain parent) {
		this.name = Objects.requireNonNull(name, "name cannot be null");
		this.parent = parent;
		this.domains = new LinkedHashMap<>();
		this.folders = new LinkedHashMap<>();
	}

	protected ReadWriteLock getLock() {
		return ((DomainLockAccessor) rootDomain()).getDomainLock();
	}

	@Override
	public ReadWriteLock getDomainLock() {
		return getLock();
	}

	@Override
	public <T extends Domain> T addDomain(T newDomain) {
		Objects.requireNonNull(newDomain, "newDomain cannot be null");
		getLock().writeLock().lock();
		try {
			domains.put(newDomain.name(), newDomain);
			return newDomain;
		} finally {
			getLock().writeLock().unlock();
		}
	}

	@Override
	public <T extends DomainFolder> T addFolder(T newFolder) {
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
	public boolean containsDomain(DomainPath path) {
		if (path == null || path.isRoot())
			return false;

		getLock().readLock().lock();
		try {
			String[] elements = path.elements();
			Domain current = this;

			for (String element : elements) {
				if (current == null)
					return false;

				current = current.listDomains().stream()
						.filter(d -> d.name().equals(element))
						.findFirst()
						.orElse(null);
			}

			return current != null;
		} finally {
			getLock().readLock().unlock();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends DomainFolder> T getFolder(DomainPath path) {
		if (path == null || path.isRoot())
			return null;

		getLock().readLock().lock();
		try {
			String[] elements = path.elements();
			if (elements.length == 1)
				return (T) folders.get(elements[0]);

			Domain current = this;
			for (int i = 0; i < elements.length - 1; i++) {
				final String element = elements[i];
				current = current.listDomains().stream()
						.filter(d -> d.name().equals(element))
						.findFirst()
						.orElse(null);

				if (current == null)
					return null;
			}

			return current.getFolder(DomainPath.of(elements[elements.length - 1]));
		} finally {
			getLock().readLock().unlock();
		}
	}

	@Override
	public List<? extends Domain> listDomains() {
		getLock().readLock().lock();
		try {
			return Collections.unmodifiableList(new ArrayList<>(domains.values()));
		} finally {
			getLock().readLock().unlock();
		}
	}

	@Override
	public List<? extends DomainFolder> listFolders(DomainPath path) {
		getLock().readLock().lock();
		try {
			if (path == null || path.isRoot())
				return Collections.unmodifiableList(new ArrayList<>(folders.values()));

			Domain target = this;
			String[] elements = path.elements();

			for (String element : elements) {
				target = target.listDomains().stream()
						.filter(d -> d.name().equals(element))
						.findFirst()
						.orElse(null);

				if (target == null)
					return Collections.emptyList();
			}

			return target.listFolders(DomainPath.of(""));
		} finally {
			getLock().readLock().unlock();
		}
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public Domain parentDomain() {
		return parent;
	}

	@Override
	public Domain removeDomain(String name) {
		getLock().writeLock().lock();
		try {
			return domains.remove(name);
		} finally {
			getLock().writeLock().unlock();
		}
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
}