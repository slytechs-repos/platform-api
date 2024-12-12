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
package com.slytechs.jnet.jnetruntime.pipeline;

/**
 * Encapsulates information about an error that occurred during data processing.
 */
public class ProcessingError {
	private final Throwable cause;
	private final Processor<?> sourceProcessor;
	private final Object failedData;
	private final Object[] invokationArgs; // processor invokation args for functional interface <T> 
	private final ErrorSeverity severity;

	/**
	 * Creates a new processing error.
	 *
	 * @param cause           the underlying cause of the error
	 * @param sourceProcessor the processor where the error occurred
	 * @param failedData      the data that was being processed when the error
	 *                        occurred
	 * @param severity        the severity of the error
	 */
	public ProcessingError(Throwable cause, Processor<?> sourceProcessor,
			Object failedData, ErrorSeverity severity, Object... args) {
		this.cause = cause;
		this.sourceProcessor = sourceProcessor;
		this.failedData = failedData;
		this.severity = severity;
		this.invokationArgs = args;
	}

	/**
	 * Gets the underlying cause of the error.
	 *
	 * @return the cause
	 */
	public Throwable getCause() {
		return cause;
	}

	/**
	 * Gets the processor where the error occurred.
	 *
	 * @return the source processor
	 */
	public Processor<?> getSourceProcessor() {
		return sourceProcessor;
	}

	/**
	 * Gets the data that was being processed when the error occurred.
	 *
	 * @return the failed data
	 */
	public Object getFailedData() {
		return failedData;
	}

	/**
	 * Gets the severity of the error.
	 *
	 * @return the error severity
	 */
	public ErrorSeverity getSeverity() {
		return severity;
	}

	public Object[] invokationArgs() {
		return invokationArgs;
	}
}