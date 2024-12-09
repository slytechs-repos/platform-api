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

/**
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public abstract class OutputTransformer<OUT, T> {

	public interface OutputFactory<IN, T, T_BASE extends OutputTransformer<IN, T>> {
		T_BASE newOutputTransformer(int priority, String name);
	}

	public interface OutputMapper<OUT, T> {

		T createMappedOutput(Supplier<OUT> sink);
	}

	private final T input;
	private OUT output;
	private int priority;
	private String name;
	private Object id;

	public OutputTransformer(int priority, String name) {
		this.priority = priority;
		this.name = name;
		this.id = name;
		this.input = (T) this;
	}

	public OutputTransformer(int priority, String name, OutputMapper<OUT, T> sink) {
		this.priority = priority;
		this.name = name;
		this.id = name;
		this.input = sink.createMappedOutput(this::getOutput);
	}

	public OutputTransformer(int priority, Object id) {
		this.priority = priority;
		this.name = getClass().getSimpleName();
		this.id = name;
		this.input = (T) this;
	}

	public OutputTransformer(int priority, Object id, OutputMapper<OUT, T> sink) {
		this.priority = priority;
		this.name = getClass().getSimpleName();
		this.id = name;
		this.input = sink.createMappedOutput(this::getOutput);
	}

	public final OUT getOutput() {
		return output;
	}

	public final void setName(String newName) {
		this.name = newName;
	}

	public final void setId(Object id) {
		this.id = id;
	}

	public final int priority() {
		return this.priority;
	}

	public final String name() {
		return this.name;
	}

	public final Object id() {
		return id;
	}

}
