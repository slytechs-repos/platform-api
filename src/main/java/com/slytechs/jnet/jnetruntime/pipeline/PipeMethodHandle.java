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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

/**
 * Represents a method handle in the pipeline framework, providing a way to
 * invoke methods and manage output data.
 *
 * @param <T> The type of output data produced by this method handle
 * @author Mark Bednarczyk
 */
public final class PipeMethodHandle<T> implements HasOutputData<T> {

	/**
	 * Interface for adapting data handles in the pipeline.
	 *
	 * @param <T_IN>  The input type for the adaptor
	 * @param <T_OUT> The output type for the adaptor
	 * @author Mark Bednarczyk
	 */
	public interface DataHandleAdaptor<T_IN, T_OUT> {
		
		/**
		 * Specialized interface for unary (same input and output type) data handle
		 * adaptors.
		 *
		 * @param <T> The type of data for both input and output
		 * @author Mark Bednarczyk
		 */
		public interface UniDataHandleAdaptor<T> extends DataHandleAdaptor<T, T> {
		}

		/**
		 * Creates an adaptor for the given method handle.
		 *
		 * @param handle The method handle to adapt
		 * @return An adapted object of type T_IN
		 */
		T_IN createAdaptor(PipeMethodHandle<T_OUT> handle);
	}

	/**
	 * Creates a PipeMethodHandle from a Method object.
	 *
	 * @param <T>            The type of output data produced by this method handle
	 * @param method         The Method object to create a handle from
	 * @param container      The object instance containing the method (null for
	 *                       static methods)
	 * @param containerClass The class containing the method
	 * @return A new PipeMethodHandle instance
	 * @throws IllegalStateException if the method handle cannot be created
	 */
	public static <T> PipeMethodHandle<T> from(Method method, Object container, Class<?> containerClass) {
		try {
			boolean isStatic = (container == null);
			var methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
			var mh = isStatic
					? MethodHandles.lookup().findStatic(containerClass, method.getName(), methodType)
					: MethodHandles.lookup()
							.findVirtual(containerClass, method.getName(), methodType)
							.bindTo(container);
			return new PipeMethodHandle<>(mh);
		} catch (Throwable e) {
			e.printStackTrace();
			throw new IllegalStateException(e);
		}
	}

	/** The handle. */
	private final MethodHandle handle;
	
	/** The output. */
	private HasOutputData<T> output;

	/**
	 * Constructs a new PipeMethodHandle with the given MethodHandle.
	 *
	 * @param handle The MethodHandle to wrap
	 */
	private PipeMethodHandle(MethodHandle handle) {
		this.handle = handle;
	}

	/**
	 * Invokes the wrapped method handle with the given arguments.
	 *
	 * @param args The arguments to pass to the method
	 * @throws RuntimeException if the invocation fails
	 */
	void invoke(Object... args) {
		try {
			handle.invokeWithArguments(args);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T outputData() {
		return this.output.outputData();
	}

	/**
	 * Sets the output supplier for this method handle.
	 *
	 * @param newOutputSupplier The new output supplier
	 */
	void setOutputSupplier(HasOutputData<T> newOutputSupplier) {
		this.output = newOutputSupplier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataType outputType() {
		return output.outputType();
	}
}