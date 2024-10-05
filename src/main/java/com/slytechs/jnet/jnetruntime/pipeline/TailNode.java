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
import java.util.stream.Collectors;

/**
 * The Class TailNode.
 *
 * @param <T> the generic type
 * @author Mark Bednarczyk
 */
public final class TailNode<T>
		extends BuiltinNode<T, TailNode<T>> {

	/** The output map. */
	private final Map<Object, AbstractOutput<T, ?, ?>> outputMap = new HashMap<>();
	private final DataList<T> inputList;

	/**
	 * Instantiates a new tail node.
	 *
	 * @param parent the parent
	 * @param name   public Registration addInput(InputEntryPoint<?> input) {
	 * @param type   the type
	 */
	public TailNode(Pipeline<T, ?> parent, String name, DataType type) {
		super(parent, Pipeline.TAIL_BUILTIN_PRIORITY, name, type, type.empty());

		this.inputList = new DataList<>(type, this::inputData);
	}

	/**
	 * Adds the output.
	 *
	 * @param output the output
	 * @param id     the id
	 */
	public void addOutput(AbstractOutput<T, ?, ?> output, Object id) {

		try {
			writeLock.lock();

			// Check for valid ID types
			assert false ||
					id instanceof String ||
					id instanceof DataType
					: "output [%s] id [%s] must of type String or DataType"
							.formatted(output.name(), id);

			if (outputMap.containsKey(id))
				throw new IllegalArgumentException("output [%s] with this id [%s] already exists in pipeline [%s]"
						.formatted(output.name(), id, name()));

			outputMap.put(id, output);
			inputList.add(output.inputData());

			output.setRegistration(() -> {
				outputMap.remove(id);
				inputList.remove(output.inputData());
			});

			super.prevProcessor.calculateOutput();

		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * Outputs to string.
	 *
	 * @return the string
	 */
	public String outputsToString() {
		try {
			readLock.lock();

			return outputMap.values().stream()
					.sorted()
					.map(out -> (out.isEnabled() ? "%s=>%s" : "!%s=>%s")
							.formatted(out.name(), out.outputsToString()))
					.collect(Collectors.joining(", ", "OX[", "]"));

		} finally {
			readLock.unlock();
		}
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.AbstractProcessor#nextElement(com.slytechs.jnet.jnetruntime.pipeline.AbstractProcessor)
	 */
	@Override
	public void nextElement(AbstractProcessor<T, ?> e) {
		if (e == null)
			return;

		throw new UnsupportedOperationException(
				"Can not append processor [%s] before the tail node, use addOutput() method to add data sinks "
						.formatted(e.name()));
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.AbstractProcessor#calculateOutput()
	 */
	@Override
	boolean calculateOutput() {
		return false;
	}

}