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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.slytechs.jnet.platform.api.data.common.processor.Processor;
import com.slytechs.jnet.platform.api.data.event.AttributeEvent;
import com.slytechs.jnet.platform.api.data.handler.ProcessorEvent;
import com.slytechs.jnet.platform.api.data.handler.ProcessorEventType;
import com.slytechs.jnet.platform.api.data.pipeline.impl.PipelineBase;
import com.slytechs.jnet.platform.api.util.Registration;

/**
 * Support class for managing PipelineBase event listeners and firing events.
 */
public class PipelineEventSupport {

	private final List<PipelineListener> listeners = new CopyOnWriteArrayList<>();
	private final PipelineBase<?> PipelineBase;
	private ErrorPolicy defaultErrorPolicy = ErrorPolicy.SUPPRESS;

	public PipelineEventSupport(PipelineBase<?> PipelineBase) {
		this.PipelineBase = PipelineBase;
	}

	public boolean isEmpty() {
		return listeners.isEmpty();
	}

	/**
	 * Gets the default error policy.
	 *
	 * @return the default error policy
	 */
	public ErrorPolicy getDefaultErrorPolicy() {
		return defaultErrorPolicy;
	}

	/**
	 * Adds a PipelineBase listener.
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
		ProcessorEvent evt = new ProcessorEvent(PipelineBase, PipelineBase, processor, type);
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
		AttributeEvent evt = new AttributeEvent(PipelineBase, PipelineBase, name, oldValue, newValue);
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
		PipelineErrorEvent evt = new PipelineErrorEvent(PipelineBase, PipelineBase, error, severity);
		for (PipelineListener listener : listeners) {
			try {
				listener.onError(evt);
			} catch (Exception e) {
				// Log secondary error but don't recursively fire events
				System.err.println("Error in error handler: " + e);
			}
		}
	}

	/**
	 * @param policy
	 */
	public void setDefaultErrorPolicy(ErrorPolicy policy) {
		this.defaultErrorPolicy = policy;
	}
}