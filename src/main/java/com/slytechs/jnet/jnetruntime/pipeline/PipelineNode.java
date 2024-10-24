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
import com.slytechs.jnet.jnetruntime.util.Registration;

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
 * @author Mark Bednarczyk
 */
public interface PipelineNode<T_BASE extends PipelineNode<T_BASE>>
		extends HasName, Registration {

	/**
	 * Sets the auto-prune functionality for this node.
	 * 
	 * When auto-prune is enabled, the node will be automatically deactivated if it
	 * has no active downstream nodes to receive its output. This optimization
	 * occurs when all downstream nodes are either disabled, bypassed, or not
	 * expecting input.
	 * 
	 * Auto-pruning allows for dynamic optimization of the processing pipeline by
	 * eliminating unnecessary computations in inactive branches.
	 *
	 * @param enableAutoPrune true to enable auto-pruning, false to disable it
	 * @return the current instance, allowing for method chaining
	 */
	T_BASE autoPrune(boolean enableAutoPrune);

	/**
	 * Sets the bypass state of the pipeline component.
	 *
	 * @param b true to bypass the component, false otherwise
	 * @return This component instance for method chaining
	 */
	T_BASE bypass(boolean b);

	/**
	 * Sets the bypass state of the pipeline component based on a boolean supplier.
	 *
	 * @param b A BooleanSupplier that determines whether to bypass the component
	 * @return This component instance for method chaining
	 */
	T_BASE bypass(BooleanSupplier b);

	/**
	 * Enables or disables the pipeline component.
	 *
	 * @param b true to enable the component, false to disable
	 * @return This component instance for method chaining
	 */
	T_BASE enable(boolean b);

	/**
	 * Enables or disables the pipeline component based on a boolean supplier.
	 *
	 * @param b A BooleanSupplier that determines whether to enable the component
	 * @return This component instance for method chaining
	 */
	T_BASE enable(BooleanSupplier b);

	/**
	 * Checks if the auto-prune functionality is currently enabled for this node.
	 * 
	 * Auto-pruning, when enabled, automatically deactivates this node if it has no
	 * active downstream nodes to receive its output. This occurs when all
	 * downstream nodes are either disabled, bypassed, or not expecting input.
	 * 
	 * This method allows you to query the current auto-prune status without
	 * modifying it.
	 * 
	 * @return true if auto-pruning is enabled, false otherwise
	 * @see #autoPrune(boolean) for setting the auto-prune functionality
	 */
	boolean isAutoPruned();

	/**
	 * Checks if the pipeline component is currently bypassed.
	 *
	 * @return true if the component is bypassed, false otherwise
	 */
	boolean isBypassed();

	/**
	 * Checks if the pipeline component is currently enabled.
	 *
	 * @return true if the component is enabled, false otherwise
	 */
	boolean isEnabled();

	/**
	 * Sets a new name for the pipeline node.
	 *
	 * @param newName The new name for the node
	 * @return This component instance for method chaining
	 */
	T_BASE name(String newName);
}