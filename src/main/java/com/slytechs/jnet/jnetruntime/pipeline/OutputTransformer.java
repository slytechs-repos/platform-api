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

import java.util.function.Supplier;

import com.slytechs.jnet.jnetruntime.util.Enableable;
import com.slytechs.jnet.jnetruntime.util.Named;
import com.slytechs.jnet.jnetruntime.util.Prioritizable;
import com.slytechs.jnet.jnetruntime.util.Registration.AutoRegistration;

/**
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public abstract class OutputTransformer<IN, OUT>
		implements Transformer<IN, OUT>, Comparable<Prioritizable>, Prioritizable, Enableable, Named {

	public interface OutputMapper<IN, OUT> {

		interface SimpleOutputMapper<IN, OUT> extends OutputMapper<IN, OUT> {
			IN createMappedOutput(Supplier<OUT> sink);

			@Override
			default IN createMappedOutput(Supplier<OUT> sink, OutputTransformer<IN, OUT> output) {
				return createMappedOutput(sink);
			}

		}

		IN createMappedOutput(Supplier<OUT> sink, OutputTransformer<IN, OUT> output);
	}

	private final IN input;

	private OUT output;

	private int priority;

	Tail<IN> tail;

	private String name;

	private Object id;
	private boolean enabled;
	private final DataType<OUT> dataType;

	@SuppressWarnings("unchecked")
	public OutputTransformer(int priority, Object id, DataType<OUT> dataType) {
		this.priority = priority;
		this.name = Named.toName(id);
		this.id = id;
		this.dataType = dataType;
		this.input = (IN) this;
		this.output = dataType.empty();
	}

	public OutputTransformer(int priority, Object id, DataType<OUT> dataType, OutputMapper<IN, OUT> sink) {
		this.priority = priority;
		this.name = Named.toName(id);
		this.id = id;
		this.dataType = dataType;
		this.input = sink.createMappedOutput(this::getOutput, this);
		this.output = dataType.empty();
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Prioritizable o) {
		if (this.priority == o.priority())
			return 0;

		return this.priority < o.priority()
				? -1
				: 1;
	}

	public AutoRegistration connect(OUT out) {
		output = out;

		return disconnectRegistration;
	}

	/** Preallocated AutoCloseable registration that calls on disconnect */
	private final AutoRegistration disconnectRegistration = this::disconnect;

	public void disconnect() {
		output = null;
	}

	public DataType<OUT> dataType() {
		return dataType;
	}

	public final IN getInput() {
		return input;
	}

	public final OUT getOutput() {
		return output;
	}

	public final Object id() {
		return id;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.util.Enableable#isEnabled()
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
	 * @see com.slytechs.jnet.jnetruntime.util.Enableable#setEnable(boolean)
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

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name();
	}

}
