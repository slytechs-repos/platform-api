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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.slytechs.jnet.jnetruntime.pipeline.OutputTransformer.OutputMapper;
import com.slytechs.jnet.jnetruntime.util.Registration;

/**
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public class Tail<IN> extends Processor<IN> {

	private static class DefaultDataTransformer<IN, OUT> extends OutputTransformer<IN, OUT> {

		public DefaultDataTransformer(int priority, Object id, DataType<OUT> dataType, OutputMapper<IN, OUT> sink) {
			super(priority, id, dataType, sink);
		}

	}

	private final Map<Object, OutputTransformer<IN, ?>> transformersById = new HashMap<>();

	private final List<OutputTransformer<IN, ?>> activeTransformers = new ArrayList<>();

	Tail(Pipeline<IN> pipeline) {
		super(Integer.MAX_VALUE, "tail", (IN) null);
		super.pipeline = pipeline;

		setOutput(dataType().empty());
	}
	public <OUT> Registration addOutput(int priority, OutputMapper<IN, OUT> sink, DataType<OUT> dataType) {
		return addOutput(priority, dataType, sink, dataType);
	}

	public <OUT> Registration addOutput(int priority, Object id, OutputMapper<IN, OUT> sink,
			DataType<OUT> dataType) {

		OutputTransformer<IN, OUT> output = new DefaultDataTransformer<IN, OUT>(priority, id, dataType, sink);

		return registerOutput(output);
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Processor#getInput()
	 */
	@Override
	public IN getInput() {
		return getOutput();
	}

	@SuppressWarnings("unchecked")
	public <OUT> OutputTransformer<IN, OUT> getOutputTransformer(Object id) {
		if (!transformersById.containsKey(id))
			throw pipeline.outputTransformerNotFound(id);

		return (OutputTransformer<IN, OUT>) transformersById.get(id);
	}

	public Registration registerOutput(OutputTransformer<IN, ?> newOutput) {
		Object id = newOutput.id();

		if (transformersById.containsKey(id))
			throw pipeline.duplicateOutputException(id);

		newOutput.tail = this;

		transformersById.put(id, newOutput);
		activeTransformers.add(newOutput);

		Collections.sort(activeTransformers);

		relink();

		return () -> {
			transformersById.remove(id);
			activeTransformers.remove(newOutput);

			relink();
		};
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Processor#relink()
	 */
	@Override
	void relink() {
		if (activeTransformers.size() == 1) {
			setOutput(activeTransformers.get(0).getInput());

			return;

		} else if (activeTransformers.isEmpty()) {
			setOutput(dataType().empty());

			return;
		}

		var outputs = super.dataType().arrayAllocator().apply(activeTransformers.size());

		for (int i = 0; i < outputs.length; i++) {
			var trans = activeTransformers.get(i);

			IN input = trans.getInput();
			outputs[i] = input;
		}

		IN newOutput = dataType().optimizeArray(outputs);

		setOutput(newOutput);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "OUT["
				+ activeTransformers.stream()
						.map(OutputTransformer::toString)
						.collect(Collectors.joining(" && "))
				+ "]";
	}

}
