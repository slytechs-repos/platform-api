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
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import com.slytechs.jnet.jnetruntime.pipeline.DataHandler;
import com.slytechs.jnet.jnetruntime.pipeline.DataType;
import com.slytechs.jnet.jnetruntime.pipeline.Processor;

/**
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 *
 */
public final class AnnotatedProcessorNode<T> extends AbstractNode<T, AnnotatedProcessorNode<T>> {

	public interface MethodHandleAdaptor<T> {
		T createAdaptor(MethodHandle handle, Object instance);
	}

	private static <T> AnnotatedProcessorNode<T> createAnnotatedProcessor(
			Pipeline<T, ?> channel,
			Class<?> containerClass,
			Object container,
			DataType dataType,
			Method method,
			MethodHandleAdaptor<T> invoker) {

		boolean isStatic = (container == null);

		try {
			var mh = isStatic
					? MethodHandles.lookup()
							.findStatic(containerClass, method.getName(),
									MethodType.methodType(method.getReturnType(), method.getParameterTypes()))
					: MethodHandles.lookup()
							.findVirtual(containerClass, method.getName(),
									MethodType.methodType(method.getReturnType(), method.getParameterTypes()));

			Processor processor = method.getAnnotation(Processor.class);
			int priority = processor.priority();
			String name = processor.name();

			if (name.isBlank())
				name = method.getName();

			return new AnnotatedProcessorNode<>(channel, priority, name, dataType, container, mh, method, invoker);

		} catch (Throwable e) {
			e.printStackTrace();

			throw new IllegalStateException(e);
		}
	}

	private static Class<?>[] extractDataParameters(Class<?> templateClass) {

		var methodList = Arrays.stream(templateClass.getDeclaredMethods())
				.filter(m -> (m.getModifiers() & Modifier.STATIC) == 0)
				.toList();

		if (methodList.size() > 1)
			methodList = methodList.stream()
					.filter(m -> m.isAnnotationPresent(DataHandler.class))
					.toList();

		if (methodList.isEmpty())
			throw new IllegalStateException("invalid data handle type, no dynamic data method in type %s"
					.formatted(templateClass.toString()));

		if (methodList.size() > 1)
			throw new IllegalStateException("invalid data handle type, too many data methods in type %s"
					.formatted(templateClass.toString()));

		return methodList.get(0).getParameterTypes();
	}

	private static <T> List<AnnotatedProcessorNode<T>> listAnnotatedProcessorMethods(
			Pipeline<T, ?> channel,
			Object containerInstance,
			Class<?> containerClass,
			DataType dataType,
			List<AnnotatedProcessorNode<T>> list,
			MethodHandleAdaptor<T> adaptor) {

		if (containerClass == Object.class)
			return list;

		for (var m : containerClass.getDeclaredMethods()) {

			boolean isStatic = (m.getModifiers() & Modifier.STATIC) == Modifier.STATIC;

			if (m.isAnnotationPresent(Processor.class) && matchParameters(m, dataType)) {
				Processor processor = m.getAnnotation(Processor.class);

				AnnotatedProcessorNode<T> node = createAnnotatedProcessor(
						channel,
						containerClass,
						isStatic ? null : containerInstance,
						dataType,
						m,
						adaptor);

				if (node != null) {
					list.add(node);

					node.enable(processor.enable());
				}
			}

		}

		return listAnnotatedProcessorMethods(channel, containerInstance, containerClass.getSuperclass(), dataType, list,
				adaptor);
	}

	public static <T> List<AnnotatedProcessorNode<T>> list(
			Pipeline<T, ?> channel,
			Object container,
			DataType type,
			MethodHandleAdaptor<T> invoker) {

		Class<?> containerClass;
		if (container instanceof Class<?> cl)
			containerClass = cl;
		else
			containerClass = container.getClass();

		List<AnnotatedProcessorNode<T>> list = listAnnotatedProcessorMethods(
				channel,
				container,
				containerClass,
				type,
				new ArrayList<>(),
				invoker);

		Collections.sort(list, (p1, p2) -> p1.priority() - p2.priority());

		return list;
	}

	private static boolean matchParameters(Method method, DataType dataType) {

		if (!method.isAnnotationPresent(Processor.class))
			return false;

		Processor processor = method.getAnnotation(Processor.class);

		Class<?> dataClass = dataType.dataSupport().dataClass();
		if (dataClass != processor.value())
			return false;

		Class<?>[] dataParameters = extractDataParameters(processor.value());
		Class<?>[] classParameters = method.getParameterTypes();

		var p1 = classParameters;
		var p2 = dataParameters;

		return (p1.length == p2.length)
				&& IntStream.range(0, p1.length)
						.filter(i -> !p1[i].isAssignableFrom(p2[i]))
						.findFirst()
						.isEmpty();
	}

	private final Object instance;
	private final MethodHandle handle;
	private final Method method;

	private AnnotatedProcessorNode(
			Pipeline<T, ?> parent,
			int priority,
			String name,
			DataType dataType,
			Object processorContainer,
			MethodHandle handle,
			Method method,
			MethodHandleAdaptor<T> adaptor) {
		super(parent, priority, name, adaptor.createAdaptor(handle, processorContainer), dataType);

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
