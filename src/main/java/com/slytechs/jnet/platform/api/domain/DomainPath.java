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

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Represents an immutable path within a domain hierarchy, providing navigation
 * and path manipulation capabilities. DomainPath enforces path validation rules
 * and provides consistent path representation across the domain system.
 * 
 * <p>
 * Path Format:
 * </p>
 * <ul>
 * <li>Paths are separated by forward slashes ('/')</li>
 * <li>Leading and trailing slashes are normalized</li>
 * <li>Path elements must start with a letter</li>
 * <li>Path elements may contain letters, numbers, underscore, and hyphen</li>
 * <li>Empty path represents the root</li>
 * </ul>
 * 
 * <p>
 * Examples of valid paths:
 * </p>
 * 
 * <pre>{@code
 * /                  - Root path
 * /protocols        - Single element path
 * /ipv4/headers    - Multi-element path
 * /config/security - Nested path
 * }</pre>
 * 
 * <p>
 * Examples of invalid paths:
 * </p>
 * 
 * <pre>{@code
 * /123name         - Cannot start with number
 * /name@path       - Invalid special character
 * //double/slash   - Invalid empty element
 * }</pre>
 * 
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>{@code
 * // Create paths
 * DomainPath path1 = DomainPath.of("protocols", "ipv4");
 * DomainPath path2 = DomainPath.of("protocols/ipv4");
 * 
 * // Manipulate paths
 * DomainPath extended = path1.resolve("headers");
 * DomainPath parent = extended.parent();
 * 
 * // Navigate
 * String[] elements = path1.elements();
 * String firstElement = path1.element(0);
 * }</pre>
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public final class DomainPath {

	/**
	 * The path separator character used to delimit path elements. This is a forward
	 * slash ('/') by convention.
	 */
	public static final char SEPARATOR = '/';

	/**
	 * Regex pattern for validating path element names. Elements must:
	 * <ul>
	 * <li>Start with a letter</li>
	 * <li>Contain only letters, numbers, underscore, and hyphen</li>
	 * <li>Not be empty</li>
	 * </ul>
	 */
	private static final Pattern VALID_NAME = Pattern.compile("[a-zA-Z][a-zA-Z0-9_-]*");

	/** Empty element array used for the root path. */
	private static final String[] EMPTY_ELEMENTS = new String[0];

	/**
	 * The immutable root path constant, representing the top of the domain
	 * hierarchy. This path has no elements and is represented as a single forward
	 * slash ("/").
	 */
	public static final DomainPath ROOT = new DomainPath(EMPTY_ELEMENTS);

	/**
	 * Creates a new DomainPath from a string representation. The string is
	 * normalized by removing leading and trailing slashes before processing.
	 * 
	 * <p>
	 * Special cases:
	 * </p>
	 * <ul>
	 * <li>null path returns ROOT</li>
	 * <li>empty path returns ROOT</li>
	 * <li>single slash ("/") returns ROOT</li>
	 * </ul>
	 *
	 * @param path the string path to parse
	 * @return a new DomainPath instance
	 * @throws IllegalArgumentException if any path element is invalid
	 */
	public static DomainPath of(String path) {
		if (path == null || path.isEmpty() || path.equals(String.valueOf(SEPARATOR)))
			return ROOT;

		// Remove leading and trailing separators
		String normalized = path.charAt(0) == SEPARATOR ? path.substring(1) : path;
		normalized = normalized.charAt(normalized.length() - 1) == SEPARATOR ? normalized.substring(0, normalized
				.length() - 1) : normalized;

		String[] elements = normalized.split(String.valueOf(SEPARATOR));

		// Validate each path element
		for (String element : elements) {
			if (!VALID_NAME.matcher(element).matches())
				throw new IllegalArgumentException(
						"Invalid path element '" + element + "'. Path elements must start with a letter " +
								"and contain only letters, numbers, underscore, and hyphen.");
		}

		return new DomainPath(elements);
	}

	/**
	 * Creates a new DomainPath from an array of elements. Each element is validated
	 * individually against the path element rules.
	 *
	 * @param elements the path elements
	 * @return a new DomainPath instance
	 * @throws IllegalArgumentException if any element is null or invalid
	 */
	public static DomainPath of(String... elements) {
		if (elements == null || elements.length == 0)
			return ROOT;

		// Validate each element
		for (String element : elements) {
			if (element == null || !VALID_NAME.matcher(element).matches())
				throw new IllegalArgumentException(
						"Invalid path element '" + element + "'. Path elements must start with a letter " +
								"and contain only letters, numbers, underscore, and hyphen.");
		}

		return new DomainPath(elements.clone());
	}

	/** The immutable array of path elements. */
	private final String[] elements;

	/** Cached hash code for performance. */
	private final int hashCode;

	/** Lazily initialized string representation. */
	private String stringValue;

	/**
	 * Private constructor to enforce the use of factory methods. This ensures all
	 * paths are properly validated before creation.
	 *
	 * @param elements the validated path elements
	 */
	private DomainPath(String[] elements) {
		this.elements = elements;
		this.hashCode = Arrays.hashCode(elements);
	}

	/**
	 * Returns a defensive copy of the path elements.
	 *
	 * @return a new array containing the path elements
	 */
	public String[] elements() {
		return elements.clone();
	}

	/**
	 * Returns the path element at the specified index.
	 *
	 * @param index the index of the element to retrieve
	 * @return the path element
	 * @throws IndexOutOfBoundsException if index is out of range
	 */
	public String element(int index) {
		return elements[index];
	}

	/**
	 * Returns the number of elements in this path.
	 *
	 * @return the path length (number of elements)
	 */
	public int length() {
		return elements.length;
	}

	/**
	 * Checks if this path represents the root of the domain hierarchy.
	 *
	 * @return true if this is the root path (no elements)
	 */
	public boolean isRoot() {
		return elements.length == 0;
	}

	/**
	 * Creates a new path by appending additional elements to this path. The
	 * original path remains unchanged.
	 *
	 * @param additionalElements elements to append
	 * @return a new path with the additional elements
	 * @throws IllegalArgumentException if any additional element is invalid
	 */
	public DomainPath resolve(String... additionalElements) {
		if (additionalElements == null || additionalElements.length == 0)
			return this;

		String[] newElements = new String[elements.length + additionalElements.length];
		System.arraycopy(elements, 0, newElements, 0, elements.length);
		System.arraycopy(additionalElements, 0, newElements, elements.length, additionalElements.length);

		return new DomainPath(newElements);
	}

	/**
	 * Creates a new path by resolving another path against this path. If the other
	 * path is null or root, this path is returned unchanged.
	 *
	 * @param other the path to resolve against this path
	 * @return a new path combining this path with the other path
	 */
	public DomainPath resolve(DomainPath other) {
		if (other == null || other.isRoot())
			return this;

		return resolve(other.elements);
	}

	/**
	 * Returns the parent path by removing the last element. If this is already the
	 * root path, returns itself.
	 *
	 * @return the parent path
	 */
	public DomainPath parent() {
		if (isRoot())
			return this;

		return new DomainPath(Arrays.copyOf(elements, elements.length - 1));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof DomainPath))
			return false;

		DomainPath other = (DomainPath) obj;
		return Arrays.equals(elements, other.elements);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public String toString() {
		if (stringValue == null) {
			if (isRoot())
				stringValue = String.valueOf(SEPARATOR);
			else
				stringValue = String.valueOf(SEPARATOR) + String.join(String.valueOf(SEPARATOR), elements);
		}
		return stringValue;
	}
}