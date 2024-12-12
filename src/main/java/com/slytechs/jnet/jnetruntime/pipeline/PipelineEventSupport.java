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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.slytechs.jnet.jnetruntime.util.Registration;

/**
 * Support class for managing pipeline event listeners and firing events.
 */
public class PipelineEventSupport {
    
    private final List<PipelineListener> listeners = new CopyOnWriteArrayList<>();
    private final Pipeline<?> pipeline;
    
    public PipelineEventSupport(Pipeline<?> pipeline) {
        this.pipeline = pipeline;
    }
    
    /**
     * Adds a pipeline listener.
     *
     * @param listener the listener to add
     * @return a registration that can be used to remove the listener
     */
    public Registration addListener(PipelineListener listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }
    
    /**
     * Notifies listeners about a processor change.
     */
    public void fireProcessorChanged(Processor<?> processor, ProcessorEventType type) {
        ProcessorEvent evt = new ProcessorEvent(pipeline, pipeline, processor, type);
        for (PipelineListener listener : listeners) {
            try {
                listener.onProcessorChanged(evt);
            } catch (Exception e) {
                fireError(e, ErrorSeverity.WARNING);
            }
        }
    }
    
    /**
     * Notifies listeners about an attribute change.
     */
    public void fireAttributeChanged(String name, Object oldValue, Object newValue) {
        AttributeEvent evt = new AttributeEvent(pipeline, pipeline, name, oldValue, newValue);
        for (PipelineListener listener : listeners) {
            try {
                listener.onAttributeChanged(evt);
            } catch (Exception e) {
                fireError(e, ErrorSeverity.WARNING);
            }
        }
    }
    
    /**
     * Notifies listeners about an error.
     */
    public void fireError(Throwable error, ErrorSeverity severity) {
        PipelineErrorEvent evt = new PipelineErrorEvent(pipeline, pipeline, error, severity);
        for (PipelineListener listener : listeners) {
            try {
                listener.onError(evt);
            } catch (Exception e) {
                // Log secondary error but don't recursively fire events
                System.err.println("Error in error handler: " + e);
            }
        }
    }
}