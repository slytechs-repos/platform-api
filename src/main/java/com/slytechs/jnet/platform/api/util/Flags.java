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
package com.slytechs.jnet.platform.api.util;

/**
 * Utility class for bitwise flag operations.
 * 
 * This class provides static methods to check if specific flags are set or
 * clear in an integer value. It is designed to be used with bitwise flags,
 * where each bit in an integer represents a boolean state.
 *
 * @author Mark Bednarczyk
 * @version 1.0
 * @since 1.0
 */
public final class Flags {

	/**
	 * Checks if a specific flag is set in the given value.
	 * 
	 * This method performs a bitwise AND operation between the value and the flag.
	 * If the result equals the flag, it means all bits in the flag are set in the
	 * value.
	 *
	 * @param value The integer value to check against.
	 * @param flag  The flag (bit mask) to check for.
	 * @return {@code true} if the flag is set in the value, {@code false}
	 *         otherwise.
	 */
	public static boolean isSet(int value, int flag) {
		return (value & flag) == flag;
	}

	/**
	 * Checks if a specific flag is clear (not set) in the given value.
	 * 
	 * This method performs a bitwise AND operation between the value and the flag.
	 * If the result is zero, it means none of the bits in the flag are set in the
	 * value.
	 *
	 * @param value The integer value to check against.
	 * @param flag  The flag (bit mask) to check for.
	 * @return {@code true} if the flag is clear in the value, {@code false}
	 *         otherwise.
	 */
	public static boolean isClear(int value, int flag) {
		return (value & flag) == 0;
	}

	/**
	 * Private constructor to prevent instantiation of this utility class.
	 * 
	 * This constructor is declared as private to ensure that no instances of this
	 * class can be created. All methods in this class are static and should be
	 * accessed directly through the class name.
	 */
	private Flags() {
		// Do not instantiate
	}
}