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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.slytechs.jnet.platform.api.pipeline.OutputTransformer.OutputMapper;
import com.slytechs.jnet.platform.api.pipeline.OutputTransformer.OutputMapper.SimpleOutputMapper;
import com.slytechs.jnet.platform.api.util.Registration;

/**
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public class Tail<IN> extends Processor<IN> {

	static class MappedDataTransformer<IN, OUT> extends OutputTransformer<IN, OUT> {

		public MappedDataTransformer(int priority, Object id, DataType<OUT> dataType, OutputMapper<IN, OUT> sink) {
			super(priority, id, dataType, sink);
		}

	}

	private final Map<Object, OutputTransformer<IN, ?>> transformersById = new HashMap<>();

	private final List<OutputTransformer<IN, ?>> activeTransformers = new ArrayList<>();

	private final OutputSwitch<IN> outputSwitch;

	private final OutputStack<IN> outputStack;

	Tail(Pipeline<IN> pipeline) {
		super(Integer.MAX_VALUE, "tail", (IN) null);
		super.pipeline = pipeline;

		this.outputSwitch = new OutputSwitch<>(this);
		this.outputStack = new OutputStack<>(this);

		setOutput(dataType().empty());
	}

	public <OUT> OutputTransformer<IN, OUT> addOutput(int priority, SimpleOutputMapper<IN, OUT> sink,
			DataType<OUT> dataType) {
		return addOutput(priority, (OutputMapper<IN, OUT>) sink, dataType);

	}

	public <OUT> OutputTransformer<IN, OUT> addOutput(int priority, OutputMapper<IN, OUT> sink,
			DataType<OUT> dataType) {
		return addOutput(priority, dataType, sink, dataType);
	}

	public <OUT> OutputTransformer<IN, OUT> addOutput(int priority, Object id, SimpleOutputMapper<IN, OUT> sink,
			DataType<OUT> dataType) {
		return addOutput(priority, id, (OutputMapper<IN, OUT>) sink, dataType);
	}

	public <OUT> OutputTransformer<IN, OUT> addOutput(int priority, Object id, OutputMapper<IN, OUT> sink,
			DataType<OUT> dataType) {

		OutputTransformer<IN, OUT> output = new MappedDataTransformer<IN, OUT>(priority, id, dataType, sink);

		var _ = registerOutput(output);

		return output;
	}

	public OutputStack<IN> getOutputStack() {
		return outputStack;
	}

	public OutputSwitch<IN> getOutputSwitch() {
		return outputSwitch;
	}

	/**
	 * @see com.slytechs.jnet.platform.api.pipeline.Processor#getInput()
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
	 * @see com.slytechs.jnet.platform.api.pipeline.Processor#relink()
	 */
	@Override
	void relink() {

		int switchSize = outputSwitch.isEmpty() ? 0 : 1;

		var outputs = super.dataType().arrayAllocator().apply(activeTransformers.size() + switchSize);

		for (int i = 0; i < activeTransformers.size(); i++) {
			var trans = activeTransformers.get(i);

			IN input = trans.getInput();
			outputs[i] = input;
		}

		if (switchSize > 0)
			outputs[outputs.length - 1] = outputSwitch.getInput();

		IN newOutput = dataType().optimizeArray(outputs);

		setOutput(newOutput);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String activeStr = activeTransformers.isEmpty()
				? ""
				: activeTransformers.stream()
						.map(OutputTransformer::toString)
						.collect(Collectors.joining("&", "{", "}"));
		String switchStr = outputSwitch.isEmpty() ? "" : outputSwitch.toString();
		String stackStr = outputStack.isEmpty() ? "" : outputStack.toString();

		return Stream.of(activeStr, switchStr, stackStr)
				.filter(Predicate.not(String::isEmpty))
				.collect(Collectors.joining(" & "));

	}

}
