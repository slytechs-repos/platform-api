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

import java.util.Set;

import com.slytechs.jnet.platform.api.domain.Domain;
import com.slytechs.jnet.platform.api.domain.DomainPath;

/**
 * A specialized domain that provides hierarchical organization and management
 * of values. ValueDomain extends the base Domain interface to support
 * value-specific operations while maintaining the standard domain hierarchy
 * capabilities.
 * 
 * <p>
 * ValueDomain serves as a container for both values and value folders, allowing
 * for hierarchical organization of values within a domain tree structure. This
 * organization facilitates:
 * </p>
 * <ul>
 * <li>Hierarchical value management</li>
 * <li>Path-based value lookups</li>
 * <li>Domain-scoped value operations</li>
 * <li>Thread-safe value access</li>
 * </ul>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * ValueDomain protocol = new ValueDomainBase("protocol", parent);
 * 
 * // Check value existence using path
 * if (protocol.containsValue("headers/type")) {
 * 	System.out.println("Header type exists");
 * }
 * 
 * // Or using DomainPath
 * DomainPath path = DomainPath.of("headers/type");
 * if (protocol.containsValue(path)) {
 * 	System.out.println("Header type exists");
 * }
 * }</pre>
 * 
 * <p>
 * The ValueDomain interface extends the base Domain interface, inheriting all
 * its capabilities while adding value-specific operations. All operations are
 * thread-safe, utilizing the domain's locking mechanism.
 * </p>
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 * @see Domain
 * @see Value
 * @see ValueFolder
 */
public interface ValueDomain extends Domain {

	/**
	 * Checks if a value exists at the specified path within this domain's
	 * hierarchy. The path can traverse through multiple levels of domains and
	 * folders.
	 * 
	 * <p>
	 * The path format follows the standard domain path convention:
	 * </p>
	 * <ul>
	 * <li>{@code "folder/value"} - Value in a folder</li>
	 * <li>{@code "subdomain/folder/value"} - Value in a folder in a subdomain</li>
	 * <li>{@code "value"} - Value directly in the domain</li>
	 * </ul>
	 *
	 * @param path the path to the value to check
	 * @return true if a value exists at the specified path, false otherwise
	 * @throws IllegalArgumentException if path is null
	 */
	boolean containsValue(DomainPath path, Object... params);

	/**
	 * Convenience method that converts a string path to a DomainPath and checks for
	 * value existence. This method is equivalent to:
	 * {@code containsValue(DomainPath.of(path))}
	 *
	 * @param path the string path to the value to check
	 * @return true if a value exists at the specified path, false otherwise
	 * @throws IllegalArgumentException if path is null or invalid
	 * @see #containsValue(DomainPath)
	 */
	default boolean containsValue(String path, Object... params) {
		return containsValue(DomainPath.of(path), params);
	}

	/**
	 * Computes the union of two collections or maps specified by their paths.
	 * Supports wildcard and parameterized queries for maps.
	 *
	 * @param path1  the first path to resolve
	 * @param path2  the second path to resolve
	 * @param params optional parameters for parameterized queries
	 * @return a set containing the union of the resolved values
	 */
	Set<?> unionSet(String path1, String path2, Object... params);

	/**
	 * Computes the intersection of two collections or maps specified by their
	 * paths. Supports wildcard and parameterized queries for maps.
	 *
	 * <p>
	 * For maps, the paths may specify operations on keys or values using the syntax
	 * {@code {key=*}} or {@code {value=*}}, respectively.
	 * </p>
	 *
	 * <p>
	 * Example usage:
	 * </p>
	 * 
	 * <pre>
	 * Set<?> result = valueDomain.intersectionSet("domain/set1", "domain/map1{key=*}");
	 * </pre>
	 *
	 * @param path1  the first path to resolve
	 * @param path2  the second path to resolve
	 * @param params optional parameters for parameterized queries
	 * @return a set containing the intersection of the resolved values
	 * @throws IllegalArgumentException if the paths are invalid or incompatible
	 */
	Set<?> intersectionSet(String path1, String path2, Object... params);

	/**
	 * Computes the difference of two collections or maps specified by their paths.
	 * Supports wildcard and parameterized queries for maps.
	 *
	 * <p>
	 * For maps, the paths may specify operations on keys or values using the syntax
	 * {@code {key=*}} or {@code {value=*}}, respectively.
	 * </p>
	 *
	 * <p>
	 * Example usage:
	 * </p>
	 * 
	 * <pre>
	 * Set<?> result = valueDomain.differenceSet("domain/set1", "domain/map1{value=a,b,c}");
	 * </pre>
	 *
	 * @param path1  the first path to resolve
	 * @param path2  the second path to resolve
	 * @param params optional parameters for parameterized queries
	 * @return a set containing the difference of the resolved values
	 * @throws IllegalArgumentException if the paths are invalid or incompatible
	 */
	Set<?> differenceSet(String path1, String path2, Object... params);

	/**
	 * Resolves the value of a given hierarchical path within the domain.
	 * 
	 * <p>
	 * The path may include wildcards or parameterized elements to support dynamic
	 * resolution of complex structures such as lists and maps.
	 * </p>
	 *
	 * <p>
	 * For example, resolving the path {@code "domain/mapElement{key=a,b,c}"} with
	 * no additional parameters will return a map or collection containing the
	 * values for the keys {@code "a", "b", and "c"}.
	 * </p>
	 *
	 * <p>
	 * Example usage:
	 * </p>
	 * 
	 * <pre>
	 * Object value = valueDomain.resolveValue(DomainPath.of("domain/mapElement{key=*}"));
	 * </pre>
	 *
	 * @param path   the hierarchical path to resolve
	 * @param params optional parameters for parameterized queries
	 * @return the resolved value, or {@code null} if the path cannot be fully
	 *         resolved
	 * @throws IllegalArgumentException if the path or parameters are invalid
	 */
	Object resolveValue(DomainPath path, Object... params);

	/**
	 * Resolves the value of a given hierarchical path specified as a string within
	 * the domain.
	 *
	 * <p>
	 * This method is a convenience wrapper for
	 * {@link #resolveValue(DomainPath, Object...)} that allows the path to be
	 * specified as a simple string. The string is automatically parsed into a
	 * {@link DomainPath} object before resolution.
	 * </p>
	 *
	 * <p>
	 * The path may include wildcards or parameterized elements to support dynamic
	 * resolution of complex structures such as lists and maps.
	 * </p>
	 *
	 * <p>
	 * For example, resolving the path {@code "domain/mapElement{key=a,b,c}"} with
	 * no additional parameters will return a map or collection containing the
	 * values for the keys {@code "a", "b", and "c"}.
	 * </p>
	 *
	 * <p>
	 * Example usage:
	 * </p>
	 * 
	 * <pre>
	 * Object value = valueDomain.resolveValue("domain/mapElement{key=*}");
	 * </pre>
	 *
	 * @param path   the hierarchical path to resolve, specified as a string
	 * @param params optional parameters for parameterized queries
	 * @return the resolved value, or {@code null} if the path cannot be fully
	 *         resolved
	 * @throws IllegalArgumentException if the path or parameters are invalid
	 */
	default Object resolveValue(String path, Object... params) {
		return resolveValue(DomainPath.of(path), params);
	}

}