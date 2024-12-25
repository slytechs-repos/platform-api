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
package com.slytechs.jnet.platform.api.data;

import java.util.function.Consumer;

import com.slytechs.jnet.platform.api.data.pipeline.ErrorPolicy;
import com.slytechs.jnet.platform.api.util.Registration;

/**
 * Core interface for data processing components in the platform pipeline
 * architecture. Processors transform input data and produce output that can be
 * chained with other processors. Each processor has configurable priority,
 * error handling, and monitoring capabilities through peek operations.
 *
 * <p>
 * A processor's behavior can be controlled through:
 * <ul>
 * <li>Enable/disable state
 * <li>Processing priority (order in the chain)
 * <li>Error handling policies
 * <li>Peek operations for monitoring
 * </ul>
 *
 * <p>
 * Typical usage:
 * 
 * <pre>{@code
 * DataProcessor<Packet> processor = ...;
 * processor.setErrorPolicy(ErrorPolicy.PROPAGATE)
 *         .setPriority(50)
 *         .enable();
 * }</pre>
 *
 * @param <T> Type of data being processed
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc
 * @since 1.0.0
 */
public interface DataProcessor<T> {

	/**
	 * Gets the input data for this processor. Input represents the data that will
	 * be processed when this processor executes.
	 *
	 * @return Current input data source
	 */
	T getInput();

	/**
	 * Gets the processed output data. Output represents transformed data after
	 * processing completes.
	 *
	 * @return Processed output data, may be null if processing hasn't completed
	 */
	T getOutput();

	/**
	 * Gets the data type descriptor for processed data. The descriptor provides
	 * type information and utilities for data manipulation.
	 *
	 * @return Data type descriptor
	 */
	DataType<T> dataType();

	/**
	 * Disables this processor. When disabled, processor will be skipped in the
	 * processing chain.
	 *
	 * @return This processor for method chaining
	 */
	DataProcessor<T> disable();

	/**
	 * Enables this processor. When enabled, processor will actively participate in
	 * data processing.
	 *
	 * @return This processor for method chaining
	 */
	DataProcessor<T> enable();

	/**
	 * Gets current error handling policy. Policy determines how errors are handled
	 * during processing.
	 *
	 * @return Current error policy
	 */
	ErrorPolicy getErrorPolicy();

	/**
	 * Gets processor's unique identifier.
	 *
	 * @return Processor identifier
	 */
	Object id();

	/**
	 * Checks if processor is currently enabled.
	 *
	 * @return true if enabled, false if disabled
	 */
	boolean isEnabled();

	/**
	 * Gets processor's descriptive name.
	 *
	 * @return Processor name
	 */
	String name();

	/**
	 * Gets processor's current priority. Priority determines order of execution in
	 * processing chain.
	 *
	 * @return Current priority value
	 */
	int priority();

	/**
	 * Sets processor's priority level. Higher priority processors execute earlier
	 * in the processing chain.
	 *
	 * @param newPriority Priority value (typically 0-100)
	 * @return This processor for method chaining
	 * @throws IllegalArgumentException If priority value out of valid range
	 */
	DataProcessor<T> setPriority(int newPriority);

	/**
	 * Enables or disables this processor.
	 *
	 * @param newState true to enable, false to disable
	 * @return This processor for method chaining
	 */
	DataProcessor<T> setEnable(boolean newState);

	/**
	 * Sets error handling policy.
	 *
	 * @param policy New error policy to apply
	 * @throws IllegalArgumentException If policy is null
	 */
	void setErrorPolicy(ErrorPolicy policy);

	/**
	 * Sets processor's unique identifier.
	 *
	 * @param newId New identifier
	 * @return This processor for method chaining
	 * @throws IllegalArgumentException If newId is null
	 */
	DataProcessor<T> setId(Object newId);

	/**
	 * Adds a peek processor to observe data without modifying processing chain.
	 * Equivalent to peek(newPeeker, r -> {}).
	 *
	 * @param newPeeker Processor to receive peek data
	 * @return This processor for method chaining
	 * @throws IllegalArgumentException If newPeeker is null
	 */
	DataProcessor<T> peek(T newPeeker);

	/**
	 * Adds a peek processor with registration callback for cleanup.
	 *
	 * <p>
	 * Example:
	 * 
	 * <pre>{@code
	 * processor.peek(monitor, reg -> {
	 * 	// Store registration to unregister later
	 * 	this.registration = reg;
	 * });
	 * // Later...
	 * registration.unregister(); // Remove peek processor
	 * }</pre>
	 *
	 * @param newPeeker Processor to receive peek data
	 * @param registrar Callback to receive unregistration handle
	 * @return This processor for method chaining
	 * @throws IllegalArgumentException If newPeeker or registrar is null
	 */
	DataProcessor<T> peek(T newPeeker, Consumer<Registration> registrar);
}