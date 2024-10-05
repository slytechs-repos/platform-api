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

import com.slytechs.jnet.jnetruntime.pipeline.DataProcessor.ProcessorFactory.Named;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.InputFactory;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.InputFactory.Arg1;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.InputFactory.Arg2;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.InputTransformer;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.OutputFactory;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.OutputTransformer;

/**
 * The Class SubPipelineProcessor.
 *
 * @param <T> the generic type
 * @author Mark Bednarczyk
 */
class SubPipelineProcessor<T>
		extends AbstractProcessor<T, SubPipelineProcessor<T>>
		implements Pipeline<T, SubPipelineProcessor<T>> {

	/**
	 * Input from.
	 *
	 * @param <T>         the generic type
	 * @param subPipeline the sub pipeline
	 * @return the t
	 */
	private static <T> T inputFrom(Pipeline<T, ?> subPipeline) {
		return subPipeline.head().inputData();
	}

	/** The sub pipeline. */
	private final AbstractPipeline<T, ?> subPipeline;

	/**
	 * Output data.
	 *
	 * @return the t
	 * @see com.slytechs.jnet.jnetruntime.pipeline.AbstractProcessor#outputData()
	 */
	@Override
	public T outputData() {
		return super.outputData();
	}

	/**
	 * Instantiates a new sub pipeline processor.
	 *
	 * @param pipeline    the pipeline
	 * @param priority    the priority
	 * @param name        the name
	 * @param subPipeline the sub pipeline
	 */
	public SubPipelineProcessor(Pipeline<T, ?> pipeline, int priority, String name, Pipeline<T, ?> subPipeline) {
		super(pipeline, priority, name, subPipeline.dataType(), inputFrom(subPipeline));
		this.subPipeline = (AbstractPipeline<T, ?>) subPipeline;
	}

	/**
	 * Adds the input.
	 *
	 * @param <T_INPUT> the generic type
	 * @param factory   the factory
	 * @return the t input
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Pipeline#addInput(com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.InputFactory)
	 */
	@Override
	public <T_INPUT extends InputTransformer<?>> T_INPUT addInput(InputFactory<T, T_INPUT> factory) {
		return subPipeline.addInput(factory);
	}

	/**
	 * Adds the input.
	 *
	 * @param <T_INPUT> the generic type
	 * @param <T1>      the generic type
	 * @param id        the id
	 * @param arg1      the arg 1
	 * @param factory   the factory
	 * @return the t input
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Pipeline#addInput(java.lang.String,
	 *      java.lang.Object,
	 *      com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.InputFactory.Arg1)
	 */
	@Override
	public <T_INPUT extends InputTransformer<?>, T1> T_INPUT addInput(String id, T1 arg1,
			Arg1<T, T_INPUT, T1> factory) {
		return subPipeline.addInput(id, arg1, factory);
	}

	/**
	 * Adds the input.
	 *
	 * @param <T_INPUT> the generic type
	 * @param id        the id
	 * @param factory   the factory
	 * @return the t input
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Pipeline#addInput(java.lang.String,
	 *      com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.InputFactory)
	 */
	@Override
	public <T_INPUT extends InputTransformer<?>> T_INPUT addInput(String id, InputFactory<T, T_INPUT> factory) {
		return subPipeline.addInput(id, factory);
	}

	/**
	 * Adds the input.
	 *
	 * @param <T_INPUT> the generic type
	 * @param <T_ARG1>  the generic type
	 * @param <T_ARG2>  the generic type
	 * @param id        the id
	 * @param arg1      the arg 1
	 * @param arg2      the arg 2
	 * @param factory   the factory
	 * @return the t input
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Pipeline#addInput(java.lang.String,
	 *      java.lang.Object, java.lang.Object,
	 *      com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.InputFactory.Arg2)
	 */
	@Override
	public <T_INPUT extends InputTransformer<?>, T_ARG1, T_ARG2> T_INPUT addInput(String id, T_ARG1 arg1, T_ARG2 arg2,
			Arg2<T, T_INPUT, T_ARG1, T_ARG2> factory) {
		return subPipeline.addInput(id, arg1, arg2, factory);
	}

	/**
	 * Adds the output.
	 *
	 * @param <T_OUTPUT> the generic type
	 * @param factory    the factory
	 * @return the t output
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Pipeline#addOutput(com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.OutputFactory)
	 */
	@Override
	public <T_OUTPUT extends OutputTransformer<?>> T_OUTPUT addOutput(OutputFactory<T, T_OUTPUT> factory) {
		return subPipeline.addOutput(factory);
	}

	/**
	 * Adds the output.
	 *
	 * @param <T_OUTPUT> the generic type
	 * @param id         the id
	 * @param factory    the factory
	 * @return the t output
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Pipeline#addOutput(java.lang.String,
	 *      com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.OutputFactory)
	 */
	@Override
	public <T_OUTPUT extends OutputTransformer<?>> T_OUTPUT addOutput(String id, OutputFactory<T, T_OUTPUT> factory) {
		return subPipeline.addOutput(id, factory);
	}

	/**
	 * Adds the processor.
	 *
	 * @param <T_PROC>         the generic type
	 * @param priority         the priority
	 * @param processorFactory the processor factory
	 * @return the t proc
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Pipeline#addProcessor(int,
	 *      com.slytechs.jnet.jnetruntime.pipeline.DataProcessor.ProcessorFactory)
	 */
	@Override
	public <T_PROC extends DataProcessor<T, T_PROC>> T_PROC addProcessor(int priority,
			ProcessorFactory<T, T_PROC> processorFactory) {
		return subPipeline.addProcessor(priority, processorFactory);
	}

	/**
	 * Adds the processor.
	 *
	 * @param <T_PROC>         the generic type
	 * @param priority         the priority
	 * @param name             the name
	 * @param processorFactory the processor factory
	 * @return the t proc
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Pipeline#addProcessor(int,
	 *      java.lang.String,
	 *      com.slytechs.jnet.jnetruntime.pipeline.DataProcessor.ProcessorFactory.Named)
	 */
	@Override
	public <T_PROC extends DataProcessor<T, T_PROC>> T_PROC addProcessor(int priority,
			String name, Named<T, T_PROC> processorFactory) {
		return subPipeline.addProcessor(priority, name, processorFactory);
	}

	/**
	 * Data type.
	 *
	 * @return the data type
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Pipeline#dataType()
	 */
	@Override
	public DataType dataType() {
		return subPipeline.dataType();
	}

	/**
	 * Head.
	 *
	 * @return the head node
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Pipeline#head()
	 */
	@Override
	public HeadNode<T> head() {
		return subPipeline.head();
	}

	/**
	 * Tail.
	 *
	 * @return the tail node
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Pipeline#tail()
	 */
	@Override
	public TailNode<T> tail() {
		return subPipeline.tail();
	}

}
