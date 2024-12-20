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
package com.slytechs.jnet.platform.api.pipeline;

import java.util.function.Supplier;

import com.slytechs.jnet.platform.api.internal.util.function.FunctionalProxies;
import com.slytechs.jnet.platform.api.util.Named;

/**
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public class InputTransformer<IN, T>
		implements Transformer<IN, T>, Named {

	public interface InputMapper<IN, OUT> {
		public static abstract class GenericInputMapper<IN, T> implements InputMapper<IN, T> {

			private InputMapper<IN, T> proxy;

			public GenericInputMapper(InputMapper<IN, T> proxy) {
				this.proxy = proxy;
			}

			/**
			 * @see com.slytechs.jnet.platform.api.pipeline.InputTransformer.InputMapper#createMappedInput(java.util.function.Supplier)
			 */
			@Override
			public IN createMappedInput(Supplier<T> sink, InputTransformer<IN, T> input) {
				return proxy.createMappedInput(sink, input);
			}

		}

		interface SimpleInputMapper<IN, OUT> extends InputMapper<IN, OUT> {
			IN createMappedInput(Supplier<OUT> sink);

			@Override
			default IN createMappedInput(Supplier<OUT> sink, InputTransformer<IN, OUT> input) {
				return createMappedInput(sink);
			}

		}

		IN createMappedInput(Supplier<OUT> sink, InputTransformer<IN, OUT> input);
	}

	IN input;

	private String name;
	private Object id;
	Head<T> head;
	private final DataType<IN> dataType;
	private final IN inline;
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

	public InputTransformer(Object id, DataType<IN> dataType, InputMapper<IN, T> mapper) {
		this.dataType = dataType;
		this.name = dataType.name();
		this.id = id;
		this.inline = mapper.createMappedInput(this::getOutput, this);
	}

	protected InputTransformer(Object id, InputMapper<IN, T> mapper) {
		this.dataType = DT.from(this);
		this.name = dataType.name();
		this.id = id;
		this.inline = mapper.createMappedInput(this::getOutput, this);
	}

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

	public InputTransformer(String name, DataType<IN> dataType, InputMapper<IN, T> mapper) {
		this.dataType = dataType;
		this.name = name;
		this.id = name;
		this.inline = mapper.createMappedInput(this::getOutput, this);
	}

	protected InputTransformer(String name, InputMapper<IN, T> mapper) {
		this.dataType = DT.from(this);
		this.name = name;
		this.id = name;
		this.inline = mapper.createMappedInput(this::getOutput, this);
	}

	void clearHead() {
		this.head = null;
		this.input = null;
	}

	public final DataType<IN> dataType() {
		return dataType;
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

	private void handleError(Throwable e) {
		head.handleError(e, null);
	}

	public final Object id() {
		return this.id;
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

	@Override
	public final String name() {
		return name;
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
		return name;
	}

}
