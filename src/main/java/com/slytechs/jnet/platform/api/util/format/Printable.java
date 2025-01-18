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
import java.io.Writer;

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
 * String conciseDetails = printableObject.printToString(Detail.SUMMARY);
 * printableObject.printTo(System.out, Detail.MEDIUM);
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
			printTo(b, detail);
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
	default void printTo(Appendable out) throws IOException {
		printTo(out, DEFAULT_DETAIL);
	}

	/**
	 * Prints the object to the specified {@link Appendable} using the given detail
	 * level.
	 *
	 * @param out    the appendable to which the formatted output will be written.
	 * @param detail the level of detail to use when formatting the output.
	 * @throws IOException if an I/O error occurs during writing.
	 */
	void printTo(Appendable out, Detail detail) throws IOException;

	/**
	 * Prints the formatted representation of this object to the specified
	 * {@link Appendable} using a custom {@link DetailFormatter} and a specified
	 * {@link Detail} level.
	 *
	 * <p>
	 * This method provides a flexible way to output a formatted representation of
	 * the object by delegating the actual formatting logic to the provided
	 * {@link DetailFormatter}. The level of detail in the output is controlled by
	 * the {@link Detail} parameter, making this suitable for different verbosity
	 * requirements.
	 * </p>
	 *
	 * <h2>Usage Example:</h2>
	 * 
	 * <pre>{@code
	 * StringBuilder builder = new StringBuilder();
	 * MyObject myObject = new MyObject();
	 * DetailFormatter formatter = new CustomFormatter();
	 *
	 * myObject.printToFormatted(builder, formatter, Detail.DETAILED);
	 * System.out.println(builder.toString());
	 * }</pre>
	 *
	 * <h2>Parameters:</h2>
	 * <ul>
	 * <li>{@code out} - The {@link Appendable} to which the formatted output is
	 * written. This can be a {@link StringBuilder}, {@link Writer}, or any other
	 * compatible {@link Appendable}.</li>
	 * <li>{@code formatter} - The {@link DetailFormatter} responsible for
	 * generating the formatted output. This defines the structure and style of the
	 * output.</li>
	 * <li>{@code detail} - The {@link Detail} level that controls the verbosity of
	 * the output. Examples include basic, detailed, and verbose levels.</li>
	 * </ul>
	 *
	 * <h2>Throws:</h2>
	 * <ul>
	 * <li>{@link IOException} if an I/O error occurs while writing to the
	 * {@link Appendable}.</li>
	 * <li>{@link NullPointerException} if {@code formatter} or {@code detail} is
	 * {@code null}.</li>
	 * </ul>
	 *
	 * @param out       the {@link Appendable} to which the formatted output is
	 *                  written
	 * @param formatter the {@link DetailFormatter} responsible for generating the
	 *                  formatted output
	 * @param detail    the {@link Detail} level controlling the verbosity of the
	 *                  output
	 * @throws IOException          if an I/O error occurs while writing to the
	 *                              {@link Appendable}
	 * @throws NullPointerException if {@code formatter} or {@code detail} is
	 *                              {@code null}
	 * @see DetailFormatter
	 * @see Detail
	 */
	default void printToFormatted(Appendable out, DetailFormatter formatter, Detail detail) throws IOException {
		formatter.formatTo(out, this, detail);
	}

	/**
	 * Prints the object to a string using the specified detail level.
	 *
	 * @param detail the level of detail to use when formatting the output.
	 * @return a string representation of the object formatted with the specified
	 *         detail level.
	 */
	default void printToStdout(Detail detail) {
		try {
			printTo(System.out, detail);
		} catch (IOException e) {
			throw new IllegalStateException("An error occurred while printing to a string", e);
		}
	}

	/**
	 * Prints the object to a string using the specified detail level.
	 *
	 * @param detail the level of detail to use when formatting the output.
	 * @return a string representation of the object formatted with the specified
	 *         detail level.
	 */
	default void printToStderr(Detail detail) {
		try {
			printTo(System.err, detail);
		} catch (IOException e) {
			throw new IllegalStateException("An error occurred while printing to a string", e);
		}
	}

	/**
	 * Prints the object to a string using the specified detail level.
	 *
	 * @param detail the level of detail to use when formatting the output.
	 * @return a string representation of the object formatted with the specified
	 *         detail level.
	 */
	default void printlnToStdout(Detail detail) {
		try {
			printTo(System.out, detail);
			System.out.println();
		} catch (IOException e) {
			throw new IllegalStateException("An error occurred while printing to a string", e);
		}
	}

	/**
	 * Prints the object to a string using the specified detail level.
	 *
	 * @param detail the level of detail to use when formatting the output.
	 * @return a string representation of the object formatted with the specified
	 *         detail level.
	 */
	default void printlnToStderr(Detail detail) {
		try {
			printTo(System.err, detail);
			System.err.println();
		} catch (IOException e) {
			throw new IllegalStateException("An error occurred while printing to a string", e);
		}
	}
}
