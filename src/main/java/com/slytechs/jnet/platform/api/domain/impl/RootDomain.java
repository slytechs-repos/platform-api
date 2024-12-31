package com.slytechs.jnet.platform.api.domain.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import com.slytechs.jnet.platform.api.domain.Domain;
import com.slytechs.jnet.platform.api.domain.DomainFolder;
import com.slytechs.jnet.platform.api.domain.DomainPath;
import com.slytechs.jnet.platform.api.util.Registration;

public final class RootDomain implements Domain, DomainLockAccessor {

	private final ReadWriteLock domainLock = new ReentrantReadWriteLock();
	private final Map<String, Domain> domains;
	private final Map<String, DomainFolder> folders;

	public RootDomain() {
		this.domains = new LinkedHashMap<>();
		this.folders = new LinkedHashMap<>();
	}

	@Override
	public ReadWriteLock getDomainLock() {
		return domainLock;
	}

	@Override
	public <T extends Domain> T addDomain(T newDomain) {
		Objects.requireNonNull(newDomain, "newDomain cannot be null");
		domainLock.writeLock().lock();
		try {
			domains.put(newDomain.name(), newDomain);
			return newDomain;
		} finally {
			domainLock.writeLock().unlock();
		}
	}

	@Override
	public <T extends Domain> T addDomain(T newDomain, Consumer<Registration> registration) {
		Objects.requireNonNull(registration, "registration cannot be null");
		domainLock.writeLock().lock();
		try {
			registration.accept(() -> removeDomain(newDomain.name()));
			return addDomain(newDomain);
		} finally {
			domainLock.writeLock().unlock();
		}
	}

	@Override
	public <T extends DomainFolder> T addFolder(T newFolder) {
		Objects.requireNonNull(newFolder, "newFolder cannot be null");
		domainLock.writeLock().lock();
		try {
			folders.put(newFolder.name(), newFolder);
			return newFolder;
		} finally {
			domainLock.writeLock().unlock();
		}
	}

	@Override
	public <T extends DomainFolder> T addFolder(T newFolder, Consumer<Registration> registration) {
		Objects.requireNonNull(registration, "registration cannot be null");
		domainLock.writeLock().lock();
		try {
			registration.accept(() -> removeFolder(newFolder.name()));
			return addFolder(newFolder);
		} finally {
			domainLock.writeLock().unlock();
		}
	}

	@Override
	public boolean containsDomain(DomainPath path) {
		if (path == null || path.isRoot())
			return false;

		domainLock.readLock().lock();
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
			domainLock.readLock().unlock();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends DomainFolder> T getFolder(DomainPath path) {
		if (path == null || path.isRoot())
			return null;

		domainLock.readLock().lock();
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
			domainLock.readLock().unlock();
		}
	}

	@Override
	public List<? extends Domain> listDomains() {
		domainLock.readLock().lock();
		try {
			return Collections.unmodifiableList(new ArrayList<>(domains.values()));
		} finally {
			domainLock.readLock().unlock();
		}
	}

	@Override
	public List<? extends DomainFolder> listFolders(DomainPath path) {
		domainLock.readLock().lock();
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
			domainLock.readLock().unlock();
		}
	}

	@Override
	public String name() {
		return ""; // Root domain has empty name
	}

	@Override
	public Domain parentDomain() {
		return null; // Root domain has no parent
	}

	@Override
	public Domain removeDomain(String name) {
		domainLock.writeLock().lock();
		try {
			return domains.remove(name);
		} finally {
			domainLock.writeLock().unlock();
		}
	}

	@Override
	public DomainFolder removeFolder(String name) {
		domainLock.writeLock().lock();
		try {
			return folders.remove(name);
		} finally {
			domainLock.writeLock().unlock();
		}
	}

	@Override
	public Domain rootDomain() {
		return Domain.root();
	}

	@Override
	public String toString() {
		domainLock.readLock().lock();
		try {
			return String.format("RootDomain[domains=%d, folders=%d]",
					domains.size(), folders.size());
		} finally {
			domainLock.readLock().unlock();
		}
	}
}