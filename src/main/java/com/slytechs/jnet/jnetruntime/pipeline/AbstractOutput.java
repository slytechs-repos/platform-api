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
import com.slytechs.jnet.jnetruntime.util.HasName;
import com.slytechs.jnet.jnetruntime.util.HasPriority;
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
public class AbstractOutput<T_IN, T_OUT, T_BASE extends DataTransformer<T_IN, T_OUT, T_BASE>>
		extends AbstractTransformer<T_IN, T_OUT, T_BASE>
		implements OutputTransformer<T_OUT>, Comparable<AbstractOutput<?, ?, ?>>, HasPriority {

	/**
	 * Implementation of the EndPoint interface for managing output endpoints.
	 *
	 * @param <T> the generic type
	 * @author Mark Bednarczyk
	 */
	private class EndPointImpl<T> implements EndPoint<T_OUT>, Comparable<EndPoint<T_OUT>> {

		/** The id. */
		private final String id;

		/** The priority. */
		private int priority = HasPriority.DEFAULT_PRIORITY_VALUE;

		/** The data list registration. */
		private Registration dataListRegistration;

		/**
		 * Constructs a new EndPointImpl with the given ID.
		 *
		 * @param id The identifier for this endpoint
		 */
		public EndPointImpl(String id) {
			this.id = id;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public DataType outputData() {
			return outputType();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String id() {
			return id;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void outputData(T_OUT data) {
			if (this.dataListRegistration != null)
				throw new IllegalStateException("output's [%s] endpoint [%s] is already set"
						.formatted(name(), id()));

			outputDataList.add(data);
			this.dataListRegistration = () -> outputDataList.remove(data);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void unregister() {
			if (dataListRegistration != null)
				dataListRegistration.unregister();

			endPointMap.remove(id);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int priority() {
			return priority;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public EndPoint<T_OUT> priority(int newPriority) {
			HasPriority.checkPriorityValue(newPriority);
			this.priority = newPriority;
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int compareTo(EndPoint<T_OUT> o) {
			return this.priority - o.priority();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String name() {
			return id;
		}
	}

	/** The tail node. */
	@SuppressWarnings("unused")
	private final TailNode<T_IN> tailNode;

	/** The output data list. */
	private final DataList<T_OUT> outputDataList;

	/** The end point map. */
	private final Map<String, EndPoint<T_OUT>> endPointMap = new HashMap<>();

	/** The priority. */
	private int priority = HasPriority.DEFAULT_PRIORITY_VALUE;

	/**
	 * Constructs a new AbstractOutput with the specified parameters.
	 *
	 * @param tailNode   The tail node of the pipeline
	 * @param name       The name of this output transformer
	 * @param inputType  The type of input data
	 * @param outputType The type of output data
	 */
	public AbstractOutput(TailNode<T_IN> tailNode, String name, DataType inputType, DataType outputType) {
		super(name, inputType, outputType);
		this.tailNode = tailNode;
		this.outputDataList = new DataList<>(outputType);
		this.outputDataList.addChangeListener(super::outputData);
		enable(true);
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
		super(name, input, inputType, outputType);
		this.tailNode = tailNode;
		this.outputDataList = new DataList<>(outputType);
		this.outputDataList.addChangeListener(super::outputData);
		enable(true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T_OUT addOutputData(T_OUT data) {
		registerOutputData(data);
		return data;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(AbstractOutput<?, ?, ?> o) {
		return this.priority - o.priority;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EndPoint<T_OUT> createEndPoint(String id) {
		var endpoint = new EndPointImpl<T_OUT>(id);
		endPointMap.put(id, endpoint);
		return endpoint;
	}

	/**
	 * Returns a string representation of the outputs.
	 *
	 * @return A string representation of the outputs
	 */
	public String outputsToString() {
		return endPointMap.values().stream()
				.sorted()
				.map(HasName::name)
				.collect(Collectors.joining(",", "<", ">"));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int priority() {
		return priority;
	}

	/**
	 * Sets the priority of this output transformer.
	 *
	 * @param newPriority The new priority value
	 * @return This output transformer instance
	 * @throws IllegalArgumentException if the priority value is invalid
	 */
	public T_BASE priority(int newPriority) throws IllegalArgumentException {
		HasPriority.checkPriorityValue(newPriority);
		this.priority = newPriority;
		return us();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Registration registerOutputData(T_OUT data) {
		outputDataList.add(data);
		return () -> outputDataList.remove(data);
	}
}