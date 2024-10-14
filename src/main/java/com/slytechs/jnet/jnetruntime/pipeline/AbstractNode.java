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

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BooleanSupplier;

import com.slytechs.jnet.jnetruntime.util.HasPriority;
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
public class AbstractNode<T_BASE extends PipelineNode<T_BASE>>
		implements PipelineNode<T_BASE>, HasPriority, Comparable<HasPriority>, Registration {

	/** The name. */
	private String name;

	/** The registration. */
	private final AtomicReference<Registration> registration = new AtomicReference<>();

	/** The enabled. */
	private boolean enabled = true;

	/** The bypass. */
	private boolean bypass;

	protected final Lock readLock;
	protected final Lock writeLock;
	protected ReadWriteLock rwLock;

	private int priority;

	/**
	 * Constructs an AbstractComponent with no name.
	 */
	public AbstractNode(PipelineNode<?> component, int priority) {
		this(component, "", priority);
	}

	/**
	 * Constructs an AbstractComponent with the specified name.
	 *
	 * @param name The name of the component
	 */
	public AbstractNode(PipelineNode<?> component, String name, int priority) {
		this(((AbstractNode<?>) component).rwLock, name, priority);
	}

	/**
	 * Constructs an AbstractComponent with the specified name.
	 *
	 * @param name The name of the component
	 */
	private AbstractNode(ReadWriteLock rwLock, String name, int priority) {
		this.rwLock = rwLock;
		this.priority = priority;
		this.readLock = rwLock.readLock();
		this.writeLock = rwLock.writeLock();
		this.name = name;
	}

	AbstractNode(String name) {
		this(new ReentrantReadWriteLock(), name, HasPriority.DEFAULT_PRIORITY_VALUE);
	}

	/**
	 * Sets the bypass state of the component.
	 *
	 * @param b True to bypass the component, false otherwise
	 * @return This component instance for method chaining
	 */
	@Override
	public final T_BASE bypass(boolean b) {
		try {
			writeLock.lock();
			if (bypass == b) {
				return us();
			}

			this.bypass = b;

		} finally {
			writeLock.unlock();
		}

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

	protected final void checkIfIsEnabled() {
		if (enabled == false) {
			throw new IllegalStateException("%s [%s] is disabled"
					.formatted(getClass().getSimpleName(), name()));
		}
	}

	protected final void checkIfIsRegistered() {
		if (registration == null) {
			throw new IllegalStateException("%s [%s] is not registered"
					.formatted(getClass().getSimpleName(), name()));
		}
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public final int compareTo(HasPriority o) {
		return (this.priority < o.priority()) ? -1 : 1;
	}

	void disableDirect() {
		try {
			writeLock.lock();

			this.enabled = false;

		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * Enables or disables the component.
	 *
	 * @param b True to enable the component, false to disable
	 * @return This component instance for method chaining
	 */
	@Override
	public final T_BASE enable(boolean b) {
		try {
			writeLock.lock();

			if (enabled == b) {
				return us();
			}

			this.enabled = b;

		} finally {
			writeLock.unlock();
		}

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
		try {
			readLock.lock();

			return bypass;

		} finally {
			readLock.unlock();
		}
	}

	/**
	 * Checks if the component is currently enabled.
	 *
	 * @return True if the component is enabled, false otherwise
	 */
	@Override
	public final boolean isEnabled() {
		try {
			readLock.lock();

			return enabled;

		} finally {
			readLock.unlock();
		}
	}

	/**
	 * Gets the name of the component.
	 *
	 * @return The name of the component
	 */
	@Override
	public final String name() {
		try {
			readLock.lock();

			return name;

		} finally {
			readLock.unlock();
		}
	}

	/**
	 * Sets a new name for the component.
	 *
	 * @param newName The new name for the component
	 * @return This component instance for method chaining
	 */
	@Override
	public final T_BASE name(String newName) {
		try {
			writeLock.lock();

			this.name = newName;

		} finally {
			writeLock.unlock();
		}

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
	 * Hook method called when the priority values changes. Subclasses can override
	 * this to implement specific behavior.
	 *
	 * @param newPriority The new priority
	 */
	protected void onPriorityChange(int newPriority) {

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

	@Override
	public final int priority() {
		try {
			readLock.lock();

			return this.priority;

		} finally {
			readLock.unlock();
		}
	}

	public final T_BASE priority(int newPriority) {
		try {
			writeLock.lock();
			if (this.priority == newPriority) {
				return us();
			}

			HasPriority.checkPriorityValue(newPriority);

			this.priority = newPriority;
		} finally {
			writeLock.unlock();
		}

		onPriorityChange(newPriority);

		return us();
	}

	/**
	 * Sets the registration for this component. This method is package-private and
	 * should only be called by the pipeline management code.
	 *
	 * @param orNull The new Registration object, or null to unregister
	 */
	final void setRegistration(Registration orNull) {
		if (enabled && (orNull == null) && (registration != null)) {
			throw new IllegalStateException("element [%s] must be disabled before registration can be removed"
					.formatted(name()));
		}

		this.registration.set(orNull);

		if (orNull == null) {
			onUnregistered();
		} else {
			onRegistration();
		}
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

	/**
	 * @see com.slytechs.jnet.jnetruntime.util.Registration#unregister()
	 */
	@Override
	public void unregister() {
		if (registration.get() != null) {
			registration.get().unregister();
		}

		registration.set(null);
	}
}