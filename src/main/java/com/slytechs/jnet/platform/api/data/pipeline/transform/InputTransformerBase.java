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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.slytechs.jnet.platform.api.data.DataLiteral;
import com.slytechs.jnet.platform.api.data.DataType;
import com.slytechs.jnet.platform.api.data.pipeline.Transformer;
import com.slytechs.jnet.platform.api.util.Named;
import com.slytechs.jnet.platform.api.util.function.FunctionalProxies;

/**
 * @param <IN>
 * @param <T>
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public class InputTransformerBase<IN, T>
		implements Transformer<IN, T>, Named {

	IN input;
	protected String name;
	protected Object id;
	protected final DataType<IN> dataType;
	protected final IN inline;
	protected Lock readLock;

	private TransformerParent<T> parent;

	@SuppressWarnings("unchecked")
	protected InputTransformerBase(Object id) {
		this.dataType = DataLiteral.from(id);
		this.name = dataType.name();
		this.id = id;
		this.inline = (IN) this;
	}

	@SuppressWarnings("unchecked")
	protected InputTransformerBase(Object id, DataType<IN> dataType) {
		this.dataType = dataType;
		this.name = dataType.name();
		this.id = id;
		this.inline = (IN) this;
	}

	public InputTransformerBase(Object id, DataType<IN> dataType, InputMapper<IN, T> mapper) {
		this.dataType = dataType;
		this.name = dataType.name();
		this.id = id;
		this.inline = mapper.createMappedInput(this::getOutput, this);
	}

	protected InputTransformerBase(Object id, InputMapper<IN, T> mapper) {
		this.dataType = DataLiteral.from(id);
		this.name = dataType.name();
		this.id = id;
		this.inline = mapper.createMappedInput(this::getOutput, this);
	}

	@SuppressWarnings("unchecked")
	protected InputTransformerBase(String name) {
		this.dataType = DataLiteral.from(this);
		this.name = name;
		this.id = name;
		this.inline = (IN) this;
	}

	@SuppressWarnings("unchecked")
	protected InputTransformerBase(String name, DataType<IN> dataType) {
		this.dataType = dataType;
		this.name = name;
		this.id = name;
		this.inline = (IN) this;
	}

	public InputTransformerBase(String name, DataType<IN> dataType, InputMapper<IN, T> mapper) {
		this.dataType = dataType;
		this.name = name;
		this.id = name;
		this.inline = mapper.createMappedInput(this::getOutput, this);
	}

	protected InputTransformerBase(String name, InputMapper<IN, T> mapper) {
		this.dataType = DataLiteral.from(this);
		this.name = name;
		this.id = name;
		this.inline = mapper.createMappedInput(this::getOutput, this);
	}

	public void clearParent() {
		setParent(null);
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
		assert parent != null : "input not connected to pipeline";

		return parent.getInput();
	}

	private void handleError(Throwable e) {
		assert parent != null : "input not connected to pipeline";
		
		parent.handleError(e, this);
	}

	public final Object id() {
		return this.id;
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

	public void setParent(TransformerParent<T> newParent) {
		if (newParent == null) {
			this.readLock = new ReentrantLock();
			this.parent = null;
			this.input = null;

			return;
		}

		assert this.parent == null : "input already connected to Head";

		this.parent = newParent;

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
		this.readLock = newParent.getReadLock();
		this.input = FunctionalProxies.createLockable(dataType.dataClass(), inline, readLock, this::handleError);

	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name;
	}

}