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

import com.slytechs.jnet.platform.api.util.Detail;

/**
 * A utility interface for objects that can be formatted and printed to an
 * {@link Appendable} or converted to a string representation. The formatting
 * respects different levels of detail defined by the {@link Detail} enum.
 *
 * <p>
 * Implementing classes can define how the output is formatted based on the
 * provided detail level, enabling flexibility in the verbosity of the printed
 * information. The default detail level is {@link Detail#DEFAULT}, which is set
 * to {@link Detail#HIGH}.
 * </p>
 *
 * <p>
 * Example usage:
 * 
 * <pre>
 * Printable printableObject = ...;
 * String fullDetails = printableObject.printToString(Detail.HIGH);
 * String conciseDetails = printableObject.printToString(Detail.LOW);
 * printableObject.print(System.out, Detail.MEDIUM);
 * </pre>
 * </p>
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public interface Printable {

	/**
	 * The default level of detail used when no specific detail level is provided.
	 */
	Detail DEFAULT_DETAIL = Detail.DEFAULT;

	/**
	 * Prints the object to a string using the default detail level.
	 *
	 * @return a string representation of the object formatted with the default
	 *         detail level.
	 */
	default String printToString() {
		return printToString(DEFAULT_DETAIL);
	}

	/**
	 * Prints the object to a string using the specified detail level.
	 *
	 * @param detail the level of detail to use when formatting the output.
	 * @return a string representation of the object formatted with the specified
	 *         detail level.
	 */
	default String printToString(Detail detail) {
		StringBuilder b = new StringBuilder();
		try {
			print(b, detail);
			return b.toString();
		} catch (IOException e) {
			throw new IllegalStateException("An error occurred while printing to a string", e);
		}
	}

	/**
	 * Prints the object to the specified {@link Appendable} using the default
	 * detail level.
	 *
	 * @param out the appendable to which the formatted output will be written.
	 * @throws IOException if an I/O error occurs during writing.
	 */
	default void print(Appendable out) throws IOException {
		print(out, DEFAULT_DETAIL);
	}

	/**
	 * Prints the object to the specified {@link Appendable} using the given detail
	 * level.
	 *
	 * @param out    the appendable to which the formatted output will be written.
	 * @param detail the level of detail to use when formatting the output.
	 * @throws IOException if an I/O error occurs during writing.
	 */
	void print(Appendable out, Detail detail) throws IOException;
}
