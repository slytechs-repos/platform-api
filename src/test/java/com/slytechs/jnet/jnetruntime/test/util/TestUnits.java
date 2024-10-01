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
package com.slytechs.jnet.jnetruntime.test.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.slytechs.jnet.jnetruntime.util.CountUnit;

/**
 * The Class TestUnits.
 */
class TestUnits {

	/**
	 * Sets the up.
	 *
	 * @throws Exception the exception
	 */
	@BeforeEach
	void setUp() throws Exception {
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
	 * Parses the units.
	 */
	@Test
	void parseUnits() {
		final var UNIT_CLASS = CountUnit.class;
		final var EXPECTED = CountUnit.KILO;

//		assertEquals(EXPECTED, UnitUtils.parseUnits("10k", UNIT_CLASS));
//		assertEquals(EXPECTED, UnitUtils.parseUnits("10 k", UNIT_CLASS));
//		assertEquals(EXPECTED, UnitUtils.parseUnits("10-k", UNIT_CLASS));
//		assertEquals(EXPECTED, UnitUtils.parseUnits("10_k", UNIT_CLASS));
//		assertEquals(EXPECTED, UnitUtils.parseUnits("10 (k)", UNIT_CLASS));
//		assertEquals(EXPECTED, UnitUtils.parseUnits("10@k", UNIT_CLASS));
	}

	/**
	 * Strip units.
	 */
	@Test
	void stripUnits() {
		final var UNIT_CLASS = CountUnit.class;
		final var EXPECTED = "10";

//		assertEquals(EXPECTED, UnitUtils.stripUnits("10k", UNIT_CLASS));
//		assertEquals(EXPECTED, UnitUtils.stripUnits("10 k", UNIT_CLASS));
//		assertEquals(EXPECTED, UnitUtils.stripUnits("10-k", UNIT_CLASS));
//		assertEquals(EXPECTED, UnitUtils.stripUnits("10_k", UNIT_CLASS));
//		assertEquals(EXPECTED, UnitUtils.stripUnits("10 (k)", UNIT_CLASS));
//		assertEquals(EXPECTED, UnitUtils.stripUnits("10@k", UNIT_CLASS));
	}

}
