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
package com.slytechs.jnet.jnetruntime.pipeline2;

import java.lang.invoke.MethodHandle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import com.slytechs.jnet.jnetruntime.pipeline.DataType;
import com.slytechs.jnet.jnetruntime.pipeline.Processor;

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

		<T, U> DataTypes(Class<T> arrayFactory, Function<T[], T> arrayWrapper) {
			this.dataSupport = new DataSupport<T>(this, arrayFactory, null, arrayWrapper);
		}

		<T, U> DataTypes(Class<T> arrayFactory) {
			this.dataSupport = new DataSupport<T>(this, arrayFactory);
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

	interface PipelineData {
		void handleData(String name, int id);
	}

	interface InputData {
		void handleInput(char[] name, long id);
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

	static class Container {

		static PipelineData dataAdaptor(MethodHandle handle, Object instance) {
			return (name, id) -> {
				try {
					if (instance == null)
						handle.invoke(name, id);
					else
						handle.invoke(instance, name, id);
				} catch (Throwable e) {
					throw new RuntimeException(e);
				}
			};
		}

		@Processor(PipelineData.class)
		public void handle1(String name, int id) {
			System.out.printf("handle1:: %s, %d%n", name, id);
		}

		@Processor(PipelineData.class)
		public static void handle2(String name, int id) {
			System.out.printf("handle2:: %s, %d%n", name, id);
		}

	}

	static class InputTransformer
			extends AbstractTransformer<InputData, PipelineData, InputTransformer> implements InputData {

		/**
		 * @param name
		 * @param input
		 * @param inputType
		 * @param outputType
		 */
		public InputTransformer(String name) {
			super(name, DataTypes.INPUT_DATA, DataTypes.PIPELINE_DATA);
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.pipeline2.TestSyntax.InputData#handleInput(char[],
		 *      long)
		 */
		@Override
		public void handleInput(char[] name, long id) {
			String str = new String(name);
			int newId = (int) id;

			getOutput().handleData(str, newId);
		}

	}

	static class OutputTransformer
			extends AbstractTransformer<PipelineData, OutputData, OutputTransformer>
			implements PipelineData {

		/**
		 * @param name
		 * @param inputType
		 * @param output
		 * @param outputType
		 */
		public OutputTransformer(String name) {
			super(name, DataTypes.PIPELINE_DATA, DataTypes.OUTPUT_DATA);
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.pipeline2.TestSyntax.PipelineData#handleData(java.lang.String,
		 *      int)
		 */
		@Override
		public void handleData(String name, int id) {
			StringBuilder b = new StringBuilder(name);
			AtomicInteger ai = new AtomicInteger(id);

			getOutput().handleOutput(b, ai);
		}

	}

	static class MyPipeline extends AbstractPipeline<PipelineData, MyPipeline> {

		public MyPipeline() {
			super("my-pipeline", DataTypes.PIPELINE_DATA);
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		var container = new Container();
		var pipeline = new MyPipeline();

		var list = AnnotatedProcessorNode.list(
				pipeline,
				container,
				DataTypes.PIPELINE_DATA,
				Container::dataAdaptor);

		list.forEach(System.out::println);

		list.stream()
				.map(PipelineNode::getData)
				.forEach(d -> d.handleData("name", 1));

		System.out.println(new InputTransformer("input"));
		System.out.println(new OutputTransformer("output"));
	}

}
