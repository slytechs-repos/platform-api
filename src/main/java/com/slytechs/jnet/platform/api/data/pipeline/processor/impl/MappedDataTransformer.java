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
package com.slytechs.jnet.platform.api.data.pipeline.processor.impl;

import com.slytechs.jnet.platform.api.data.DataType;
import com.slytechs.jnet.platform.api.data.pipeline.transform.OutputTransformer;
import com.slytechs.jnet.platform.api.data.pipeline.transform.OutputTransformer.OutputMapper;

public class MappedDataTransformer<IN, OUT> extends OutputTransformer<IN, OUT> {

	public MappedDataTransformer(int priority, Object id, DataType<OUT> dataType, OutputMapper<IN, OUT> sink) {
		super(priority, id, dataType, sink);
	}

}