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

	private final T inlineWithUncaughProcessorErrorHandling;

	Head(Pipeline<T> pipeline) {
		super(-1, "head");

		super.initializePipeline(pipeline);

		this.inlineWithUncaughProcessorErrorHandling = FunctionalProxies.createThrowableSupplier(
				dataType().dataClass(),
				this::getOutput,
				this::handleUncaughtProcessingError);
	}

	private void handleUncaughtProcessingError(Throwable e) {
		e.printStackTrace();
	}

	public <IN> Registration addInput(String name, InputMapper<IN, T> mapper) {

		var input = new DefaultInputTransformer<IN, T>(name, mapper);

		return registerInput(input);
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
		return inlineWithUncaughProcessorErrorHandling;
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

}
