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

import static com.slytechs.jnet.jnetruntime.pipeline.PipelineUtils.ID;

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
		extends AbstractNode<T_BASE>
		implements DataProcessor<T, T_BASE>, DownstreamDataListener<T>, DoublyLinkedElement<AbstractProcessor<T, ?>> {

	/** The data type. */
	private final DataType dataType;

	/** The input data. */
	private T inputData;

	/** The output data. */
	protected T outputData; // Auto maintained and updated by DataList object
	private T outputNextIn;

	/** The output list. */
	private final DataList<T> outputList;

	/** The pipeline. */
	private final AbstractPipeline<T, ?> pipeline;

	/** The next processor. */
	AbstractProcessor<T, ?> nextProcessor;

	AbstractProcessor<T, ?> prevProcessor;

	protected AbstractProcessor(int priority, String name, DataType dataType) {
		super(name);

		this.pipeline = null;
		this.outputList = null;
		this.dataType = dataType;

	}

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

		if (!(pipeline instanceof AbstractPipeline<T, ?> apipeline)) {
			throw new IllegalArgumentException("invalid pipeline [%s]".formatted(pipeline.name()));
		}

		if (!type.isCompatibleWith(getClass())) {
			throw new IllegalArgumentException("processor subclass must implement data interface [%s]"
					.formatted(type.dataClass()));
		}

		this.pipeline = apipeline;
		this.inputData = (T) this;
		this.dataType = type;
		this.outputList = new DataList<>(type, this::setGuardedOutputField);
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
		if (!(pipeline instanceof AbstractPipeline<T, ?> ac)) {
			throw new IllegalArgumentException("invalid pipeline [%s]".formatted(pipeline.name()));
		}
		this.pipeline = ac;
		this.inputData = data;
		this.dataType = type;
		this.outputList = new DataList<>(type, this::setGuardedOutputField);
	}
	
	private void removeFromOutputList(T data) {
		try {
			writeLock.lock();
			outputList.remove(data);
			
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * Adds a local output to this processor's output list.
	 *
	 * @param newData The local output to add
	 * @return A Registration object for unregistering the output
	 */
	final protected Registration addToOutputList(T newData) {
		try {
			writeLock.lock();

			outputList.addFirst(newData);
			
			return () -> removeFromOutputList(newData);

		} finally {
			writeLock.unlock();
		}
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
		if (inputData == null) {
			inputData = inputType().empty();
		}
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
	 * @see com.slytechs.jnet.jnetruntime.pipeline.AbstractNode#onBypass(boolean)
	 */
	@Override
	protected void onBypass(boolean newValue) {
		if (isEnabled() == false) {
			return;
		}
		
		if (newValue == true) {
			prevProcessor.onDataDownstreamChange(outputData);
		} else {
			prevProcessor.onDataDownstreamChange(inputData);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onEnable(boolean newValue) {
		checkIfIsRegistered();

		if (newValue) {
			pipeline.activateProcessor(this);
		} else {
			pipeline.deactivateProcessor(this);
		}
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.AbstractNode#onPriorityChange(int)
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
				+ ", output=" + ID(inputData)
				+ ", input=" + ID(outputData)
				+ ", dataType=" + dataType
				+ ", name=" + name()
				+ "]";
	}

	/**
	 * Updates the output data of this processor.
	 *
	 * @param newOutput The new output data
	 */
	private void setGuardedOutputField(T newOutput) {
		try {
			writeLock.lock();

			this.outputData = newOutput;
		} finally {
			writeLock.unlock();
		}
	}

	private boolean updateOutputList(T newOutput) {
		try {
			writeLock.lock();

			// No change
			if (newOutput == outputNextIn) {
				return false;
			}

			if (registrationNextIn != null) {
				registrationNextIn.unregister();
				registrationNextIn = null;
			}

			if (newOutput != null) {
				outputList.add(newOutput);
				registrationNextIn = () -> outputList.remove(newOutput);
			}

			this.outputNextIn = newOutput;

			return true;
		} finally {
			writeLock.unlock();
		}
	}

	private Registration registrationNextIn;

	@Override
	public void onDataDownstreamChange(T newData) {

		try {
			writeLock.lock();

			boolean isEffectiveBypass = (isBypassed() || newData == null);

			/*
			 * If bypassed, then the upstream node needs the pass through newData to bypass
			 * us, otherwise, our inline-input doesn't change so upstream does not need a
			 * notification.
			 * 
			 * Null new data is also a implicit bypass, but no is-bypassed flags are
			 * modified, but effectively with newData == null we are bypassed as well, so we
			 * pass on.
			 */
			if (isEffectiveBypass) {
				updateOutputList(null);
				prevProcessor.onDataDownstreamChange(newData);

			} else {
				updateOutputList(newData);
			}

		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public void linkAllUpstream(T newData) {
		try {
			writeLock.lock();
			
			updateOutputList(newData);

			boolean isEffectiveBypass = (isBypassed() || newData == null);

			if (isEffectiveBypass) {
				prevProcessor.linkAllUpstream(newData);

			} else {
				prevProcessor.linkAllUpstream(inputData);
			}

		} finally {
			writeLock.unlock();
		}

	}
}
