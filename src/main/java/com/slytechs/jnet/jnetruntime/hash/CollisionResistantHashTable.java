package com.slytechs.jnet.jnetruntime.hash;

/**
 * A collision-resistant hash table implementation that uses linear probing and
 * maintains collision counts for optimized lookups.
 *
 * @param <K> The type of keys stored in the hash table
 * @param <V> The type of values stored in the hash table
 */
public class CollisionResistantHashTable<K, V> {

	private static class Entry<K, V> {
		K key;
		V value;
		long hashKey;
		int collisionCount;
		boolean deleted;

		Entry() {
			this.deleted = true;
		}

		void set(K key, V value, long hashKey, int collisionCount) {
			this.key = key;
			this.value = value;
			this.hashKey = hashKey;
			this.collisionCount = collisionCount;
			this.deleted = false;
		}

		void markDeleted() {
			this.deleted = true;
		}

		boolean isEmpty() {
			return deleted;
		}

		void copyFrom(Entry<K, V> other) {
			this.key = other.key;
			this.value = other.value;
			this.hashKey = other.hashKey;
			this.collisionCount = other.collisionCount;
			this.deleted = other.deleted;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Entry [key=" + key + ", value=" + value + ", hashKey=" + hashKey + ", collisionCount="
					+ collisionCount + ", deleted=" + deleted + "]";
		}
	}

	private final Entry<K, V>[] table;
	private final int tableSize;
	private int size;

	@SuppressWarnings("unchecked")
	public CollisionResistantHashTable(int capacity) {
		this.tableSize = capacity;
		this.table = new Entry[capacity];

		// Lazy initialization of entries
		for (int i = 0; i < capacity; i++) {
			table[i] = new Entry<>();
		}
	}

	/**
	 * Generates a 64-bit hash key from the given key.
	 */
	private long generateHashKey(K key) {
		if (key == null) {
			throw new IllegalArgumentException("Key cannot be null");
		}
		long hash = key.hashCode();
		hash = hash ^ (hash >>> 32);
		return (hash * 0x7FFF_FFFF_FFFF_FFFFL);
	}

	/**
	 * Maps a hash key to a table index.
	 */
	private int hashToIndex(long hashKey) {
		return Math.abs((int) (hashKey % tableSize));
	}

	/**
	 * Puts a key-value pair into the hash table:
	 * 
	 * <pre>
	 * 1. If slot is empty, insert there
	 * 2. If key matches existing key (in initial slot or collision chain), update it
	 * 3. If collisions exist and table isn't full, insert in next empty slot and update collision count
	 * </pre>
	 * 
	 * @param key   The key to insert
	 * @param value The value to insert
	 * @return true if insertion or update succeeded, false otherwise
	 */
	public boolean put(K key, V value) {
		if (key == null) {
			return false;
		}

		long hashKey = generateHashKey(key);
		int initialIndex = hashToIndex(hashKey);
		Entry<K, V> entry = table[initialIndex];

		// If slot is empty, insert here
		if (entry.isEmpty()) {
			entry.set(key, value, hashKey, 0);
			size++;
			return true;
		}

		// If this is the matching key in initial position, update it
		if (entry.hashKey == hashKey && entry.key.equals(key)) {
			entry.value = value;
			return true;
		}

		// If there are collisions, search the chain for a matching key
		if (entry.collisionCount > 0) {
			int remainingProbes = entry.collisionCount;

			for (int i = initialIndex + 1; i < initialIndex + tableSize && remainingProbes > 0; i++) {
				int probeIndex = i % tableSize;
				Entry<K, V> probeEntry = table[probeIndex];

				if (!probeEntry.isEmpty()) {
					if (probeEntry.hashKey == hashKey && probeEntry.key.equals(key)) {
						// Found matching key in chain, update it
						probeEntry.value = value;
						return true;
					}
					if (hashToIndex(probeEntry.hashKey) == initialIndex) {
						remainingProbes--;
					}
				}
			}
		}

		// No matching key found, try to insert in next empty slot if table isn't full
		if (size < tableSize) {
			// Find next empty slot
			int probeIndex = (initialIndex + 1) % tableSize;
			while (!table[probeIndex].isEmpty()) {
				probeIndex = (probeIndex + 1) % tableSize;
				if (probeIndex == initialIndex) {
					return false; // Shouldn't happen since size < tableSize, but just in case
				}
			}

			// Insert at empty slot and increment collision count at initial position
			entry.collisionCount++;
			table[probeIndex].set(key, value, hashKey, 0);
			size++;
			return true;
		}

		return false;
	}

	@Override
	public String toString() {
		return dumpTableRows();
	}

	/**
	 * Gets a value from the hash table by key.
	 */
	public V get(K key) {
		if (key == null) {
			return null;
		}

		long hashKey = generateHashKey(key);
		int index = hashToIndex(hashKey);

		Entry<K, V> entry = table[index];
		if (entry.isEmpty()) {
			return null;
		}

		// Check initial position
		if (entry.hashKey == hashKey && entry.key.equals(key)) {
			return entry.value;
		}

		// Linear probe through the entire table, but optimize using collision count
		int remainingProbes = entry.collisionCount;
		for (int i = index + 1; i < index + tableSize && remainingProbes > 0; i++) {
			int probeIndex = i % tableSize;
			entry = table[probeIndex];

			if (entry.isEmpty()) {
				continue;
			}

			if (entry.hashKey == hashKey && entry.key.equals(key)) {
				return entry.value;
			}

			if (hashToIndex(entry.hashKey) == index) {
				remainingProbes--;
			}
		}

		return null;
	}

	/**
	 * Removes an entry from the hash table.
	 */
	public V remove(K key) {
		if (key == null) {
			return null;
		}

		long hashKey = generateHashKey(key);
		int index = hashToIndex(hashKey);

		Entry<K, V> entry = table[index];
		if (entry.isEmpty()) {
			return null;
		}

		// Check if entry to remove is at initial position
		if (entry.hashKey == hashKey && entry.key.equals(key)) {
			V value = entry.value;

			if (entry.collisionCount == 0) {
				entry.markDeleted();
				size--;
				return value;
			}

			// Search for an imposter entry to move to this position
			int remainingProbes = entry.collisionCount;
			for (int i = index + 1; i < index + tableSize && remainingProbes > 0; i++) {
				int probeIndex = i % tableSize;
				Entry<K, V> imposter = table[probeIndex];

				if (!imposter.isEmpty() && hashToIndex(imposter.hashKey) == index) {
					// Move imposter to the removed position
					entry.key = imposter.key;
					entry.value = imposter.value;
					entry.hashKey = imposter.hashKey;
					entry.collisionCount--;

					// Mark the imposter slot as deleted
					imposter.markDeleted();
					size--;
					return value;
				}

				if (!imposter.isEmpty() && hashToIndex(imposter.hashKey) == index) {
					remainingProbes--;
				}
			}
		}

		// Search in collision chain
		int remainingProbes = entry.collisionCount;
		for (int i = index + 1; i < index + tableSize && remainingProbes > 0; i++) {
			int probeIndex = i % tableSize;
			Entry<K, V> probeEntry = table[probeIndex];

			if (!probeEntry.isEmpty()) {
				if (probeEntry.hashKey == hashKey && probeEntry.key.equals(key)) {
					V value = probeEntry.value;
					probeEntry.markDeleted();
					entry.collisionCount--;
					size--;
					return value;
				}

				if (hashToIndex(probeEntry.hashKey) == index) {
					remainingProbes--;
				}
			}
		}

		return null;
	}

	/**
	 * Returns the number of entries in the hash table.
	 */
	public int size() {
		return size;
	}

	/**
	 * Returns whether the hash table is empty.
	 */
	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * Dumps the contents of the hash table in a formatted table view. Shows index,
	 * hash key, key, value, collision count, and deletion status for each entry.
	 * 
	 * @return A formatted string representing the table contents
	 */
	public String dumpTableRows() {
		StringBuilder sb = new StringBuilder();

		// Header
		sb.append(String.format("%-6s | %-18s | %-20s | %-20s | %-8s | %-7s%n",
				"INDEX", "HASH KEY", "KEY", "VALUE", "COLLISN", "DELETED"));

		// Separator line
		sb.append("-".repeat(88)).append("\n");

		// Table contents
		for (int i = 0; i < tableSize; i++) {
			Entry<K, V> entry = table[i];

			if (entry.isEmpty()) {
				// Show empty slots in a condensed format
				sb.append(String.format("%-6d | %-18s | %-20s | %-20s | %-8s | %-7s%n",
						i, "-", "-", "-", "-", "YES"));
			} else {
				// Format the hash key in hex
				String hashKeyHex = String.format("0x%016X", entry.hashKey);

				// Format key and value, handling null and long values
				String keyStr = formatField(entry.key, 20);
				String valueStr = formatField(entry.value, 20);

				// Add the row
				sb.append(String.format("%-6d | %-18s | %-20s | %-20s | %-8d | %-7s%n",
						i,
						hashKeyHex,
						keyStr,
						valueStr,
						entry.collisionCount,
						entry.deleted ? "YES" : "NO"));
			}

			// Add a visual separator every 5 rows for better readability
			if ((i + 1) % 5 == 0 && i < tableSize - 1) {
				sb.append("-".repeat(88)).append("\n");
			}
		}

		// Footer with statistics
		sb.append("-".repeat(88)).append("\n");
		sb.append(String.format("Total Entries: %d, Table Size: %d, Load Factor: %.2f%n",
				size, tableSize, (float) size / tableSize));

		return sb.toString();
	}

	/**
	 * Helper method to format field values for display, handling null values and
	 * truncating long values.
	 */
	private String formatField(Object field, int maxLength) {
		if (field == null) {
			return "null";
		}

		String str = field.toString();
		if (str.length() > maxLength - 3) {
			return str.substring(0, maxLength - 3) + "...";
		}
		return str;
	}

	/**
	 * Finds the index of an entry with the given key. Note that this index is not
	 * stable and may change after removal operations, as collision entries
	 * (imposters) may be moved to fill gaps in the collision chains.
	 * 
	 * @param key The key to search for
	 * @return The index of the entry, or -1 if not found
	 * @implNote The returned index should not be stored for later use as it may
	 *           become invalid after any removal operation. Collision entries may
	 *           be relocated to maintain chain integrity during removals.
	 */
	public int indexOf(K key) {
		if (key == null) {
			return -1;
		}

		long hashKey = generateHashKey(key);
		int initialIndex = hashToIndex(hashKey);
		Entry<K, V> entry = table[initialIndex];

		// Check initial position
		if (entry.isEmpty()) {
			return -1;
		}

		if (entry.hashKey == hashKey && entry.key.equals(key)) {
			return initialIndex;
		}

		// Search collision chain if it exists
		if (entry.collisionCount > 0) {
			int remainingProbes = entry.collisionCount;

			for (int i = initialIndex + 1; i < initialIndex + tableSize && remainingProbes > 0; i++) {
				int probeIndex = i % tableSize;
				Entry<K, V> probeEntry = table[probeIndex];

				if (!probeEntry.isEmpty()) {
					if (probeEntry.hashKey == hashKey && probeEntry.key.equals(key)) {
						return probeIndex;
					}
					if (hashToIndex(probeEntry.hashKey) == initialIndex) {
						remainingProbes--;
					}
				}
			}
		}

		return -1;
	}

	/**
	 * Finds the index of an entry with the given hash key. Returns the first
	 * matching entry if multiple exist.
	 * 
	 * @param hashKey The hash key to search for
	 * @return The index of the entry, or -1 if not found
	 */
	public int indexOf(long hashKey) {
		int initialIndex = hashToIndex(hashKey);
		Entry<K, V> entry = table[initialIndex];

		// Check initial position
		if (entry.isEmpty()) {
			return -1;
		}

		if (entry.hashKey == hashKey) {
			return initialIndex;
		}

		// Search collision chain if it exists
		if (entry.collisionCount > 0) {
			int remainingProbes = entry.collisionCount;

			for (int i = initialIndex + 1; i < initialIndex + tableSize && remainingProbes > 0; i++) {
				int probeIndex = i % tableSize;
				Entry<K, V> probeEntry = table[probeIndex];

				if (!probeEntry.isEmpty()) {
					if (probeEntry.hashKey == hashKey) {
						return probeIndex;
					}
					if (hashToIndex(probeEntry.hashKey) == initialIndex) {
						remainingProbes--;
					}
				}
			}
		}

		return -1;
	}
}