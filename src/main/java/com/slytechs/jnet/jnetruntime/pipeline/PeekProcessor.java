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

import static com.slytechs.jnet.jnetruntime.util.SystemProperties.boolValue;

import com.slytechs.jnet.jnetruntime.NotFound;
import com.slytechs.jnet.jnetruntime.pipeline.DataProcessor.ProcessorFactory;
import com.slytechs.jnet.jnetruntime.pipeline.DataType.DataSupport;

/**
 * @author Mark Bednarczyk
 *
 */
public class PeekProcessor<T>
		extends AbstractProcessor<T, PeekProcessor<T>> 
		implements ProcessorFactory<T, PeekProcessor<T>>{
		
	/** System property which enables/disables the {@code PeekProcessor} (default is true). */
	public static final String PROPERTY_PEEK_PROCESSOR_ENABLE = "peek.processor.enable";

	/** System property which bypasses the {@code PeekProcessor} (default is false). */
	public static final String PROPERTY_PEEK_PROCESSOR_BYPASS = "peek.processor.bypass";
	
	public static final String NAME = "Peek";
	
	private final boolean envEnable = boolValue(PROPERTY_PEEK_PROCESSOR_ENABLE, true);
	private final boolean envBypass = boolValue(PROPERTY_PEEK_PROCESSOR_BYPASS, false);
	
	/**
	 * @param pipeline
	 * @param priority
	 * @param name
	 * @param type
	 */
	private PeekProcessor(Pipeline<T, ?> pipeline, int priority) {
		super(pipeline, priority, NAME, pipeline.dataType(), null);

		/*
		 * Peek processor forwards all input data to output so add update both
		 * outputData and inputData. This way they will both
		 * be automatically updated to the latest output changes.
		 */
		super.outputList.addChangeListener(this::inputData);
		
		enable(envEnable);
		bypass(envBypass);
	}
	
	public PeekProcessor(Class<T> dataClass) throws NotFound {
		super(0, NAME, DataSupport.lookupClass(dataClass));
	}
	
	public PeekProcessor(DataType dataType) throws NotFound {
		super(0, NAME, dataType);
	}

	public PeekProcessor<T> peek(T peekAction) {
		addToOutputList(peekAction);

		return this;
	}

	@Override
	public PeekProcessor<T> newProcessor(Pipeline<T, ?> parent, int priority) {
		return new PeekProcessor<>(parent, priority);
	}

}
