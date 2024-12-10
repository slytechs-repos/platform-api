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

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import com.slytechs.jnet.jnetruntime.util.DoublyLinkedElement;

/**
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public abstract class Processor<T>
		implements DoublyLinkedElement<Processor<T>>, Comparable<Processor<T>> {

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[" + name + "]";
	}

	public interface ProcessorMapper<T> {
		T createMappedProcessor(Supplier<T> sink);
	}

	boolean enabledState;

	protected ReadWriteLock rwLock = new ReentrantReadWriteLock();
	protected Lock readLock = rwLock.readLock();
	protected Lock writeLock = rwLock.writeLock();

	protected Pipeline<T> pipeline;

	private Processor<T> nextProcessor;

	private Processor<T> prevProcessor;

	private String name;

	private Object id;

	private final T inlineData;

	protected T outputData;
	private int priority;

	@SuppressWarnings("unchecked")
	protected Processor(int priority, String name) {
		this.priority = priority;
		this.name = Objects.requireNonNull(name, "name");
		this.id = name;

		this.inlineData = (T) this;
	}

	protected Processor(int priority, String name, T inlineData) {
		this.priority = priority;
		this.name = Objects.requireNonNull(name, "name");
		this.id = name;
		this.inlineData = inlineData;
	}

	protected Processor(int priority, String name, ProcessorMapper<T> mapper) {
		this.priority = priority;
		this.name = Objects.requireNonNull(name, "name");
		this.id = name;
		this.inlineData = mapper.createMappedProcessor(this::getOutput);
	}

	void setRwLock(ReadWriteLock rwLock) {
		this.rwLock = rwLock;
		this.readLock = rwLock.readLock();
		this.writeLock = rwLock.writeLock();
	}

	void reset() {
		setRwLock(new ReentrantReadWriteLock());
		this.pipeline = null;
		this.enabledState = false;
	}

	void initialize(Pipeline<T> newPipeline) {
		this.pipeline = newPipeline;
		this.enabledState = true;

		this.setRwLock(newPipeline.rwLock);
	}

	public DataType<T> dataType() {
		return pipeline.dataType();
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

	public Processor<T> disable() {
		return setEnable(false);
	}

	public Processor<T> enable() {
		return setEnable(true);
	}

	public T getInput() {
		return inlineData;
	}

	public T getOutput() {
		return outputData;
	}

	void relink() {
		var newOutput = nextElement().getInput();

		setOutput(newOutput);
	}

	void setOutput(T newOutput) {
		var oldOutput = this.outputData;
		if (oldOutput == newOutput)
			return; // No change

		this.outputData = newOutput;

		// Propagate change upstream
		var prevProcessor = prevElement();
		if (prevProcessor != null)
			prevProcessor.setOutput(this.getInput());
	}

	public Object id() {
		return id;
	}

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
		return this.priority;
	}

	public Processor<T> setEnable(boolean newState) {
		pipeline.setEnableProcessor(this, newState);

		return this;
	}

	public Processor<T> setId(Object newId) {
		this.id = Objects.requireNonNull(id, "id");

		return this;
	}

}
