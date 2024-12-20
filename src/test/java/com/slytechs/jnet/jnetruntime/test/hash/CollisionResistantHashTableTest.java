package com.slytechs.jnet.jnetruntime.test.hash;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.slytechs.jnet.platform.api.hash.CollisionResistantHashTable;

public class CollisionResistantHashTableTest {
	private CollisionResistantHashTable<String, Integer> table;
	private static final int DEFAULT_SIZE = 10;

	@BeforeEach
	void setUp() {
		table = new CollisionResistantHashTable<>(DEFAULT_SIZE);
	}

	@Test
	@DisplayName("Test basic put and get operations")
	void testBasicPutAndGet() {
		assertTrue(table.put("test1", 1));
		assertTrue(table.put("test2", 2));

		assertEquals(1, table.get("test1"));
		assertEquals(2, table.get("test2"));

		// Update existing key
		assertTrue(table.put("test1", 3));
		assertEquals(3, table.get("test1"));
		assertEquals(2, table.get("test2"));
	}

	@Test
	@DisplayName("Test null key handling")
	void testNullKey() {
		assertFalse(table.put(null, 1));
		assertNull(table.get(null));
		assertNull(table.remove(null));
	}

	@Test
	@DisplayName("Test slot overwriting")
	void testSlotOverwriting() {

		var twoRowTable = new CollisionResistantHashTable<String, Integer>(2);

		assertTrue(twoRowTable.put("key1", 1));
		assertEquals(1, twoRowTable.get("key1"));
		assertEquals(1, twoRowTable.size());

		// Overwrite with same key
		assertTrue(twoRowTable.put("key1", 2));
		assertEquals(2, twoRowTable.get("key1"));
		assertEquals(1, twoRowTable.size());

		// Overwrite with different key
		assertTrue(twoRowTable.put("key2", 3));
		assertEquals(3, twoRowTable.get("key2"));
		assertEquals(2, twoRowTable.size());
	}

	@Test
	@DisplayName("Test collision chain behavior")
	void testCollisionChain() {
		class CollisionKey {
			private final String value;

			CollisionKey(String value) {
				this.value = value;
			}

			@Override
			public int hashCode() {
				return 1; // Force collisions
			}

			@Override
			public boolean equals(Object obj) {
				if (!(obj instanceof CollisionKey))
					return false;
				return value.equals(((CollisionKey) obj).value);
			}

			@Override
			public String toString() {
				return value;
			}
		}

		CollisionResistantHashTable<CollisionKey, Integer> collisionTable = new CollisionResistantHashTable<>(5);

		CollisionKey key1 = new CollisionKey("A");
		CollisionKey key2 = new CollisionKey("B");
		CollisionKey key3 = new CollisionKey("C");

		// Build collision chain
		assertTrue(collisionTable.put(key1, 1));
		assertTrue(collisionTable.put(key2, 2));
		assertTrue(collisionTable.put(key3, 3));

		assertEquals(1, collisionTable.get(key1));
		assertEquals(2, collisionTable.get(key2));
		assertEquals(3, collisionTable.get(key3));

		// Overwrite head of chain
		CollisionKey key4 = new CollisionKey("D");
		assertTrue(collisionTable.put(key4, 4));

		assertEquals(4, collisionTable.get(key4));
		assertEquals(2, collisionTable.get(key2));
		assertEquals(3, collisionTable.get(key3));
		
		assertEquals(1, collisionTable.get(key1));
	}

	@Test
	@DisplayName("Test remove operation with collisions")
	void testRemoveWithCollisions() {
		class CollisionKey {
			private final String value;

			CollisionKey(String value) {
				this.value = value;
			}

			@Override
			public int hashCode() {
				return 1;
			}

			@Override
			public boolean equals(Object obj) {
				if (!(obj instanceof CollisionKey))
					return false;
				return value.equals(((CollisionKey) obj).value);
			}
		}

		CollisionResistantHashTable<CollisionKey, Integer> collisionTable = new CollisionResistantHashTable<>(5);

		CollisionKey key1 = new CollisionKey("A");
		CollisionKey key2 = new CollisionKey("B");

		// Setup initial chain
		assertTrue(collisionTable.put(key1, 1));
		assertTrue(collisionTable.put(key2, 2));

		assertEquals(1, collisionTable.get(key1));
		assertEquals(2, collisionTable.get(key2));

		// Remove first key
		assertEquals(1, collisionTable.remove(key1));
		assertNull(collisionTable.get(key1));
		assertEquals(2, collisionTable.get(key2));

		// Add new key after removal
		CollisionKey key3 = new CollisionKey("C");
		assertTrue(collisionTable.put(key3, 3));
		assertEquals(3, collisionTable.get(key3));
		assertEquals(2, collisionTable.get(key2));
	}

	@Test
	@DisplayName("Test mixed operations")
	void testMixedOperations() {
		// Insert some initial values
		assertTrue(table.put("A", 1));
		assertTrue(table.put("B", 2));
		assertTrue(table.put("C", 3));

		// Verify initial state
		assertEquals(1, table.get("A"));
		assertEquals(2, table.get("B"));
		assertEquals(3, table.get("C"));

		// Update existing value
		assertTrue(table.put("B", 20));
		assertEquals(20, table.get("B"));

		// Remove a value
		assertEquals(1, table.remove("A"));
		assertNull(table.get("A"));

		// Insert new value in removed slot
		assertTrue(table.put("D", 4));
		assertEquals(4, table.get("D"));

		// Verify other values remain unchanged
		assertEquals(20, table.get("B"));
		assertEquals(3, table.get("C"));
	}
	
	interface IndexChangeNotification {
		void onIndexChange(int oldIndex, int newIndex);
	}

	@Test
	@DisplayName("Test collision chain preservation")
	void testCollisionChainPreservation() {
		class FixedHashKey {
			private final String value;

			FixedHashKey(String value) {
				this.value = value;
			}

			@Override
			public int hashCode() {
				return 1;
			}

			@Override
			public boolean equals(Object obj) {
				if (!(obj instanceof FixedHashKey))
					return false;
				return value.equals(((FixedHashKey) obj).value);
			}
			
			@Override
			public String toString() {
				return value;
			}
		}
		
		CollisionResistantHashTable<FixedHashKey, Integer> collisionTable = new CollisionResistantHashTable<>(5);

		FixedHashKey[] keys = new FixedHashKey[4];
		for (int i = 0; i < keys.length; i++) {
			keys[i] = new FixedHashKey("Key" + i);
			assertTrue(collisionTable.put(keys[i], i));
		}

		// Verify all entries
		for (int i = 0; i < keys.length; i++) {
			assertEquals(i, collisionTable.get(keys[i]));
		}

		// Override middle of chain
		FixedHashKey newKey = new FixedHashKey("NewKey");
		assertTrue(collisionTable.put(newKey, 99));

		// Verify chain integrity
		assertEquals(99, collisionTable.get(newKey));
		for (int i = 1; i < keys.length; i++) {
			assertEquals(i, collisionTable.get(keys[i]));
		}
		
		FixedHashKey tooManyKey = new FixedHashKey("tooMany");
		assertFalse(collisionTable.put(tooManyKey, 100));
		assertNull(collisionTable.get(tooManyKey));
	}
}