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
package com.slytechs.jnet.platform.api.data.common.processor.impl;

import com.slytechs.jnet.platform.api.data.handler.ProcessingError;
import com.slytechs.jnet.platform.api.data.pipeline.ErrorPolicy;
import com.slytechs.jnet.platform.api.data.pipeline.ErrorSeverity;

/**
 * Interface defining error handling capabilities of a data-flow framework.
 */
public interface ErrorHandlingDataFlow {

	/**
	 * Gets the default error policy for the data-flow.
	 */
	ErrorPolicy getDefaultErrorPolicy();

	/**
	 * Handles a processing error according to the data-flow configuration.
	 */
	void handleProcessingError(ProcessingError error);

	void fireError(Throwable error, ErrorSeverity severity);
	
	boolean hasErrorListeners();
}