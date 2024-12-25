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

import com.slytechs.jnet.platform.api.data.DataType;
import com.slytechs.jnet.platform.api.data.pipeline.impl.PipelineBase;

/**
 * Abstract base class for implementing custom data processing pipelines.
 * Provides the core pipeline infrastructure while allowing customization of
 * processing logic.
 *
 * <p>
 * Example custom implementation:
 * 
 * <pre>{@code
 * public class PacketPipeline extends Pipeline<Packet> {
 * 	public PacketPipeline() {
 * 		super("packet-pipeline", PacketType.INSTANCE);
 * 
 * 		// Add custom packet processors
 * 		addProcessor(new HeaderProcessor());
 * 		addProcessor(new PayloadProcessor());
 * 	}
 * }
 * }</pre>
 *
 * @param <T> Type of data flowing through pipeline
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc
 * @since 1.0.0
 */
public abstract class Pipeline<T> extends PipelineBase<T> implements DataPipeline<T> {

	/**
	 * Creates new pipeline.
	 *
	 * @param name     Pipeline identifier
	 * @param dataType Type of data processed
	 * @throws NullPointerException if name or dataType null
	 */
	protected Pipeline(String name, DataType<T> dataType) {
		super(name, dataType);
	}
}
