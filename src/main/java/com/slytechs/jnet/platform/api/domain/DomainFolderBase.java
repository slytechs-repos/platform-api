package com.slytechs.jnet.platform.api.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Base implementation of DomainFolder that provides common functionality for
 * folder management within a domain hierarchy.
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public abstract class DomainFolderBase implements DomainFolder {

	private final String name;
	private final Domain ownerDomain;
	private final DomainFolder parent;
	private final Map<String, DomainFolder> subFolders;

	/**
	 * Constructs a new DomainFolderBase with the specified name and domain.
	 *
	 * @param name   the name of this folder
	 * @param domain the domain that owns this folder
	 * @param parent the parent folder, can be null for root folders
	 * @throws IllegalArgumentException if domain is null or name is null
	 */
	protected DomainFolderBase(String name, Domain domain, DomainFolder parent) {
		if (domain == null)
			throw new IllegalArgumentException("domain cannot be null");
		if (name == null)
			throw new IllegalArgumentException("name cannot be null");

		this.name = name;
		this.ownerDomain = domain;
		this.parent = parent;
		this.subFolders = new LinkedHashMap<>();
	}

	@Override
	public DomainFolder addFolder(DomainFolder newFolder) {
		if (newFolder == null)
			throw new IllegalArgumentException("newFolder cannot be null");

		subFolders.put(newFolder.name(), newFolder);
		return newFolder;
	}

	@Override
	public boolean containsFoder(String name) { // Note: method name matches interface typo
		return subFolders.containsKey(name);
	}

	@Override
	public Domain domain() {
		return ownerDomain;
	}

	@Override
	public List<DomainFolder> folders() {
		return Collections.unmodifiableList(new ArrayList<>(subFolders.values()));
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
		return subFolders.remove(name);
	}

	@Override
	public String toString() {
		StringBuilder path = new StringBuilder();
		DomainFolder current = this;

		while (current != null) {
			path.insert(0, current.name());
			path.insert(0, '/');
			current = current.parent();
		}

		return path.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof DomainFolder))
			return false;

		DomainFolder other = (DomainFolder) obj;
		return name.equals(other.name()) &&
				domain().equals(other.domain()) &&
				(parent == null ? other.parent() == null : parent.equals(other.parent()));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + name.hashCode();
		result = prime * result + domain().hashCode();
		result = prime * result + (parent == null ? 0 : parent.hashCode());
		return result;
	}
}