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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.slytechs.jnet.platform.api.data.DataType;
import com.slytechs.jnet.platform.api.data.pipeline.processor.Tail;
import com.slytechs.jnet.platform.api.data.pipeline.processor.impl.MappedDataTransformer;
import com.slytechs.jnet.platform.api.data.pipeline.transform.OutputTransformer.OutputMapper;
import com.slytechs.jnet.platform.api.data.pipeline.transform.OutputTransformer.OutputMapper.SimpleOutputMapper;

/**
 * 
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public class OutputStack<IN> implements AutoCloseable {

	private final DataType<IN> dataType;
	private final Stack<OutputTransformer<IN, ?>> stack = new Stack<>();
	private final Tail<IN> tail;

	private final List<OutputTransformer<IN, ?>> available = new ArrayList<>();

	public OutputStack(Tail<IN> tail) {
		this.tail = tail;
		this.dataType = tail.dataType();
	}

	public <OUT> OutputStack<IN> push(OutputTransformer<IN, OUT> output) {
		stack.push(output);

		tail.setOutput(output.getInput());

		return this;
	}

	public void pop() {
		stack.pop();
		if (stack.isEmpty()) {
//			tail.setOutput(null);

			return;
		}

		OutputTransformer<IN, ?> output = stack.pop();
		tail.setOutput(output.getInput());
	}

	public <OUT> OutputTransformer<IN, OUT> createTransformer(Object id, SimpleOutputMapper<IN, OUT> sink,
			DataType<OUT> dataType) {

		return createTransformer(id, (OutputMapper<IN, OUT>) sink, dataType);
	}

	public <OUT> OutputTransformer<IN, OUT> createTransformer(Object id, OutputMapper<IN, OUT> sink,
			DataType<OUT> dataType) {
		OutputTransformer<IN, OUT> output = new MappedDataTransformer<IN, OUT>(0, id, dataType, sink);

		available.add(output);

		return output;
	}

	/**
	 * @param output
	 * @return
	 */
	public void remove(OutputTransformer<IN, ?> output) {
		stack.remove(output);
	}

	/**
	 * @return
	 */
	public boolean isEmpty() {
		return available.isEmpty();
	}

	@Override
	public String toString() {
		var list = new ArrayList<>(stack.stream()
				.map(t -> "+" + t.name())
				.toList())
				.reversed();

		available.stream()
				.filter(Predicate.not(t -> stack.contains(t)))
				.map(t -> "*" + t.name())
				.forEach(list::add);

		var str = list.stream()
				.collect(Collectors.joining("|"));

		return "STACK{"
				+ str
				+ "}";
	}

	/**
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() {
		pop();
	}
}
