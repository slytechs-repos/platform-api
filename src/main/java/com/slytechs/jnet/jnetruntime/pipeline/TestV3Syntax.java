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

import com.slytechs.jnet.jnetruntime.util.Registration;

/**
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public class TestV3Syntax {

	static class StringPipeline extends Pipeline<Consumer<String>> {

		static Consumer<String> wrapDataArray(Consumer<String>[] dataArray) {
			return str -> {
				for (var e : dataArray)
					e.accept(str);
			};
		}

		/**
		 * @param dataType
		 */
		protected StringPipeline(String name) {
			super("String pipeline", StringPipeline::wrapDataArray);

			head().<LongConsumer>addInputTransformer("long", sink -> {
				return num -> sink.get().accept(Long.toString(num));
			});

			head().<IntConsumer>addInputTransformer("integer", sink -> {
				return num -> sink.get().accept(Integer.toString(num));
			});

			this.addProcessor("toUppercase", sink -> {
				return str -> sink.get().accept(str.toUpperCase());
			});

			this.addProcessor("ToLowercase", sink -> {
				return str -> sink.get().accept(str.toLowerCase());
			});
		}

	}

	static class ToUppercase extends Processor<Consumer<String>> {

		public ToUppercase(int priority) {
			super(priority, "ToUppercase", String::toUpperCase);
		}

	}

	static class ToLowercase extends Processor<Consumer<String>> {

		public ToLowercase(int priority) {
			super(priority, "ToUppercase", String::toLowerCase);
		}

	}

	static class LongToString
			extends InputTransformer<LongConsumer, Consumer<String>>
			implements LongConsumer {

		public LongToString(String name) {
			super("long");
		}

		/**
		 * @see java.util.function.LongConsumer#accept(long)
		 */
		@Override
		public void accept(long value) {
			getOutput().accept(Long.toString(value));
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		var registrations = new ArrayList<Registration>();
		var pipeline = new StringPipeline("String testing");

		pipeline.onNewRegistration(registrations::add);

		pipeline.addProcessor(new ToUppercase(0));
		pipeline.addProcessor(new ToLowercase(1));

		pipeline.head().<LongConsumer>addInputTransformer("long", outputSupplier -> {
			return (long num) -> outputSupplier.get().accept(Long.toString(num));
		});
	}

}
