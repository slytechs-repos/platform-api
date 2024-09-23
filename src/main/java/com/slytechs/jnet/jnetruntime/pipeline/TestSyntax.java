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

import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.PipelineInput;

/**
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 *
 */
public class TestSyntax {

	enum DataTypes implements DataType {
		PIPELINE_DATA(PipelineData.class),
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
		void handleInput(char[] name, long id);
	}

	@TypeLookup(DataTypes.class)
	private static class MyPipeline extends AbstractPipeline<PipelineData, MyPipeline> {

		public MyPipeline() {
			super("my-pipeline", DataTypes.PIPELINE_DATA);
		}

		@Processor(PipelineData.class)
		void process(String name, int id, Map<String, Object> ctx, PipelineData out) {
			name = name.toUpperCase();

			out.handleData(name, id, ctx);
		}
	}

	interface OutputData {
		static OutputData wrapArray(OutputData[] array) {
			return (nb, ai) -> {
				for (var out : array)
					out.handleOutput(nb, ai);
			};
		}

		void handleOutput(StringBuilder nameBuffer, AtomicInteger atomicId);
	}

	interface PipelineData {
		void handleData(String name, int id, Map<String, Object> context);
	}

	private static class TestPipelineInput
			extends AbstractTransformer<InputData, PipelineData, TestPipelineInput>
			implements PipelineInput<InputData>, InputData {

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
		public void handleInput(char[] name, long id) {
			String str = new String(name);
			int i = (int) id;

			outputData().handleData(str, i, ctx);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		var pipeline = new MyPipeline();

		TestPipelineInput b = pipeline.<InputData, TestPipelineInput>newInputBuilder(TestPipelineInput::new);
		assert b != null;

		InputData d0 = new TestPipelineInput("en0");

		InputData d1 = pipeline.newInputData("en0", TestPipelineInput::new);
		assert d1 != null;

		InputData d2 = pipeline.newInputData("en1", true, TestPipelineInput::new);
		assert d2 != null;

		pipeline.enable(true);

		d1.handleInput("Mark".toCharArray(), 10);
	}

}
