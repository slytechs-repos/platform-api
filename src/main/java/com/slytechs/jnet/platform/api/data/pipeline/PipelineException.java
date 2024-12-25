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
package com.slytechs.jnet.platform.api.data.pipeline;

/**
 * Base exception class for all pipeline-related exceptions.
 */
public class PipelineException extends RuntimeException {

	private static final long serialVersionUID = -6533678977309226567L;

	/**
	 * Constructs a new pipeline exception with the specified detail message.
	 *
	 * @param message the detail message
	 */
	public PipelineException(String message) {
		super(message);
	}

	/**
	 * Constructs a new pipeline exception with the specified detail message and
	 * cause.
	 *
	 * @param message the detail message
	 * @param cause   the cause
	 */
	public PipelineException(String message, Throwable cause) {
		super(message, cause);
	}
}