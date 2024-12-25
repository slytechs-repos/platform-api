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

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.slytechs.jnet.platform.api.data.DataType;
import com.slytechs.jnet.platform.api.data.pipeline.processor.Tail;
import com.slytechs.jnet.platform.api.data.pipeline.processor.impl.MappedDataTransformer;
import com.slytechs.jnet.platform.api.data.pipeline.transform.OutputTransformer.OutputMapper;
import com.slytechs.jnet.platform.api.data.pipeline.transform.OutputTransformer.OutputMapper.SimpleOutputMapper;
import com.slytechs.jnet.platform.api.internal.util.function.FunctionalProxies;

/**
 * 
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public final class OutputSwitch<IN> {

	private final IN empty;
	private final Tail<IN> tail;
	private final IN inline;
	private final DataType<IN> dataType;
	private IN selectedOutputTransformer;
	private int selectedIndex;

	private final Lock writeLock;

	@SuppressWarnings("rawtypes")
	private OutputTransformer[] outputs = {};

	public OutputSwitch(Tail<IN> tail) {
		this.tail = tail;
		this.dataType = tail.dataType();
		this.writeLock = tail.getReadLock();
		this.empty = FunctionalProxies.createNoOpProxy(dataType.dataClass());
		this.inline = FunctionalProxies.createThrowableSupplier(
				dataType.dataClass(),
				this::getSelected,
				this::handleErrors);

		reset();
	}

	public boolean isEmpty() {
		return outputs.length == 0;
	}

	public int size() {
		return outputs.length;
	}

	private void handleErrors(Throwable err) {
		tail.handleError(err, selectedOutputTransformer);
	}

	public IN getInput() {
		return inline;
	}

	public <OUT> OutputTransformer<IN, OUT> setOutput(int index, SimpleOutputMapper<IN, OUT> mapper,
			DataType<OUT> dataType) {
		return setOutput(index, (OutputMapper<IN, OUT>) mapper, dataType);
	}

	public <OUT> OutputTransformer<IN, OUT> setOutput(int index, OutputMapper<IN, OUT> mapper,
			DataType<OUT> dataType) {

		String name = dataType.name().replace("<Object>", "<?>");

		OutputTransformer<IN, OUT> output = new MappedDataTransformer<IN, OUT>(index, name, dataType, mapper);

		return setOutput(index, output);
	}

	public <OUT> OutputTransformer<IN, OUT> setOutput(int index, OutputTransformer<IN, OUT> output) {
		if (index >= outputs.length)
			outputs = Arrays.copyOf(outputs, index + 1);

		if (outputs[index] != null)
			throw new IllegalArgumentException("switch index already taken [%d]".formatted(index));

		outputs[index] = output;

		tail.relink();

		return output;
	}

	public void reset() {
		writeLock.lock();

		try {

			this.selectedOutputTransformer = empty;
			this.selectedIndex = -1;

		} finally {
			writeLock.unlock();
		}
	}

	@SuppressWarnings("unchecked")
	public <OUT> OutputTransformer<IN, OUT> select(int index) {

		writeLock.lock();

		try {
			if (selectedIndex == index)
				return (OutputTransformer<IN, OUT>) selectedOutputTransformer;

			Objects.checkIndex(index, outputs.length);

			OutputTransformer<IN, OUT> output = outputs[index];
			assert output != null;

			this.selectedOutputTransformer = output.getInput();
			this.selectedIndex = index;

			return output;

		} finally {
			writeLock.unlock();
		}
	}

	private IN getSelected() {
		return selectedOutputTransformer;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		var str = IntStream.range(0, outputs.length)
				.mapToObj(i -> "%3$s%d:%s".formatted(i, outputs[i].name(), (selectedIndex == i) ? "*" : ""))
				.collect(Collectors.joining("|"));

		return "SWITCH{"
				+ str
				+ "}";
	}
}
