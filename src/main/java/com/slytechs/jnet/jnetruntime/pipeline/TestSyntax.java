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
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 *
 */
public class TestSyntax {

	enum DataTypes implements DataType {
		PIPELINE_DATA(PipelineData.class, DataTypes::wrapPipeline),
		INPUT_DATA(InputData.class),
		OUTPUT_DATA(OutputData.class, DataTypes::wrapOutput),

		;

		private final DataSupport<?> dataSupport;

		<T, U> DataTypes(Class<T> arrayFactory) {
			this.dataSupport = new DataSupport<T>(this, arrayFactory);
		}

		<T, U> DataTypes(Class<T> arrayFactory, Function<T[], T> arrayWrapper) {
			this.dataSupport = new DataSupport<T>(this, arrayFactory, null, arrayWrapper);
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.pipeline.DataType#dataSupport()
		 */
		@SuppressWarnings("unchecked")
		@Override
		public <T> DataSupport<T> dataSupport() {
			return (DataSupport<T>) dataSupport;
		}

		static PipelineData wrapPipeline(PipelineData[] array) {
			return (n, i, ctx) -> {
				for (var out : array)
					out.handlePipelineData(n, i, ctx);
			};
		}

		static OutputData wrapOutput(OutputData[] array) {
			return (nb, ai) -> {
				for (var out : array)
					out.handleOutputData(nb, ai);
			};
		}

	}

	public interface InputData {
		void handleInputData(char[] name, long id);
	}

	@ATypeLookup(DataTypes.class)
	private static class MyPipeline extends AbstractPipeline<PipelineData, MyPipeline> {

		public MyPipeline() {
			super("my-pipeline", DataTypes.PIPELINE_DATA);
		}

		@AProcessor(PipelineData.class)
		void process(String name, int id, Map<String, Object> ctx, PipelineData out) {
			name = name.toUpperCase();

			out.handlePipelineData(name, id, ctx);
		}
	}

	interface OutputData {
		void handleOutputData(StringBuilder nameBuffer, AtomicInteger atomicId);
	}

	interface PipelineData {

		void handlePipelineData(String name, int id, Map<String, Object> context);
	}

	private static class TestPipelineInput
			extends AbstractInput<InputData, PipelineData, TestPipelineInput>
			implements InputData {

		final Map<String, Object> ctx = new HashMap<>();

		public TestPipelineInput(HeadNode<PipelineData> head) {
			this(head, "test-input");
		}

		public TestPipelineInput(HeadNode<PipelineData> head, String name) {
			super(head, name, DataTypes.INPUT_DATA, DataTypes.PIPELINE_DATA);
		}

		@Override
		public void handleInputData(char[] name, long id) {
			String str = new String(name);
			int i = (int) id;

			outputData().handlePipelineData(str, i, ctx);
		}
	}

	private static class TestPipelineOutput
			extends AbstractOutput<PipelineData, OutputData, TestPipelineOutput>
			implements PipelineData {

		public TestPipelineOutput(TailNode<PipelineData> tailNode) {
			this(tailNode, "test-output");
		}

		public TestPipelineOutput(TailNode<PipelineData> tailNode, String name) {
			super(tailNode, name, DataTypes.PIPELINE_DATA, DataTypes.OUTPUT_DATA);
		}

		/**
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

	private static class ToUppercaseProcessor
			extends AbstractProcessor<PipelineData, ToUppercaseProcessor>
			implements PipelineData {

		public static final String NAME = "to_upper";

		public ToUppercaseProcessor(Pipeline<PipelineData, ?> pipeline, int priority) {
			super(pipeline, priority, NAME, DataTypes.PIPELINE_DATA);
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.pipeline.TestSyntax.PipelineData#handlePipelineData(java.lang.String,
		 *      int, java.util.Map)
		 */
		@Override
		public void handlePipelineData(String name, int id, Map<String, Object> context) {
			name = name.toUpperCase();
			id++;

			outputData().handlePipelineData(name, id, context);
		}

		public ToUppercaseProcessor peek(PipelineData peekAction) {
			super.addOutputToNode(peekAction);
			return this;
		}

	}

	private static class ToLowercaseProcessor
			extends AbstractProcessor<PipelineData, ToLowercaseProcessor>
			implements PipelineData {

		public static final String NAME = "to_lower";

		public ToLowercaseProcessor(Pipeline<PipelineData, ?> pipeline, int priority) {
			super(pipeline, priority, NAME, DataTypes.PIPELINE_DATA);
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.pipeline.TestSyntax.PipelineData#handlePipelineData(java.lang.String,
		 *      int, java.util.Map)
		 */
		@Override
		public void handlePipelineData(String name, int id, Map<String, Object> context) {
			name = name.toLowerCase();
			id++;

			outputData().handlePipelineData(name, id, context);
		}

		public ToLowercaseProcessor peek(PipelineData peekAction) {
			super.addOutputToNode(peekAction);
			return this;
		}

	}

	public static void main(String[] args) throws NotFound {

		var pipeline = new MyPipeline();

		TestPipelineInput input = pipeline.addInput(TestPipelineInput::new);
		EntryPoint<InputData> entryPoint = input.createEntryPoint("entry#1");
		input.createEntryPoint("entry#2");

		pipeline.addOutput(TestPipelineOutput::new)
				.createEndPoint("end#1")
				.outputData((n, i) -> System.out.printf("name=%s, id=%d%n", n, i.get()));

		pipeline.addProcessor(2, ToUppercaseProcessor::new)
				.peek((n, i, c) -> System.out.println(n));

		pipeline.addProcessor(1, ToLowercaseProcessor::new)
				.peek((n, i, c) -> System.out.println(n));

		pipeline.enable(true);

		System.out.println(pipeline);

		InputData data = entryPoint.inputData();
		data.handleInputData("George".toCharArray(), 10);
		System.out.println(pipeline);

		input.enable(false);
		data.handleInputData("Michael".toCharArray(), 10);
		System.out.println(pipeline);
	}

}
