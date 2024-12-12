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
 * Exception thrown when there are problems with pipeline attributes.
 */
class PipelineAttributeException extends PipelineException {
    
    private static final long serialVersionUID = -3355958897195702593L;

    /**
     * Constructs a new pipeline attribute exception with the specified detail message.
     *
     * @param message the detail message
     */
    public PipelineAttributeException(String message) {
        super(message);
    }

    /**
     * Constructs a new pipeline attribute exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public PipelineAttributeException(String message, Throwable cause) {
        super(message, cause);
    }
}