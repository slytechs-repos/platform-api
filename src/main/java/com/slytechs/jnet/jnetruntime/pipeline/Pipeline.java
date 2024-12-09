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

import com.slytechs.jnet.jnetruntime.pipeline.Processor.ProcessorMapper;
import com.slytechs.jnet.jnetruntime.util.DoublyLinkedPriorityQueue;
import com.slytechs.jnet.jnetruntime.util.Registration;

/**
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public abstract class Pipeline<T> {

	public interface DataReducer<T> {

		static <T> DataReducer<T> of(DataReducer<T> reducer, T empty) {

			return new DataReducer<T>() {

				@Override
				public T empty() {
					return empty;
				}

				@Override
				public T reduceArray(T[] array) {
					return reducer.reduceArray(array);
				}
			};
		}

		@SuppressWarnings("unchecked")
		default T empty() {
			return reduceArray((T[]) new Object[0]);
		}

		default T optimizeArray(T[] array) {
			if ((array == null) || (array.length == 0) || (array.length == 1) && (array[0] == null))
				return empty();

			if (array.length == 1)
				return array[0];

			return reduceArray(array);
		}

		T reduceArray(T[] array);
	}

	private final Lock readLock;
	private final Lock writeLock;

	private final Map<String, Processor<T>> processorsByName = new HashMap<>();
	private final Map<Object, Processor<T>> processorsById = new HashMap<>();
	private final Queue<Processor<T>> activeProcessors = new DoublyLinkedPriorityQueue<Processor<T>>();

	private final Head<T> head = new Head<>(this);
	private final Tail<T> tail = new Tail<>(this);
	private final DataReducer<T> dataReducer;
	private String name;

	protected Pipeline(String name, DataReducer<T> reducer) {
		this.dataReducer = reducer;
		this.name = Objects.requireNonNull(name, "name");
		var rwLock = new ReentrantReadWriteLock();
		this.readLock = rwLock.readLock();
		this.writeLock = rwLock.writeLock();
	}

	public String name() {
		return name;
	}

	void setEnableProcessor(Processor<T> processor, boolean newState) {
		if (newState == processor.isEnabled())
			return;

		writeLock.lock();
		try {

			processor.enabledState = newState;

			if (newState == true) {
				activeProcessors.offer(processor);
				relinkProcessor(processor);
				relinkProcessor(processor.prevElement());

			} else {

				var prev = processor.prevElement();
				activeProcessors.remove(processor);
				relinkProcessor(prev);
			}

		} finally {
			writeLock.unlock();
		}
	}

	final T reduceDataArray(T[] dataArray) {
		return dataReducer.optimizeArray(dataArray);
	}

	public Pipeline<T> onNewRegistration(Consumer<Registration> action) {
		return onNewProcessor((r, p) -> action.accept(r));
	}

	private final List<BiConsumer<Registration, Processor<T>>> registrationListeners = new ArrayList<>();

	public Pipeline<T> onNewProcessor(BiConsumer<Registration, Processor<T>> action) {
		registrationListeners.add(action);

		return this;
	}

	public Head<T> head() {
		return head;
	}

	public Tail<T> tail() {
		return tail;
	}
	
	public void addProcessor(String name, ProcessorMapper<T> unary) {
		
	}
	

	public Registration addProcessor(Processor<T> newProcessor) {
		String name = newProcessor.name();
		Object id = newProcessor.id();

		writeLock.lock();
		try {
			if (processorsById.containsKey(id))
				throw new IllegalArgumentException("processor already registered [%s]".formatted(name));

			if (newProcessor.pipeline != null)
				throw new IllegalStateException("processor already initialized [%s]".formatted(name));

			// Initialize new processor
			newProcessor.pipeline = this;
			newProcessor.readLock = this.readLock;

			// Fast lookup
			processorsByName.put(name, newProcessor);
			processorsById.put(id, newProcessor);

			// doubly linked list of processors (priority sorted)
			activeProcessors.offer(newProcessor);

			relinkProcessor(newProcessor);
			relinkProcessor(newProcessor.prevElement());

			Registration reg = () -> removeProcessor(newProcessor);

			registrationListeners.forEach(l -> l.accept(reg, newProcessor));

			return reg;

		} finally {
			writeLock.unlock();
		}

	}

	private void removeProcessor(Processor<T> processor) {
		String name = processor.name();
		Object id = processor.id();

		Processor<T> prevProcessor = processor.prevElement();

		activeProcessors.remove(processor);

		processorsByName.remove(name);
		processorsById.remove(id);

		// Reset processor
		processor.pipeline = null;
		processor.readLock = null;

		relinkProcessor(prevProcessor);
	}

	private void relinkProcessor(Processor<T> processor) {
		processor.outputData = processor.nextElement().getInput();

	}

	public Processor<T> getProcessor(String name) {
		readLock.lock();

		try {
			var p = processorsByName.get(name);
			if (p == null)
				throw new IllegalArgumentException("processor not found [name=%s]".formatted(name));

			return p;
		} finally {
			readLock.unlock();
		}
	}

	public Processor<T> getProcessor(Object id) {
		readLock.lock();

		try {
			var p = processorsById.get(id);
			if (p == null)
				throw new IllegalArgumentException("processor not found [id=%s]".formatted(id));

			return p;
		} finally {
			readLock.unlock();
		}
	}
}
