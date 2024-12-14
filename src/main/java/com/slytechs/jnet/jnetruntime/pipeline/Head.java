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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.slytechs.jnet.jnetruntime.internal.util.function.FunctionalProxies;
import com.slytechs.jnet.jnetruntime.pipeline.InputTransformer.InputMapper;
import com.slytechs.jnet.jnetruntime.util.Registration;

/**
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public class Head<T> extends Processor<T> {

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

	Head(Pipeline<T> pipeline) {
		super(-1, "head");

		super.initializePipeline(pipeline);
		this.inlineWithErrorHandler = FunctionalProxies.createThrowableSupplier(
				dataType().dataClass(),
				this::getOutput,
				this::handleProcessingError);
	}

	public <IN> InputTransformer<IN, T> addInput(InputTransformer<IN, T> input) {

		var _ = registerInput(input);

		return input;
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
				throw pipeline.inputNotFoundException(id);

			return p.getInput();
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Processor#getInput()
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
				throw pipeline.inputNotFoundException(key);

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
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Processor#getOutput()
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

		pipeline.fireError(e, ErrorSeverity.ERROR);
	}

	public Registration registerInput(InputTransformer<?, T> newInput) {

		writeLock.lock();

		try {
			var id = newInput.id();
			var dt = newInput.dataType();
			var key = new InputKey<>(id, dt);

			newInput.initializeHead(this);

			inputsById.put(key, newInput);

			return () -> removeInput(key, newInput);
		} finally {
			writeLock.unlock();
		}
	}

	private void removeInput(InputKey<?> key, InputTransformer<?, T> input) {
		writeLock.lock();

		try {

			input.clearHead();
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
		return " IN["
				+ inputsById.values().stream()
						.map(InputTransformer::toString)
						.collect(Collectors.joining(" || "))
				+ "]";
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Processor#setOutput(java.lang.Object)
	 */
	@Override
	void setOutput(T newOutput) {
		writeLock.lock();

		try {
			super.setOutput(newOutput);

			if (pipeline.hasErrorListeners()) {
				this.inline = inlineWithErrorHandler;

			} else {
				this.inline = getOutput();
			}
		} finally {
			writeLock.unlock();
		}

	}

}
