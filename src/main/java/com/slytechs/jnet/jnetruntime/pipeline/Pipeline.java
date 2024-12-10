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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.slytechs.jnet.jnetruntime.pipeline.Processor.ProcessorMapper;
import com.slytechs.jnet.jnetruntime.util.DoublyLinkedPriorityQueue;
import com.slytechs.jnet.jnetruntime.util.Registration;

/**
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public abstract class Pipeline<T> {

	private static class DefaultProcessor<T> extends Processor<T> {

		/**
		 * @param priority
		 * @param name
		 * @param inlineData
		 */
		protected DefaultProcessor(int priority, String name, ProcessorMapper<T> mapper) {
			super(priority, name, mapper);
		}

	}

	final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
	private final Lock readLock;
	private final Lock writeLock;

	private final Map<Object, Processor<T>> processorsById = new HashMap<>();
	private final Queue<Processor<T>> activeProcessors = new DoublyLinkedPriorityQueue<Processor<T>>();

	private final Head<T> head;
	private final Tail<T> tail;
	private final DataType<T> dataType;
	private String name;

	private final List<BiConsumer<Registration, Processor<T>>> registrationListeners = new ArrayList<>();

	protected Pipeline(String name, DataType<T> reducer) {
		this.dataType = reducer;
		this.name = Objects.requireNonNull(name, "name");
		this.readLock = rwLock.readLock();
		this.writeLock = rwLock.writeLock();

		this.head = new Head<>(this);
		this.tail = new Tail<>(this);

		activeProcessors.offer(head);
		activeProcessors.offer(tail);
	}

	public final Registration addProcessor(int priority, String name, ProcessorMapper<T> mapper) {

		var processor = new DefaultProcessor<T>(priority, name, mapper);

		return addProcessor(processor);
	}

	public final Registration addProcessor(Processor<T> newProcessor) {
		String name = newProcessor.name();
		Object id = newProcessor.id();

		writeLock.lock();
		try {
			if (processorsById.containsKey(id))
				throw new IllegalArgumentException("processor already registered [%s]".formatted(name));

			if (newProcessor.isEnabled())
				throw new IllegalStateException("processor already initialized [%s]".formatted(name));

			// Initialize new processor
			newProcessor.initialize(this);

			// Fast lookup
			processorsById.put(id, newProcessor);

			// doubly linked list of processors (priority sorted)
			activeProcessors.offer(newProcessor);

			newProcessor.relink();

			Registration reg = () -> removeProcessor(newProcessor);

			registrationListeners.forEach(l -> l.accept(reg, newProcessor));

			return reg;

		} finally {
			writeLock.unlock();
		}

	}

	public final DataType<T> dataType() {
		return dataType;
	}

	public Processor<T> getProcessor(Object id) {
		readLock.lock();

		try {
			var p = processorsById.get(id);
			if (p == null)
				throw processorNotFoundException(id);

			return p;
		} finally {
			readLock.unlock();
		}
	}

	public final Head<T> head() {
		return head;
	}

	IllegalArgumentException inputNotFoundException(Object id) {
		return new IllegalArgumentException("input not found [id=%s]".formatted(id));
	}

	public String name() {
		return name;
	}

	public Pipeline<T> onNewProcessor(BiConsumer<Registration, Processor<T>> action) {
		registrationListeners.add(action);

		return this;
	}

	public Pipeline<T> onNewRegistration(Consumer<Registration> action) {
		return onNewProcessor((r, p) -> action.accept(r));
	}

	/**
	 * @param id
	 * @return
	 */
	IllegalArgumentException outputTransformerNotFound(Object id) {
		return new IllegalArgumentException("output not found [id=%s]".formatted(id));
	}

	IllegalArgumentException processorNotFoundException(Object id) {
		return new IllegalArgumentException("processor not found [id=%s]".formatted(id));
	}

	IllegalArgumentException duplicateOutputException(Object id) {
		return new IllegalArgumentException("duplicate output transformer [id=%s]".formatted(id));
	}

	private void removeProcessor(Processor<T> processor) {
		Object id = processor.id();

		Processor<T> prevProcessor = processor.prevElement();

		activeProcessors.remove(processor);

		processorsById.remove(id);

		// Reset processor
		processor.reset();

		prevProcessor.relink();
	}

	void setEnableProcessor(Processor<T> processor, boolean newState) {
		if (newState == processor.isEnabled())
			return;

		writeLock.lock();
		try {

			processor.enabledState = newState;

			if (newState == true) {
				activeProcessors.offer(processor);
				processor.relink();

			} else {

				var prevProcessor = processor.prevElement();
				activeProcessors.remove(processor);
				prevProcessor.relink();
			}

		} finally {
			writeLock.unlock();
		}
	}

	public final Tail<T> tail() {
		return tail;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name() + " ["
				+ activeProcessors.stream()
						.map(Processor::toString)
						.collect(Collectors.joining(" > "))
				+ "]";
	}

	public <IN> IN inputConnector(Object id) {
		return head.connector(id);
	}

	public <IN> IN inputConnector(Object id, Class<IN> inClass) {
		return head.connector(id);
	}

	public <OUT> Registration outputConnect(Object id, T sink) {
		return tail().getOutput(id).connect(sink);
	}
}
