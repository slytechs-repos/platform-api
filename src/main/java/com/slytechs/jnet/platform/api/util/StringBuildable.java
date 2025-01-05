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

import com.slytechs.jnet.platform.api.util.format.Detail;

/**
 * Standard interface for building up toString fragments from complex objects.
 *
 * @author Mark Bednarczyk
 */
public interface StringBuildable {
	
	/** The default detail. */
	Detail DEFAULT_DETAIL = Detail.SUMMARY;

	/**
	 * Builds up a string from multiple string components or parts.
	 *
	 * @param b      the string builder
	 * @param detail TODO
	 * @return typically same builder 'b' that was passed, but also potentially a
	 *         new builder that contains the string buffer data and to be used for
	 *         the next step in a multi-step string building process.
	 */
	StringBuilder buildString(StringBuilder b, Detail detail);

	/**
	 * Builds the string.
	 *
	 * @return the string
	 */
	default String buildString() {
		return buildString(Detail.DEFAULT);
	}

	/**
	 * Builds the string.
	 *
	 * @param detail the detail
	 * @return the string
	 */
	default String buildString(Detail detail) {
		return buildString(new StringBuilder(), detail)
				.toString();
	}
}
