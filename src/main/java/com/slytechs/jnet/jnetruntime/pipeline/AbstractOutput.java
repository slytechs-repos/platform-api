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

import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.OutputTransformer;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.OutputTransformer.EndPoint.EndPointFactory;
import com.slytechs.jnet.jnetruntime.util.HasName;
import com.slytechs.jnet.jnetruntime.util.Registration;

/**
 * Abstract base class for output transformers in a pipeline.
 * 
 * <p>
 * This class extends AbstractTransformer and implements OutputTransformer,
 * providing a foundation for creating specific output transformer
 * implementations. It manages end points for the output and handles the
 * interaction with the pipeline's tail node.
 * </p>
 *
 * @param <T_IN>   The type of input data
 * @param <T_OUT>  The type of output data
 * @param <T_BASE> The specific type of the transformer implementation
 * @author Mark Bednarczyk
 */
public abstract class AbstractOutput<T_IN, T_OUT, T_BASE extends DataTransformer<T_IN, T_OUT, T_BASE>>
		extends AbstractTransformer<T_IN, T_OUT, T_BASE>
		implements OutputTransformer<T_OUT> {

	/** The tail node. */
	@SuppressWarnings("unused")
	private final TailNode<T_IN> tailNode;

	/** The output data list. */
	final DataList<T_OUT> outputList;

	/** The end point map. */
	final Map<String, EndPoint<T_OUT>> endPointMap = new HashMap<>();

	private Object userOpaque;

	/**
	 * Constructs a new AbstractOutput with the specified parameters.
	 *
	 * @param tailNode   The tail node of the pipeline
	 * @param name       The name of this output transformer
	 * @param inputType  The type of input data
	 * @param outputType The type of output data
	 */
	public AbstractOutput(TailNode<T_IN> tailNode, String name, DataType inputType, DataType outputType) {
		super(tailNode, name, inputType, outputType);
		this.tailNode = tailNode;
		this.outputList = new DataList<>(outputType, super::outputData);
	}

	/**
	 * Constructs a new AbstractOutput with the specified parameters and initial
	 * input data.
	 *
	 * @param tailNode   The tail node of the pipeline
	 * @param name       The name of this output transformer
	 * @param input      The initial input data
	 * @param inputType  The type of input data
	 * @param outputType The type of output data
	 */
	public AbstractOutput(TailNode<T_IN> tailNode, String name, T_IN input, DataType inputType, DataType outputType) {
		super(tailNode, name, input, inputType, outputType);
		this.tailNode = tailNode;
		this.outputList = new DataList<>(outputType, super::outputData);

		enable(true);
	}

	Registration addToOutputList(T_OUT data) {
		writeLock.lock();
		try {
			outputList.add(data);

			return () -> removeFromOutputList(data);

		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EndPoint<T_OUT> createEndPoint(String id) {
		EndPoint<T_OUT> endpoint = new MultiEndPoint<T_OUT>(this, id);

		writeLock.lock();
		try {
			endPointMap.put(id, endpoint);

			return endpoint;

		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.OutputTransformer#createEndPoint(java.lang.String,
	 *      com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.OutputTransformer.EndPoint.EndPointFactory)
	 */
	@Override
	public EndPoint<T_OUT> createEndPoint(String id, EndPointFactory<T_OUT> factory) {
		EndPoint<T_OUT> endpoint = factory.newEndPointInstance(this, id);

		writeLock.lock();
		try {
			endPointMap.put(id, endpoint);

			return endpoint;

		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * Returns a string representation of the outputs.
	 *
	 * @return A string representation of the outputs
	 */
	public String outputsToString() {
		readLock.lock();
		try {
			return endPointMap.values().stream()
					.sorted()
					.map(HasName::name)
					.map("\"%s\""::formatted)
					.collect(Collectors.joining(",", "O[", "]"));

		} finally {
			readLock.unlock();
		}
	}

	void removeEndPoint(EndPoint<?> endPoint) {
		writeLock.lock();
		try {
			endPointMap.remove(endPoint.id());

		} finally {
			writeLock.unlock();
		}
	}

	private void removeFromOutputList(T_OUT data) {
		writeLock.lock();
		try {
			outputList.remove(data);

		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.DataTransformer#userOpaque()
	 */
	@Override
	public Object userOpaque() {
		return this.userOpaque;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.DataTransformer#userOpaque(java.lang.Object)
	 */
	@Override
	public void userOpaque(Object newOpaque) {
		this.userOpaque = newOpaque;
	}

}