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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.slytechs.jnet.jnetruntime.pipeline.PipeMethodHandle.DataHandleAdaptor;

/**
 * Represents a transformer in a pipeline that is configured using annotations.
 * This class extends AbstractTransformer and provides functionality to create
 * and manage transformers based on annotated methods.
 *
 * @param <T_IN>  The input type for the transformer
 * @param <T_OUT> The output type for the transformer
 * 
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 */
class AnnotatedTransformer<T_IN, T_OUT>
		extends AbstractTransformer<T_IN, T_OUT, AnnotatedTransformer<T_IN, T_OUT>>
		implements HasOutputData<T_OUT> {

	/**
	 * Creates a list of AnnotatedTransformer instances from a container object.
	 *
	 * @param <T_IN>            The input type for the transformers
	 * @param <T_OUT>           The output type for the transformers
	 * @param container         The container object with annotated methods
	 * @param dataHandleAdaptor The adaptor for creating data handles
	 * @return A list of AnnotatedTransformer instances
	 */
	public static <T_IN, T_OUT> List<AnnotatedTransformer<?, ?>> list(
			Object container,
			DataHandleAdaptor<T_IN, T_OUT> dataHandleAdaptor) {
		if (container instanceof Class<?> containerClass)
			return list(null, containerClass, dataHandleAdaptor);
		return list(container, container.getClass(), dataHandleAdaptor);
	}

	/**
	 * Creates a list of AnnotatedTransformer instances from a container object and
	 * its class.
	 *
	 * @param container         The container object with annotated methods (can be
	 *                          null for static methods)
	 * @param containerClass    The class of the container
	 * @param dataHandleAdaptor The adaptor for creating data handles
	 * @return A list of AnnotatedTransformer instances
	 * @throws NullPointerException if containerClass is null
	 */
	private static List<AnnotatedTransformer<?, ?>> list(
			Object container,
			Class<?> containerClass, DataHandleAdaptor<?, ?> dataHandleAdaptor) {
		Objects.requireNonNull(containerClass, "container class");
		List<AnnotatedTransformer<?, ?>> list = new ArrayList<>();
		for (var m : containerClass.getDeclaredMethods()) {
			var at = createTransformer(m, container, containerClass, dataHandleAdaptor);
			if (at != null)
				list.add(at);
		}
		return list;
	}

	/**
	 * Creates an AnnotatedTransformer instance from an annotated method.
	 *
	 * @param <T_IN>            The input type for the transformer
	 * @param <T_OUT>           The output type for the transformer
	 * @param method            The annotated method
	 * @param container         The container object (null for static methods)
	 * @param containerClass    The class containing the annotated method
	 * @param dataHandleAdaptor The adaptor for creating data handles
	 * @return An AnnotatedTransformer instance, or null if the method is not
	 *         properly annotated
	 */
	private static <T_IN, T_OUT> AnnotatedTransformer<T_IN, T_OUT> createTransformer(Method method, Object container,
			Class<?> containerClass, DataHandleAdaptor<T_IN, T_OUT> dataHandleAdaptor) {
		ATransformer transformer = PipelineUtils.lookupAnnotationRecusively(method, ATransformer.class);
		ATypeLookup lookup = PipelineUtils.lookupAnnotationRecusively(method, ATypeLookup.class);
		if (transformer == null || lookup == null)
			return null;
		String name = transformer.name().isBlank() ? method.getName() : transformer.name();
		var inDataClass = transformer.in();
		var outDataClass = transformer.out();
		var inDataType = PipelineUtils.lookupDataType(inDataClass, lookup);
		var outDataType = PipelineUtils.lookupDataType(outDataClass, lookup);
		if (inDataType == null || outDataType == null)
			return null;
		if (!PipelineUtils.matchDataParameters(method, inDataClass, inDataType))
			return null;
		PipeMethodHandle<T_OUT> mh = PipeMethodHandle.from(method, container, containerClass);
		T_IN adaptedInput = dataHandleAdaptor.createAdaptor(mh);
		AnnotatedTransformer<T_IN, T_OUT> at = new AnnotatedTransformer<>(
				name,
				adaptedInput,
				inDataType,
				outDataType);
		mh.setOutputSupplier(at);
		return at;
	}

	/**
	 * Constructs a new AnnotatedTransformer.
	 *
	 * @param name       The name of this transformer
	 * @param in         The input data
	 * @param inputType  The input data type
	 * @param outputType The output data type
	 */
	private AnnotatedTransformer(String name, T_IN in, DataType inputType, DataType outputType) {
		super(name, in, inputType, outputType);
	}
}