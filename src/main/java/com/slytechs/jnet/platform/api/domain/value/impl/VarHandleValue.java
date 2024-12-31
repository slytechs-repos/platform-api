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
package com.slytechs.jnet.platform.api.domain.value.impl;

import java.lang.invoke.VarHandle;

import com.slytechs.jnet.platform.api.domain.value.Value;

/**
 * 
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public class VarHandleValue implements Value {

	private final VarHandle handle;
	private String name;
	private final Object target;

	/**
	 * 
	 */
	public VarHandleValue(String name, VarHandle handle) {
		this.name = name;
		this.handle = handle;
		this.target = null;
	}

	/**
	 * 
	 */
	public VarHandleValue(String name, Object target, VarHandle handle) {
		this.name = name;
		this.target = target;
		this.handle = handle;
	}

	/**
	 * @see com.slytechs.jnet.platform.api.util.Named#name()
	 */
	@Override
	public String name() {
		return name;
	}

	/**
	 * @see com.slytechs.jnet.protocol.api.domain.value.Value#setValue(java.lang.Object)
	 */
	@Override
	public <T> void setValue(T newValue) {
		if (target == null)
			handle.set(newValue);
		else
			handle.set(target, newValue);
	}

	/**
	 * @see com.slytechs.jnet.platform.api.domain.value.Value#get()
	 */
	@Override
	public Object get() {
		throw new UnsupportedOperationException("not implemented yet");
	}

	/**
	 * @see com.slytechs.jnet.platform.api.domain.value.Value#set(java.lang.Object)
	 */
	@Override
	public void set(Object newValue) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	/**
	 * @see com.slytechs.jnet.platform.api.domain.value.Value#compareAndSet(java.lang.Object,
	 *      java.lang.Object)
	 */
	@Override
	public boolean compareAndSet(Object expectedValue, Object newValue) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	/**
	 * @see com.slytechs.jnet.platform.api.domain.value.Value#getAndSet(java.lang.Object)
	 */
	@Override
	public Object getAndSet(Object newValue) {
		throw new UnsupportedOperationException("not implemented yet");
	}

}
