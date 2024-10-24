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
 * Represents a processor in a pipeline that is configured using annotations.
 * This class extends AbstractProcessor and provides functionality to create and
 * manage processors based on annotated methods.
 *
 * @param <T> The type of data processed by this processor
 * @author Mark Bednarczyk
 */
final class AnnotatedProcessor<T> extends AbstractProcessor<T, AnnotatedProcessor<T>> {

	/**
	 * Interface for adapting method handles to the processor's data type.
	 *
	 * @param <T> The type of data processed by the processor
	 * @author Mark Bednarczyk
	 */
	public interface MethodHandleAdaptor<T> {
		/**
		 * Creates an adaptor for the given method handle.
		 *
		 * @param handle The method handle to adapt
		 * @return An adapted object of type T
		 */
		T createAdaptor(@SuppressWarnings("rawtypes") PipeMethodHandle handle);
	}

	/**
	 * Interface for creating method invokers.
	 *
	 * @param <T_IN>  The input type for the invoker
	 * @param <T_OUT> The output type for the invoker
	 */
	public interface MethodInvokerFactory<T_IN, T_OUT> {
		
		/**
		 * Specialized interface for unary (same input and output type) method invokers.
		 *
		 * @param <T> The type of data for both input and output
		 * @author Mark Bednarczyk
		 */
		interface Uni<T> extends MethodInvokerFactory<T, T> {
		}

		/**
		 * Creates an invoker for the given method handle.
		 *
		 * @param handle   The method handle to invoke
		 * @param instance The instance on which to invoke the method (null for static
		 *                 methods)
		 * @param out      The output object
		 * @return The input object
		 */
		T_IN invoker(@SuppressWarnings("rawtypes") PipeMethodHandle handle, Object instance, T_OUT out);
	}

	/**
	 * Creates an AnnotatedProcessor instance.
	 *
	 * @param <T>            The type of data processed by the processor
	 * @param channel        The pipeline channel
	 * @param containerClass The class containing the annotated method
	 * @param container      The container object (null for static methods)
	 * @param dataType       The data type processed by this processor
	 * @param method         The annotated method
	 * @param processor      The AProcessor annotation
	 * @param invoker        The method handle adaptor
	 * @return A new AnnotatedProcessor instance
	 * @throws IllegalStateException if the processor cannot be created
	 */
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

	/**
	 * Creates a list of AnnotatedProcessor instances from a container object with a
	 * specified data type.
	 *
	 * @param <T>       The type of data processed by the processors
	 * @param channel   The pipeline channel
	 * @param container The container object with annotated methods
	 * @param type      The specific data type to filter processors (can be null)
	 * @param invoker   The method handle adaptor
	 * @return A list of AnnotatedProcessor instances
	 */
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

	/**
	 * Creates a list of AnnotatedProcessor instances from a container object.
	 *
	 * @param <T>       The type of data processed by the processors
	 * @param channel   The pipeline channel
	 * @param container The container object with annotated methods
	 * @param invoker   The method handle adaptor
	 * @return A list of AnnotatedProcessor instances
	 */
	public static <T> List<AnnotatedProcessor<T>> list(
			Pipeline<T, ?> channel,
			Object container,
			MethodHandleAdaptor<T> invoker) {
		return list(channel, container, null, invoker);
	}

	/**
	 * Recursively lists all annotated processor methods in a class hierarchy.
	 *
	 * @param <T>               The type of data processed by the processors
	 * @param channel           The pipeline channel
	 * @param containerInstance The container object instance
	 * @param containerClass    The current class being examined
	 * @param list              The list to populate with processors
	 * @param adaptor           The method handle adaptor
	 * @return The populated list of AnnotatedProcessor instances
	 */
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
			ATypeLookup lookup = PipelineUtils.lookupAnnotationRecusively(m, ATypeLookup.class);
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

	/** The instance. */
	private final Object instance;
	
	/** The handle. */
	@SuppressWarnings({
			"rawtypes",
			"unused" })
	private final PipeMethodHandle handle;
	
	/** The method. */
	private final Method method;

	/**
	 * Constructs a new AnnotatedProcessor.
	 *
	 * @param parent             The parent pipeline
	 * @param priority           The priority of this processor
	 * @param name               The name of this processor
	 * @param dataType           The data type processed by this processor
	 * @param processorContainer The container object (null for static methods)
	 * @param handle             The method handle for this processor
	 * @param method             The annotated method
	 * @param adaptor            The method handle adaptor
	 * @throws NullPointerException if dataType is null
	 */
	private AnnotatedProcessor(
			Pipeline<T, ?> parent,
			int priority,
			String name,
			DataType dataType,
			Object processorContainer,
			@SuppressWarnings("rawtypes") PipeMethodHandle handle,
			Method method,
			MethodHandleAdaptor<T> adaptor) {
		super(parent, priority, name, dataType, adaptor.createAdaptor(handle));

		Objects.requireNonNull(dataType, "dataType");

		this.instance = processorContainer;
		this.method = method;
		this.handle = handle;
	}

	/**
	 * {@inheritDoc}
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