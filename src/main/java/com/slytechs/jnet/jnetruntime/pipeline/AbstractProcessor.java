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

import com.slytechs.jnet.jnetruntime.util.DoublyLinkedElement;
import com.slytechs.jnet.jnetruntime.util.Registration;

/**
 * Abstract base class for data processors in a pipeline.
 * 
 * <p>
 * This class implements the core functionality of a data processor, including
 * input and output data management, priority handling, and pipeline
 * integration. It serves as a foundation for creating specific data processor
 * implementations.
 * </p>
 *
 * @param <T>      The type of data processed by this processor
 * @param <T_BASE> The specific type of the processor implementation
 * @author Mark Bednarczyk
 */
public class AbstractProcessor<T, T_BASE extends DataProcessor<T, T_BASE>>
		extends AbstractComponent<T_BASE>
		implements DataProcessor<T, T_BASE>, DoublyLinkedElement<AbstractProcessor<T, ?>> {

	/** The data type. */
	private final DataType dataType;

	/** The input data. */
	private T inputData;

	/** The output data. */
	private T outputData; // Auto maintained and updated by DataList object
	private T outputNextIn;

	/** The output list. */
	private final DataList<T> outputList;

	/** The pipeline. */
	private final AbstractPipeline<T, ?> pipeline;

	/** The next processor. */
	AbstractProcessor<T, ?> nextProcessor;

	AbstractProcessor<T, ?> prevProcessor;

	/**
	 * Constructs a new processor with the specified parameters.
	 *
	 * @param pipeline The pipeline this processor belongs to
	 * @param priority The priority of this processor
	 * @param name     The name of this processor
	 * @param type     The data type this processor handles
	 * @throws IllegalArgumentException if the pipeline is invalid or the processor
	 *                                  class is incompatible with the data type
	 */
	@SuppressWarnings("unchecked")
	public AbstractProcessor(Pipeline<T, ?> pipeline, int priority, String name, DataType type) {
		super(pipeline, name, priority);

		if (!(pipeline instanceof AbstractPipeline<T, ?> apipeline))
			throw new IllegalArgumentException("invalid pipeline [%s]".formatted(pipeline.name()));

		if (!type.isCompatibleWith(getClass()))
			throw new IllegalArgumentException("processor subclass must implement data interface [%s]"
					.formatted(type.dataClass()));

		this.pipeline = apipeline;
		this.inputData = (T) this;
		this.dataType = type;
		this.outputList = new DataList<>(type, this::updateOutputField);
	}

	/**
	 * Constructs a new processor with the specified parameters and initial data.
	 *
	 * @param pipeline The pipeline this processor belongs to
	 * @param priority The priority of this processor
	 * @param name     The name of this processor
	 * @param type     The data type this processor handles
	 * @param data     The initial data for this processor
	 * @throws IllegalArgumentException if the pipeline is invalid
	 */
	AbstractProcessor(Pipeline<T, ?> pipeline, int priority, String name, DataType type, T data) {
		super(pipeline, name, priority);
		if (!(pipeline instanceof AbstractPipeline<T, ?> ac))
			throw new IllegalArgumentException("invalid pipeline [%s]".formatted(pipeline.name()));
		this.pipeline = ac;
		this.inputData = data;
		this.dataType = type;
		this.outputList = new DataList<>(type, this::updateOutputField);
	}

	/**
	 * Adds a local output to this processor's output list.
	 *
	 * @param localOutput The local output to add
	 * @return A Registration object for unregistering the output
	 */
	final protected Registration addExternalOutput(T localOutput) {
		try {
			writeLock.lock();

			outputList.addFirst(localOutput);
			return () -> outputList.remove(localOutput);

		} finally {
			writeLock.unlock();
		}
	}

	T calculateInput() {
		if (isBypassed())
			return nextProcessor.calculateInput();

		calculateOutput();

		return inputData;
	}

	boolean calculateOutput() {
		if (isBypassed()) {
			prevProcessor.calculateOutput();

			return false;
		}

		/* check for changes in our output and next chained input */
		T latestIn = nextProcessor.calculateInput();
		if (outputList.contains(latestIn))
			return false; // No changes, nothing to do

		/*
		 * Remove previous data descriptor interface and replace with the latest.
		 * 
		 * 
		 * Note: `DataList::outputList` automatically updates the `this.outputData`
		 * field when the list changes, with the combined T wrapper to dispatch to all
		 * T's in the `outputList`.
		 */
		outputList.remove(outputNextIn);
		outputList.add(latestIn);

		/* Save to make sure we keep track of chained data descriptor link */
		outputNextIn = latestIn;
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataType dataType() {
		return dataType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T inputData() {
		return inputData;
	}

	/**
	 * Sets the input data for this processor.
	 *
	 * @param newInputData The new input data
	 */
	void inputData(T newInputData) {
		this.inputData = newInputData;
		if (inputData == null)
			inputData = inputType().empty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataType inputType() {
		return this.dataType;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.util.DoublyLinkedElement#nextElement()
	 */
	@Override
	public AbstractProcessor<T, ?> nextElement() {
		return nextProcessor;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.util.DoublyLinkedElement#nextElement(java.lang.Object)
	 */
	@Override
	public void nextElement(AbstractProcessor<T, ?> e) {
		this.nextProcessor = e;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.AbstractComponent#onBypass(boolean)
	 */
	@Override
	protected void onBypass(boolean newValue) {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onEnable(boolean newValue) {
		checkIfIsRegistered();

		if (newValue)
			pipeline.activateProcessor(this);
		else
			pipeline.deactivateProcessor(this);
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.AbstractComponent#onPriorityChange(int)
	 */
	@Override
	protected void onPriorityChange(int newPriority) {
		checkIfIsRegistered();

		pipeline.resortProcessor(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T outputData() {
		return outputData;
	}

	T outputData(T out) {
		return this.outputData = out;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataType outputType() {
		return this.dataType;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.util.DoublyLinkedElement#prevElement()
	 */
	@Override
	public AbstractProcessor<T, ?> prevElement() {
		return prevProcessor;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.util.DoublyLinkedElement#prevElement(java.lang.Object)
	 */
	@Override
	public void prevElement(AbstractProcessor<T, ?> e) {
		this.prevProcessor = e;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return ""
				+ getClass().getSimpleName()
				+ " [priority=" + priority()
				+ ", enabled=" + isEnabled()
				+ ", dataType=" + dataType
				+ ", name=" + name()
				+ "]";
	}

	/**
	 * Updates the output data of this processor.
	 *
	 * @param newOutput The new output data
	 */
	private void updateOutputField(T newOutput) {
		try {
			writeLock.lock();

			outputData(newOutput);

		} finally {
			writeLock.unlock();
		}
	}
}