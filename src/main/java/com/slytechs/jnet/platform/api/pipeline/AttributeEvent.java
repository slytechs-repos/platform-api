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
 * Event fired when a pipeline attribute changes.
 */
public class AttributeEvent extends PipelineEvent {
    
    private static final long serialVersionUID = 1L;
    
    private final String attributeName;
    private final Object oldValue;
    private final Object newValue;

    /**
     * Creates a new attribute event.
     *
     * @param source the source of the event
     * @param pipeline the pipeline where the event occurred
     * @param attributeName the name of the changed attribute
     * @param oldValue the old attribute value
     * @param newValue the new attribute value
     */
    public AttributeEvent(Object source, Pipeline<?> pipeline, String attributeName, 
            Object oldValue, Object newValue) {
        super(source, pipeline);
        this.attributeName = attributeName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * Gets the name of the changed attribute.
     *
     * @return the attribute name
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * Gets the old value of the attribute.
     *
     * @return the old value
     */
    public Object getOldValue() {
        return oldValue;
    }

    /**
     * Gets the new value of the attribute.
     *
     * @return the new value
     */
    public Object getNewValue() {
        return newValue;
    }
}