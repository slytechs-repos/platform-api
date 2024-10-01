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

import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.InputTransformer;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.InputTransformer.EntryPoint.EntryPointFactory;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.InputTransformer.EntryPoint.EntryPointFactory.Arg1;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.InputTransformer.EntryPoint.EntryPointFactory.Arg2;

/**
 * Abstract base class for input transformers in a pipeline.
 * 
 * <p>
 * This class extends AbstractTransformer and implements InputTransformer,
 * providing a foundation for creating specific input transformer
 * implementations. It manages entry points for the input and handles the
 * interaction with the pipeline's head node.
 * </p>
 *
 * @param <T_IN>   The type of input data
 * @param <T_OUT>  The type of output data
 * @param <T_BASE> The specific type of the transformer implementation
 * @author Mark Bednarczyk
 */
public class AbstractInput<T_IN, T_OUT, T_BASE extends DataTransformer<T_IN, T_OUT, T_BASE>>
		extends AbstractTransformer<T_IN, T_OUT, T_BASE>
		implements InputTransformer<T_IN> {

	/**
	 * Default implementation of EntryPoint for AbstractInput.
	 *
	 * @param <T> The type of input data
	 * @author Mark Bednarczyk
	 */
	private static class DefaultEntryPoint<T> extends AbstractEntryPoint<T> {

		/**
		 * Constructs a new DefaultEntryPoint.
		 *
		 * @param input The input transformer
		 * @param id    The identifier for this entry point
		 */
		public DefaultEntryPoint(InputTransformer<T> input, String id) {
			super((AbstractInput<T, ?, ?>) input, id);
		}
	}

	/** The head node. */
	private final HeadNode<T_OUT> headNode;

	/** The entry point map. */
	private final Map<String, EntryPoint<T_IN>> entryPointMap = new HashMap<>();

	/**
	 * Constructs a new AbstractInput with the specified parameters.
	 *
	 * @param headNode   The head node of the pipeline
	 * @param name       The name of this input transformer
	 * @param inputType  The type of input data
	 * @param outputType The type of output data
	 */
	public AbstractInput(HeadNode<T_OUT> headNode, String name, DataType inputType, DataType outputType) {
		super(name, inputType, outputType);
		this.headNode = headNode;
		enable(true);
	}

	/**
	 * Constructs a new AbstractInput with the specified parameters and initial
	 * input data.
	 *
	 * @param headNode   The head node of the pipeline
	 * @param name       The name of this input transformer
	 * @param input      The initial input data
	 * @param inputType  The type of input data
	 * @param outputType The type of output data
	 */
	public AbstractInput(HeadNode<T_OUT> headNode, String name, T_IN input, DataType inputType, DataType outputType) {
		super(name, input, inputType, outputType);
		this.headNode = headNode;
		enable(true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T_OUT outputData() {
		return headNode.outputData();
	}

	/**
	 * Returns a string representation of the input entry points.
	 *
	 * @return A string representation of the input entry points
	 */
	public String inputsToString() {
		return entryPointMap.keySet().stream()
				.map(String::valueOf)
				.collect(Collectors.joining(", ", "<", ">"));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntryPoint<T_IN> createEntryPoint(String id) {
		return createEntryPoint(id, DefaultEntryPoint::new);
	}

	/**
	 * Unregisters an entry point from this input transformer.
	 *
	 * @param entryPoint The entry point to unregister
	 */
	void unregister(EntryPoint<T_IN> entryPoint) {
		this.entryPointMap.remove(entryPoint.id());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onEnable(boolean newValue) {
		headNode.onInputEnable(newValue, this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T_ENTRY extends EntryPoint<T_IN>> T_ENTRY createEntryPoint(String id,
			EntryPointFactory<T_IN, T_ENTRY> factory) {
		var sink = factory.newEntryPointInstance(this, id);
		entryPointMap.put(id, sink);
		return sink;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T_ENTRY extends EntryPoint<T_IN>, T_ARG1> T_ENTRY createEntryPoint(String id,
			T_ARG1 arg1, Arg1<T_IN, T_ENTRY, T_ARG1> factory) {
		var sink = factory.newEntryPointInstance1Arg(this, id, arg1);
		entryPointMap.put(id, sink);
		return sink;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T_ENTRY extends EntryPoint<T_IN>, T1, T2> T_ENTRY createEntryPoint(
			String id,
			T1 arg1,
			T2 arg2,
			Arg2<T_IN, T_ENTRY, T1, T2> factory) {
		var sink = factory.newEntryPointInstance2Args(this, id, arg1, arg2);
		entryPointMap.put(id, sink);
		return sink;
	}
}