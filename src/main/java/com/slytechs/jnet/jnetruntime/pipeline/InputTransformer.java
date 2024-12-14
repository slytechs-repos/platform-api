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

import com.slytechs.jnet.jnetruntime.internal.util.function.FunctionalProxies;

/**
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public class InputTransformer<IN, T>
		implements Transformer<IN, T> {

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "<<" + name + ">>";
	}

	public interface InputMapper<IN, T> {
		public static abstract class GenericInputMapper<IN, T> implements InputMapper<IN, T> {

			private InputMapper<IN, T> proxy;

			public GenericInputMapper(InputMapper<IN, T> proxy) {
				this.proxy = proxy;
			}

			/**
			 * @see com.slytechs.jnet.jnetruntime.pipeline.InputTransformer.InputMapper#createMappedInput(java.util.function.Supplier)
			 */
			@Override
			public IN createMappedInput(Supplier<T> sink) {
				return proxy.createMappedInput(sink);
			}

		}

		IN createMappedInput(Supplier<T> sink);
	}

	public interface InputFactory<IN, T, T_BASE extends InputTransformer<IN, T>> {
		T_BASE newInputTransformer(String name);
	}

	IN input;
	private String name;
	private Object id;
	Head<T> head;
	private final DataType<IN> dataType;
	private final IN inline;

	@SuppressWarnings("unchecked")
	protected InputTransformer(String name) {
		this.dataType = DT.from(this);
		this.name = name;
		this.id = name;
		this.inline = (IN) this;
	}

	@SuppressWarnings("unchecked")
	protected InputTransformer(String name, DataType<IN> dataType) {
		this.name = name;
		this.id = name;
		this.inline = (IN) this;
		this.dataType = dataType;
	}

	protected InputTransformer(String name, InputMapper<IN, T> mapper) {
		this.dataType = DT.from(this);
		this.name = name;
		this.id = name;
		this.inline = mapper.createMappedInput(this::getOutput);
	}

	public InputTransformer(String name, DataType<IN> dataType, InputMapper<IN, T> mapper) {
		this.dataType = dataType;
		this.name = name;
		this.id = name;
		this.inline = mapper.createMappedInput(this::getOutput);
	}

	@SuppressWarnings("unchecked")
	protected InputTransformer(Object id) {
		this.dataType = DT.from(this);
		this.name = dataType.name();
		this.id = id;
		this.inline = (IN) this;
	}

	@SuppressWarnings("unchecked")
	protected InputTransformer(Object id, DataType<IN> dataType) {
		this.dataType = dataType;
		this.name = dataType.name();
		this.id = id;
		this.inline = (IN) this;
	}

	protected InputTransformer(Object id, InputMapper<IN, T> mapper) {
		this.dataType = DT.from(this);
		this.name = dataType.name();
		this.id = id;
		this.inline = mapper.createMappedInput(this::getOutput);
	}

	public InputTransformer(Object id, DataType<IN> dataType, InputMapper<IN, T> mapper) {
		this.dataType = dataType;
		this.name = dataType.name();
		this.id = id;
		this.inline = mapper.createMappedInput(this::getOutput);
	}

	void initializeHead(Head<T> head) {
		assert this.head == null : "input already connected to Head";

		this.head = head;

		/*
		 * Inject a functional interface proxy of type <IN> to read-lock on every
		 * forwarding call to the inline transformer
		 * 
		 * [user-dispatcher]->[read-lock-proxy]->[inline-transformer]->[pipe-processors]
		 * 
		 * The read-lock is for structural changes to the pipeline itself, not the data
		 * being processed. Any changes such as enable/disable/change on inputs,
		 * processors or outputs, will acquire a write-lock before making any changes to
		 * the pipeline structure or state. This allows timers and other threads to
		 * manipulate the state of the pipeline itself in a thread safe and coherent
		 * manner.
		 */
		var readLock = head.readLock;
		this.input = FunctionalProxies.createLockable(dataType.dataClass(), inline, readLock, this::handleError);
	}

	private void handleError(Throwable e) {
		head.handleError(e, null);
	}

	void clearHead() {
		this.head = null;
		this.input = null;
	}

	public final DataType<IN> dataType() {
		return dataType;
	}

	public final void setName(String newName) {
		this.name = newName;
	}

	public final void setId(Object id) {
		this.id = id;
	}

	@Override
	public final String name() {
		return name;
	}

	public final Object id() {
		return this.id;
	}

	public final IN getInput() {
		return input;
	}

	public final IN getInputPerma() {
		return getInput();
	}

	public final T getOutput() {
		assert head != null : "input not connected to pipeline";

		return head.getInput();
	}

}
