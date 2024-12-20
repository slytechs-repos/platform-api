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
package com.slytechs.jnet.platform.api.internal.util;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

import com.slytechs.jnet.platform.api.util.Numberable;

/**
 * @author Mark Bednarczyk
 *
 */
public class MemoryNumberable implements Numberable {

	private final MemorySegment mseg;
	private final long offset;
	private final VarHandle varHandle;

	public MemoryNumberable(MemorySegment mseg, long offset) {
		this.mseg = mseg;
		this.offset = offset;
		this.varHandle = null;

	}

	/**
	 * @see com.slytechs.jnet.platform.api.util.Numberable#as(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Number> T as(Class<T> numberClass) {

		Number num;

		if (varHandle != null) {

			if (numberClass.isAssignableFrom(Number.class))
				num = (Number) varHandle.get(mseg, offset);

			else
				throw new IllegalArgumentException("unsupported type " + numberClass);

		} else {

			if (numberClass == byte.class || numberClass == Byte.class)
				num = mseg.get(ValueLayout.JAVA_BYTE, offset);

			else if (numberClass == short.class || numberClass == Short.class)
				num = mseg.get(ValueLayout.JAVA_SHORT, offset);

			else if (numberClass == int.class || numberClass == Integer.class)
				num = mseg.get(ValueLayout.JAVA_INT, offset);

			else if (numberClass == long.class || numberClass == Long.class)
				num = mseg.get(ValueLayout.JAVA_LONG, offset);

			else if (numberClass == float.class || numberClass == Float.class)
				num = mseg.get(ValueLayout.JAVA_SHORT, offset);

			else if (numberClass == double.class || numberClass == Double.class)
				num = mseg.get(ValueLayout.JAVA_SHORT, offset);

			else
				throw new IllegalArgumentException("unsupported type " + numberClass);
		}

		if (numberClass == byte.class || numberClass == Byte.class)
			return (T) (Number) num.byteValue();

		else if (numberClass == short.class || numberClass == Short.class)
			return (T) (Number) num.shortValue();

		else if (numberClass == int.class || numberClass == Integer.class)
			return (T) (Number) num.intValue();

		else if (numberClass == long.class || numberClass == Long.class)
			return (T) (Number) num.longValue();

		else if (numberClass == float.class || numberClass == Float.class)
			return (T) (Number) num.floatValue();

		else if (numberClass == double.class || numberClass == Double.class)
			return (T) (Number) num.doubleValue();

		throw new IllegalArgumentException("unsupported type " + numberClass);
	}

	/**
	 * @see com.slytechs.jnet.platform.api.util.Numberable#asUnsigned(java.lang.Class)
	 */
	@Override
	public <T extends Number> int asUnsignedInt(Class<T> unsignedClass) {
		Number num = as(unsignedClass);

		if (unsignedClass == byte.class || unsignedClass == Byte.class)
			return Byte.toUnsignedInt(num.byteValue());

		else if (unsignedClass == short.class || unsignedClass == Short.class)
			return Short.toUnsignedInt(num.shortValue());

		else if (unsignedClass == int.class || unsignedClass == Integer.class)
			return Byte.toUnsignedInt(num.byteValue());

		throw new IllegalArgumentException("unsupported type " + unsignedClass);
	}

	/**
	 * @see com.slytechs.jnet.platform.api.util.Numberable#asUnsigned(java.lang.Class)
	 */
	@Override
	public <T extends Number> long asUnsignedLong(Class<T> unsignedClass) {
		Number num = as(unsignedClass);

		if (unsignedClass == byte.class || unsignedClass == Byte.class)
			return Byte.toUnsignedLong(num.byteValue());

		else if (unsignedClass == short.class || unsignedClass == Short.class)
			return Short.toUnsignedLong(num.shortValue());

		else if (unsignedClass == int.class || unsignedClass == Integer.class)
			return Integer.toUnsignedLong(num.intValue());

		throw new IllegalArgumentException("unsupported type " + unsignedClass);
	}
}
