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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.slytechs.jnet.platform.api.data.DataType;
import com.slytechs.jnet.platform.api.data.common.processor.Processor;
import com.slytechs.jnet.platform.api.data.common.processor.impl.BuiltinProcessor;
import com.slytechs.jnet.platform.api.data.pipeline.Pipeline;
import com.slytechs.jnet.platform.api.data.pipeline.impl.PipelineBase;
import com.slytechs.jnet.platform.api.data.pipeline.processor.impl.MappedDataTransformer;
import com.slytechs.jnet.platform.api.data.pipeline.transform.OutputStack;
import com.slytechs.jnet.platform.api.data.pipeline.transform.OutputSwitch;
import com.slytechs.jnet.platform.api.data.pipeline.transform.OutputTransformer;
import com.slytechs.jnet.platform.api.data.pipeline.transform.OutputTransformer.OutputMapper;
import com.slytechs.jnet.platform.api.data.pipeline.transform.OutputTransformer.OutputMapper.SimpleOutputMapper;
import com.slytechs.jnet.platform.api.data.pipeline.transform.TransformerParent;
import com.slytechs.jnet.platform.api.util.Registration;

/**
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public class Tail<IN> extends Processor<IN>
		implements TransformerParent<IN>, BuiltinProcessor {

	private final Map<Object, OutputTransformer<IN, ?>> transformersById = new HashMap<>();

	private final List<OutputTransformer<IN, ?>> activeTransformers = new ArrayList<>();

	private final OutputSwitch<IN> outputSwitch;

	private final OutputStack<IN> outputStack;

	public Tail(PipelineBase<IN> pipeline) {
		super(Pipeline.TAIL_PROCESSOR_PRIORITY, "tail", (IN) null);
		super.setParent(pipeline, pipeline);

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
	 * @see com.slytechs.jnet.platform.api.data.common.processor.Processor#getInput()
	 */
	@Override
	public IN getInput() {
		return getOutput();
	}

	@SuppressWarnings("unchecked")
	public <OUT> OutputTransformer<IN, OUT> getOutputTransformer(Object id) {
		if (!transformersById.containsKey(id))
			throw super.outputTransformerNotFound(id);

		return (OutputTransformer<IN, OUT>) transformersById.get(id);
	}

	public Registration registerOutput(OutputTransformer<IN, ?> newOutput) {
		Object id = newOutput.id();

		if (transformersById.containsKey(id))
			throw super.duplicateOutputException(id);

		newOutput.setParent(this);

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
	 * @see com.slytechs.jnet.platform.api.data.common.processor.Processor#relink()
	 */
	@Override
	public void relink() {

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

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.transform.TransformerParent#getRwLock()
	 */
	@Override
	public ReadWriteLock getRwLock() {
		return rwLock;
	}

}
