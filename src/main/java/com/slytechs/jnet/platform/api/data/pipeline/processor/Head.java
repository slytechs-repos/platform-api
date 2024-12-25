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
package com.slytechs.jnet.platform.api.data.pipeline.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.stream.Collectors;

import com.slytechs.jnet.platform.api.data.DataType;
import com.slytechs.jnet.platform.api.data.common.processor.Processor;
import com.slytechs.jnet.platform.api.data.common.processor.impl.BuiltinProcessor;
import com.slytechs.jnet.platform.api.data.pipeline.ErrorSeverity;
import com.slytechs.jnet.platform.api.data.pipeline.Pipeline;
import com.slytechs.jnet.platform.api.data.pipeline.impl.PipelineBase;
import com.slytechs.jnet.platform.api.data.pipeline.transform.InputMapper;
import com.slytechs.jnet.platform.api.data.pipeline.transform.InputMapper.SimpleInputMapper;
import com.slytechs.jnet.platform.api.data.pipeline.transform.InputTransformer;
import com.slytechs.jnet.platform.api.data.pipeline.transform.TransformerParent;
import com.slytechs.jnet.platform.api.internal.util.function.FunctionalProxies;
import com.slytechs.jnet.platform.api.util.Registration;

/**
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public class Head<T>
		extends Processor<T>
		implements TransformerParent<T>, BuiltinProcessor {

	record InputKey<IN>(Object id, DataType<IN> dataType) {

		public InputKey(Object id) {
			this(id, (id instanceof DataType dt) ? dt : null);
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return Objects.hash(id);
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;

			@SuppressWarnings("unchecked")
			InputKey<IN> other = (InputKey<IN>) obj;
			return Objects.equals(id, other.id)
					&& (dataType == null || other.dataType == null)
							? true
							: Objects.equals(dataType, other.dataType);
		}

	}

	private final Map<InputKey<?>, InputTransformer<?, T>> inputsById = new HashMap<>();

	private T inline;
	private final T inlineWithErrorHandler;

	public Head(PipelineBase<T> pipeline) {
		super(Pipeline.HEAD_PROCESSOR_PRIORITY, "head");

		super.setParent(pipeline, pipeline);
		this.inlineWithErrorHandler = FunctionalProxies.createThrowableSupplier(
				dataType().dataClass(),
				this::getOutput,
				this::handleProcessingError);
	}

	public <IN> InputTransformer<IN, T> addInput(InputTransformer<IN, T> input) {

		var _ = registerInput(input);

		return input;
	}

	public <IN> InputTransformer<IN, T> addInput(String name, SimpleInputMapper<IN, T> mapper, DataType<IN> dataType) {
		return addInput(name, (InputMapper<IN, T>) mapper, dataType);
	}

	public <IN> InputTransformer<IN, T> addInput(String name, InputMapper<IN, T> mapper, DataType<IN> dataType) {

		var input = new InputTransformer<IN, T>(name, dataType, mapper);

		var _ = registerInput(input);

		return input;
	}

	public <IN> IN connector(Object id) {
		readLock.lock();

		try {
			InputTransformer<IN, T> p = getInputTransformer(id, null);
			if (p == null)
				throw super.inputNotFoundException(id);

			return p.getInput();
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.common.processor.Processor#getInput()
	 */
	@Override
	public final T getInput() {
		return inline;
	}

	public <IN> InputTransformer<IN, T> getInputTransformer(DataType<IN> dataType) {
		return getInputTransformer(dataType, dataType);
	}

	private <IN> InputTransformer<IN, T> getInputTransformer(InputKey<IN> key) {
		readLock.lock();

		try {
			@SuppressWarnings("unchecked")
			InputTransformer<IN, T> p = (InputTransformer<IN, T>) inputsById.get(key);
			if (p == null)
				throw super.inputNotFoundException(key);

			return p;
		} finally {
			readLock.unlock();
		}
	}

	public <IN> InputTransformer<IN, T> getInputTransformer(Object id) {
		return getInputTransformer(new InputKey<IN>(id));
	}

	public <IN> InputTransformer<IN, T> getInputTransformer(Object id, DataType<IN> dataType) {
		return getInputTransformer(new InputKey<IN>(id, dataType));
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.common.processor.Processor#getOutput()
	 */
	@Override
	public T getOutput() {
		if (outputData == null)
			throw new IllegalStateException("no output for pipeline");

		return outputData;
	}

	private void handleProcessingError(Throwable e) {

		// Unwrap
		while (e.getCause() != null)
			e = e.getCause();

		errorSupport.fireError(e, ErrorSeverity.ERROR);
	}

	public Registration registerInput(InputTransformer<?, T> newInput) {

		writeLock.lock();

		try {
			var id = newInput.id();
			var dt = newInput.dataType();
			var key = new InputKey<>(id, dt);

			newInput.setParent(this);

			inputsById.put(key, newInput);

			return () -> removeInput(key, newInput);
		} finally {
			writeLock.unlock();
		}
	}

	private void removeInput(InputKey<?> key, InputTransformer<?, T> input) {
		writeLock.lock();

		try {

			input.clearParent();
			inputsById.remove(key);

		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return inputsById.values().stream()
				.map(InputTransformer::toString)
				.collect(Collectors.joining("|", "{", "}"));
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.common.processor.Processor#setOutput(java.lang.Object)
	 */
	@Override
	public void setOutput(T newOutput) {
		writeLock.lock();

		try {
			super.setOutput(newOutput);

			if (errorSupport.hasErrorListeners()) {
				this.inline = inlineWithErrorHandler;

			} else {
				this.inline = getOutput();
			}
		} finally {
			writeLock.unlock();
		}

	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.transform.TransformerParent#getRwLock()
	 */
	@Override
	public ReadWriteLock getRwLock() {
		return rwLock;
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.transform.TransformerParent#handleError(java.lang.Throwable)
	 */
	@Override
	public void handleError(Throwable e, Object source) {
	}

}
