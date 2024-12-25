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
package com.slytechs.jnet.platform.api.data.pipeline.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.slytechs.jnet.platform.api.data.DataType;
import com.slytechs.jnet.platform.api.data.common.processor.Processor;
import com.slytechs.jnet.platform.api.data.common.processor.ProcessorMapper;
import com.slytechs.jnet.platform.api.data.common.processor.ProcessorMapper.SimpleProcessorMapper;
import com.slytechs.jnet.platform.api.data.common.processor.impl.ErrorHandlingDataFlow;
import com.slytechs.jnet.platform.api.data.common.processor.impl.ProcessorBase;
import com.slytechs.jnet.platform.api.data.common.processor.impl.ProcessorParent;
import com.slytechs.jnet.platform.api.data.event.AttributeEvent;
import com.slytechs.jnet.platform.api.data.handler.ProcessingError;
import com.slytechs.jnet.platform.api.data.handler.ProcessorEvent;
import com.slytechs.jnet.platform.api.data.handler.ProcessorEventType;
import com.slytechs.jnet.platform.api.data.pipeline.DataPipeline;
import com.slytechs.jnet.platform.api.data.pipeline.ErrorPolicy;
import com.slytechs.jnet.platform.api.data.pipeline.ErrorSeverity;
import com.slytechs.jnet.platform.api.data.pipeline.PipelineErrorEvent;
import com.slytechs.jnet.platform.api.data.pipeline.PipelineEventSupport;
import com.slytechs.jnet.platform.api.data.pipeline.PipelineListener;
import com.slytechs.jnet.platform.api.data.pipeline.processor.Head;
import com.slytechs.jnet.platform.api.data.pipeline.processor.Tail;
import com.slytechs.jnet.platform.api.data.pipeline.transform.InputTransformer;
import com.slytechs.jnet.platform.api.data.pipeline.transform.OutputTransformer;
import com.slytechs.jnet.platform.api.util.Detail;
import com.slytechs.jnet.platform.api.util.DoublyLinkedPriorityQueue;
import com.slytechs.jnet.platform.api.util.Registration;

/**
 * Base implementation of a data pipeline that manages processor chains and data
 * flow. Provides error handling, event support, and processor lifecycle
 * management.
 *
 * <p>
 * Key features:
 * <ul>
 * <li>Priority-based processor ordering
 * <li>Thread-safe processor management
 * <li>Event notifications
 * <li>Error handling
 * </ul>
 *
 * @param <T> Type of data flowing through pipeline
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc
 * @since 1.0.0
 */
public abstract class PipelineBase<T>
		implements ErrorHandlingDataFlow, DataPipeline<T>, ProcessorParent<T> {

	/**
	 * Default processor implementation.
	 */
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

	/** Priority for head processor */
	public static final int HEAD_PROCESSOR_PRIORITY = -1;

	/** Priority for tail processor */
	public static final int TAIL_PROCESSOR_PRIORITY = Integer.MAX_VALUE;

	/** Lock for thread-safe operations */
	final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
	private final Lock readLock;
	private final Lock writeLock;

	/** Maps processor IDs to processors */
	private final Map<Object, Processor<T>> processorsById = new HashMap<>();

	/** Active processors ordered by priority */
	private final Queue<ProcessorBase<T>> activeProcessors = new DoublyLinkedPriorityQueue<ProcessorBase<T>>();

	/** Pipeline endpoints */
	private final Head<T> head;
	private final Tail<T> tail;

	/** Pipeline configuration */
	private final DataType<T> dataType;
	private String name;

	/** Event handling */
	private final PipelineEventSupport eventSupport;
	private final ArrayList<BiConsumer<Registration, Processor<T>>> processorRegistrations;

	/**
	 * Creates pipeline with name and data type.
	 *
	 * @param name     Pipeline name
	 * @param dataType Data type handled by pipeline
	 * @throws NullPointerException if name or dataType null
	 */
	protected PipelineBase(String name, DataType<T> dataType) {
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

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#addAttributeChangeListener(java.util.function.Consumer)
	 */
	@Override
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

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#addPipelineErrorConsumer(java.util.function.Consumer)
	 */
	@Override
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

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#addPipelineErrorListener(java.util.function.Consumer)
	 */
	@Override
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

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#addPipelineListener(com.slytechs.jnet.platform.api.data.pipeline.PipelineListener)
	 */
	@Override
	public Registration addPipelineListener(PipelineListener listener) {
		var reg = eventSupport.addListener(listener);

		head.relink();

		return reg;
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#addProcessor(int,
	 *      java.lang.String,
	 *      com.slytechs.jnet.platform.api.data.common.processor.Processor.ProcessorMapper)
	 */
	@Override
	public final Processor<T> addProcessor(int priority, String name, ProcessorMapper<T> mapper) {

		var processor = new DefaultProcessor<T>(priority, name, mapper);

		var _ = registerProcessor(processor);

		return processor;
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#addProcessor(int,
	 *      java.lang.String,
	 *      com.slytechs.jnet.platform.api.data.common.processor.ProcessorMapper.SimpleProcessorMapper)
	 */
	@Override
	public final Processor<T> addProcessor(int priority, String name, SimpleProcessorMapper<T> mapper) {
		return addProcessor(priority, name, (ProcessorMapper<T>) mapper);
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#addProcessor(com.slytechs.jnet.platform.api.data.common.processor.Processor)
	 */
	@Override
	public final Processor<T> addProcessor(Processor<T> newProcessor) {

		var _ = registerProcessor(newProcessor);

		return newProcessor;
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#addProcessorChangeListener(java.util.function.Consumer)
	 */
	@Override
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

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#dataType()
	 */
	@Override
	public final DataType<T> dataType() {
		return dataType;
	}

	public void fireAttributeChanged(String name, Object oldValue, Object newValue) {
		eventSupport.fireAttributeChanged(name, oldValue, newValue);
	}

	@Override
	public void fireError(Throwable error, ErrorSeverity severity) {
		eventSupport.fireError(error, severity);
	}

	public void fireProcessorChanged(Processor<?> processor, ProcessorEventType type) {
		eventSupport.fireProcessorChanged(processor, type);
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#getDefaultErrorPolicy()
	 */
	@Override
	public ErrorPolicy getDefaultErrorPolicy() {
		return ErrorPolicy.SUPPRESS;
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#getProcessor(java.lang.Object)
	 */
	@Override
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

	/**
	 * @see com.slytechs.jnet.platform.api.data.common.processor.impl.ProcessorParent#getRwLock()
	 */
	@Override
	public ReadWriteLock getRwLock() {
		return rwLock;
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#handleProcessingError(com.slytechs.jnet.platform.api.data.handler.ProcessingError)
	 */
	@Override
	public void handleProcessingError(ProcessingError error) {
		fireError(error.getCause(), error.getSeverity());
	}

	@Override
	public final boolean hasErrorListeners() {
		return !eventSupport.isEmpty();
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#head()
	 */
	@Override
	public final Head<T> head() {
		return head;
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#in(java.lang.Object)
	 */
	@Override
	public <IN> IN in(Object id) {
		return head.connector(id);
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#in(java.lang.Object,
	 *      java.lang.Class)
	 */
	@Override
	public <IN> IN in(Object id, Class<IN> inClass) {
		return head.connector(id);
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#in(java.lang.Object,
	 *      java.util.function.Consumer)
	 */
	@Override
	public <IN> void in(Object id, Consumer<IN> consumer) {
		consumer.accept(in(id));
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#in(java.lang.Object,
	 *      java.util.function.Consumer, java.lang.Class)
	 */
	@Override
	public <IN> void in(Object id, Consumer<IN> consumer, Class<IN> inClass) {
		consumer.accept(in(id));
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#in(java.lang.Object,
	 *      java.util.function.Consumer,
	 *      com.slytechs.jnet.platform.api.data.DataType)
	 */
	@Override
	public <IN> void in(Object id, Consumer<IN> consumer, DataType<IN> dataType) {
		consumer.accept(in(id));
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#in(java.lang.Object,
	 *      com.slytechs.jnet.platform.api.data.DataType)
	 */
	@Override
	public <IN> IN in(Object id, DataType<IN> dataType) {
		return head.connector(id);
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#inputTransformer(java.lang.Object)
	 */
	@Override
	public <IN> InputTransformer<IN, T> inputTransformer(Object id) {
		return head.getInputTransformer(id);
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#inputTransformer(java.lang.Object,
	 *      java.lang.Class)
	 */
	@Override
	public <IN> InputTransformer<IN, T> inputTransformer(Object id, Class<IN> inClass) {
		return head.getInputTransformer(id);
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#inputTransformer(java.lang.Object,
	 *      com.slytechs.jnet.platform.api.data.DataType)
	 */
	@Override
	public <IN> InputTransformer<IN, T> inputTransformer(Object id, DataType<IN> dataType) {
		return head.getInputTransformer(id);
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.DataFlow#isTransformable()
	 */
	@Override
	public boolean isTransformable() {
		return true;
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#name()
	 */
	@Override
	public String name() {
		return name;
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.common.processor.impl.ProcessorParent#onEnableProcessor(com.slytechs.jnet.platform.api.data.common.processor.impl.ProcessorBase,
	 *      boolean)
	 */
	@Override
	public void onEnableProcessor(ProcessorBase<T> processor, boolean newState) {
		if (newState == processor.isEnabled())
			return;

		writeLock.lock();
		try {

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

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#onNewProcessor(java.util.function.BiConsumer)
	 */
	@Override
	public PipelineBase<T> onNewProcessor(BiConsumer<Registration, Processor<T>> action) {
		processorRegistrations.add(action);

		return this;
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#onNewRegistration(java.util.function.Consumer)
	 */
	@Override
	public PipelineBase<T> onNewRegistration(Consumer<Registration> action) {
		return onNewProcessor((r, p) -> action.accept(r));
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#out(java.lang.Object,
	 *      OUT)
	 */
	@Override
	public <OUT> Registration out(Object id, OUT sink) {
		return tail().getOutputTransformer(id).connect(sink);
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#out(java.lang.Object,
	 *      OUT, java.lang.Class)
	 */
	@Override
	public <OUT> Registration out(Object id, OUT sink, Class<OUT> outClass) {
		return tail().getOutputTransformer(id).connect(sink);
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#out(java.lang.Object,
	 *      OUT, com.slytechs.jnet.platform.api.data.DataType)
	 */
	@Override
	public <OUT> Registration out(Object id, OUT sink, DataType<OUT> dataType) {
		return tail().getOutputTransformer(id).connect(sink);
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#outputTransformer(java.lang.Object)
	 */
	@Override
	public <OUT> OutputTransformer<T, OUT> outputTransformer(Object id) {
		return tail().getOutputTransformer(id);
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#outputTransformer(java.lang.Object,
	 *      java.lang.Class)
	 */
	@Override
	public <OUT> OutputTransformer<T, OUT> outputTransformer(Object id, Class<OUT> outClass) {
		return tail().getOutputTransformer(id);
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#outputTransformer(java.lang.Object,
	 *      com.slytechs.jnet.platform.api.data.DataType)
	 */
	@Override
	public <OUT> OutputTransformer<T, OUT> outputTransformer(Object id, DataType<OUT> dataType) {
		return tail().getOutputTransformer(id);
	}

	public IllegalArgumentException processorNotFoundException(Object id) {
		return new IllegalArgumentException("processor not found [id=%s]".formatted(id));
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#registerProcessor(com.slytechs.jnet.platform.api.data.common.processor.Processor)
	 */
	@Override
	public final Registration registerProcessor(Processor<T> newProcessor) {
		Object id = newProcessor.id();

		writeLock.lock();
		try {
			if (processorsById.containsKey(id))
				throw new IllegalArgumentException("processor already registered [%s]".formatted(id));

			if (newProcessor.isEnabled())
				throw new IllegalStateException("processor already initialized [%s]".formatted(id));

			// Initialize new processor
			newProcessor.setParent(this, this);

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

		ProcessorBase<T> prevProcessor = processor.prevElement();

		activeProcessors.remove(processor);

		processorsById.remove(id);

		// Reset processor
		processor.clearParent();

		prevProcessor.relink();
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#setDefaultErrorPolicy(com.slytechs.jnet.platform.api.data.pipeline.ErrorPolicy)
	 */
	@Override
	public void setDefaultErrorPolicy(ErrorPolicy policy) {
		eventSupport.setDefaultErrorPolicy(policy);
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#tail()
	 */
	@Override
	public final Tail<T> tail() {
		return tail;
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#toString()
	 */
	@Override
	public String toString() {
		if (activeProcessors.size() == 2)
			return name() + ": <no processors>";

		return name() + ": "
				+ activeProcessors.stream()
						.filter(p -> (p != head && p != tail))
						.map(ProcessorBase::toString)
						.collect(Collectors.joining(" → "));
	}

	/**
	 * @see com.slytechs.jnet.platform.api.data.pipeline.DataPipeline#toString(Detail)
	 */
	@Override
	public String toString(Detail detail) {
		return switch (detail) {
		case OFF -> "";
		case LOW -> toString();

		default -> name() + ": "
				+ activeProcessors.stream()
						.map(ProcessorBase::toString)
						.collect(Collectors.joining(" → "));
		};
	}
}
