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
package com.slytechs.jnet.platform.api.pipeline;

/**
 * Event fired when an error occurs in the pipeline.
 */
public class PipelineErrorEvent extends PipelineEvent {
    
    private static final long serialVersionUID = 1L;
    
    private final Throwable error;
    private final ErrorSeverity severity;

    /**
     * Creates a new pipeline error event.
     *
     * @param source the source of the event
     * @param pipeline the pipeline where the error occurred
     * @param error the error that occurred
     * @param severity the severity of the error
     */
    public PipelineErrorEvent(Object source, Pipeline<?> pipeline, Throwable error, ErrorSeverity severity) {
        super(source, pipeline);
        this.error = error;
        this.severity = severity;
    }

    /**
     * Gets the error that occurred.
     *
     * @return the error
     */
    public Throwable getError() {
        return error;
    }

    /**
     * Gets the severity of the error.
     *
     * @return the error severity
     */
    public ErrorSeverity getSeverity() {
        return severity;
    }
}