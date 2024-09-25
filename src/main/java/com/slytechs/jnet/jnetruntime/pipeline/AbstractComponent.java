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
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 *
 */
public abstract class AbstractComponent<T_BASE extends PipeComponent<T_BASE>>
		implements PipeComponent<T_BASE> {

	private String name;
	private Registration registration;
	private boolean enabled;
	private boolean bypass;

	public AbstractComponent() {
	}

	public AbstractComponent(String name) {
		this.name = name;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.PipeComponent#bypass(boolean)
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
	 * @see com.slytechs.jnet.jnetruntime.pipeline.PipeComponent#bypass(java.util.function.BooleanSupplier)
	 */
	@Override
	public final T_BASE bypass(BooleanSupplier b) {
		return bypass(b.getAsBoolean());
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.PipeComponent#enable(boolean)
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
	 * @see com.slytechs.jnet.jnetruntime.pipeline.PipeComponent#enable(java.util.function.BooleanSupplier)
	 */
	@Override
	public final T_BASE enable(BooleanSupplier b) {
		return enable(b.getAsBoolean());
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.PipeComponent#isBypassed()
	 */
	@Override
	public final boolean isBypassed() {
		return bypass;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.PipeComponent#isEnabled()
	 */
	@Override
	public final boolean isEnabled() {
		return enabled;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.util.HasName#name()
	 */
	@Override
	public final String name() {
		return name;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.PipeComponent#name(java.lang.String)
	 */
	@Override
	public final T_BASE name(String newName) {
		this.name = newName;

		return us();
	}

	protected void onBypass(boolean newValue) {

	}

	protected void onEnable(boolean newValue) {

	}

	protected void onRegistration() {

	}

	protected void onUnregistered() {

	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.util.HasRegistration#registration()
	 */
	@Override
	public final Optional<Registration> registration() {
		return Optional.ofNullable(registration);
	}

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

	@SuppressWarnings("unchecked")
	protected final T_BASE us() {
		return (T_BASE) this;
	}
}
