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

import com.slytechs.jnet.jnetruntime.util.HasName;
import com.slytechs.jnet.jnetruntime.util.HasRegistration;

/**
 * Defines the common interface for components in a pipeline system.
 * 
 * <p>
 * This interface extends HasName and HasRegistration, providing naming and
 * registration capabilities to all pipeline components. It also defines methods
 * for enabling, bypassing, and managing the state of pipeline components.
 * </p>
 *
 * @param <T_BASE> The specific type of the pipeline component implementation
 *
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 */
public interface PipeComponent<T_BASE extends PipeComponent<T_BASE>> extends HasName, HasRegistration {

	/**
	 * Enables or disables the pipeline component.
	 *
	 * @param b true to enable the component, false to disable
	 * @return This component instance for method chaining
	 */
	T_BASE enable(boolean b);

	/**
	 * Sets the bypass state of the pipeline component.
	 *
	 * @param b true to bypass the component, false otherwise
	 * @return This component instance for method chaining
	 */
	T_BASE bypass(boolean b);

	/**
	 * Checks if the pipeline component is currently enabled.
	 *
	 * @return true if the component is enabled, false otherwise
	 */
	boolean isEnabled();

	/**
	 * Checks if the pipeline component is currently bypassed.
	 *
	 * @return true if the component is bypassed, false otherwise
	 */
	boolean isBypassed();

	/**
	 * Sets a new name for the pipeline component.
	 *
	 * @param newName The new name for the component
	 * @return This component instance for method chaining
	 */
	T_BASE name(String newName);

	/**
	 * Enables or disables the pipeline component based on a boolean supplier.
	 *
	 * @param b A BooleanSupplier that determines whether to enable the component
	 * @return This component instance for method chaining
	 */
	T_BASE enable(BooleanSupplier b);

	/**
	 * Sets the bypass state of the pipeline component based on a boolean supplier.
	 *
	 * @param b A BooleanSupplier that determines whether to bypass the component
	 * @return This component instance for method chaining
	 */
	T_BASE bypass(BooleanSupplier b);
}