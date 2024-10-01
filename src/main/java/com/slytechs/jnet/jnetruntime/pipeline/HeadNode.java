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

import static java.util.function.Predicate.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class HeadNode<T>
		extends BuiltinNode<T, HeadNode<T>> {

	private final Map<Object, AbstractInput<?, T, ?>> inputMap = new HashMap<>();

	/**
	 * @param priority
	 * @param name
	 * @param type
	 */
	public HeadNode(Pipeline<T, ?> parent, String name, DataType type) {
		super(parent, Pipeline.HEAD_BUILTIN_PRIORITY, name, type, null);

		enable(true);
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.AbstractProcessor#nextProcessor(com.slytechs.jnet.jnetruntime.pipeline.AbstractProcessor)
	 */
	@Override
	void nextProcessor(AbstractProcessor<T, ?> next) {
		super.nextProcessor(next);

		T out = (next == null)
				? null
				: next.inputData();

		inputMap.values().forEach(t -> t.outputData(out));
	}

	public void addInput(AbstractInput<?, T, ?> input, Object id) {
		if (inputMap.containsKey(id))
			throw new IllegalArgumentException("input [%s] with this id [%s] already exists in pipeline [%s]"
					.formatted(input.name(), id, name()));

		inputMap.put(id, input);
	}

	public String inputsToString() {
		return inputMap.values().stream()
				.map(in -> (in.isEnabled() ? "%s%s" : "!%s%s").formatted(in.name(), in.inputsToString()))
				.collect(Collectors.joining(", ", "[", "]"));
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.AbstractProcessor#reLinkData()
	 */
	@Override
	void reLinkData() {

		super.reLinkData();

		T headOutput = outputData();

		inputMap.values().stream()
				.filter(AbstractInput::isEnabled)
				.filter(not(AbstractInput::isBypassed))
				.peek(AbstractInput::reLinkData)
				.forEach(t -> t.outputData(headOutput));
	}

	public void onInputEnable(boolean b, AbstractInput<?, T, ?> input) {
//		reLinkData();
	}
}