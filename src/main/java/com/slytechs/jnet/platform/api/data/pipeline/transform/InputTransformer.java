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
package com.slytechs.jnet.platform.api.data.pipeline.transform;

import com.slytechs.jnet.platform.api.data.DataType;
import com.slytechs.jnet.platform.api.data.pipeline.Transformer;
import com.slytechs.jnet.platform.api.util.Named;

/**
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public class InputTransformer<IN, T>
		extends InputTransformerBase<IN, T>
		implements Transformer<IN, T>, Named {

	protected InputTransformer(Object id) {
		super(id);
	}

	protected InputTransformer(Object id, DataType<IN> dataType) {
		super(id, dataType);
	}

	public InputTransformer(Object id, DataType<IN> dataType, InputMapper<IN, T> mapper) {
		super(id, dataType, mapper);
	}

	protected InputTransformer(Object id, InputMapper<IN, T> mapper) {
		super(id, mapper);
	}

	protected InputTransformer(String name) {
		super(name);
	}

	protected InputTransformer(String name, DataType<IN> dataType) {
		super(name, dataType);
	}

	public InputTransformer(String name, DataType<IN> dataType, InputMapper<IN, T> mapper) {
		super(name, dataType, mapper);
	}

	protected InputTransformer(String name, InputMapper<IN, T> mapper) {
		super(name, mapper);
	}

}
