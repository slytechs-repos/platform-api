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
 * Interface for handling errors during data processing.
 */
public interface ProcessingErrorHandler {
    
    /**
     * Checks if this handler can handle the given error.
     *
     * @param error the error to check
     * @return true if this handler can handle the error, false otherwise
     */
    boolean canHandle(ProcessingError error);
    
    /**
     * Handles the given processing error.
     *
     * @param error the error to handle
     * @throws Exception if error handling fails
     */
    void handleError(ProcessingError error) throws Exception;
}