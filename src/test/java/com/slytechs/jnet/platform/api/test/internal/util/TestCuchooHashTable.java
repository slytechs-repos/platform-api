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
package com.slytechs.jnet.platform.api.test.internal.util;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.slytechs.jnet.platform.api.hash.CuckooHashTable;
import com.slytechs.jnet.platform.api.hash.HashTable;
import com.slytechs.jnet.platform.api.test.Tests;

/**
 * The Class TestCuchooHashTable.
 */
class TestCuchooHashTable {

	/** The key. */
	ByteBuffer key;
	
	/** The table. */
	HashTable<String> table;

	/**
	 * Sets the up.
	 *
	 * @throws Exception the exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		key = ByteBuffer.allocateDirect(HashTable.MAX_KEY_SIZE_BYTES);
		table = new CuckooHashTable<>();
		table.enableStickyData(true);
	}

	/**
	 * Tear down.
	 *
	 * @throws Exception the exception
	 */
	@AfterEach
	void tearDown() throws Exception {
	}

	/**
	 * Test empty lookup.
	 */
	@Test
	void test_emptyLookup() {
		key.putInt(0, 10);

		assertEquals(-1, table.lookup(key));
	}

	/**
	 * Test add.
	 */
	@Test
	void test_add() {
		var VALUE = "entry1";
		key.putInt(0, 10);

		int index = table.add(key, VALUE);
		assertNotEquals(-1, index);
		assertEquals(VALUE, table.get(index).data());
	}

	/**
	 * Test add and lookup.
	 */
	@Test
	void test_addAndLookup() {
		var VALUE = "entry1";
		key.putInt(0, 10);

		table.add(key, VALUE);

		int index = table.lookup(key);

		assertNotEquals(-1, index);
		assertEquals(VALUE, table.get(index).data());

	}

	/**
	 * Test add and lookup invalid key.
	 */
	@Test
	void test_addAndLookup_invalidKey() {
		var VALUE = "entry1";
		key.putInt(0, 10);

		table.add(key, VALUE);

		key.putInt(0, 1234); // Set invalid key

		int index = table.lookup(key);

		assertEquals(-1, index);
	}

	/**
	 * Test add multiple and lookup.
	 */
	@Test
	void test_addMultipleAndLookup() {

		for (int i = 0; i < HashTable.DEFAULT_TABLE_SIZE; i++) {
			var VALUE = "entry_" + i;
			key.putInt(0, i);

			table.add(key, VALUE);

			int index = table.lookup(key);

			assertNotEquals(-1, index, "at index %d".formatted(i));
			assertEquals(VALUE, table.get(index).data());
		}
		Tests.out.println(table);
	}

}
