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
 * A formatter interface for generating detailed string representations of
 * objects. Implementations of this interface are responsible for formatting a
 * target object and writing the output to an {@link Appendable}, such as a
 * {@link StringBuilder} or a {@link Writer}.
 *
 * <p>
 * This interface provides flexibility for creating formatted, detailed, and
 * customized representations of objects based on the {@link Detail} level
 * provided. The level of detail may vary, supporting use cases ranging from
 * simple summaries to verbose debug outputs.
 * </p>
 *
 * <h2>Usage Example:</h2>
 * 
 * <pre>{@code
 * DetailFormatter formatter = ...; // Obtain a formatter implementation
 * StringBuilder output = new StringBuilder();
 * MyObject obj = new MyObject();
 * Detail detail = Detail.VERBOSE;
 *
 * formatter.formatTo(output, obj, detail);
 *
 * System.out.println(output.toString());
 * }</pre>
 * 
 * <h2>Key Features:</h2>
 * <ul>
 * <li>Supports multiple levels of detail for formatting, as determined by
 * {@link Detail}.</li>
 * <li>Enables output to any {@link Appendable}, such as a
 * {@link StringBuilder}, {@link Writer}, or {@link StringBuffer}.</li>
 * <li>Allows for customization based on object types and application-specific
 * needs.</li>
 * </ul>
 *
 * @param <T> The type of the object to be formatted.
 * 
 * @author Mark Bednarczyk
 * @author Sly Technologies Inc.
 * 
 * @see Appendable
 * @see Detail
 */
public interface DetailFormatter {

	/**
	 * Returns the default detail level used when none is specified.
	 *
	 * @return the default {@link Detail} level.
	 */
	Detail getDefaultDetail();

	/**
	 * Formats the specified target object into a string representation using the
	 * default {@link Detail} level.
	 *
	 * <p>
	 * This method simplifies formatting by omitting the explicit {@link Detail}
	 * parameter. It uses the default detail level provided by the
	 * {@link #getDefaultDetail()} method.
	 * </p>
	 *
	 * <h2>Usage Example:</h2>
	 * 
	 * <pre>{@code
	 * DetailFormatter formatter = ...; // Obtain a formatter implementation
	 * MyObject obj = new MyObject();
	 *
	 * String formattedOutput = formatter.formatToString(obj);
	 * System.out.println(formattedOutput);
	 * }</pre>
	 *
	 * <h2>Features:</h2>
	 * <ul>
	 * <li>Convenient default formatting method.</li>
	 * <li>Delegates to {@link #formatToString(Object, Detail)} with the default
	 * detail level.</li>
	 * </ul>
	 *
	 * @param target the object to be formatted; cannot be {@code null}.
	 * @return a {@link String} containing the formatted representation of the
	 *         object using the default {@link Detail} level.
	 * @throws NullPointerException if {@code target} is {@code null}.
	 * @see #formatToString(Object, Detail)
	 * @see #getDefaultDetail()
	 * @see Detail
	 */
	default String formatToString(Object target) {
		return formatToString(target, getDefaultDetail());
	}

	/**
	 * Formats the specified target object into a detailed string representation
	 * based on the provided {@link Detail} level.
	 *
	 * <p>
	 * This method creates a {@link StringBuilder}, delegates the formatting to
	 * {@link #formatTo(Appendable, Object, Detail)}, and returns the result as a
	 * {@link String}. It ensures that any {@link IOException} encountered during
	 * the formatting process is wrapped and rethrown as an
	 * {@link IllegalStateException}.
	 * </p>
	 *
	 * <h2>Usage Example:</h2>
	 * 
	 * <pre>{@code
	 * DetailFormatter formatter = ...; // Obtain a formatter implementation
	 * MyObject obj = new MyObject();
	 * Detail detail = Detail.HIGH;
	 *
	 * String formattedOutput = formatter.formatToString(obj, detail);
	 * System.out.println(formattedOutput);
	 * }</pre>
	 *
	 * <h2>Features:</h2>
	 * <ul>
	 * <li>Provides a convenient way to format an object directly into a
	 * string.</li>
	 * <li>Automatically manages the {@link Appendable} creation process
	 * internally.</li>
	 * <li>Handles exceptions gracefully, ensuring a consistent programming
	 * interface.</li>
	 * </ul>
	 *
	 * @param target the object to be formatted; cannot be {@code null}.
	 * @param detail the {@link Detail} level dictating the verbosity of the output;
	 *               cannot be {@code null}.
	 * @return a {@link String} containing the formatted representation of the
	 *         object.
	 * @throws NullPointerException  if {@code target} or {@code detail} is
	 *                               {@code null}.
	 * @throws IllegalStateException if an {@link IOException} occurs during the
	 *                               formatting process.
	 * @see #formatTo(Appendable, Object, Detail)
	 * @see Detail
	 */
	default String formatToString(Object target, Detail detail) {
		var sb = new StringBuilder();

		try {
			formatTo(sb, target, detail);
		} catch (IOException e) {
			throw new IllegalStateException("An error occurred while formatting to a string", e);
		}

		return sb.toString();
	}

	/**
	 * Formats the specified target object into a detailed string representation and
	 * appends the result to the provided {@link Appendable}.
	 *
	 * @param out    the {@link Appendable} to which the formatted output is
	 *               written; cannot be {@code null}.
	 * @param target the object to be formatted; cannot be {@code null}.
	 * @param detail the {@link Detail} level dictating the verbosity of the output;
	 *               cannot be {@code null}.
	 * @throws NullPointerException     if {@code out}, {@code target}, or
	 *                                  {@code detail} is {@code null}.
	 * @throws IOException              if an I/O error occurs while writing to the
	 *                                  {@link Appendable}.
	 * @throws IllegalArgumentException if the target object is incompatible with
	 *                                  the formatter's capabilities.
	 */
	void formatTo(Appendable out, Object target, Detail detail) throws IOException;

	/**
	 * Formats the specified target object using the default {@link Detail} level
	 * and appends the result to the provided {@link Appendable}.
	 *
	 * @param out    the {@link Appendable} to which the formatted output is
	 *               written; cannot be {@code null}.
	 * @param target the object to be formatted; cannot be {@code null}.
	 * @throws NullPointerException     if {@code out} or {@code target} is
	 *                                  {@code null}.
	 * @throws IOException              if an I/O error occurs while writing to the
	 *                                  {@link Appendable}.
	 * @throws IllegalArgumentException if the target object is incompatible with
	 *                                  the formatter's capabilities.
	 */
	default void formatTo(Appendable out, Object target) throws IOException {
		formatTo(out, target, getDefaultDetail());
	}
}
