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

import java.util.function.BooleanSupplier;

import com.slytechs.jnet.jnetruntime.pipeline.DataProcessor.ProcessorFactory;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.InputFactory;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.InputTransformer;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.OutputFactory;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.OutputTransformer;
import com.slytechs.jnet.jnetruntime.util.HasName;

/**
 * Represents a flexible and configurable data processing pipeline with inputs,
 * a sequence of processors, and outputs.
 * 
 * <p>
 * The pipeline structure consists of three main components:
 * </p>
 * <ol>
 * <li><strong>Inputs:</strong> Located at the head of the pipeline, inputs
 * receive data from various sources (e.g., network interfaces, files, or data
 * streams).</li>
 * <li><strong>Processors:</strong> A chain or sequence of data processors that
 * perform operations on the data as it flows through the pipeline.</li>
 * <li><strong>Outputs:</strong> Located at the tail of the pipeline, outputs
 * handle the processed data, which can be sent to multiple destinations or
 * further processing stages.</li>
 * </ol>
 * 
 * <p>
 * Key features of the pipeline include:
 * </p>
 * <ul>
 * <li>Support for multiple input sources</li>
 * <li>Configurable processing stages with priority-based ordering</li>
 * <li>Multiple output handlers for flexible data distribution</li>
 * <li>Bypass functionality for conditional processing</li>
 * <li>Named components for easy identification and management</li>
 * </ul>
 * 
 * <p>
 * The pipeline is designed to handle various data types and can be customized
 * for specific processing needs, such as network packet processing, data
 * transformation, or real-time analytics.
 * </p>
 * 
 * <p>
 * For a visual representation of the pipeline structure, refer to the
 * accompanying diagram.
 * </p>
 * 
 * <pre>
 * ```mermaid
 * flowchart LR
 *     PCAP((("Pcap"))) -- NativeHandler --> I2[["en0"]]
 *     I1[["input 1"]] --> H{"Head Node"}
 *     I2 --> H
 *     I3[["Input N"]] --> H
 *     H --> P1["Packet Repeater"]
 *     P1 --> P1D1(("Data")) & P2["Delay"] & P1D2(("Data"))
 *     P2 --> T{"Tail Node"}
 *     T --> O1("Output 1") & O2("Output 2") & O3("Queue Packets")
 *     O1 --> D1(("Data 1")) & D2(("Data N"))
 *     O2 --> D3(("Data 1"))
 *     O3 --> D5(("Data 1")) & D6(("Data N"))
 *     style H fill:#f9f,stroke:#333,stroke-width:2px,color:#000
 *     style P1 fill:#9cf,stroke:#333,stroke-width:2px,color:#000
 *     style P2 fill:#9cf,stroke:#333,stroke-width:2px,color:#000
 *     style T fill:#ff9,stroke:#333,stroke-width:2px,color:#000
 * ```
 * </pre>
 * 
 * @param <T>      The type of data processed by this pipeline
 * @param <T_BASE> The base type of the pipeline implementation
 * 
 * @see HeadNode
 * @see TailNode
 * @see DataProcessor
 * @see InputTransformer
 * @see OutputTransformer
 */
public interface Pipeline<T, T_BASE extends Pipeline<T, T_BASE>> extends HasName, PipeComponent<T_BASE> {

	/**
	 * Factory interface for creating new pipeline instances.
	 *
	 * @param <T>      The type of data processed by the pipeline
	 * @param <T_BASE> The base type of the pipeline implementation
	 */
	interface PipelineFactory<T, T_BASE extends Pipeline<T, T_BASE>> {
		/**
		 * Creates a new pipeline instance with the given name.
		 *
		 * @param name The name of the pipeline
		 * @return A new pipeline instance
		 */
		T_BASE newPipeline(String name);
	}

	/** Priority value for built-in head components */
	int HEAD_BUILTIN_PRIORITY = -1;

	/** Priority value for built-in tail components */
	int TAIL_BUILTIN_PRIORITY = Integer.MAX_VALUE;

	/**
	 * Adds an output transformer to the pipeline.
	 *
	 * @param <T_OUTPUT> The type of the output transformer
	 * @param factory    The factory to create the output transformer
	 * @return The created output transformer
	 */
	<T_OUTPUT extends OutputTransformer<?>> T_OUTPUT addOutput(OutputFactory<T, T_OUTPUT> factory);

	/**
	 * Adds an output transformer with a specific ID to the pipeline.
	 *
	 * @param <T_OUTPUT> The type of the output transformer
	 * @param id         The identifier for the output
	 * @param factory    The factory to create the output transformer
	 * @return The created output transformer
	 */
	<T_OUTPUT extends OutputTransformer<?>> T_OUTPUT addOutput(String id, OutputFactory<T, T_OUTPUT> factory);

	/**
	 * Adds an input transformer to the pipeline.
	 *
	 * @param <T_INPUT> The type of the input transformer
	 * @param factory   The factory to create the input transformer
	 * @return The created input transformer
	 */
	<T_INPUT extends InputTransformer<?>> T_INPUT addInput(InputFactory<T, T_INPUT> factory);

	/**
	 * Adds an input transformer with a specific ID to the pipeline.
	 *
	 * @param <T_INPUT> The type of the input transformer
	 * @param id        The identifier for the input transformer
	 * @param factory   The factory to create the input transformer
	 * @return The created input transformer
	 */
	<T_INPUT extends InputTransformer<?>> T_INPUT addInput(String id, InputFactory<T, T_INPUT> factory);

	/**
	 * Adds an input transformer with a specific ID and one argument to the
	 * pipeline.
	 *
	 * @param <T_INPUT> The type of the input transformer
	 * @param <T1>      The type of the argument
	 * @param id        The identifier for the input transformer
	 * @param arg1      The argument for the factory
	 * @param factory   The factory to create the input transformer
	 * @return The created input transformer
	 */
	<T_INPUT extends InputTransformer<?>, T1> T_INPUT addInput(String id, T1 arg1,
			InputFactory.Arg1<T, T_INPUT, T1> factory);

	/**
	 * Adds an input transformer with a specific ID and two arguments to the
	 * pipeline.
	 *
	 * @param <T_INPUT> The type of the input transformer
	 * @param <T1>      The type of the first argument
	 * @param <T2>      The type of the second argument
	 * @param id        The identifier for the input transformer
	 * @param arg1      The first argument for the factory
	 * @param arg2      The second argument for the factory
	 * @param factory   The factory to create the input transformer
	 * @return The created input transformer
	 */
	<T_INPUT extends InputTransformer<?>, T1, T2> T_INPUT addInput(String id, T1 arg1, T2 arg2,
			InputFactory.Arg2<T, T_INPUT, T1, T2> factory);

	/**
	 * Adds a processor to the pipeline with a specified priority.
	 *
	 * @param <T_PROC>         The type of the processor
	 * @param priority         The priority of the processor in the pipeline
	 * @param processorFactory The factory to create the processor
	 * @return The created processor
	 */
	<T_PROC extends DataProcessor<T, T_PROC>> T_PROC addProcessor(int priority,
			ProcessorFactory<T, T_PROC> processorFactory);

	/**
	 * Adds a named processor to the pipeline with a specified priority.
	 *
	 * @param <T_PROC>         The type of the processor
	 * @param priority         The priority of the processor in the pipeline
	 * @param name             The name of the processor
	 * @param processorFactory The factory to create the named processor
	 * @return The created processor
	 */
	<T_PROC extends DataProcessor<T, T_PROC>> T_PROC addProcessor(int priority, String name,
			ProcessorFactory.Named<T, T_PROC> processorFactory);

	/**
	 * Sets the bypass state of the pipeline.
	 *
	 * @param b True to bypass the pipeline, false otherwise
	 * @return This pipeline instance
	 */
	@Override
	T_BASE bypass(boolean b);

	/**
	 * Sets the bypass state of the pipeline using a boolean supplier.
	 *
	 * @param b A supplier that determines whether to bypass the pipeline
	 * @return This pipeline instance
	 */
	@Override
	default T_BASE bypass(BooleanSupplier b) {
		return bypass(b.getAsBoolean());
	}

	/**
	 * Gets the data type processed by this pipeline.
	 *
	 * @return The data type of the pipeline
	 */
	DataType dataType();

	/**
	 * Gets the head node of the pipeline.
	 *
	 * @return The head node
	 */
	HeadNode<T> head();

	/**
	 * Checks if the pipeline is currently bypassed.
	 *
	 * @return True if the pipeline is bypassed, false otherwise
	 */
	@Override
	boolean isBypassed();

	/**
	 * Gets the tail node of the pipeline.
	 *
	 * @return The tail node
	 */
	TailNode<T> tail();
}