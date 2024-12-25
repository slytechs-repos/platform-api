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
package com.slytechs.jnet.platform.api.data.pipeline.transform.impl;

import com.slytechs.jnet.platform.api.data.DataType;
import com.slytechs.jnet.platform.api.data.pipeline.Transformer;
import com.slytechs.jnet.platform.api.data.pipeline.processor.Tail;
import com.slytechs.jnet.platform.api.data.pipeline.transform.OutputTransformer;
import com.slytechs.jnet.platform.api.data.pipeline.transform.OutputTransformer.OutputMapper;
import com.slytechs.jnet.platform.api.util.Enableable;
import com.slytechs.jnet.platform.api.util.Named;
import com.slytechs.jnet.platform.api.util.Prioritizable;

/**
 * @param <IN>
 * @param <OUT>
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public class OutputTransformerBase<IN, OUT>
		implements Transformer<IN, OUT>, Comparable<Prioritizable>, Prioritizable.LowToHigh, Enableable, Named {

	protected final IN input;
	protected OUT output;
	private int priority;
	private String name;
	private Object id;
	private boolean enabled;
	protected final DataType<OUT> dataType;
	private final OutputTransformer<IN, OUT> us = (OutputTransformer<IN, OUT>) this;

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public OutputTransformerBase(int priority, Object id, DataType<OUT> dataType) {
		super();
		this.input = (IN) this;
		this.dataType = dataType;
		this.name = Named.toName(id);
		this.id = id;
		this.output = dataType.empty();
	}

	public OutputTransformerBase(int priority, Object id, DataType<OUT> dataType, OutputMapper<IN, OUT> mapper) {
		super();
		this.dataType = dataType;
		this.name = Named.toName(id);
		this.id = id;
		this.output = dataType.empty();
		this.input = mapper.createMappedOutput(this::getOutput, us);
	}

	public void setParent(Tail<IN> tail) {
	}

	public DataType<OUT> dataType() {
		return dataType;
	}

	public final Object id() {
		return id;
	}

	public final IN getInput() {
		return input;
	}

	public final OUT getOutput() {
		return output;
	}

	/**
	 * @see com.slytechs.jnet.platform.api.util.Enableable#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public final String name() {
		return this.name;
	}

	@Override
	public final int priority() {
		return this.priority;
	}

	/**
	 * @see com.slytechs.jnet.platform.api.util.Enableable#setEnable(boolean)
	 */
	@Override
	public void setEnable(boolean newState) {
		if (this.enabled == newState)
			return;

		this.enabled = newState;

		onEnableChange(newState);
	}

	public final void setId(Object id) {
		this.id = id;
	}

	@Override
	public final void setName(String newName) {
		this.name = newName;
	}

}