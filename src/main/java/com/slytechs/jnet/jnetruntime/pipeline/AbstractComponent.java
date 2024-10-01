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

import java.util.Optional;
import java.util.function.BooleanSupplier;

import com.slytechs.jnet.jnetruntime.util.Registration;

/**
 * Abstract base class for pipeline components, implementing common
 * functionality and providing a foundation for specific component
 * implementations.
 * 
 * <p>
 * This class provides implementations for enabling/disabling components,
 * bypassing components, naming, and registration management. It also defines
 * hooks for subclasses to implement specific behaviors when the component's
 * state changes.
 * </p>
 *
 * @param <T_BASE> The specific type of the pipeline component, used for method
 *                 chaining
 * @author Mark Bednarczyk
 */
public abstract class AbstractComponent<T_BASE extends PipeComponent<T_BASE>>
		implements PipeComponent<T_BASE> {

	/** The name. */
	private String name;

	/** The registration. */
	private Registration registration;

	/** The enabled. */
	private boolean enabled;

	/** The bypass. */
	private boolean bypass;

	/**
	 * Constructs an AbstractComponent with no name.
	 */
	public AbstractComponent() {
	}

	/**
	 * Constructs an AbstractComponent with the specified name.
	 *
	 * @param name The name of the component
	 */
	public AbstractComponent(String name) {
		this.name = name;
	}

	/**
	 * Sets the bypass state of the component.
	 *
	 * @param b True to bypass the component, false otherwise
	 * @return This component instance for method chaining
	 */
	@Override
	public final T_BASE bypass(boolean b) {
		if (bypass == b)
			return us();
		this.bypass = b;
		onBypass(b);
		return us();
	}

	/**
	 * Sets the bypass state of the component using a boolean supplier.
	 *
	 * @param b A supplier that determines whether to bypass the component
	 * @return This component instance for method chaining
	 */
	@Override
	public final T_BASE bypass(BooleanSupplier b) {
		return bypass(b.getAsBoolean());
	}

	/**
	 * Enables or disables the component.
	 *
	 * @param b True to enable the component, false to disable
	 * @return This component instance for method chaining
	 */
	@Override
	public final T_BASE enable(boolean b) {
		if (enabled == b)
			return us();
		this.enabled = b;
		onEnable(b);
		return us();
	}

	/**
	 * Enables or disables the component using a boolean supplier.
	 *
	 * @param b A supplier that determines whether to enable the component
	 * @return This component instance for method chaining
	 */
	@Override
	public final T_BASE enable(BooleanSupplier b) {
		return enable(b.getAsBoolean());
	}

	/**
	 * Determines if this component is a built-in component.
	 *
	 * @return True if this is a built-in component, false otherwise
	 */
	public boolean isBuiltin() {
		return false;
	}

	/**
	 * Checks if the component is currently bypassed.
	 *
	 * @return True if the component is bypassed, false otherwise
	 */
	@Override
	public final boolean isBypassed() {
		return bypass;
	}

	/**
	 * Checks if the component is currently enabled.
	 *
	 * @return True if the component is enabled, false otherwise
	 */
	@Override
	public final boolean isEnabled() {
		return enabled;
	}

	/**
	 * Gets the name of the component.
	 *
	 * @return The name of the component
	 */
	@Override
	public final String name() {
		return name;
	}

	/**
	 * Sets a new name for the component.
	 *
	 * @param newName The new name for the component
	 * @return This component instance for method chaining
	 */
	@Override
	public final T_BASE name(String newName) {
		this.name = newName;
		return us();
	}

	/**
	 * Hook method called when the bypass state changes. Subclasses can override
	 * this to implement specific behavior.
	 *
	 * @param newValue The new bypass state
	 */
	protected void onBypass(boolean newValue) {
	}

	/**
	 * Hook method called when the enabled state changes. Subclasses can override
	 * this to implement specific behavior.
	 *
	 * @param newValue The new enabled state
	 */
	protected void onEnable(boolean newValue) {
	}

	/**
	 * Hook method called when the component is registered. Subclasses can override
	 * this to implement specific behavior.
	 */
	protected void onRegistration() {
	}

	/**
	 * Hook method called when the component is unregistered. Subclasses can
	 * override this to implement specific behavior.
	 */
	protected void onUnregistered() {
	}

	/**
	 * Gets the registration information for this component.
	 *
	 * @return An Optional containing the Registration object if registered, or an
	 *         empty Optional if not
	 */
	@Override
	public final Optional<Registration> registration() {
		return Optional.ofNullable(registration);
	}

	/**
	 * Sets the registration for this component. This method is package-private and
	 * should only be called by the pipeline management code.
	 *
	 * @param orNull The new Registration object, or null to unregister
	 */
	final void setRegistration(Registration orNull) {
		if (enabled && (orNull == null) && (registration != null))
			throw new IllegalStateException("element [%s] must be disabled before registration can be removed"
					.formatted(name()));
		this.registration = orNull;
		if (orNull == null)
			onUnregistered();
		else
			onRegistration();
	}

	/**
	 * Helper method to return this instance cast to the appropriate type for method
	 * chaining.
	 *
	 * @return This instance cast to T_BASE
	 */
	@SuppressWarnings("unchecked")
	protected final T_BASE us() {
		return (T_BASE) this;
	}
}