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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 *
 */
public final class AnnotatedProcessor<T> extends AbstractProcessor<T, AnnotatedProcessor<T>> {

	public interface MethodHandleAdaptor<T> {
		T createAdaptor(PipeMethodHandle handle);
	}

	public interface MethodInvokerFactory<T_IN, T_OUT> {
		interface Uni<T> extends MethodInvokerFactory<T, T> {
		}

		T_IN invoker(PipeMethodHandle handle, Object instance, T_OUT out);
	}

	private static <T> AnnotatedProcessor<T> createProcessor(
			Pipeline<T, ?> channel,
			Class<?> containerClass,
			Object container,
			DataType dataType,
			Method method,
			AProcessor processor,
			MethodHandleAdaptor<T> invoker) {

		try {
			var methodHandle = PipeMethodHandle.from(method, container, containerClass);

			int priority = processor.priority();
			String name = processor.name();

			if (name.isBlank())
				name = method.getName();

			return new AnnotatedProcessor<>(channel, priority, name, dataType, container, methodHandle, method,
					invoker);

		} catch (Throwable e) {
			e.printStackTrace();

			throw new IllegalStateException(e);
		}
	}

	public static <T> List<AnnotatedProcessor<T>> list(
			Pipeline<T, ?> channel,
			Object container,
			MethodHandleAdaptor<T> invoker) {
		return list(channel, container, null, invoker);
	}

	public static <T> List<AnnotatedProcessor<T>> list(
			Pipeline<T, ?> channel,
			Object container,
			DataType type,
			MethodHandleAdaptor<T> invoker) {

		Class<?> containerClass;
		if (container instanceof Class<?> cl)
			containerClass = cl;
		else
			containerClass = container.getClass();

		List<AnnotatedProcessor<T>> list = listAnnotatedProcessorMethods(
				channel,
				container,
				containerClass,
				new ArrayList<>(),
				invoker);

		Collections.sort(list, (p1, p2) -> p1.priority() - p2.priority());

		return list;
	}

	private static <T> List<AnnotatedProcessor<T>> listAnnotatedProcessorMethods(
			Pipeline<T, ?> channel,
			Object containerInstance,
			Class<?> containerClass,
			List<AnnotatedProcessor<T>> list,
			MethodHandleAdaptor<T> adaptor) {

		if (containerClass == Object.class)
			return list;

		for (var m : containerClass.getDeclaredMethods()) {
			AProcessor processor = PipelineUtils.lookupAnnotationRecusively(m, AProcessor.class);
			TypeLookup lookup = PipelineUtils.lookupAnnotationRecusively(m, TypeLookup.class);
			if (processor == null || lookup == null)
				continue;

			boolean isStatic = (m.getModifiers() & Modifier.STATIC) == Modifier.STATIC;

			DataType dataType = PipelineUtils.lookupDataType(processor.value(), lookup);

			if (PipelineUtils.matchDataParameters(m, processor.value(), dataType)) {
				AnnotatedProcessor<T> node = createProcessor(
						channel,
						containerClass,
						isStatic ? null : containerInstance,
						dataType,
						m,
						processor,
						adaptor);

				if (node != null) {
					list.add(node);

					node.enable(processor.enable());
				}
			}

		}

		return listAnnotatedProcessorMethods(channel, containerInstance, containerClass.getSuperclass(), list, adaptor);
	}

	private final Object instance;
	private final PipeMethodHandle handle;
	private final Method method;

	private AnnotatedProcessor(
			Pipeline<T, ?> parent,
			int priority,
			String name,
			DataType dataType,
			Object processorContainer,
			PipeMethodHandle handle,
			Method method,
			MethodHandleAdaptor<T> adaptor) {
		super(parent, priority, name, dataType, adaptor.createAdaptor(handle));

		Objects.requireNonNull(dataType, "dataType");

		this.instance = processorContainer;
		this.method = method;
		this.handle = handle;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AnnotatedProcessorNode ["
				+ "name=" + name()
				+ ", method=" + method
				+ (instance == null ? "" : ", instance=" + instance)
				+ "]";
	}
}
