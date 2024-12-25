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

import java.lang.foreign.MemoryLayout;

import static java.lang.foreign.MemoryLayout.*;
import static java.lang.foreign.ValueLayout.*;

/**
 * @author Mark Bednarczyk
 *
 */
public final class NativeLayout {

	private static final long INT32_SIZE, LONG64_SIZE, ADDRESS_SIZE;

	static {
		NativeABI abi = NativeABI.current();

		if (NativeABI.is32bit()) {
			INT32_SIZE = 4;
			LONG64_SIZE = 8;
			ADDRESS_SIZE = 4;

		} else {
			INT32_SIZE = 8;
			LONG64_SIZE = 8;
			ADDRESS_SIZE = 8;
		}

	}

	public static MemoryLayout PAD32(long numBytes) {
		return sequenceLayout(INT32_SIZE - numBytes, JAVA_BYTE);
	}

	public static MemoryLayout PAD64(long numBytes) {
		return sequenceLayout(LONG64_SIZE - numBytes, JAVA_BYTE);
	}

	private NativeLayout() {
	}

}
