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
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.InputEntryPoint;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.OutputEndPoint;

/**
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 *
 */
public class TestSyntax {

	enum DataTypes implements DataType {
		PIPELINE_DATA(PipelineData.class, PipelineData::wrapArray),
		INPUT_DATA(InputData.class),
		OUTPUT_DATA(OutputData.class, OutputData::wrapArray),;

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
	}

	public interface InputData {
		void handleInputData(char[] name, long id);
	}

	@TypeLookup(DataTypes.class)
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
		static OutputData wrapArray(OutputData[] array) {
			return (nb, ai) -> {
				for (var out : array)
					out.handleOutputData(nb, ai);
			};
		}

		void handleOutputData(StringBuilder nameBuffer, AtomicInteger atomicId);
	}

	interface PipelineData {
		static PipelineData wrapArray(PipelineData[] array) {
			return (n, i, ctx) -> {
				for (var out : array)
					out.handlePipelineData(n, i, ctx);
			};
		}

		void handlePipelineData(String name, int id, Map<String, Object> context);
	}

	private static class TestPipelineInput
			extends AbstractInput<InputData, PipelineData, TestPipelineInput>
			implements InputData {

		final Map<String, Object> ctx = new HashMap<>();

		public TestPipelineInput() {
			this("input-data-transformer");
		}

		public TestPipelineInput(String name) {
			super(name, DataTypes.INPUT_DATA, DataTypes.PIPELINE_DATA);
		}

		public TestPipelineInput(String name, boolean enable) {
			super(name, DataTypes.INPUT_DATA, DataTypes.PIPELINE_DATA);

			enable(enable);
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

		public TestPipelineOutput(String name) {
			super(name, DataTypes.PIPELINE_DATA, DataTypes.OUTPUT_DATA);
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

	private static class TestProcessor
			extends AbstractProcessor<PipelineData, TestProcessor>
			implements PipelineData {

		public TestProcessor(Pipeline<PipelineData, ?> pipeline, int priority, String name) {
			super(pipeline, priority, name, DataTypes.PIPELINE_DATA);
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

	}

	public static void main(String[] args) throws NotFound {

		var pipeline = new MyPipeline();

		InputEntryPoint<InputData> in = new TestPipelineInput("en0");
		pipeline.registerInput(in);

		OutputEndPoint<OutputData> out = new TestPipelineOutput("");
		out.addOutputData((n, a) -> System.out.printf("name=%s, id=%d", n, a.get()));
		pipeline.registerOutput(out);
		pipeline.getOutput(DataTypes.OUTPUT_DATA);

		pipeline.enable(true);

		in.inputData().handleInputData("George".toCharArray(), 10);
	}

}
