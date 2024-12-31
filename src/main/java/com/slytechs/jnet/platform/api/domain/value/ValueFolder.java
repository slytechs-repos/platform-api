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
package com.slytechs.jnet.platform.api.domain.value;

import java.util.List;
import java.util.function.Consumer;

import com.slytechs.jnet.platform.api.domain.DomainFolder;
import com.slytechs.jnet.platform.api.util.Registration;

/**
 * A specialized folder type within a domain hierarchy that manages and
 * organizes Value objects. ValueFolder extends the base DomainFolder interface
 * to provide value-specific operations while maintaining the hierarchical
 * structure of the domain system.
 * 
 * <p>
 * ValueFolders serve as containers for Value instances, providing methods for:
 * </p>
 * <ul>
 * <li>Adding and removing values</li>
 * <li>Querying value existence</li>
 * <li>Listing contained values</li>
 * <li>Retrieving specific values by name</li>
 * </ul>
 * 
 * <p>
 * The folder provides thread-safe operations through the domain's locking
 * mechanism. All operations that modify the folder's content are synchronized
 * using the domain's lock.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * ValueFolder headers = new ValueFolderBase("headers", domain, parent);
 * 
 * // Add a value with cleanup registration
 * headers.addValue(new PojoValue("type", 0x0800), reg -> {
 * 	System.out.println("Value cleanup registered");
 * });
 * 
 * // Retrieve a value
 * Value type = headers.value("type");
 * 
 * // List all values
 * headers.listValues().forEach(v -> System.out.println(v.name() + " = " + v.get()));
 * }</pre>
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 * @see Value
 * @see DomainFolder
 */
public interface ValueFolder extends DomainFolder {

	/**
	 * Adds a new value to this folder. The operation is atomic and thread-safe,
	 * using the domain's locking mechanism.
	 *
	 * @param <T>      the type of value being added
	 * @param newValue the value to add to this folder
	 * @return the added value
	 * @throws IllegalArgumentException if newValue is null
	 * @throws IllegalStateException    if a value with the same name already exists
	 */
	<T extends Value> T addValue(T newValue);

	/**
	 * Adds a new value to this folder with a registration for cleanup. This method
	 * provides a way to register cleanup operations that will be executed when the
	 * value is removed.
	 * 
	 * <p>
	 * The registration callback is invoked immediately after the value is added,
	 * providing a Registration object that can be used to clean up resources when
	 * the value is removed.
	 * </p>
	 *
	 * @param <T>          the type of value being added
	 * @param newValue     the value to add to this folder
	 * @param registration a consumer that receives the cleanup registration
	 * @return the added value
	 * @throws IllegalArgumentException if newValue or registration is null
	 * @throws IllegalStateException    if a value with the same name already exists
	 */
	default <T extends Value> T addValue(T newValue, Consumer<Registration> registration) {
		registration.accept(() -> removeValue(newValue.name()));
		return addValue(newValue);
	}

	/**
	 * Checks if this folder contains a value with the specified name.
	 *
	 * @param name the name of the value to check for
	 * @return true if a value with the specified name exists in this folder, false
	 *         otherwise
	 * @throws IllegalArgumentException if name is null
	 */
	boolean containsValue(String name);

	/**
	 * Returns an unmodifiable list of all values in this folder. The returned list
	 * is a snapshot of the folder's contents at the time this method is called.
	 *
	 * @return an unmodifiable list of all values in this folder
	 */
	List<? extends Value> listValues();

	/**
	 * Removes and returns the value with the specified name from this folder. If no
	 * value exists with the specified name, returns null.
	 *
	 * @param name the name of the value to remove
	 * @return the removed value, or null if no value was found with the specified
	 *         name
	 * @throws IllegalArgumentException if name is null
	 */
	Value removeValue(String name);

	/**
	 * Retrieves a value by name, casting it to the expected type. If no value
	 * exists with the specified name, returns null.
	 *
	 * @param <T>  the expected type of the value
	 * @param name the name of the value to retrieve
	 * @return the value cast to type T, or null if not found
	 * @throws IllegalArgumentException if name is null
	 * @throws ClassCastException       if the value exists but cannot be cast to
	 *                                  type T
	 */
	<T extends Value> T value(String name);
}