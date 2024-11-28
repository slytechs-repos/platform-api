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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import com.slytechs.jnet.jnetruntime.NotFound;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.InputTransformer.EntryPoint;

/**
 * The Class TestSyntax.
 *
 * @author Mark Bednarczyk
 */
public class TestSyntax {

	/**
	 * The Enum DataTypes.
	 *
	 * @author Mark Bednarczyk
	 */
	enum DataTypes implements DataType {
		
		/** The pipeline data. */
		PIPELINE_DATA(PipelineData.class, DataTypes::wrapPipeline),
		
		/** The input data. */
		INPUT_DATA(InputData.class),
		
		/** The output data. */
		OUTPUT_DATA(OutputData.class, DataTypes::wrapOutput),

		;

		/** The data settingsSupport. */
		private final DataSupport<?> dataSupport;

		/**
		 * Instantiates a new data types.
		 *
		 * @param <T>       the generic type
		 * @param <U>       the generic type
		 * @param dataClass the data class
		 */
		<T, U> DataTypes(Class<T> dataClass) {
			this.dataSupport = new DataSupport<T>(this, dataClass);
		}

		/**
		 * Instantiates a new data types.
		 *
		 * @param <T>          the generic type
		 * @param <U>          the generic type
		 * @param dataClass    the data class
		 * @param arrayWrapper the array wrapper
		 */
		<T, U> DataTypes(Class<T> dataClass, Function<T[], T> arrayWrapper) {
			this.dataSupport = new DataSupport<T>(this, dataClass, arrayWrapper);
		}

		/**
		 * Data settingsSupport.
		 *
		 * @param <T> the generic type
		 * @return the data settingsSupport
		 * @see com.slytechs.jnet.jnetruntime.pipeline.DataType#dataSupport()
		 */
		@SuppressWarnings("unchecked")
		@Override
		public <T> DataSupport<T> dataSupport() {
			return (DataSupport<T>) dataSupport;
		}

		/**
		 * Wrap pipeline.
		 *
		 * @param array the array
		 * @return the pipeline data
		 */
		static PipelineData wrapPipeline(PipelineData[] array) {
			return (n, i, ctx) -> {
				for (var out : array)
					out.handlePipelineData(n, i, ctx);
			};
		}

		/**
		 * Wrap output.
		 *
		 * @param array the array
		 * @return the output data
		 */
		static OutputData wrapOutput(OutputData[] array) {
			return (nb, ai) -> {
				for (var out : array)
					out.handleOutputData(nb, ai);
			};
		}

	}

	/**
	 * The Interface InputData.
	 *
	 * @author Mark Bednarczyk
	 */
	public interface InputData {
		
		/**
		 * Handle input data.
		 *
		 * @param name the name
		 * @param id   the id
		 */
		void handleInputData(char[] name, long id);
	}

	/**
	 * The Class MyPipeline.
	 *
	 * @author Mark Bednarczyk
	 */
	@ATypeLookup(DataTypes.class)
	private static class MyPipeline extends AbstractPipeline<PipelineData, MyPipeline> {

		/**
		 * Instantiates a new my pipeline.
		 */
		public MyPipeline() {
			super("my-pipeline", DataTypes.PIPELINE_DATA);
		}

		/**
		 * Process.
		 *
		 * @param name the name
		 * @param id   the id
		 * @param ctx  the ctx
		 * @param out  the out
		 */
		@AProcessor(PipelineData.class)
		void process(String name, int id, Map<String, Object> ctx, PipelineData out) {
			name = name.toUpperCase();

			out.handlePipelineData(name, id, ctx);
		}
	}

	/**
	 * The Interface OutputData.
	 *
	 * @author Mark Bednarczyk
	 */
	interface OutputData {
		
		/**
		 * Handle output data.
		 *
		 * @param nameBuffer the name buffer
		 * @param atomicId   the atomic id
		 */
		void handleOutputData(StringBuilder nameBuffer, AtomicInteger atomicId);
	}

	/**
	 * The Interface PipelineData.
	 *
	 * @author Mark Bednarczyk
	 */
	interface PipelineData {

		/**
		 * Handle pipeline data.
		 *
		 * @param name    the name
		 * @param id      the id
		 * @param context the context
		 */
		void handlePipelineData(String name, int id, Map<String, Object> context);
	}

	/**
	 * The Class TestPipelineInput.
	 *
	 * @author Mark Bednarczyk
	 */
	private static class TestPipelineInput
			extends AbstractInput<InputData, PipelineData, TestPipelineInput>
			implements InputData {

		/** The ctx. */
		final Map<String, Object> ctx = new HashMap<>();

		/**
		 * Instantiates a new test pipeline input.
		 *
		 * @param head the head
		 */
		public TestPipelineInput(HeadNode<PipelineData> head) {
			this(head, "test-input");
		}

		/**
		 * Instantiates a new test pipeline input.
		 *
		 * @param head the head
		 * @param name the name
		 */
		public TestPipelineInput(HeadNode<PipelineData> head, String name) {
			super(head, name, DataTypes.INPUT_DATA, DataTypes.PIPELINE_DATA);
		}

		/**
		 * Handle input data.
		 *
		 * @param name the name
		 * @param id   the id
		 * @see com.slytechs.jnet.jnetruntime.pipeline.TestSyntax.InputData#handleInputData(char[],
		 *      long)
		 */
		@Override
		public void handleInputData(char[] name, long id) {
			String str = new String(name);
			int i = (int) id;

			outputData().handlePipelineData(str, i, ctx);
		}
	}

	/**
	 * The Class TestPipelineOutput.
	 *
	 * @author Mark Bednarczyk
	 */
	private static class TestPipelineOutput
			extends AbstractOutput<PipelineData, OutputData, TestPipelineOutput>
			implements PipelineData {

		/**
		 * Instantiates a new test pipeline output.
		 *
		 * @param tailNode the tail node
		 */
		public TestPipelineOutput(TailNode<PipelineData> tailNode) {
			this(tailNode, "test-output");
		}

		/**
		 * Instantiates a new test pipeline output.
		 *
		 * @param tailNode the tail node
		 * @param name     the name
		 */
		public TestPipelineOutput(TailNode<PipelineData> tailNode, String name) {
			super(tailNode, name, DataTypes.PIPELINE_DATA, DataTypes.OUTPUT_DATA);
		}

		/**
		 * Handle pipeline data.
		 *
		 * @param name    the name
		 * @param id      the id
		 * @param context the context
		 * @see com.slytechs.jnet.jnetruntime.pipeline.TestSyntax.PipelineData#handlePipelineData(java.lang.String,
		 *      int, java.util.Map)
		 */
		@Override
		public void handlePipelineData(String name, int id, Map<String, Object> context) {
			StringBuilder sb = new StringBuilder(name);
			AtomicInteger ai = new AtomicInteger(id);

			outputData().handleOutputData(sb, ai);
		}

	}

	/**
	 * The Class ToUppercaseProcessor.
	 *
	 * @author Mark Bednarczyk
	 */
	private static class ToUppercaseProcessor
			extends AbstractProcessor<PipelineData, ToUppercaseProcessor>
			implements PipelineData {

		/** The Constant NAME. */
		public static final String NAME = "to_upper";

		/**
		 * Instantiates a new to uppercase processor.
		 *
		 * @param pipeline the pipeline
		 * @param priority the priority
		 */
		public ToUppercaseProcessor(Pipeline<PipelineData, ?> pipeline, int priority) {
			super(pipeline, priority, NAME, DataTypes.PIPELINE_DATA);
		}

		/**
		 * Handle pipeline data.
		 *
		 * @param name    the name
		 * @param id      the id
		 * @param context the context
		 * @see com.slytechs.jnet.jnetruntime.pipeline.TestSyntax.PipelineData#handlePipelineData(java.lang.String,
		 *      int, java.util.Map)
		 */
		@Override
		public void handlePipelineData(String name, int id, Map<String, Object> context) {
			name = name.toUpperCase();
			id++;

			outputData().handlePipelineData(name, id, context);
		}

		/**
		 * Peek.
		 *
		 * @param peekAction the peek action
		 * @return the to uppercase processor
		 */
		public ToUppercaseProcessor peek(PipelineData peekAction) {
			super.addToOutputList(peekAction);
			return this;
		}

	}

	/**
	 * The Class ToLowercaseProcessor.
	 *
	 * @author Mark Bednarczyk
	 */
	private static class ToLowercaseProcessor
			extends AbstractProcessor<PipelineData, ToLowercaseProcessor>
			implements PipelineData {

		/** The Constant NAME. */
		public static final String NAME = "to_lower";

		/**
		 * Instantiates a new to lowercase processor.
		 *
		 * @param pipeline the pipeline
		 * @param priority the priority
		 */
		public ToLowercaseProcessor(Pipeline<PipelineData, ?> pipeline, int priority) {
			super(pipeline, priority, NAME, DataTypes.PIPELINE_DATA);
		}

		/**
		 * Handle pipeline data.
		 *
		 * @param name    the name
		 * @param id      the id
		 * @param context the context
		 * @see com.slytechs.jnet.jnetruntime.pipeline.TestSyntax.PipelineData#handlePipelineData(java.lang.String,
		 *      int, java.util.Map)
		 */
		@Override
		public void handlePipelineData(String name, int id, Map<String, Object> context) {
			name = name.toLowerCase();
			id++;

			outputData().handlePipelineData(name, id, context);
		}

		/**
		 * Peek.
		 *
		 * @param peekAction the peek action
		 * @return the to lowercase processor
		 */
		public ToLowercaseProcessor peek(PipelineData peekAction) {
			super.addToOutputList(peekAction);
			return this;
		}

	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws NotFound the not found
	 */
	public static void main(String[] args) throws NotFound {

		var pipeline = new MyPipeline();

		TestPipelineInput input = pipeline.addInput(TestPipelineInput::new);
		EntryPoint<InputData> entryPoint = input.createEntryPoint("entry#1");
		input.createEntryPoint("entry#2");

		pipeline.addOutput(TestPipelineOutput::new)
				.createEndPoint("end#1")
				.data((n, i) -> System.out.printf("name=%s, id=%d%n", n, i.get()));

		pipeline.addProcessor(2, ToUppercaseProcessor::new)
				.peek((n, i, c) -> System.out.println(n));

		pipeline.addProcessor(1, ToLowercaseProcessor::new)
				.peek((n, i, c) -> System.out.println(n));

		pipeline.enable(true);

		System.out.println(pipeline);

		InputData data = entryPoint.data();
		data.handleInputData("George".toCharArray(), 10);
		System.out.println(pipeline);

		input.enable(false);
		data.handleInputData("Michael".toCharArray(), 10);
		System.out.println(pipeline);
	}

}
