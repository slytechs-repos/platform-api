/*
 * Sly Technologies Free License
 * 
 * Copyright 2024 Sly Technologies Inc.
 *
 * Licensed under the Sly Technologies Free License (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.slytechs.com/free-license-text
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.slytechs.jnet.platform.api.domain;

import java.util.List;
import java.util.function.Consumer;

import com.slytechs.jnet.platform.api.domain.impl.RootDomain;
import com.slytechs.jnet.platform.api.incubator.StableValue;
import com.slytechs.jnet.platform.api.util.Named;
import com.slytechs.jnet.platform.api.util.Registration;

/**
 * Represents a hierarchical container that organizes and manages domains,
 * folders, and their associated resources. Domains form a tree structure where
 * each domain can contain child domains and folders, providing a hierarchical
 * namespace for organizing related functionality.
 * 
 * <p>
 * The domain hierarchy starts with a singleton root domain, accessible via
 * {@link #root()}. All operations within the domain hierarchy are thread-safe,
 * using a domain-scoped locking mechanism.
 * </p>
 * 
 * <p>
 * Key features of domains include:
 * </p>
 * <ul>
 * <li>Hierarchical organization of resources</li>
 * <li>Path-based resource lookup</li>
 * <li>Resource lifecycle management</li>
 * <li>Thread-safe operations</li>
 * <li>Registration-based cleanup</li>
 * </ul>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * // Get root domain
 * Domain root = Domain.root();
 * 
 * // Create and add a child domain
 * ProtocolDomain ipv4 = new ProtocolDomain("ipv4", root);
 * root.addDomain(ipv4, reg -> {
 * 	System.out.println("IPv4 domain cleanup registered");
 * });
 * 
 * // Add a folder with cleanup
 * ConfigFolder config = new ConfigFolder("config", ipv4);
 * ipv4.addFolder(config, reg -> {
 * 	System.out.println("Config folder cleanup registered");
 * });
 * 
 * // Access resources by path
 * DomainFolder folder = root.getFolder("ipv4/config");
 * }</pre>
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 * @see DomainFolder
 * @see DomainPath
 */
public interface Domain extends Named {

	/** The global root domain, lazily initialized using StableValue. */
	StableValue<Domain> ROOT = StableValue.ofSupplier(RootDomain::new);

	DomainPath EMPTY_DOMAIN_PATH = DomainPath.of("empty");

	/**
	 * Returns the root domain instance, creating it if necessary. This serves as
	 * the entry point to the domain hierarchy.
	 *
	 * @return the root domain instance
	 */
	static Domain root() {
		return ROOT.get();
	}

	/**
	 * Adds a new child domain to this domain. This operation is atomic and
	 * thread-safe.
	 *
	 * @param <T>       the type of domain being added
	 * @param newDomain the domain to add
	 * @return the added domain
	 * @throws IllegalArgumentException if newDomain is null
	 * @throws IllegalStateException    if a domain with the same name already
	 *                                  exists
	 */
	<T extends Domain> T addDomain(T newDomain);

	/**
	 * Adds a new child domain with registration for cleanup. The registration
	 * callback is invoked immediately after the domain is added, providing a way to
	 * register cleanup operations.
	 *
	 * @param <T>          the type of domain being added
	 * @param newDomain    the domain to add
	 * @param registration callback to receive the cleanup registration
	 * @return the added domain
	 * @throws IllegalArgumentException if newDomain or registration is null
	 * @throws IllegalStateException    if a domain with the same name already
	 *                                  exists
	 */
	default <T extends Domain> T addDomain(T newDomain, Consumer<Registration> registration) {
		registration.accept(() -> removeDomain(newDomain.name()));
		return addDomain(newDomain);
	}

	/**
	 * Adds a new folder to this domain. This operation is atomic and thread-safe.
	 *
	 * @param <T>       the type of folder being added
	 * @param newFolder the folder to add
	 * @return the added folder
	 * @throws IllegalArgumentException if newFolder is null
	 * @throws IllegalStateException    if a folder with the same name already
	 *                                  exists
	 */
	<T extends DomainFolder> T addFolder(T newFolder);

	/**
	 * Adds a new folder with registration for cleanup. The registration callback is
	 * invoked immediately after the folder is added.
	 *
	 * @param <T>          the type of folder being added
	 * @param newFolder    the folder to add
	 * @param registration callback to receive the cleanup registration
	 * @return the added folder
	 * @throws IllegalArgumentException if newFolder or registration is null
	 * @throws IllegalStateException    if a folder with the same name already
	 *                                  exists
	 */
	default <T extends DomainFolder> T addFolder(T newFolder, Consumer<Registration> registration) {
		registration.accept(() -> removeFolder(newFolder.name()));
		return addFolder(newFolder);
	}

	/**
	 * Checks if a domain exists at the specified path within this domain's
	 * hierarchy.
	 *
	 * @param path the path to check
	 * @return true if a domain exists at the specified path
	 * @throws IllegalArgumentException if path is null
	 */
	boolean containsDomain(DomainPath path);

	/**
	 * Convenience method that converts a string path to DomainPath and checks for
	 * domain existence.
	 *
	 * @param path the string path to check
	 * @return true if a domain exists at the specified path
	 * @throws IllegalArgumentException if path is null or invalid
	 */
	default boolean containsDomain(String path) {
		return containsDomain(DomainPath.of(path));
	}

	/**
	 * Retrieves a folder at the specified path within this domain's hierarchy.
	 *
	 * @param <T>  the expected type of the folder
	 * @param path the path to the folder
	 * @return the folder, or null if not found
	 * @throws IllegalArgumentException if path is null
	 * @throws ClassCastException       if the folder exists but cannot be cast to
	 *                                  type T
	 */
	<T extends DomainFolder> T getFolder(DomainPath path);

	/**
	 * Convenience method that converts a string path to DomainPath and retrieves a
	 * folder.
	 *
	 * @param <T>  the expected type of the folder
	 * @param path the string path to the folder
	 * @return the folder, or null if not found
	 * @throws IllegalArgumentException if path is null or invalid
	 * @throws ClassCastException       if the folder exists but cannot be cast to
	 *                                  type T
	 */
	default <T extends DomainFolder> T getFolder(String path) {
		return getFolder(DomainPath.of(path));
	}

	/**
	 * Returns a list of all child domains in this domain.
	 *
	 * @return an unmodifiable list of child domains
	 */
	List<? extends Domain> listDomains();

	/**
	 * Lists all folders at the specified path within this domain's hierarchy.
	 *
	 * @param path the path to list folders from
	 * @return an unmodifiable list of folders
	 * @throws IllegalArgumentException if path is null
	 */
	List<? extends DomainFolder> listFolders(DomainPath path);

	/**
	 * Convenience method that converts a string path to DomainPath and lists
	 * folders at that path.
	 *
	 * @param path the string path to list folders from
	 * @return an unmodifiable list of folders
	 * @throws IllegalArgumentException if path is null or invalid
	 */
	default List<? extends DomainFolder> listFolders(String path) {
		return listFolders(DomainPath.of(path));
	}

	/**
	 * Returns the name of this domain.
	 *
	 * @return the domain name
	 */
	@Override
	String name();

	/**
	 * Returns the parent domain of this domain.
	 *
	 * @return the parent domain, or null if this is the root domain
	 */
	Domain parentDomain();

	/**
	 * Removes a child domain by name.
	 *
	 * @param name the name of the domain to remove
	 * @return the removed domain, or null if not found
	 * @throws IllegalArgumentException if name is null
	 */
	Domain removeDomain(String name);

	/**
	 * Removes a folder by name.
	 *
	 * @param name the name of the folder to remove
	 * @return the removed folder, or null if not found
	 * @throws IllegalArgumentException if name is null
	 */
	DomainFolder removeFolder(String name);

	/**
	 * Returns the root domain of the hierarchy. This is equivalent to calling
	 * {@link #root()} but may be overridden by implementations.
	 *
	 * @return the root domain
	 */
	default Domain rootDomain() {
		return ROOT.get();
	}
}