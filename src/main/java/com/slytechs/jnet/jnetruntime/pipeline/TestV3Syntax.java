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

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.function.Supplier;

import com.slytechs.jnet.jnetruntime.util.Registration;

/**
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public class TestV3Syntax {

	static class StringPipeline extends Pipeline<Consumer<StringBuilder>> {

		static Consumer<StringBuilder> wrapper(Consumer<StringBuilder>[] dataArray) {
			return str -> {
				for (var e : dataArray)
					e.accept(str);
			};
		}

		static Consumer<String> wrapDataString(Consumer<String>[] dataArray) {
			return str -> {
				for (var e : dataArray)
					e.accept(str);
			};
		}

		/**
		 * @param dataType
		 */
		public StringPipeline(String name) {
			super("String pipeline", new RawDataType<>(e -> {}, StringPipeline::wrapper));

			head().<LongConsumer>addInput("long", sink -> {
				return num -> sink.get().accept(new StringBuilder(Long.toString(num)));
			});

			head().<IntConsumer>addInput("integer", sink -> {
				return num -> sink.get().accept(new StringBuilder(Integer.toString(num)));
			});

			this.addProcessor(10, "ToUppercase", sink -> {
				return str -> sink.get().accept(str.append("-ToUppercase"));
			});

			this.addProcessor(20, "ToLowercase", sink -> {
				return str -> sink.get().accept(str.append("-ToLowercase"));
			});

			tail().addOutput(0, "ToString", new GenericDataType<Consumer<String>>() {},
					(Supplier<Consumer<String>> sink) -> {
						return str -> sink.get().accept(str.toString());
					});

			tail().addOutput(0, new GenericDataType<>(e -> {}) {}, (
					Supplier<Consumer<String>> sink) -> {
				return str -> sink.get().accept(str.toString());
			});

			System.out.println("Lookup:: " + tail().getOutput(new GenericDataType<Consumer<String>>() {})
					.dataType());
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		var registrations = new ArrayList<Registration>();
		var pipeline = new StringPipeline("String testing");

		pipeline.onNewRegistration(registrations::add);

		pipeline.inputConnector("integer", IntConsumer.class).accept(10);;
		pipeline.outputConnect(new GenericDataType<Consumer<String>>() {}, System.out::println);

		System.out.println(pipeline);
	}

}
