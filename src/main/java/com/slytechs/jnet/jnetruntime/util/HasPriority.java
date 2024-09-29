/*
 * Sly Technologies Free License
 * 
 * Copyright 2023 Sly Technologies Inc.
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

import java.util.Comparator;

/**
 * Defines an interface for objects that have a priority value, typically used
 * to prioritize protocol processing.
 * 
 * <p>
 * The priority system is based on numerical values where lower positive numbers
 * indicate higher priority. A value of {@code 0} represents the highest
 * possible priority.
 * </p>
 * 
 * <p>
 * When two or more objects have the same priority value, their relative
 * precedence is undefined.
 * </p>
 * 
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 */
public interface HasPriority {

	/** The default priority value. */
	int DEFAULT_PRIORITY_VALUE = 0;

	/** The minimum allowed priority value. */
	int MIN_PRIORITY_VALUE = 0;

	/** The maximum allowed priority value. */
	int MAX_PRIORITY_VALUE = (Integer.MAX_VALUE - 1);

	/**
	 * Comparator that sorts HasPriority objects from highest to lowest priority.
	 */
	Comparator<HasPriority> HIGH_LOW_COMPARATOR = HasPriority::compareHighToLow;

	/**
	 * Comparator that sorts HasPriority objects from lowest to highest priority.
	 */
	Comparator<HasPriority> LOW_HIGH_COMPARATOR = HasPriority::compareLowToHigh;

	/**
	 * Validates the given priority value.
	 *
	 * @param value the priority value to check
	 * @throws IllegalArgumentException if the value is outside the valid range
	 *                                  [{@code MIN_PRIORITY_VALUE},
	 *                                  {@code MAX_PRIORITY_VALUE}]
	 */
	static void checkPriorityValue(int value) throws IllegalArgumentException {
		if (value < MIN_PRIORITY_VALUE || value > MAX_PRIORITY_VALUE)
			throw new IllegalArgumentException("Priority value out of range [%d]".formatted(value));
	}

	/**
	 * Compares two HasPriority objects from higher priority (lower values) to lower
	 * priority (higher values).
	 *
	 * @param a the first HasPriority object
	 * @param b the second HasPriority object
	 * @return a negative integer, zero, or a positive integer as the first argument
	 *         has higher, equal to, or lower priority than the second
	 */
	static int compareHighToLow(HasPriority a, HasPriority b) {
		return a.priority() - b.priority();
	}

	/**
	 * Compares two HasPriority objects from lower priority (higher values) to
	 * higher priority (lower values).
	 *
	 * @param a the first HasPriority object
	 * @param b the second HasPriority object
	 * @return a negative integer, zero, or a positive integer as the first argument
	 *         has lower, equal to, or higher priority than the second
	 */
	static int compareLowToHigh(HasPriority a, HasPriority b) {
		return b.priority() - a.priority();
	}

	/**
	 * Checks if the given value is within the valid priority range.
	 *
	 * @param value the priority value to check
	 * @return true if the value is within the valid range, false otherwise
	 */
	static boolean isValidPriority(int value) {
		return value >= MIN_PRIORITY_VALUE && value <= MAX_PRIORITY_VALUE;
	}

	/**
	 * Returns the priority value of this object.
	 *
	 * @return the priority value, between {@code MIN_PRIORITY_VALUE} and
	 *         {@code MAX_PRIORITY_VALUE}, inclusive
	 */
	int priority();
}