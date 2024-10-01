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
package com.slytechs.jnet.jnetruntime.util;

import java.util.Objects;

/**
 * Implements Semantic Versioning 2.0.0 as specified at https://semver.org. This
 * class provides methods for version comparison, parsing, and validation.
 *
 * @author Sly Technologies
 */
public class Version implements Comparable<Version> {

	/**
	 * Interface for classes that provide version information.
	 *
	 * @author Mark Bednarczyk
	 */
	public interface HasVersion {
		/**
		 * Gets the version of the implementing class.
		 *
		 * @return the Version object
		 */
		Version getVersion();
	}

	/**
	 * Checks if the major versions of two version strings are compatible.
	 *
	 * @param v1 the first version string
	 * @param v2 the second version string
	 * @throws InvalidVersionException if the versions are incompatible or invalid
	 */
	public static void checkMajorVersion(String v1, String v2) throws InvalidVersionException {
		fromString(v1).checkMajorVersion(fromString(v2));
	}

	/**
	 * Checks if the minor versions of two version strings are compatible.
	 *
	 * @param v1 the first version string
	 * @param v2 the second version string
	 * @throws InvalidVersionException if the versions are incompatible or invalid
	 */
	public static void checkMinorVersion(String v1, String v2) throws InvalidVersionException {
		fromString(v1).checkMinorVersion(fromString(v2));
	}

	/**
	 * Parses a version string into a Version object.
	 *
	 * @param v the version string to parse
	 * @return a new Version object
	 * @throws InvalidVersionException if the version string is invalid
	 */
	public static Version fromString(String v) throws InvalidVersionException {
		String[] c = Objects.requireNonNull(v, "Version.fromString(v)").split("[.]");

		try {
			int major = Integer.parseInt(c[0]);
			int minor = (c.length > 1) ? Integer.parseInt(c[1]) : 0;
			int maintenance = (c.length > 2) ? Integer.parseInt(c[2]) : 0;
			String build = (c.length > 3) ? c[3] : null;

			return new Version(major, minor, maintenance, build);
		} catch (Throwable e) {
			throw new InvalidVersionException("invalid format [" + v + "]", e);
		}
	}

	/**
	 * Creates a Version object from a string, throwing a runtime
	 * IllegalStateException if invalid.
	 *
	 * @param v the version string
	 * @return a new Version object
	 * @throws IllegalStateException if the version string is invalid
	 */
	public static Version of(String v) {
		try {
			return Version.fromString(v);
		} catch (InvalidVersionException e) {
			throw new IllegalStateException("malformed version string", e);
		}
	}

	/** The major. */
	private int major;
	
	/** The minor. */
	private int minor;
	
	/** The maintenance. */
	private int maintenance;
	
	/** The build. */
	private String build;

	/**
	 * Constructs a new Version with major and minor components.
	 *
	 * @param major the major version number
	 * @param minor the minor version number
	 */
	public Version(int major, int minor) {
		this(major, minor, 0, null);
	}

	/**
	 * Constructs a new Version with major, minor, and maintenance components.
	 *
	 * @param major       the major version number
	 * @param minor       the minor version number
	 * @param maintenance the maintenance version number
	 */
	public Version(int major, int minor, int maintenance) {
		this(major, minor, maintenance, null);
	}

	/**
	 * Constructs a new Version with all components.
	 *
	 * @param major       the major version number
	 * @param minor       the minor version number
	 * @param maintenance the maintenance version number
	 * @param build       the build identifier
	 */
	public Version(int major, int minor, int maintenance, String build) {
		this.major = major;
		this.minor = minor;
		this.maintenance = maintenance;
		this.build = build;
	}

	/**
	 * Checks if this version is compatible with the major version of the given
	 * HasVersion object.
	 *
	 * @param runtimeVersion the HasVersion object to check against
	 * @throws InvalidVersionException if the versions are incompatible
	 */
	public void checkMajorVersion(HasVersion runtimeVersion) throws InvalidVersionException {
		checkMajorVersion(runtimeVersion.getVersion());
	}

	/**
	 * Checks if this version is compatible with the major version of the given
	 * Version object.
	 *
	 * @param runtimeVersion the Version object to check against
	 * @throws InvalidVersionException if the versions are incompatible
	 */
	public void checkMajorVersion(Version runtimeVersion) throws InvalidVersionException {
		if (runtimeVersion.major < this.major) {
			throw new InvalidVersionException("runtime major version incompatible " + runtimeVersion + " with " + this);
		}
	}

	/**
	 * Checks if this version is compatible with the minor version of the given
	 * HasVersion object.
	 *
	 * @param runtimeVersion the HasVersion object to check against
	 * @throws InvalidVersionException if the versions are incompatible
	 */
	public void checkMinorVersion(HasVersion runtimeVersion) throws InvalidVersionException {
		checkMinorVersion(runtimeVersion.getVersion());
	}

	/**
	 * Checks if this version is compatible with the minor version of the given
	 * Version object.
	 *
	 * @param runtimeVersion the Version object to check against
	 * @throws InvalidVersionException if the versions are incompatible
	 */
	public void checkMinorVersion(Version runtimeVersion) throws InvalidVersionException {
		if (runtimeVersion.major < this.major) {
			throw new InvalidVersionException("runtime major version incompatible " + runtimeVersion + " with " + this);
		}

		if (runtimeVersion.minor < this.minor) {
			throw new InvalidVersionException("runtime minor version incompatible" + runtimeVersion + " with " + this);
		}
	}

	/**
	 * Compares this Version to a version string.
	 *
	 * @param o the version string to compare to
	 * @return a negative integer, zero, or a positive integer as this Version is
	 *         less than, equal to, or greater than the specified version string
	 */
	public int compareTo(String o) {
		return toString().compareTo(o);
	}

	/**
	 * Compares this Version to another Version object.
	 *
	 * @param o the Version object to be compared
	 * @return a negative integer, zero, or a positive integer as this Version is
	 *         less than, equal to, or greater than the specified Version
	 */
	@Override
	public int compareTo(Version o) {
		return compareTo(o.toString());
	}

	/**
	 * Returns a string representation of this Version.
	 *
	 * @return a string representation of this Version
	 */
	@Override
	public String toString() {
		return "" + major
				+ "." + minor
				+ "." + maintenance
				+ (build == null ? "" : "." + build);
	}
}