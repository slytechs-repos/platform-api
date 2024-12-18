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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.slytechs.jnet.jnetruntime.function.GuardedCode;
import com.slytechs.jnet.jnetruntime.util.DoublyLinkedElement;
import com.slytechs.jnet.jnetruntime.util.Enableable.FluentEnableable;
import com.slytechs.jnet.jnetruntime.util.Registration;

/**
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public abstract class Processor<T>
		implements DoublyLinkedElement<Processor<T>>, Comparable<Processor<T>>, FluentEnableable<Processor<T>> {

	public interface ProcessorMapper<T> {

		interface SimpleProcessorMapper<T> extends ProcessorMapper<T> {

			T createMappedProcessor(Supplier<T> sink);

			@Override
			default T createMappedProcessor(Supplier<T> sink, Processor<T> processor) {
				return createMappedProcessor(sink);
			}

		}

		T createMappedProcessor(Supplier<T> sink, Processor<T> processor);
	}

	boolean enabledState;

	protected ReadWriteLock rwLock = new ReentrantReadWriteLock();

	protected Lock readLock = rwLock.readLock();
	protected Lock writeLock = rwLock.writeLock();
	protected Pipeline<T> pipeline;
	protected GuardedCode rwGuard = new GuardedCode(rwLock);

	private Processor<T> nextProcessor;
	private Processor<T> prevProcessor;
	private String name;

	private Object id;
	private final T inlineData;

	protected T outputData;

	private int priority;
	private ProcessorErrorSupport errorSupport;
	private final List<T> peekers = new ArrayList<>();

	@SuppressWarnings("unchecked")
	protected Processor(int priority, String name) {
		this.priority = priority;
		this.name = Objects.requireNonNull(name, "name");
		this.id = createId();

		this.inlineData = (T) this;
	}

	protected Processor(int priority, String name, ProcessorMapper<T> mapper) {
		this.priority = priority;
		this.name = Objects.requireNonNull(name, "name");
		this.id = createId();
		this.inlineData = mapper.createMappedProcessor(this::getOutput, this);
	}

	protected Processor(int priority, String name, T inlineData) {
		this.priority = priority;
		this.name = Objects.requireNonNull(name, "name");
		this.id = createId();
		this.inlineData = inlineData;
	}

	private Object createId() {
		return name + ":" + priority;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Processor<T> o) {
		if (this.priority == o.priority())
			return 0;

		return (this.priority < o.priority()) ? -1 : 1;
	}

	public DataType<T> dataType() {
		return pipeline.dataType();
	}

	@Override
	public Processor<T> disable() {
		return setEnable(false);
	}

	@Override
	public Processor<T> enable() {
		return setEnable(true);
	}

	public ErrorPolicy getErrorPolicy() {
		return errorSupport.getErrorPolicy();
	}

	public T getInput() {
		return inlineData;
	}

	public T getOutput() {
		return outputData;
	}

	protected void handleError(Throwable error, Object data) {

		while (error.getCause() != null)
			error = error.getCause();

		error.printStackTrace();

//		errorSupport.handleError(error, data);
	}

	public Object id() {
		return id;
	}

	void initializePipeline(Pipeline<T> newPipeline) {
		this.pipeline = newPipeline;
		this.enabledState = true;
		this.errorSupport = new ProcessorErrorSupport(this, newPipeline);

		this.setRwLock(newPipeline.rwLock);
	}

	@Override
	public boolean isEnabled() {
		readLock.lock();

		try {
			return enabledState;
		} finally {
			readLock.unlock();
		}
	}

	public String name() {
		return name;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.util.DoublyLinkedElement#nextElement()
	 */
	@Override
	public Processor<T> nextElement() {
		return nextProcessor;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.util.DoublyLinkedElement#nextElement(java.lang.Object)
	 */
	@Override
	public void nextElement(Processor<T> e) {
		this.nextProcessor = e;
	}

	protected Processor<T> peek(T newPeeker) {
		return peek(newPeeker, r -> {});
	}

	protected Processor<T> peek(T newPeeker, Consumer<Registration> registrar) {
		writeLock.lock();

		try {
			peekers.add(newPeeker);

			Registration newRegistration = () -> removePeeker(newPeeker);
			registrar.accept(newRegistration);

			relink();

			return this;
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.util.DoublyLinkedElement#prevElement()
	 */
	@Override
	public Processor<T> prevElement() {
		return prevProcessor;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.util.DoublyLinkedElement#prevElement(java.lang.Object)
	 */
	@Override
	public void prevElement(Processor<T> e) {
		this.prevProcessor = e;
	}

	public int priority() {
		readLock.lock();

		try {
			return this.priority;
		} finally {
			readLock.unlock();
		}
	}

	public Processor<T> setPriority(int newPriority) {

		writeLock.lock();

		try {
			this.priority = newPriority;
			this.id = createId();

			// The processor order has likely changed
			relink();

			return this;

		} finally {
			writeLock.unlock();
		}
	}

	void relink() {
		if (pipeline == null)
			return; // Not linked yet

		var newOutput = nextElement().getInput();

		setOutput(newOutput);
	}

	private final void removePeeker(T peeker) {
		writeLock.lock();

		try {
			peekers.remove(peeker);
		} finally {
			writeLock.unlock();
		}
	}

	void reset() {
		setRwLock(new ReentrantReadWriteLock());
		this.pipeline = null;
		this.enabledState = false;
		this.errorSupport = null;
	}

	/**
	 * @param procError
	 */
	protected void retryOnProcessingError(ProcessingError procError) {

	}

	@Override
	public Processor<T> setEnable(boolean newState) {
		pipeline.setEnableProcessor(this, newState);

		return this;
	}

	public void setErrorPolicy(ErrorPolicy policy) {
		errorSupport.setErrorPolicy(policy);
	}

	public Processor<T> setId(Object newId) {
		this.id = Objects.requireNonNull(id, "id");

		return this;
	}

	void setOutput(T newOutput) {
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

	private void setRwLock(ReadWriteLock rwLock) {
		this.rwLock = rwLock;
		this.readLock = rwLock.readLock();
		this.writeLock = rwLock.writeLock();

		this.rwGuard = new GuardedCode(rwLock, e -> handleError(e, inlineData));
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name + ":" + priority;
	}

}
