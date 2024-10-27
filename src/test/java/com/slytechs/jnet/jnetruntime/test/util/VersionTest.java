package com.slytechs.jnet.jnetruntime.test.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.slytechs.jnet.jnetruntime.util.InvalidVersionException;
import com.slytechs.jnet.jnetruntime.util.Version;

/**
 * Comprehensive test suite for the {@link Version} class. Tests include
 * parsing, comparison, compatibility checks, and edge cases.
 */
class VersionTest {

	@Nested
	class ConstructorTests {

		@Test
		void testValidConstructorWithNumbers() {
			Version version = new Version(1, 2, 3);
			assertEquals("1.2.3", version.toString());
			assertEquals(1, version.getMajor());
			assertEquals(2, version.getMinor());
			assertEquals(3, version.getPatch());
			assertNull(version.getPreRelease());
			assertNull(version.getBuildMetadata());
		}

		@Test
		void testValidConstructorWithPreRelease() {
			Version version = new Version(1, 2, 3, "alpha.1");
			assertEquals("1.2.3-alpha.1", version.toString());
			assertEquals("alpha.1", version.getPreRelease());
		}

		@Test
		void testValidConstructorWithBuildMetadata() {
			Version version = new Version(1, 2, 3, null, "build.123");
			assertEquals("1.2.3+build.123", version.toString());
			assertEquals("build.123", version.getBuildMetadata());
		}

		@Test
		void testValidConstructorWithAllComponents() {
			Version version = new Version(1, 2, 3, "beta", "build.123");
			assertEquals("1.2.3-beta+build.123", version.toString());
		}

		@Test
		void testNegativeVersionNumbers() {
			assertThrows(IllegalArgumentException.class, () -> new Version(-1, 0, 0));
			assertThrows(IllegalArgumentException.class, () -> new Version(0, -1, 0));
			assertThrows(IllegalArgumentException.class, () -> new Version(0, 0, -1));
		}
	}

	@Nested
	class ParsingTests {

		@ParameterizedTest(name = "Valid version string: {0}")
		@ValueSource(strings = {
				"0.0.0", // Zero version
				"1.0.0", // Major version only
				"0.1.0", // Minor version only
				"0.0.1", // Patch version only
				"1.2.3", // Standard version
				"10.20.30", // Multi-digit numbers
				"1.0.0-alpha", // Simple pre-release
				"1.0.0-alpha.1", // Pre-release with number
				"1.0.0-0.3.7", // Numeric pre-release
				"1.0.0-x.7.z.92", // Complex pre-release
				"1.0.0+build.1", // Simple build metadata
				"1.0.0+build.1.2", // Complex build metadata
				"1.0.0-alpha+build.1" // Pre-release and build metadata
		})
		void testValidVersionStrings(String versionStr) {
			assertDoesNotThrow(() -> new Version(versionStr));
		}

		@ParameterizedTest(name = "Invalid version string: {0}")
		@ValueSource(strings = {
				"", // Empty string
				"1", // Missing components
				"1.2", // Missing patch
				"1.2.3.4", // Too many components
				"01.2.3", // Leading zeros
				"1.02.3", // Leading zeros
				"1.2.03", // Leading zeros
				"1.2.3-", // Empty pre-release
				"1.2.3+", // Empty build metadata
				"1.2.3-@", // Invalid pre-release character
				"1.2.3+@", // Invalid build metadata character
				"a.b.c", // Non-numeric versions
				"1.2.3-alpha!1" // Invalid pre-release character
		})
		void testInvalidVersionStrings(String versionStr) {
			assertThrows(InvalidVersionException.class, () -> new Version(versionStr));
		}

		@Test
		void testNullVersionString() {
			assertThrows(NullPointerException.class, () -> new Version(null));
		}
	}

	@Nested
	class ComparisonTests {

		@ParameterizedTest(name = "{0} compared to {1} should be {2}")
		@CsvSource({
				// version1, version2, expected (-1, 0, 1)
				"1.0.0,        1.0.0,           0",
				"1.0.0,        2.0.0,           -1",
				"2.0.0,        1.0.0,           1",
				"2.1.0,        2.0.0,           1",
				"2.0.0,        2.1.0,           -1",
				"2.0.1,        2.0.0,           1",
				"2.0.0,        2.0.1,           -1",
				// Pre-release comparisons
				"1.0.0-alpha,   1.0.0,          -1",
				"1.0.0,         1.0.0-alpha,    1",
				"1.0.0-alpha,   1.0.0-alpha.1,  -1",
				"1.0.0-alpha.1, 1.0.0-alpha.2,  -1",
				"1.0.0-alpha.2, 1.0.0-alpha.1,  1",
				"1.0.0-alpha,   1.0.0-beta,     -1",
				"1.0.0-beta,    1.0.0-alpha,    1",
				// Build metadata should be ignored in comparison
				"1.0.0+build.1, 1.0.0+build.2,  0",
				"1.0.0,         1.0.0+build.1,  0"
		})
		void testVersionComparison(String version1Str, String version2Str, int expected) {
			Version v1 = new Version(version1Str);
			Version v2 = new Version(version2Str);
			assertEquals(expected, Integer.signum(v1.compareTo(v2)));
		}
	}

	@Nested
	class CompatibilityTests {

		@ParameterizedTest(name = "App version {0} compatibility with library version {1} should be {2}")
		@CsvSource({
				// version1 (app), version2 (lib), isCompatible
				"1.0.0,           1.0.0,           true", // Exact match
				"2.0.0,           2.0.0,           true", // Exact match
				"2.0.0,           2.1.0,           true", // App can use newer minor
				"2.1.0,           2.0.0,           false", // App requires newer minor
				"2.0.0,           1.0.0,           false", // Different major
				"1.0.0,           2.0.0,           false", // Different major
				// Special handling for 0.x.x versions
				"0.1.0,           0.1.0,           true", // Exact match required
				"0.1.0,           0.1.1,           true", // Patch version doesn't matter
				"0.1.1,           0.1.0,           true", // Patch version doesn't matter
				"0.2.0,           0.1.0,           false", // Different minor in 0.x
				"0.1.0,           0.2.0,           false", // Different minor in 0.x
				// Pre-release versions
				"1.0.0-alpha,     1.0.0,           true", // Pre-release can use release
				"1.0.0,           1.0.0-alpha,     true", // Release can use pre-release
				// Build metadata
				"1.0.0+build.1,   1.0.0+build.2,   true" // Build metadata ignored
		})
		void testVersionCompatibility(String version1Str, String version2Str, boolean expected) {
			Version v1 = new Version(version1Str);
			Version v2 = new Version(version2Str);
			assertEquals(expected, v1.isCompatibleWith(v2),
					String.format("Expected compatibility of %s with %s to be %s", version1Str, version2Str, expected));
		}

		@Test
		void testNullCompatibility() {
			Version v = new Version("1.0.0");
			assertThrows(NullPointerException.class, () -> v.isCompatibleWith(null));
		}
	}

	@Nested
	class EqualityTests {

		@Test
		void testEquality() {
			Version v1 = new Version("1.2.3");
			Version v2 = new Version("1.2.3");
			Version v3 = new Version("1.2.3-alpha");
			Version v4 = new Version("1.2.3+build.1");
			Version v5 = new Version("1.2.3-alpha+build.1");

			// Test basic equality
			assertEquals(v1, v2);
			assertNotEquals(v1, v3);
			assertEquals(v1, v4); // Build metadata shouldn't affect equality
			assertNotEquals(v1, v5);

			// Test hashCode consistency
			assertEquals(v1.hashCode(), v2.hashCode());
			assertNotEquals(v1.hashCode(), v3.hashCode());
			assertEquals(v1.hashCode(), v4.hashCode()); // Build metadata shouldn't affect hashCode
			assertNotEquals(v1.hashCode(), v5.hashCode());
		}

		@Test
		void testEqualityWithNull() {
			Version v1 = new Version("1.2.3");
			assertNotEquals(null, v1);
			assertNotEquals(v1, null);
		}

		@Test
		void testEqualityWithDifferentClass() {
			Version v1 = new Version("1.2.3");
			assertNotEquals(v1, "1.2.3");
		}
	}

	@Nested
	class PreReleaseTests {

		@ParameterizedTest(name = "Pre-release version: {0}")
		@ValueSource(strings = {
				"1.0.0-alpha",
				"1.0.0-alpha.1",
				"1.0.0-0.3.7",
				"1.0.0-beta",
				"1.0.0-beta.11",
				"1.0.0-rc.1",
				"1.0.0-x.7.z.92"
		})
		void testValidPreReleaseVersions(String versionStr) {
			Version version = new Version(versionStr);
			assertNotNull(version.getPreRelease());
		}

		@Test
		void testPreReleaseComparison() {
			Version[] versions = {
					new Version("1.0.0-alpha"),
					new Version("1.0.0-alpha.1"),
					new Version("1.0.0-alpha.beta"),
					new Version("1.0.0-beta"),
					new Version("1.0.0-beta.2"),
					new Version("1.0.0-beta.11"),
					new Version("1.0.0-rc.1"),
					new Version("1.0.0")
			};

			// Test that array is in ascending order
			for (int i = 0; i < versions.length - 1; i++) {
				assertTrue(versions[i].compareTo(versions[i + 1]) < 0,
						String.format("%s should be less than %s", versions[i], versions[i + 1]));
			}
		}
	}

	@Nested
	class BuildMetadataTests {

		@ParameterizedTest(name = "Build metadata: {0}")
		@ValueSource(strings = {
				"1.0.0+build.1",
				"1.0.0+build.1.2",
				"1.0.0+exp.sha.5114f85",
				"1.0.0-alpha+build.123",
				"1.0.0-beta+exp.sha.5114f85"
		})
		void testValidBuildMetadata(String versionStr) {
			Version version = new Version(versionStr);
			assertNotNull(version.getBuildMetadata());
		}

		@Test
		void testBuildMetadataIgnoredInComparison() {
			Version v1 = new Version("1.0.0+build.1");
			Version v2 = new Version("1.0.0+build.2");
			Version v3 = new Version("1.0.0");

			assertEquals(0, v1.compareTo(v2));
			assertEquals(0, v1.compareTo(v3));
			assertTrue(v1.equals(v2));
			assertTrue(v1.equals(v3));
		}
	}
}