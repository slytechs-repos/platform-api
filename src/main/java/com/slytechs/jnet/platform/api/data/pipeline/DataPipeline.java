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
package com.slytechs.jnet.platform.api.data.pipeline;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.slytechs.jnet.platform.api.data.DataFlow;
import com.slytechs.jnet.platform.api.data.DataProcessor;
import com.slytechs.jnet.platform.api.data.DataType;
import com.slytechs.jnet.platform.api.data.common.processor.Processor;
import com.slytechs.jnet.platform.api.data.common.processor.ProcessorMapper;
import com.slytechs.jnet.platform.api.data.common.processor.ProcessorMapper.SimpleProcessorMapper;
import com.slytechs.jnet.platform.api.data.event.AttributeEvent;
import com.slytechs.jnet.platform.api.data.handler.ProcessingError;
import com.slytechs.jnet.platform.api.data.handler.ProcessorEvent;
import com.slytechs.jnet.platform.api.data.pipeline.processor.Head;
import com.slytechs.jnet.platform.api.data.pipeline.processor.Tail;
import com.slytechs.jnet.platform.api.data.pipeline.transform.InputTransformer;
import com.slytechs.jnet.platform.api.data.pipeline.transform.OutputTransformer;
import com.slytechs.jnet.platform.api.util.Registration;
import com.slytechs.jnet.platform.api.util.format.Detail;

/**
 * Interface defining data pipeline operations and configuration. Supports
 * chained processing, transformations, error handling, and event monitoring.
 *
 * @param <T> Type of data flowing through pipeline
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc
 * @since 1.0.0
 */
public interface DataPipeline<T> extends DataFlow {

	/**
	 * Adds attribute change listener.
	 * 
	 * @param listener Receives attribute change events
	 * @return Registration for listener removal
	 */
	Registration addAttributeChangeListener(Consumer<AttributeEvent> listener);

	/**
	 * Adds error consumer.
	 *
	 * @param listener Receives pipeline errors
	 * @return Registration for listener removal
	 */
	Registration addPipelineErrorConsumer(Consumer<Throwable> listener);

	/**
	 * Adds error event listener.
	 *
	 * @param listener Receives pipeline error events
	 * @return Registration for listener removal
	 */
	Registration addPipelineErrorListener(Consumer<PipelineErrorEvent> listener);

	/**
	 * Adds pipeline event listener.
	 * 
	 * @param listener Receives pipeline events
	 * @return Registration for listener removal
	 */
	Registration addPipelineListener(PipelineListener listener);

	/**
	 * Adds processor with mapping.
	 *
	 * @param priority Priority level
	 * @param name     Unique name
	 * @param mapper   Data mapping logic
	 * @return Added processor
	 */
	DataProcessor<T> addProcessor(int priority, String name, ProcessorMapper<T> mapper);

	/**
	 * Adds processor with simple mapping.
	 * 
	 * @param priority Priority level
	 * @param name     Unique name
	 * @param mapper   Simple data mapper
	 * @return Added processor
	 */
	DataProcessor<T> addProcessor(int priority, String name, SimpleProcessorMapper<T> mapper);

	/**
	 * Adds existing processor.
	 *
	 * @param newProcessor Processor to add
	 * @return Added processor
	 */
	DataProcessor<T> addProcessor(Processor<T> newProcessor);

	/**
	 * Adds processor change listener.
	 *
	 * @param listener Receives processor events
	 * @return Registration for listener removal
	 */
	Registration addProcessorChangeListener(Consumer<ProcessorEvent> listener);

	/**
	 * Gets pipeline data type.
	 *
	 * @return Pipeline's data type
	 */
	DataType<T> dataType();

	/**
	 * Gets default error policy.
	 * 
	 * @return Current error policy
	 */
	ErrorPolicy getDefaultErrorPolicy();

	/**
	 * Gets processor by ID.
	 *
	 * @param id Processor ID
	 * @return Found processor or null
	 */
	DataProcessor<T> getProcessor(Object id);

	/**
	 * Handles processing error.
	 *
	 * @param error Error to handle
	 */
	void handleProcessingError(ProcessingError error);

	/**
	 * Gets pipeline head processor.
	 *
	 * @return Head processor
	 */
	Head<T> head();

	/**
	 * Gets input port.
	 *
	 * @param <IN> Input type
	 * @param id   Port ID
	 * @return Input port
	 */
	<IN> IN in(Object id);

	/**
	 * Gets typed input port.
	 *
	 * @param <IN>    Input type
	 * @param id      Port ID
	 * @param inClass Input class
	 * @return Input port
	 */
	<IN> IN in(Object id, Class<IN> inClass);

	/**
	 * Registers input consumer.
	 *
	 * @param <IN>     Input type
	 * @param id       Port ID
	 * @param consumer Input consumer
	 */
	<IN> void in(Object id, Consumer<IN> consumer);

	/**
	 * Registers typed input consumer.
	 *
	 * @param <IN>     Input type
	 * @param id       Port ID
	 * @param consumer Input consumer
	 * @param inClass  Input class
	 */
	<IN> void in(Object id, Consumer<IN> consumer, Class<IN> inClass);

	/**
	 * Registers input consumer with data type.
	 * 
	 * @param <IN>     Input type
	 * @param id       Port ID
	 * @param consumer Input consumer
	 * @param dataType Input data type
	 */
	<IN> void in(Object id, Consumer<IN> consumer, DataType<IN> dataType);

	/**
	 * Gets input port with data type.
	 *
	 * @param <IN>     Input type
	 * @param id       Port ID
	 * @param dataType Input data type
	 * @return Input port
	 */
	<IN> IN in(Object id, DataType<IN> dataType);

	/**
	 * Gets input transformer.
	 *
	 * @param <IN> Input type
	 * @param id   Transformer ID
	 * @return Input transformer
	 */
	<IN> InputTransformer<IN, T> inputTransformer(Object id);

	/**
	 * Gets typed input transformer.
	 *
	 * @param <IN>    Input type
	 * @param id      Transformer ID
	 * @param inClass Input class
	 * @return Input transformer
	 */
	<IN> InputTransformer<IN, T> inputTransformer(Object id, Class<IN> inClass);

	/**
	 * Gets input transformer with data type.
	 *
	 * @param <IN>     Input type
	 * @param id       Transformer ID
	 * @param dataType Input data type
	 * @return Input transformer
	 */
	<IN> InputTransformer<IN, T> inputTransformer(Object id, DataType<IN> dataType);

	/**
	 * Gets pipeline name.
	 *
	 * @return Pipeline name
	 */
	String name();

	/**
	 * Registers new processor callback.
	 *
	 * @param action Called when processor added
	 * @return This pipeline
	 */
	DataPipeline<T> onNewProcessor(BiConsumer<Registration, Processor<T>> action);

	/**
	 * Registers new registration callback.
	 *
	 * @param action Called when registration created
	 * @return This pipeline
	 */
	DataPipeline<T> onNewRegistration(Consumer<Registration> action);

	/**
	 * Connects output sink.
	 *
	 * @param <OUT> Output type
	 * @param id    Sink ID
	 * @param sink  Output sink
	 * @return Registration for sink removal
	 */
	<OUT> Registration out(Object id, OUT sink);

	/**
	 * Connects typed output sink.
	 *
	 * @param <OUT>    Output type
	 * @param id       Sink ID
	 * @param sink     Output sink
	 * @param outClass Output class
	 * @return Registration for sink removal
	 */
	<OUT> Registration out(Object id, OUT sink, Class<OUT> outClass);

	/**
	 * Connects output sink with data type.
	 *
	 * @param <OUT>    Output type
	 * @param id       Sink ID
	 * @param sink     Output sink
	 * @param dataType Output data type
	 * @return Registration for sink removal
	 */
	<OUT> Registration out(Object id, OUT sink, DataType<OUT> dataType);

	/**
	 * Gets output transformer.
	 *
	 * @param <OUT> Output type
	 * @param id    Transformer ID
	 * @return Output transformer
	 */
	<OUT> OutputTransformer<T, OUT> outputTransformer(Object id);

	/**
	 * Gets typed output transformer.
	 *
	 * @param <OUT>    Output type
	 * @param id       Transformer ID
	 * @param outClass Output class
	 * @return Output transformer
	 */
	<OUT> OutputTransformer<T, OUT> outputTransformer(Object id, Class<OUT> outClass);

	/**
	 * Gets output transformer with data type.
	 *
	 * @param <OUT>    Output type
	 * @param id       Transformer ID
	 * @param dataType Output data type
	 * @return Output transformer
	 */
	<OUT> OutputTransformer<T, OUT> outputTransformer(Object id, DataType<OUT> dataType);

	/**
	 * Registers processor.
	 *
	 * @param newProcessor Processor to register
	 * @return Registration for processor removal
	 */
	Registration registerProcessor(Processor<T> newProcessor);

	/**
	 * Sets default error policy.
	 *
	 * @param policy New error policy
	 */
	void setDefaultErrorPolicy(ErrorPolicy policy);

	/**
	 * Gets pipeline tail processor.
	 *
	 * @return Tail processor
	 */
	Tail<T> tail();

	/**
	 * Gets detailed string representation of pipeline inputs and outputs. Format:
	 * "{name} [inputs={in1,in2,...} outputs={out1,out2,...}]"
	 * 
	 * @param detail amount of detail to display
	 *
	 * @return String showing pipeline I/O configuration
	 */
	String toString(Detail detail);
}
