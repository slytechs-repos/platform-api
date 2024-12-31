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

import com.slytechs.jnet.platform.api.util.Named;
import com.slytechs.jnet.platform.api.util.Registration;

/**
 * Represents a hierarchical folder within a domain that can contain other folders.
 * DomainFolder provides organization and structure within domains, allowing for
 * logical grouping of related resources and nested folder hierarchies.
 * 
 * <p>
 * DomainFolders are thread-safe containers that support:
 * </p>
 * <ul>
 * <li>Nested folder hierarchies</li>
 * <li>Parent-child relationships</li>
 * <li>Resource organization</li>
 * <li>Cleanup registration</li>
 * </ul>
 * 
 * <p>
 * A DomainFolder always belongs to a specific domain and may have a parent folder.
 * All operations within a folder use the owning domain's locking mechanism to
 * ensure thread safety.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * <pre>{@code
 * // Create a folder hierarchy
 * DomainFolder config = new DomainFolderBase("config", domain, null);
 * DomainFolder security = new DomainFolderBase("security", domain, config);
 * 
 * // Add with cleanup registration
 * config.addFolder(security, reg -> {
 *     System.out.println("Security folder cleanup registered");
 * });
 * 
 * // Check folder existence
 * if (config.containsFoder("security")) {
 *     System.out.println("Security folder exists");
 * }
 * 
 * // List subfolders
 * config.folders().forEach(folder -> 
 *     System.out.println("Subfolder: " + folder.name()));
 * }</pre>
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 * @see Domain
 */
public interface DomainFolder extends Named {

    /**
     * Adds a new subfolder to this folder. This operation is atomic and thread-safe,
     * using the owning domain's locking mechanism.
     *
     * @param newFolder the folder to add
     * @return the added folder
     * @throws IllegalArgumentException if newFolder is null
     * @throws IllegalStateException if a folder with the same name already exists
     */
    DomainFolder addFolder(DomainFolder newFolder);

    /**
     * Adds a new subfolder with registration for cleanup. The registration callback
     * is invoked immediately after the folder is added, providing a way to register
     * cleanup operations that will be executed when the folder is removed.
     * 
     * <p>
     * This operation is atomic and thread-safe. The registration is guaranteed to
     * be called exactly once, immediately after successful folder addition.
     * </p>
     *
     * @param newFolder    the folder to add
     * @param registration callback to receive the cleanup registration
     * @return the added folder
     * @throws IllegalArgumentException if newFolder or registration is null
     * @throws IllegalStateException if a folder with the same name already exists
     */
    default DomainFolder addFolder(DomainFolder newFolder, Consumer<Registration> registration) {
        registration.accept(() -> removeFolder(newFolder.name()));
        return addFolder(newFolder);
    }

    /**
     * Checks if a subfolder with the specified name exists in this folder.
     * Note: The method name contains a typo (containsFoder) but is maintained
     * for backward compatibility.
     *
     * @param name the name of the folder to check for
     * @return true if a folder with the specified name exists
     * @throws IllegalArgumentException if name is null
     */
    boolean containsFoder(String name);

    /**
     * Returns the domain that owns this folder. Every folder belongs to exactly
     * one domain, which provides the locking mechanism for thread-safe operations.
     *
     * @return the owning domain
     */
    Domain domain();

    /**
     * Returns a list of all subfolders in this folder. The returned list is
     * unmodifiable and represents a snapshot of the folder's contents at the time
     * of the call.
     * 
     * <p>
     * This operation is thread-safe, using the domain's locking mechanism to
     * ensure consistency.
     * </p>
     *
     * @return an unmodifiable list of subfolders
     */
    List<DomainFolder> folders();

    /**
     * Returns the name of this folder. Folder names must be unique within their
     * immediate parent folder.
     *
     * @return the folder name
     */
    @Override
    String name();

    /**
     * Returns the parent folder of this folder. The root folder of a domain has
     * no parent and returns null.
     *
     * @return the parent folder, or null if this is a root folder
     */
    DomainFolder parent();

    /**
     * Removes a subfolder by name. If the folder has a cleanup registration, it
     * will be executed during removal.
     * 
     * <p>
     * This operation is atomic and thread-safe. If the folder doesn't exist,
     * this method returns null without error.
     * </p>
     *
     * @param name the name of the folder to remove
     * @return the removed folder, or null if no folder was found with the
     *         specified name
     * @throws IllegalArgumentException if name is null
     */
    DomainFolder removeFolder(String name);
}