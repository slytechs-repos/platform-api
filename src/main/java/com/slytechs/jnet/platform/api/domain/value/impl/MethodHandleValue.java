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

import java.lang.invoke.MethodHandle;

import com.slytechs.jnet.platform.api.domain.value.Value;

/**
 * 
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public class MethodHandleValue implements Value {

	private final MethodHandle getter;
	private final MethodHandle setter;
	private String name;

	public MethodHandleValue(String name, MethodHandle getter) {
		this.name = name;
		this.getter = getter;
		this.setter = null;
	}

	public MethodHandleValue(String name, MethodHandle getter, MethodHandle setter) {
		this.name = name;
		this.getter = getter;
		this.setter = setter;
	}

	public MethodHandleValue(String name, Object target, MethodHandle getter) {
		this.name = name;
		this.getter = getter.bindTo(target);
		this.setter = null;
	}

	public MethodHandleValue(String name, Object target, MethodHandle getter, MethodHandle setter) {
		this.name = name;
		this.getter = getter.bindTo(target);
		this.setter = setter.bindTo(target);
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
		if (setter != null)
			try {
				setter.invoke(newValue);
			} catch (Throwable e) {
				e.printStackTrace();
			}
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
