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

import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.InputTransformer.EntryPoint.EntryPointFactory;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.OutputTransformer.EndPoint.EndPointFactory;
import com.slytechs.jnet.jnetruntime.util.HasName;
import com.slytechs.jnet.jnetruntime.util.HasPriority;
import com.slytechs.jnet.jnetruntime.util.Registration;

/**
 * Represents a data transformer in a pipeline, capable of transforming input
 * data to output data.
 *
 * @param <T_IN>   The type of input data
 * @param <T_OUT>  The type of output data
 * @param <T_BASE> The specific type of the transformer implementation
 * @author Mark Bednarczyk
 */
public interface DataTransformer<T_IN, T_OUT, T_BASE extends DataTransformer<T_IN, T_OUT, T_BASE>>
		extends HasName, HasPriority, PipeComponent<T_BASE> {

	/**
	 * Factory for creating input transformers.
	 *
	 * @param <T_OUT>  The type of output data
	 * @param <T_BASE> The specific type of the input transformer
	 */
	interface InputFactory<T_OUT, T_BASE extends InputTransformer<?>> {

		/**
		 * Factory for creating input transformers with one argument.
		 *
		 * @param <T_OUT>  The type of output data
		 * @param <T_BASE> The specific type of the input transformer
		 * @param <T_ARG1> The type of the argument
		 * @author Mark Bednarczyk
		 */
		interface Arg1<T_OUT, T_BASE extends InputTransformer<?>, T_ARG1> {
			/**
			 * Creates a new instance of the input transformer with one argument.
			 *
			 * @param head The head node of the pipeline
			 * @param arg1 The argument for creation
			 * @return A new instance of the input transformer
			 */
			T_BASE newInstance1Arg(HeadNode<T_OUT> head, T_ARG1 arg1);
		}

		/**
		 * Factory for creating input transformers with two arguments.
		 *
		 * @param <T_OUT>  The type of output data
		 * @param <T_BASE> The specific type of the input transformer
		 * @param <T_ARG1> The type of the first argument
		 * @param <T_ARG2> The type of the second argument
		 * @author Mark Bednarczyk
		 */
		interface Arg2<T_OUT, T_BASE extends InputTransformer<?>, T_ARG1, T_ARG2> {
			/**
			 * Creates a new instance of the input transformer with two arguments.
			 *
			 * @param head The head node of the pipeline
			 * @param arg1 The first argument for creation
			 * @param arg2 The second argument for creation
			 * @return A new instance of the input transformer
			 */
			T_BASE newInstance2Args(HeadNode<T_OUT> head, T_ARG1 arg1, T_ARG2 arg2);
		}

		/**
		 * Creates a new instance of the input transformer.
		 *
		 * @param head The head node of the pipeline
		 * @return A new instance of the input transformer
		 */
		T_BASE newInstance(HeadNode<T_OUT> head);
	}

	/**
	 * Represents an input transformer in the pipeline.
	 *
	 * @param <T> The type of input data
	 * @author Mark Bednarczyk
	 */
	interface InputTransformer<T> extends HasInputData<T> {

		/**
		 * Represents an entry point for input data in the pipeline.
		 *
		 * @param <T> The type of input data
		 * @author Mark Bednarczyk
		 */
		interface EntryPoint<T> extends Registration, HasName, PipeComponent<EntryPoint<T>> {

			/**
			 * Factory for creating entry points.
			 *
			 * @param <T>      The type of input data
			 * @param <T_BASE> The specific type of the entry point
			 */
			interface EntryPointFactory<T, T_BASE extends EntryPoint<T>> {

				/**
				 * Factory for creating entry points with one argument.
				 *
				 * @param <T>      The type of input data
				 * @param <T_BASE> The specific type of the entry point
				 * @param <T1>     The type of the argument
				 * @author Mark Bednarczyk
				 */
				interface Arg1<T, T_BASE extends EntryPoint<T>, T1> {
					/**
					 * Creates a new instance of the entry point with one argument.
					 *
					 * @param input The input transformer
					 * @param id    The identifier for the entry point
					 * @param arg1  The argument for creation
					 * @return A new instance of the entry point
					 */
					T_BASE newEntryPointInstance1Arg(InputTransformer<T> input, String id, T1 arg1);
				}

				/**
				 * Factory for creating entry points with two arguments.
				 *
				 * @param <T>      The type of input data
				 * @param <T_BASE> The specific type of the entry point
				 * @param <T1>     The type of the first argument
				 * @param <T2>     The type of the second argument
				 * @author Mark Bednarczyk
				 */
				interface Arg2<T, T_BASE extends EntryPoint<T>, T1, T2> {
					/**
					 * Creates a new instance of the entry point with two arguments.
					 *
					 * @param input The input transformer
					 * @param id    The identifier for the entry point
					 * @param arg1  The first argument for creation
					 * @param arg2  The second argument for creation
					 * @return A new instance of the entry point
					 */
					T_BASE newEntryPointInstance2Args(InputTransformer<T> input, String id, T1 arg1, T2 arg2);
				}

				/**
				 * Creates a new instance of the entry point.
				 *
				 * @param input The input transformer
				 * @param id    The identifier for the entry point
				 * @return A new instance of the entry point
				 */
				T_BASE newEntryPointInstance(InputTransformer<T> input, String id);
			}

			/**
			 * Gets the identifier for this entry point.
			 *
			 * @return The identifier
			 */
			String id();

			/**
			 * Gets the input data for this entry point.
			 *
			 * @return The input data
			 */
			T inputData();

			/**
			 * Gets the input data type for this entry point.
			 *
			 * @return The input data type
			 */
			DataType inputType();
		}

		/**
		 * Creates a new entry point with the given identifier.
		 *
		 * @param id The identifier for the entry point
		 * @return A new entry point
		 */
		EntryPoint<T> createEntryPoint(String id);

		/**
		 * Creates a new entry point with the given identifier using a factory.
		 *
		 * @param <T_ENTRY> The specific type of the entry point
		 * @param id        The identifier for the entry point
		 * @param factory   The factory to create the entry point
		 * @return A new entry point
		 */
		<T_ENTRY extends EntryPoint<T>> T_ENTRY createEntryPoint(String id, EntryPointFactory<T, T_ENTRY> factory);

		/**
		 * Creates a new entry point with the given identifier and one argument using a
		 * factory.
		 *
		 * @param <T_ENTRY> The specific type of the entry point
		 * @param <T1>      The type of the argument
		 * @param id        The identifier for the entry point
		 * @param arg1      The argument for creation
		 * @param factory   The factory to create the entry point
		 * @return A new entry point
		 */
		<T_ENTRY extends EntryPoint<T>, T1> T_ENTRY createEntryPoint(
				String id,
				T1 arg1,
				EntryPointFactory.Arg1<T, T_ENTRY, T1> factory);

		/**
		 * Creates a new entry point with the given identifier and two arguments using a
		 * factory.
		 *
		 * @param <T_ENTRY> The specific type of the entry point
		 * @param <T1>      The type of the first argument
		 * @param <T2>      The type of the second argument
		 * @param id        The identifier for the entry point
		 * @param arg1      The first argument for creation
		 * @param arg2      The second argument for creation
		 * @param factory   The factory to create the entry point
		 * @return A new entry point
		 */
		<T_ENTRY extends EntryPoint<T>, T1, T2> T_ENTRY createEntryPoint(
				String id,
				T1 arg1,
				T2 arg2,
				EntryPointFactory.Arg2<T, T_ENTRY, T1, T2> factory);
	}

	/**
	 * Factory for creating output transformers.
	 *
	 * @param <T_IN>   The type of input data
	 * @param <T_BASE> The specific type of the output transformer
	 */
	interface OutputFactory<T_IN, T_BASE extends OutputTransformer<?>> {
		/**
		 * Creates a new instance of the output transformer.
		 *
		 * @param head The tail node of the pipeline
		 * @return A new instance of the output transformer
		 */
		T_BASE newInstance(TailNode<T_IN> head);
	}

	/**
	 * Represents an output transformer in the pipeline.
	 *
	 * @param <T> The type of output data
	 * @author Mark Bednarczyk
	 */
	interface OutputTransformer<T>
			extends HasOutputData<T>, HasPriority {

		/**
		 * Represents an end point for output data in the pipeline.
		 *
		 * @param <T> The type of output data
		 * @author Mark Bednarczyk
		 */
		interface EndPoint<T> extends Registration, HasPriority, HasName {

			interface EndPointFactory<T> {
				EndPoint<T> newEndPointInstance(OutputTransformer<T> output, String id);
			}

			/**
			 * Sets the output data for this end point.
			 *
			 * @param data The output data
			 */
			void endPointData(T data);

			/**
			 * Gets the output data type for this end point.
			 *
			 * @return The output data type
			 */
			DataType endPointType();

			/**
			 * Gets the identifier for this end point.
			 *
			 * @return The identifier
			 */
			String id();

			/**
			 * Sets the priority for this end point.
			 *
			 * @param newPriority The new priority
			 * @return This end point instance
			 */
			EndPoint<T> priority(int newPriority);
		}

		/**
		 * Adds output data to this transformer.
		 *
		 * @param data The output data to add
		 * @return The added output data
		 */
		T addOutputData(T data);

		/**
		 * Creates a new end point with the given identifier.
		 *
		 * @param id The identifier for the end point
		 * @return A new end point
		 */
		EndPoint<T> createEndPoint(String id);

		EndPoint<T> createEndPoint(String id, EndPointFactory<T> factory);

		default EndPoint<T> createMutableEndPoint(String id) {
			return createEndPoint(id, MutableEndPoint::new);
		}

		/**
		 * Registers output data with this transformer.
		 *
		 * @param data The output data to register
		 * @return A registration object for the registered data
		 */
		Registration registerOutputData(T data);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	T_BASE enable(boolean b);

	/**
	 * {@inheritDoc}
	 */
	@Override
	T_BASE enable(BooleanSupplier b);

	/**
	 * Gets the input data type for this transformer.
	 *
	 * @return The input data type
	 */
	DataType inputType();

	/**
	 * {@inheritDoc}
	 */
	@Override
	boolean isEnabled();

	/**
	 * {@inheritDoc}
	 */
	@Override
	T_BASE name(String newName);

	/**
	 * Gets the output data type for this transformer.
	 *
	 * @return The output data type
	 */
	DataType outputType();
}