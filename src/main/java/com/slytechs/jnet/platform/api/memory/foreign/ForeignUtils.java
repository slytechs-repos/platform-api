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
package com.slytechs.jnet.platform.api.memory.foreign;

import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;
import java.util.function.Consumer;

/**
 * The Class ForeignUtils.
 *
 * @author Mark Bednarczyk
 */
public final class ForeignUtils {
	
	/** The Constant EMPTY_CLEANUP. */
	public static final Consumer<MemorySegment> EMPTY_CLEANUP = new Consumer<MemorySegment>() {
		@Override
		public void accept(MemorySegment t) {
			
		}
	};

	/** The Constant DEFAULT_MAX_STRING_LEN. */
	private final static long DEFAULT_MAX_STRING_LEN = 64 * 1024;

	/**
	 * To java string.
	 *
	 * @param memorySegment the memory segment
	 * @return the string
	 */
	public static String toJavaString(Object memorySegment) {
		return toJavaString(((MemorySegment) memorySegment));
	}

	/**
	 * Checks if is null address.
	 *
	 * @param address the address
	 * @return true, if is null address
	 */
	public static boolean isNullAddress(MemorySegment address) {
		return (address == null) || (address.address() == 0);
	}

	/**
	 * To java string.
	 *
	 * @param addr the addr
	 * @return the string
	 */
	public static String toJavaString(MemorySegment addr) {
		if (ForeignUtils.isNullAddress(addr))
			return null;

		if (addr.byteSize() == 0)
			addr = addr.reinterpret(DEFAULT_MAX_STRING_LEN);

		String str = addr.getString(0);
		return str;
	}

	/**
	 * Read address.
	 *
	 * @param handle    the handle
	 * @param addressAt the address at
	 * @return the memory segment
	 */
	public static MemorySegment readAddress(VarHandle handle, MemorySegment addressAt) {
		var read = (MemorySegment) handle.get(addressAt);
		return read;
	}

	/**
	 * Instantiates a new foreign utils.
	 */
	private ForeignUtils() {
	}

}
