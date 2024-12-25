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
package com.slytechs.jnet.platform.api.data.common.processor;

import java.util.function.Consumer;

import com.slytechs.jnet.platform.api.data.DataProcessor;
import com.slytechs.jnet.platform.api.data.common.processor.impl.ProcessorBase;
import com.slytechs.jnet.platform.api.util.Enableable.FluentEnableable;
import com.slytechs.jnet.platform.api.util.Prioritizable;
import com.slytechs.jnet.platform.api.util.Registration;

/**
 * The Class Processor.
 *
 * @param <T> the generic type
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public abstract class Processor<T>
		extends ProcessorBase<T>
		implements FluentEnableable<DataProcessor<T>>, Prioritizable.LowToHigh {

	/**
	 * Creates processor with specified priority and name.
	 *
	 * @param priority Processing priority (0-100, higher executes first)
	 * @param name     Unique processor name
	 * @throws IllegalArgumentException if priority out of range or name null
	 */
	protected Processor(int priority, String name) {
		super(priority, name);
	}

	/**
	 * Creates processor with priority, name and data mapping logic.
	 *
	 * @param priority Processing priority (0-100, higher executes first)
	 * @param name     Unique processor name
	 * @param mapper   Data mapping implementation
	 * @throws IllegalArgumentException if any parameter null or priority invalid
	 */
	protected Processor(int priority, String name, ProcessorMapper<T> mapper) {
		super(priority, name, mapper);
	}

	/**
	 * Creates processor with priority, name and inline data. Inline data is
	 * processed directly without mapping.
	 *
	 * @param priority   Processing priority (0-100, higher executes first)
	 * @param name       Unique processor name
	 * @param inlineData Data to process directly
	 * @throws IllegalArgumentException if any parameter null or priority invalid
	 */
	protected Processor(int priority, String name, T inlineData) {
		super(priority, name, inlineData);
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.common.processor.impl.ProcessorBase#handleError(java.lang.Throwable,
	 *      java.lang.Object)
	 */
	@Override
	public void handleError(Throwable error, Object data) {
		super.handleError(error, data);
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.common.processor.impl.ProcessorBase#getInput()
	 */
	@Override
	public T getInput() {
		return super.getInput();
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.common.processor.impl.ProcessorBase#getOutput()
	 */
	@Override
	public T getOutput() {
		return super.getOutput();
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.DataProcessor#peek(java.lang.Object)
	 */
	@Override
	public DataProcessor<T> peek(T newPeeker) {
		return peek(newPeeker, r -> {});
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.common.processor.impl.ProcessorBase#peek(java.lang.Object,
	 *      java.util.function.Consumer)
	 */
	@Override
	public DataProcessor<T> peek(T newPeeker, Consumer<Registration> registrar) {
		return super.peek(newPeeker, registrar);
	}

	/**
	 * To string.
	 *
	 * @return the string
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name + ":" + priority;
	}

}
