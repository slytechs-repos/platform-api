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

		public StringPipeline(String name) {
			super("String pipeline", new DT<>() {});

			head().addInput("FromLong", this::inputFromLong, new DT<>() {});

			head().addInput("integer", this::inputFromInt, new DT<>() {});

			this.addProcessor(10, "ToUppercase", this::processToUppercase)
					.peek(sb -> System.out.println("PEEK1:: " + sb.toString()))
					.peek(sb -> System.out.println("PEEK2:: " + sb.toString()))

			;

			this.addProcessor(20, "ToLowercase", this::processToLowercase)
					.peek(sb -> System.out.println("PEEK1:: " + sb.toString()))

			;

			tail().addOutput(0, "ToString", output -> {
				return str -> output.get().accept(str.toString());
			}, new DT<Consumer<String>>("ToString") {});

			tail().addOutput(0, sink -> {
				return str -> sink.get().accept(str.toString());
			}, new DT<Consumer<String>>() {});

		}

		private LongConsumer inputFromLong(Supplier<Consumer<StringBuilder>> sink) {
			return (long num) -> sink.get().accept(new StringBuilder(Long.toString(num)));
		}

		private IntConsumer inputFromInt(Supplier<Consumer<StringBuilder>> sink) {
			return (int num) -> sink.get().accept(new StringBuilder(Long.toString(num)));
		}

		private Consumer<StringBuilder> processToUppercase(Supplier<Consumer<StringBuilder>> sink) {
			return str -> sink.get().accept(str.append("-ToUppercase"));
		}

		private Consumer<StringBuilder> processToLowercase(Supplier<Consumer<StringBuilder>> sink) {
			return str -> {
				sink.get().accept(str.append("-ToLowercase"));
			};
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		var registrations = new ArrayList<Registration>();
		var pipeline = new StringPipeline("String testing");

		pipeline.addPipelineErrorConsumer(Throwable::printStackTrace);

		pipeline.onNewRegistration(registrations::add);

		pipeline.out("ToString", System.out::println, new DT<Consumer<String>>() {});
		pipeline.in("FromLong", LongConsumer.class).accept(10);;

		System.out.println(pipeline);
	}

}
