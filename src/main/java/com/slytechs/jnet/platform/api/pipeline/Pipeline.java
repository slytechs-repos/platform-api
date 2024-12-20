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
package com.slytechs.jnet.platform.api.pipeline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.slytechs.jnet.platform.api.pipeline.Processor.ProcessorMapper;
import com.slytechs.jnet.platform.api.pipeline.Processor.ProcessorMapper.SimpleProcessorMapper;
import com.slytechs.jnet.platform.api.util.DoublyLinkedPriorityQueue;
import com.slytechs.jnet.platform.api.util.Registration;

/**
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public abstract class Pipeline<T> implements ErrorHandlingPipeline {

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

	private final PipelineEventSupport eventSupport;
	private final ArrayList<BiConsumer<Registration, Processor<T>>> processorRegistrations;

	protected Pipeline(String name, DataType<T> dataType) {
		this.dataType = dataType;
		this.name = Objects.requireNonNull(name, "name");
		this.readLock = rwLock.readLock();
		this.writeLock = rwLock.writeLock();
		this.eventSupport = new PipelineEventSupport(this);
		this.processorRegistrations = new ArrayList<>();

		this.head = new Head<>(this);
		this.tail = new Tail<>(this);

		activeProcessors.offer(head);
		activeProcessors.offer(tail);
	}

	public Registration addAttributeChangeListener(Consumer<AttributeEvent> listener) {
		return addPipelineListener(new PipelineListener() {

			@Override
			public void onAttributeChanged(AttributeEvent evt) {
				listener.accept(evt);
			}

			@Override
			public void onError(PipelineErrorEvent evt) {
			}

			@Override
			public void onProcessorChanged(ProcessorEvent evt) {
			}
		});
	}

	public Registration addPipelineErrorConsumer(Consumer<Throwable> listener) {
		return addPipelineListener(new PipelineListener() {

			@Override
			public void onAttributeChanged(AttributeEvent evt) {
			}

			@Override
			public void onError(PipelineErrorEvent evt) {
				listener.accept(evt.getError());
			}

			@Override
			public void onProcessorChanged(ProcessorEvent evt) {
			}
		});
	}

	public Registration addPipelineErrorListener(Consumer<PipelineErrorEvent> listener) {
		return addPipelineListener(new PipelineListener() {

			@Override
			public void onAttributeChanged(AttributeEvent evt) {
			}

			@Override
			public void onError(PipelineErrorEvent evt) {
				listener.accept(evt);
			}

			@Override
			public void onProcessorChanged(ProcessorEvent evt) {
			}
		});
	}

	public Registration addPipelineListener(PipelineListener listener) {
		var reg = eventSupport.addListener(listener);

		head.relink();

		return reg;
	}

	public final Processor<T> addProcessor(int priority, String name, SimpleProcessorMapper<T> mapper) {
		return addProcessor(priority, name, (ProcessorMapper<T>) mapper);
	}

	public final Processor<T> addProcessor(int priority, String name, ProcessorMapper<T> mapper) {

		var processor = new DefaultProcessor<T>(priority, name, mapper);

		var _ = registerProcessor(processor);

		return processor;
	}

	public final Processor<T> addProcessor(Processor<T> newProcessor) {

		var _ = registerProcessor(newProcessor);

		return newProcessor;
	}

	public Registration addProcessorChangeListener(Consumer<ProcessorEvent> listener) {
		return addPipelineListener(new PipelineListener() {

			@Override
			public void onAttributeChanged(AttributeEvent evt) {
			}

			@Override
			public void onError(PipelineErrorEvent evt) {
			}

			@Override
			public void onProcessorChanged(ProcessorEvent evt) {
				listener.accept(evt);
			}
		});
	}

	public final DataType<T> dataType() {
		return dataType;
	}

	IllegalArgumentException duplicateOutputException(Object id) {
		return new IllegalArgumentException("duplicate output transformer [id=%s]".formatted(id));
	}

	protected void fireAttributeChanged(String name, Object oldValue, Object newValue) {
		eventSupport.fireAttributeChanged(name, oldValue, newValue);
	}

	protected void fireError(Throwable error, ErrorSeverity severity) {
		eventSupport.fireError(error, severity);
	}

	protected void fireProcessorChanged(Processor<?> processor, ProcessorEventType type) {
		eventSupport.fireProcessorChanged(processor, type);
	}

	@Override
	public ErrorPolicy getDefaultErrorPolicy() {
		return ErrorPolicy.SUPPRESS;
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

	@Override
	public void handleProcessingError(ProcessingError error) {
		fireError(error.getCause(), error.getSeverity());
	}

	final boolean hasErrorListeners() {
		return !eventSupport.isEmpty();
	}

	public final Head<T> head() {
		return head;
	}

	public <IN> IN in(Object id) {
		return head.connector(id);
	}

	public <IN> IN in(Object id, Class<IN> inClass) {
		return head.connector(id);
	}

	public <IN> IN in(Object id, DataType<IN> dataType) {
		return head.connector(id);
	}

	IllegalArgumentException inputNotFoundException(Object id) {
		return new IllegalArgumentException("input not found [id=%s]".formatted(id));
	}

	public String name() {
		return name;
	}

	public Pipeline<T> onNewProcessor(BiConsumer<Registration, Processor<T>> action) {
		processorRegistrations.add(action);

		return this;
	}

	public Pipeline<T> onNewRegistration(Consumer<Registration> action) {
		return onNewProcessor((r, p) -> action.accept(r));
	}

	public <OUT> OutputTransformer<T, OUT> out(Object id) {
		return tail().getOutputTransformer(id);
	}

	public <OUT> Registration out(Object id, OUT sink) {
		return tail().getOutputTransformer(id).connect(sink);
	}

	public <OUT> Registration out(Object id, OUT sink, Class<OUT> outClass) {
		return tail().getOutputTransformer(id).connect(sink);
	}

	public <OUT> Registration out(Object id, OUT sink, DataType<OUT> dataType) {
		return tail().getOutputTransformer(id).connect(sink);
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

	public final Registration registerProcessor(Processor<T> newProcessor) {
		Object id = newProcessor.id();

		writeLock.lock();
		try {
			if (processorsById.containsKey(id))
				throw new IllegalArgumentException("processor already registered [%s]".formatted(id));

			if (newProcessor.isEnabled())
				throw new IllegalStateException("processor already initialized [%s]".formatted(id));

			// Initialize new processor
			newProcessor.initializePipeline(this);

			// Fast lookup
			processorsById.put(id, newProcessor);

			// doubly linked list of processors (priority sorted)
			activeProcessors.offer(newProcessor);

			Registration reg = () -> {
				removeProcessor(newProcessor);
				fireProcessorChanged(newProcessor, ProcessorEventType.REMOVED);
			};

			newProcessor.relink();
			processorRegistrations.forEach(l -> l.accept(reg, newProcessor));

			return reg;

		} catch (Exception e) {
			fireError(e, ErrorSeverity.ERROR);
			throw e;

		} finally {
			writeLock.unlock();
		}

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

	public void setDefaultErrorPolicy(ErrorPolicy policy) {
		eventSupport.setDefaultErrorPolicy(policy);
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
		if (activeProcessors.size() == 2)
			return name() + ": <no processors>";

		return name() + ": "
				+ activeProcessors.stream()
						.filter(p -> (p != head && p != tail))
						.map(Processor::toString)
						.collect(Collectors.joining(" → "));
	}

	public String toStringInOut() {
		return name() + ": "
				+ activeProcessors.stream()
						.map(Processor::toString)
						.collect(Collectors.joining(" → "));
	}
}
