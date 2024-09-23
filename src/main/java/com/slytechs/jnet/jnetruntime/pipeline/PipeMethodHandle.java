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

public final class PipeMethodHandle<T> implements HasDataOutput<T> {

	public interface DataHandleAdaptor<T_IN, T_OUT> {
		public interface UniDataHandleAdaptor<T> extends DataHandleAdaptor<T, T> {
		}

		T_IN createAdaptor(PipeMethodHandle<T_OUT> handle);
	}

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

	private final MethodHandle handle;
	private HasDataOutput<T> output;

	private PipeMethodHandle(MethodHandle handle) {
		this.handle = handle;
	}

	void invoke(Object... args) {
		try {
			handle.invokeWithArguments(args);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.HasDataOutput#outputData()
	 */
	@Override
	public T outputData() {
		return this.output.outputData();
	}

	void setOutputSupplier(HasDataOutput<T> newOutputSupplier) {
		this.output = newOutputSupplier;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.HasDataOutput#outputType()
	 */
	@Override
	public DataType outputType() {
		return output.outputType();
	}
}