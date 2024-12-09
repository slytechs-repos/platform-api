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
public abstract class InputTransformer<IN, T> {

	public interface InputMapper<IN, T> {

		IN createMappedInput(Supplier<T> sink);
	}

	public interface InputFactory<IN, T, T_BASE extends InputTransformer<IN, T>> {
		T_BASE newInputTransformer(String name);
	}

	private IN input;
	private String name;
	private Object id;
	Head<T> head;

	public InputTransformer(String name) {
		this.name = name;
		this.id = name;
		this.input = (IN) this;
	}

	public InputTransformer(String name, InputMapper<IN, T> mapper) {
		this.name = name;
		this.id = name;
		this.input = mapper.createMappedInput(head::getOutput);
	}

	public InputTransformer(Object id) {
		this.name = getClass().getSimpleName();
		this.id = id;
		this.input = (IN) this;
	}

	public InputTransformer(Object id, InputMapper<IN, T> mapper) {
		this.name = getClass().getSimpleName();
		this.id = id;
		this.input = mapper.createMappedInput(head::getOutput);
	}

	public final void setName(String newName) {
		this.name = newName;
	}

	public final void setId(Object id) {
		this.id = id;
	}

	public final String name() {
		return name;
	}

	public final Object id() {
		return this.id;
	}

	public final IN getInput() {
		return input;
	}

	public final T getOutput() {
		return head.getOutput();
	}

}
