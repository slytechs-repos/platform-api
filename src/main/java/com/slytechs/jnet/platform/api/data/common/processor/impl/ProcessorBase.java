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
package com.slytechs.jnet.platform.api.data.common.processor.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import com.slytechs.jnet.platform.api.data.DataProcessor;
import com.slytechs.jnet.platform.api.data.DataType;
import com.slytechs.jnet.platform.api.data.common.processor.Processor;
import com.slytechs.jnet.platform.api.data.common.processor.ProcessorMapper;
import com.slytechs.jnet.platform.api.data.handler.ProcessingError;
import com.slytechs.jnet.platform.api.data.handler.ProcessorErrorSupport;
import com.slytechs.jnet.platform.api.data.pipeline.ErrorPolicy;
import com.slytechs.jnet.platform.api.util.DoublyLinkedElement;
import com.slytechs.jnet.platform.api.util.Enableable.FluentEnableable;
import com.slytechs.jnet.platform.api.util.function.GuardedCode;
import com.slytechs.jnet.platform.api.util.Prioritizable;
import com.slytechs.jnet.platform.api.util.Registration;

/**
 * Base implementation of a data processor that provides common functionality
 * like:
 * <ul>
 * <li>Priority based processing order
 * <li>Enable/disable state
 * <li>Error handling
 * <li>Input/output transformations
 * <li>Peek operations
 * <li>Doubly-linked processor chain
 * </ul>
 *
 * @param <T> the type of data being processed
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc
 * @since 1.0.0
 */
public abstract class ProcessorBase<T>
		implements DataProcessor<T>,
		DoublyLinkedElement<ProcessorBase<T>>,
		Prioritizable.LowToHigh,
		FluentEnableable<DataProcessor<T>> {

	protected boolean enabledState;
	protected ReadWriteLock rwLock = new ReentrantReadWriteLock();
	public Lock readLock = rwLock.readLock();
	protected Lock writeLock = rwLock.writeLock();
//	protected Pipeline<T> pipeline;
	protected GuardedCode rwGuard = new GuardedCode(rwLock);
	private ProcessorBase<T> nextProcessor;
	private ProcessorBase<T> prevProcessor;
	protected String name;
	protected Object id;
	protected final T inlineData;
	protected T outputData;
	protected int priority;
	protected ProcessorErrorSupport errorSupport;
	private final List<T> peekers = new ArrayList<>();
	private final Processor<T> us = (Processor<T>) this;

	@SuppressWarnings("unchecked")
	protected ProcessorBase(int priority, String name) {
		this.priority = this instanceof BuiltinProcessor ? priority : Prioritizable.checkPriorityValue(priority);
		this.name = Objects.requireNonNull(name, "name");
		this.id = createId();
		this.inlineData = (T) this;
	}

	protected ProcessorBase(int priority, String name, ProcessorMapper<T> mapper) {
		this.priority = this instanceof BuiltinProcessor ? priority : Prioritizable.checkPriorityValue(priority);
		this.name = Objects.requireNonNull(name, "name");
		this.id = createId();
		this.inlineData = mapper.createMappedProcessor(this::getOutput, us);
	}

	protected ProcessorBase(int priority, String name, T inlineData) {
		this.priority = this instanceof BuiltinProcessor ? priority : Prioritizable.checkPriorityValue(priority);
		this.name = Objects.requireNonNull(name, "name");
		this.id = createId();
		this.inlineData = inlineData;
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.DataProcessor#getInput()
	 */
	@Override
	public T getInput() {
		return inlineData;
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.DataProcessor#getOutput()
	 */
	@Override
	public T getOutput() {
		return outputData;
	}

	protected Object createId() {
		return name + ":" + priority;
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.DataProcessor#dataType()
	 */
	@Override
	public DataType<T> dataType() {
		return parent.dataType();
	}

	public void clearParent() {
		setParent(null, null);
	}

	public IllegalArgumentException duplicateOutputException(Object id) {
		return new IllegalArgumentException("duplicate output transformer [id=%s]".formatted(id));
	}

	public IllegalArgumentException inputNotFoundException(Object id) {
		return new IllegalArgumentException("input not found [id=%s]".formatted(id));
	}

	/**
	 * @param id
	 * @return
	 */
	public IllegalArgumentException outputTransformerNotFound(Object id) {
		return new IllegalArgumentException("output not found [id=%s]".formatted(id));
	}

	public void relink() {
		if (parent == null)
			return; // Not linked yet

		var newOutput = nextElement().getInput();

		setOutput(newOutput);
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.DataProcessor#peek(java.lang.Object,
	 *      java.util.function.Consumer)
	 */
	@Override
	public DataProcessor<T> peek(T newPeeker, Consumer<Registration> registrar) {
		writeLock.lock();

		try {
			peekers.add(newPeeker);

			Registration newRegistration = () -> removePeeker(newPeeker);
			registrar.accept(newRegistration);

			relink();

			return us;
		} finally {
			writeLock.unlock();
		}
	}

	private final void removePeeker(T peeker) {
		writeLock.lock();

		try {
			peekers.remove(peeker);
		} finally {
			writeLock.unlock();
		}
	}

	public void setOutput(T newOutput) {
		writeLock.lock();

		try {
			if (peekers.isEmpty()) {
				this.outputData = newOutput;

			} else {

				/*
				 * Allocate +1 extra element for the array so we can append the next-processor
				 * output at the end, prefixed by the list of peekers
				 */
				var combinedOutputArray = dataType().arrayAllocator().apply(peekers.size() + 1);
				peekers.toArray(combinedOutputArray);
				combinedOutputArray[combinedOutputArray.length - 1] = newOutput;

				var wrappedArrayOutput = dataType().wrapArray(combinedOutputArray);
				this.outputData = wrappedArrayOutput;
			}

			// Propagate change upstream
			var prevProcessor = prevElement();
			if (prevProcessor != null)
				prevProcessor.relink();

		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * @param procError
	 */
	public void retryOnProcessingError(ProcessingError procError) {

	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.DataProcessor#disable()
	 */
	@Override
	public Processor<T> disable() {
		return setEnable(false);
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.DataProcessor#enable()
	 */
	@Override
	public Processor<T> enable() {
		return setEnable(true);
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.DataProcessor#getErrorPolicy()
	 */
	@Override
	public ErrorPolicy getErrorPolicy() {
		return errorSupport.getErrorPolicy();
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.DataProcessor#id()
	 */
	@Override
	public Object id() {
		return id;
	}

	private ProcessorParent<T> parent;

	public void setParent(ProcessorParent<T> newParent, ErrorHandlingDataFlow dataFlowErrorHandler) {
		/* Unlink from parent */

		if (newParent == null) {
			setRwLock(new ReentrantReadWriteLock());
			this.parent = null;
			this.enabledState = false;
			this.errorSupport = null;

			return;
		}

		/* Link to parent */

		this.enabledState = true;
		this.parent = newParent;
		this.errorSupport = new ProcessorErrorSupport(us, dataFlowErrorHandler);

		setRwLock(newParent.getRwLock());
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.DataProcessor#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		readLock.lock();

		try {
			return enabledState;
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.DataProcessor#name()
	 */
	@Override
	public String name() {
		return name;
	}

	/**
	 * @see com.slytechs.jnet.platform.api.util.DoublyLinkedElement#nextElement()
	 */
	@Override
	public ProcessorBase<T> nextElement() {
		return nextProcessor;
	}

	/**
	 * @see com.slytechs.jnet.platform.api.util.DoublyLinkedElement#nextElement(java.lang.Object)
	 */
	@Override
	public void nextElement(ProcessorBase<T> e) {
		this.nextProcessor = e;
	}

	/**
	 * @see com.slytechs.jnet.platform.api.util.DoublyLinkedElement#prevElement()
	 */
	@Override
	public ProcessorBase<T> prevElement() {
		return prevProcessor;
	}

	/**
	 * @see com.slytechs.jnet.platform.api.util.DoublyLinkedElement#prevElement(java.lang.Object)
	 */
	@Override
	public void prevElement(ProcessorBase<T> e) {
		this.prevProcessor = e;
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.DataProcessor#priority()
	 */
	@Override
	public int priority() {
		readLock.lock();

		try {
			return this.priority;
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.DataProcessor#setPriority(int)
	 */
	@Override
	public Processor<T> setPriority(int newPriority) {

		writeLock.lock();

		try {
			this.priority = newPriority;
			this.id = createId();

			// The processor order has likely changed
			relink();

			return us;

		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.DataProcessor#setEnable(boolean)
	 */
	@Override
	public Processor<T> setEnable(boolean newState) {

		this.enabledState = newState;

		parent.onEnableProcessor(this, newState);

		return us;
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.DataProcessor#setErrorPolicy(com.slytechs.jnet.platform.api.data.pipeline.ErrorPolicy)
	 */
	@Override
	public void setErrorPolicy(ErrorPolicy policy) {
		errorSupport.setErrorPolicy(policy);
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.DataProcessor#setId(java.lang.Object)
	 */
	@Override
	public Processor<T> setId(Object newId) {
		this.id = Objects.requireNonNull(id, "id");

		return us;
	}

	protected void handleError(Throwable error, Object data) {

		while (error.getCause() != null)
			error = error.getCause();

		error.printStackTrace();

//		errorSupport.handleError(error, data);
	}

	protected void setRwLock(ReadWriteLock rwLock) {
		this.rwLock = rwLock;
		this.readLock = rwLock.readLock();
		this.writeLock = rwLock.writeLock();

		this.rwGuard = new GuardedCode(rwLock, e -> handleError(e, inlineData));
	}

}