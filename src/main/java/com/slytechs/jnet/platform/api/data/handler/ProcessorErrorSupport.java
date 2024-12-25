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
package com.slytechs.jnet.platform.api.data.handler;

import com.slytechs.jnet.platform.api.data.common.processor.Processor;
import com.slytechs.jnet.platform.api.data.common.processor.impl.ErrorHandlingDataFlow;
import com.slytechs.jnet.platform.api.data.pipeline.ErrorPolicy;
import com.slytechs.jnet.platform.api.data.pipeline.ErrorSeverity;

/**
 * Support class for handling processor-specific errors.
 */
public class ProcessorErrorSupport {

	private final Processor<?> processor;
	private final ErrorHandlingDataFlow dataFlowHandler;
	private ErrorPolicy errorPolicy;

	public ProcessorErrorSupport(Processor<?> processor, ErrorHandlingDataFlow dataFlowErrorHandler) {
		this.processor = processor;
		this.dataFlowHandler = dataFlowErrorHandler;
	}

	/**
	 * Sets the error policy for this processor.
	 */
	public void setErrorPolicy(ErrorPolicy policy) {
		this.errorPolicy = policy;
	}

	/**
	 * Gets the error policy for this processor.
	 */
	public ErrorPolicy getErrorPolicy() {
		if (errorPolicy != null) {
			return errorPolicy;
		}
		return dataFlowHandler.getDefaultErrorPolicy();
	}

	/**
	 * Handles an error that occurred during processing.
	 */
	public void handleError(Throwable error, Object data, Object... args) {
		ProcessingError procError = new ProcessingError(
				error, processor, data, ErrorSeverity.ERROR, args);

		ErrorPolicy policy = getErrorPolicy();

		switch (policy) {
		case PROPAGATE:
			dataFlowHandler.handleProcessingError(procError);
			break;

		case SUPPRESS:
			// Just log it
			System.err.println("Suppressed error in processor: " + error);
			error.printStackTrace();
			break;

		case RETRY:
			try {
				processor.retryOnProcessingError(procError);
			} catch (Exception e) {
				// If retry fails, propagate
				dataFlowHandler.handleProcessingError(procError);
			}
			break;

		case TERMINATE:
			dataFlowHandler.handleProcessingError(
					new ProcessingError(error, processor, data, ErrorSeverity.FATAL));
			break;
		}
	}

	public boolean hasErrorListeners() {
		return dataFlowHandler.hasErrorListeners();
	}

	public void fireError(Throwable error, ErrorSeverity severity) {
		dataFlowHandler.fireError(error, severity);
	}
}