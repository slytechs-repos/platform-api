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

/**
 * Interface for components in a data processing pipeline that consume input
 * data.
 * 
 * <p>
 * This interface defines methods for accessing the input data and its type. It
 * is typically implemented by data processors, transformers, or other
 * components in a pipeline that receive or process input data.
 * </p>
 *
 * @param <T> The type of input data consumed by the implementing component
 *
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 */
public interface HasInputData<T> {

	/**
	 * Retrieves the current input data of the component.
	 * 
	 * <p>
	 * This method should return the most recent input data received by the
	 * component. The exact nature and state of this data depends on the specific
	 * implementation and the current state of the pipeline processing.
	 * </p>
	 *
	 * @return The current input data
	 */
	T inputData();

	/**
	 * Retrieves the type of the input data consumed by this component.
	 * 
	 * <p>
	 * This method should return a {@link DataType} object that describes the type
	 * of data this component expects as input. This information can be used for
	 * type checking, pipeline configuration, or debugging purposes.
	 * </p>
	 *
	 * @return The {@link DataType} representing the type of the input data
	 */
	DataType inputType();
}