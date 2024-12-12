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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.slytechs.jnet.jnetruntime.util.Registration;

/**
 * Support class for managing pipeline error handling.
 */
public class PipelineErrorSupport {
    
    private final List<ProcessingErrorHandler> errorHandlers = new ArrayList<>();
    private final PipelineEventSupport eventSupport;
    private ErrorPolicy defaultErrorPolicy = ErrorPolicy.PROPAGATE;
    
    public PipelineErrorSupport(PipelineEventSupport eventSupport) {
        this.eventSupport = eventSupport;
    }
    
    /**
     * Adds an error handler.
     *
     * @param handler the error handler to add
     * @return a registration that can be used to remove the handler
     */
    public Registration addErrorHandler(ProcessingErrorHandler handler) {
        errorHandlers.add(handler);
        return () -> errorHandlers.remove(handler);
    }
    
    /**
     * Sets the default error policy.
     */
    public void setDefaultErrorPolicy(ErrorPolicy policy) {
        this.defaultErrorPolicy = policy;
    }
    
    /**
     * Gets the default error policy.
     */
    public ErrorPolicy getDefaultErrorPolicy() {
        return defaultErrorPolicy;
    }
    
    /**
     * Gets an unmodifiable list of error handlers.
     */
    public List<ProcessingErrorHandler> getErrorHandlers() {
        return Collections.unmodifiableList(errorHandlers);
    }
    
    /**
     * Handles a processing error according to the current policy.
     */
    public void handleProcessingError(ProcessingError error) {
        // First try specific handlers
        for (ProcessingErrorHandler handler : errorHandlers) {
            try {
                if (handler.canHandle(error)) {
                    handler.handleError(error);
                    return;
                }
            } catch (Exception e) {
                eventSupport.fireError(e, ErrorSeverity.WARNING);
            }
        }
        
        // Apply default policy if no handler took care of it
        switch (defaultErrorPolicy) {
            case PROPAGATE:
                eventSupport.fireError(error.getCause(), error.getSeverity());
                break;
                
            case SUPPRESS:
                // Just log it
                System.err.println("Suppressed error in pipeline: " + error.getCause());
                break;
                
            case RETRY:
                // Implementation would be provided by specific pipeline type
                break;
                
            case TERMINATE:
                eventSupport.fireError(error.getCause(), ErrorSeverity.FATAL);
                break;
        }
    }
}