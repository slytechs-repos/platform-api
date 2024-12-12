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

import java.util.EventObject;

/**
 * Base class for all pipeline-related events.
 */
public class PipelineEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	private final Pipeline<?> pipeline;
	private final long timestamp;

	/**
	 * Creates a new pipeline event.
	 *
	 * @param source   the source of the event
	 * @param pipeline the pipeline where the event occurred
	 */
	public PipelineEvent(Object source, Pipeline<?> pipeline) {
		super(source);
		this.pipeline = pipeline;
		this.timestamp = System.currentTimeMillis();
	}

	/**
	 * Gets the pipeline where the event occurred.
	 *
	 * @return the pipeline
	 */
	public Pipeline<?> getPipeline() {
		return pipeline;
	}

	/**
	 * Gets the timestamp when this event was created.
	 *
	 * @return the timestamp in milliseconds
	 */
	public long getTimestamp() {
		return timestamp;
	}
}