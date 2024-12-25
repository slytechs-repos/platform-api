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
import com.slytechs.jnet.platform.api.data.pipeline.PipelineEvent;
import com.slytechs.jnet.platform.api.data.pipeline.impl.PipelineBase;

/**
 * Event fired when a processor is added or removed from the pipeline.
 */
public class ProcessorEvent extends PipelineEvent {

	private static final long serialVersionUID = 1L;

	private final Processor<?> processor;
	private final ProcessorEventType type;

	/**
	 * Creates a new processor event.
	 *
	 * @param source    the source of the event
	 * @param pipeline  the pipeline where the event occurred
	 * @param processor the processor involved in the event
	 * @param type      the type of processor event
	 */
	public ProcessorEvent(Object source, PipelineBase<?> pipeline, Processor<?> processor, ProcessorEventType type) {
		super(source, pipeline);
		this.processor = processor;
		this.type = type;
	}

	/**
	 * Gets the processor involved in this event.
	 *
	 * @return the processor
	 */
	public Processor<?> getProcessor() {
		return processor;
	}

	/**
	 * Gets the type of processor event.
	 *
	 * @return the event type
	 */
	public ProcessorEventType getType() {
		return type;
	}
}