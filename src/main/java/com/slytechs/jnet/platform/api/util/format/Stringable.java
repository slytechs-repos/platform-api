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
package com.slytechs.jnet.platform.api.util.format;

import java.io.IOException;

/**
 * The {@code Stringable} interface defines the contract for objects that can
 * generate a string representation with varying levels of detail. It extends
 * the {@link Printable} interface, utilizing the detailed printing
 * functionality to produce the string output.
 * 
 * <p>
 * Implementing classes can leverage the {@link Detail} parameter to control the
 * level of information included in the string representation, ranging from
 * simple summaries to comprehensive details.
 * </p>
 *
 * <p>
 * This interface is particularly useful for debugging, logging, or generating
 * human-readable descriptions of objects with a flexible level of verbosity.
 * </p>
 *
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>
 * {@code
 * Stringable myObject = ...;
 * String brief = myObject.toString(Detail.BRIEF);
 * String detailed = myObject.toString(Detail.DETAILED);
 * }
 * </pre>
 *
 * @see Printable
 * @see Detail
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public interface Stringable extends Printable {

	/**
	 * Generates a string representation of this object with the specified level of
	 * detail.
	 * 
	 * <p>
	 * The level of detail is controlled by the {@link Detail} parameter, which
	 * allows callers to request different levels of verbosity, such as
	 * {@code BRIEF}, {@code DETAILED}, or custom-defined levels.
	 * </p>
	 *
	 * @param detail the level of detail to include in the string representation
	 * @return a string representation of this object with the specified detail
	 * @see Detail
	 */
	default String toString(Detail detail) {
		return printToString(detail);
	}

	/**
	 * To string formatted using the default detail level.
	 *
	 * @param formatter the formatter, can be null at which time the method defaults
	 *                  to calling toStringFormatted(formatter, detail)
	 * @return the string
	 */
	default String toStringFormatted(DetailFormatter formatter) {
		return toStringFormatted(formatter, Printable.DEFAULT_DETAIL);
	}

	/**
	 * To string formatted.
	 *
	 * @param formatter the formatter, can be null at which time the method defaults
	 *                  to calling printToString(detail)
	 * @param detail    the detail
	 * @return the string
	 */
	default String toStringFormatted(DetailFormatter formatter, Detail detail) {
		if (formatter == null)
			return printToString(detail);

		StringBuilder out = new StringBuilder();

		try {
			printFormattedTo(out, formatter, detail);

			return out.toString();
		} catch (IOException e) {
			throw new IllegalStateException("An error occurred while printing to a string", e);
		}
	}
}
