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
 * Interface for components in a data processing pipeline that produce output
 * data descriptor interface.
 * 
 * <p>
 * This interface defines methods for accessing the output data and its type. It
 * is typically implemented by data processors, transformers, or other
 * components in a pipeline that generate or modify data.
 * </p>
 * 
 * <p>
 * Implementing classes should ensure that the {@code outputData()} and
 * {@code outputType()} methods are consistent with each other, providing
 * accurate type information for the produced data.
 * </p>
 *
 * @param <T> The type of output data produced by the implementing component
 * @author Mark Bednarczyk
 */
public interface HasOutputData<T> {

	/**
	 * Retrieves the current output data descriptor interface of the component.
	 * 
	 * <p>
	 * This method should return the most recent output data produced by the
	 * component. The exact nature and state of this data depends on the specific
	 * implementation and the current state of the pipeline processing.
	 * </p>
	 *
	 * @return The current output data
	 */
	T outputData();

	/**
	 * Retrieves the type of the output data descriptor interface produced by this
	 * component.
	 * 
	 * <p>
	 * This method should return a {@link DataType} object that describes the type
	 * of data this component outputs. This information can be used for type
	 * checking, pipeline configuration, or debugging purposes.
	 * </p>
	 *
	 * @return The {@link DataType} representing the type of the output data
	 * @see DataType
	 */
	DataType outputType();
}