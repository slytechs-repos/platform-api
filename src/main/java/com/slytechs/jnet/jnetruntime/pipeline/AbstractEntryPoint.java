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

import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.InputTransformer.EntryPoint;
import com.slytechs.jnet.jnetruntime.util.HasName;
import com.slytechs.jnet.jnetruntime.util.HasPriority;

/**
 * Abstract base class for entry points in a pipeline's input transformer.
 * 
 * <p>
 * This class implements the EntryPoint interface and provides a basic
 * implementation for entry point functionality. It acts as a bridge between the
 * external data source and the pipeline's input transformer.
 * </p>
 *
 * @param <T> The type of input data handled by this entry point
 * @author Mark Bednarczyk
 */
public abstract class AbstractEntryPoint<T>
		extends AbstractComponent<EntryPoint<T>>
		implements EntryPoint<T>, HasName {

	/** The id. */
	private final String id;

	/** The input. */
	private final AbstractInput<T, ?, ?> input;

	private T inputData;
	private final DataType inputType;

	/**
	 * Constructs a new AbstractEntryPoint with the specified input transformer and
	 * identifier.
	 *
	 * @param input The AbstractInput instance this entry point is associated with
	 * @param id    The unique identifier for this entry point
	 */
	public AbstractEntryPoint(AbstractInput<T, ?, ?> input, String id) {
		super(input, id, HasPriority.DEFAULT_PRIORITY_VALUE);
		this.id = id;
		this.input = input;
		this.inputType = input.inputType();

		inputData(input.inputData());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * Returns the unique identifier of this entry point.
	 * </p>
	 */
	@Override
	public String id() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * Retrieves the input data from the associated input transformer.
	 * </p>
	 */
	@Override
	public T inputData() {
		try {
			readLock.lock();

			checkIfIsRegistered();
			checkIfIsEnabled();

			return inputData;
		} finally {
			readLock.unlock();
		}
	}

	void inputData(T newData) {
		this.inputData = newData;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * Retrieves the input data type from the associated input transformer.
	 * </p>
	 */
	@Override
	public DataType inputType() {
		return inputType;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.AbstractComponent#onBypass(boolean)
	 */
	@Override
	protected void onBypass(boolean newValue) {
		checkIfIsRegistered();

		try {
			writeLock.lock();

			if (newValue)
				inputData = inputType.empty();
			else
				inputData = input.inputData();

		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * Unregisters this entry point from its associated input transformer.
	 * </p>
	 */
	@Override
	public void unregister() {
		input.unregister(this);
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.AbstractComponent#onEnable(boolean)
	 */
	@Override
	protected void onEnable(boolean newValue) {
		try {
			writeLock.lock();

			if (newValue)
				inputData = null;
			else
				inputData = input.inputData();

		} finally {
			writeLock.unlock();
		}
	}

}