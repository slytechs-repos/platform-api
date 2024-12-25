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
package com.slytechs.jnet.platform.api.memory.layout;

import java.util.Optional;
import java.util.OptionalLong;

import com.slytechs.jnet.platform.api.util.array.ByteArray;

/**
 * The Interface ArrayField.
 *
 * @author Sly Technologies
 */
public interface ArrayField extends BinaryField {

	/**
	 * The Interface Proxy.
	 *
	 * @author Mark Bednarczyk
	 */
	public interface Proxy extends ArrayField {

		/**
		 * Gets the byte array at.
		 *
		 * @param byteOffset the byte offset
		 * @param array      the array
		 * @param offset     the offset
		 * @param length     the length
		 * @param data       the data
		 * @return the byte array at
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#getByteArrayAt(long,
		 *      byte[], int, int, java.lang.Object)
		 */
		@Override
		default byte[] getByteArrayAt(long byteOffset, byte[] array, int offset, int length, Object data) {
			return proxyArrayField().getByteArrayAt(byteOffset, array, offset, length, data);
		}

		/**
		 * Bit offset.
		 *
		 * @return the long
		 * @see com.slytechs.jnet.platform.api.memory.layout.BinaryField#bitOffset()
		 */
		@Override
		default long bitOffset() {
			return proxyArrayField().bitOffset();
		}

		/**
		 * Bit offset.
		 *
		 * @param sequences the sequences
		 * @return the long
		 * @see com.slytechs.jnet.platform.api.memory.layout.BinaryField#bitOffset(long[])
		 */
		@Override
		default long bitOffset(long... sequences) {
			return proxyArrayField().bitOffset(sequences);
		}

		/**
		 * Bit size.
		 *
		 * @return the long
		 * @see com.slytechs.jnet.platform.api.memory.layout.BinaryField#bitSize()
		 */
		@Override
		default long bitSize() {
			return proxyArrayField().bitSize();
		}

		/**
		 * Byte offset.
		 *
		 * @return the long
		 * @see com.slytechs.jnet.platform.api.memory.layout.BinaryField#byteOffset()
		 */
		@Override
		default long byteOffset() {
			return proxyArrayField().byteOffset();
		}

		/**
		 * Byte offset.
		 *
		 * @param sequences the sequences
		 * @return the long
		 * @see com.slytechs.jnet.platform.api.memory.layout.BinaryField#byteOffset(long[])
		 */
		@Override
		default long byteOffset(long... sequences) {
			return proxyArrayField().byteOffset(sequences);
		}

		/**
		 * Field name.
		 *
		 * @return the optional
		 * @see com.slytechs.jnet.platform.api.memory.layout.BinaryField#fieldName()
		 */
		@Override
		default Optional<String> fieldName() {
			return proxyArrayField().fieldName();
		}

		/**
		 * Gets the byte array.
		 *
		 * @param array  the array
		 * @param offset the offset
		 * @param length the length
		 * @param data   the data
		 * @return the byte array
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#getByteArray(byte[],
		 *      int, int, java.lang.Object)
		 */
		@Override
		default byte[] getByteArray(byte[] array, int offset, int length, Object data) {
			return proxyArrayField().getByteArray(array, offset, length, data);
		}

		/**
		 * Gets the byte array.
		 *
		 * @param array     the array
		 * @param offset    the offset
		 * @param length    the length
		 * @param data      the data
		 * @param sequences the sequences
		 * @return the byte array
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#getByteArray(byte[],
		 *      int, int, java.lang.Object, long[])
		 */
		@Override
		default byte[] getByteArray(byte[] array, int offset, int length, Object data, long... sequences) {
			return proxyArrayField().getByteArray(array, offset, length, data, sequences);
		}

		/**
		 * Gets the byte array.
		 *
		 * @param array the array
		 * @param data  the data
		 * @return the byte array
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#getByteArray(byte[],
		 *      java.lang.Object)
		 */
		@Override
		default byte[] getByteArray(byte[] array, Object data) {
			return proxyArrayField().getByteArray(array, data);
		}

		/**
		 * Gets the byte array.
		 *
		 * @param array     the array
		 * @param data      the data
		 * @param sequences the sequences
		 * @return the byte array
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#getByteArray(byte[],
		 *      java.lang.Object, long[])
		 */
		@Override
		default byte[] getByteArray(byte[] array, Object data, long... sequences) {
			return proxyArrayField().getByteArray(array, data, sequences);
		}

		/**
		 * Gets the byte array.
		 *
		 * @param array the array
		 * @param data  the data
		 * @return the byte array
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#getByteArray(com.slytechs.jnet.platform.api.util.array.jnet.buffer.ByteArray,
		 *      java.lang.Object)
		 */
		@Override
		default ByteArray getByteArray(ByteArray array, Object data) {
			return proxyArrayField().getByteArray(array, data);
		}

		/**
		 * Gets the byte array.
		 *
		 * @param array     the array
		 * @param data      the data
		 * @param sequences the sequences
		 * @return the byte array
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#getByteArray(com.slytechs.jnet.platform.api.util.array.jnet.buffer.ByteArray,
		 *      java.lang.Object, long[])
		 */
		@Override
		default ByteArray getByteArray(ByteArray array, Object data, long... sequences) {
			return proxyArrayField().getByteArray(array, data, sequences);
		}

		/**
		 * Gets the byte array.
		 *
		 * @param data the data
		 * @return the byte array
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#getByteArray(java.lang.Object)
		 */
		@Override
		default byte[] getByteArray(Object data) {
			return proxyArrayField().getByteArray(data);
		}

		/**
		 * Gets the byte array.
		 *
		 * @param data      the data
		 * @param sequences the sequences
		 * @return the byte array
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#getByteArray(java.lang.Object,
		 *      long[])
		 */
		@Override
		default byte[] getByteArray(Object data, long... sequences) {
			return proxyArrayField().getByteArray(data, sequences);
		}

		/**
		 * Gets the int array.
		 *
		 * @param array  the array
		 * @param offset the offset
		 * @param length the length
		 * @param data   the data
		 * @return the int array
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#getIntArray(int[],
		 *      int, int, java.lang.Object)
		 */
		@Override
		default int[] getIntArray(int[] array, int offset, int length, Object data) {
			return proxyArrayField().getIntArray(array, offset, length, data);
		}

		/**
		 * Gets the int array.
		 *
		 * @param array     the array
		 * @param offset    the offset
		 * @param length    the length
		 * @param data      the data
		 * @param sequences the sequences
		 * @return the int array
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#getIntArray(int[],
		 *      int, int, java.lang.Object, long[])
		 */
		@Override
		default int[] getIntArray(int[] array, int offset, int length, Object data, long... sequences) {
			return proxyArrayField().getIntArray(array, offset, length, data, sequences);
		}

		/**
		 * Gets the int array.
		 *
		 * @param array the array
		 * @param data  the data
		 * @return the int array
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#getIntArray(int[],
		 *      java.lang.Object)
		 */
		@Override
		default int[] getIntArray(int[] array, Object data) {
			return proxyArrayField().getIntArray(array, data);
		}

		/**
		 * Gets the int array.
		 *
		 * @param array     the array
		 * @param data      the data
		 * @param sequences the sequences
		 * @return the int array
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#getIntArray(int[],
		 *      java.lang.Object, long[])
		 */
		@Override
		default int[] getIntArray(int[] array, Object data, long... sequences) {
			return proxyArrayField().getIntArray(array, data, sequences);
		}

		/**
		 * Gets the int array.
		 *
		 * @param data the data
		 * @return the int array
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#getIntArray(java.lang.Object)
		 */
		@Override
		default int[] getIntArray(Object data) {
			return proxyArrayField().getIntArray(data);
		}

		/**
		 * Gets the int array.
		 *
		 * @param data      the data
		 * @param sequences the sequences
		 * @return the int array
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#getIntArray(java.lang.Object,
		 *      long[])
		 */
		@Override
		default int[] getIntArray(Object data, long... sequences) {
			return proxyArrayField().getIntArray(data, sequences);
		}

		/**
		 * Gets the long array.
		 *
		 * @param array  the array
		 * @param offset the offset
		 * @param length the length
		 * @param data   the data
		 * @return the long array
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#getLongArray(long[],
		 *      int, int, java.lang.Object)
		 */
		@Override
		default long[] getLongArray(long[] array, int offset, int length, Object data) {
			return proxyArrayField().getLongArray(array, offset, length, data);
		}

		/**
		 * Gets the long array.
		 *
		 * @param array     the array
		 * @param offset    the offset
		 * @param length    the length
		 * @param data      the data
		 * @param sequences the sequences
		 * @return the long array
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#getLongArray(long[],
		 *      int, int, java.lang.Object, long[])
		 */
		@Override
		default long[] getLongArray(long[] array, int offset, int length, Object data, long... sequences) {
			return proxyArrayField().getLongArray(array, offset, length, data, sequences);
		}

		/**
		 * Gets the long array.
		 *
		 * @param array the array
		 * @param data  the data
		 * @return the long array
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#getLongArray(long[],
		 *      java.lang.Object)
		 */
		@Override
		default long[] getLongArray(long[] array, Object data) {
			return proxyArrayField().getLongArray(array, data);
		}

		/**
		 * Gets the long array.
		 *
		 * @param array     the array
		 * @param data      the data
		 * @param sequences the sequences
		 * @return the long array
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#getLongArray(long[],
		 *      java.lang.Object, long[])
		 */
		@Override
		default long[] getLongArray(long[] array, Object data, long... sequences) {
			return proxyArrayField().getLongArray(array, data, sequences);
		}

		/**
		 * Gets the long array.
		 *
		 * @param data the data
		 * @return the long array
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#getLongArray(java.lang.Object)
		 */
		@Override
		default long[] getLongArray(Object data) {
			return proxyArrayField().getLongArray(data);
		}

		/**
		 * Gets the long array.
		 *
		 * @param data      the data
		 * @param sequences the sequences
		 * @return the long array
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#getLongArray(java.lang.Object,
		 *      long[])
		 */
		@Override
		default long[] getLongArray(Object data, long... sequences) {
			return proxyArrayField().getLongArray(data, sequences);
		}

		/**
		 * Gets the short array.
		 *
		 * @param data the data
		 * @return the short array
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#getShortArray(java.lang.Object)
		 */
		@Override
		default short[] getShortArray(Object data) {
			return proxyArrayField().getShortArray(data);
		}

		/**
		 * Gets the short array.
		 *
		 * @param data      the data
		 * @param sequences the sequences
		 * @return the short array
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#getShortArray(java.lang.Object,
		 *      long[])
		 */
		@Override
		default short[] getShortArray(Object data, long... sequences) {
			return proxyArrayField().getShortArray(data, sequences);
		}

		/**
		 * Gets the short array.
		 *
		 * @param array  the array
		 * @param offset the offset
		 * @param length the length
		 * @param data   the data
		 * @return the short array
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#getShortArray(short[],
		 *      int, int, java.lang.Object)
		 */
		@Override
		default short[] getShortArray(short[] array, int offset, int length, Object data) {
			return proxyArrayField().getShortArray(array, offset, length, data);
		}

		/**
		 * Gets the short array.
		 *
		 * @param array     the array
		 * @param offset    the offset
		 * @param length    the length
		 * @param data      the data
		 * @param sequences the sequences
		 * @return the short array
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#getShortArray(short[],
		 *      int, int, java.lang.Object, long[])
		 */
		@Override
		default short[] getShortArray(short[] array, int offset, int length, Object data, long... sequences) {
			return proxyArrayField().getShortArray(array, offset, length, data, sequences);
		}

		/**
		 * Gets the short array.
		 *
		 * @param array the array
		 * @param data  the data
		 * @return the short array
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#getShortArray(short[],
		 *      java.lang.Object)
		 */
		@Override
		default short[] getShortArray(short[] array, Object data) {
			return proxyArrayField().getShortArray(array, data);
		}

		/**
		 * Gets the short array.
		 *
		 * @param array     the array
		 * @param data      the data
		 * @param sequences the sequences
		 * @return the short array
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#getShortArray(short[],
		 *      java.lang.Object, long[])
		 */
		@Override
		default short[] getShortArray(short[] array, Object data, long... sequences) {
			return proxyArrayField().getShortArray(array, data, sequences);
		}

		/**
		 * Layout.
		 *
		 * @return the binary layout
		 * @see com.slytechs.jnet.platform.api.memory.layout.BinaryField#layout()
		 */
		@Override
		default BinaryLayout layout() {
			return proxyArrayField().layout();
		}

		/**
		 * Proxy array field.
		 *
		 * @return the array field
		 */
		ArrayField proxyArrayField();

		/**
		 * Sets the byte array.
		 *
		 * @param array  the array
		 * @param offset the offset
		 * @param length the length
		 * @param data   the data
		 * @return the byte[]
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#setByteArray(byte[],
		 *      int, int, java.lang.Object)
		 */
		@Override
		default byte[] setByteArray(byte[] array, int offset, int length, Object data) {
			return proxyArrayField().setByteArray(array, offset, length, data);
		}

		/**
		 * Sets the byte array.
		 *
		 * @param array     the array
		 * @param offset    the offset
		 * @param length    the length
		 * @param data      the data
		 * @param sequences the sequences
		 * @return the byte[]
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#setByteArray(byte[],
		 *      int, int, java.lang.Object, long[])
		 */
		@Override
		default byte[] setByteArray(byte[] array, int offset, int length, Object data, long... sequences) {
			return proxyArrayField().setByteArray(array, offset, length, data, sequences);
		}

		/**
		 * Sets the byte array.
		 *
		 * @param array the array
		 * @param data  the data
		 * @return the byte[]
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#setByteArray(byte[],
		 *      java.lang.Object)
		 */
		@Override
		default byte[] setByteArray(byte[] array, Object data) {
			return proxyArrayField().setByteArray(array, data);
		}

		/**
		 * Sets the byte array.
		 *
		 * @param array     the array
		 * @param data      the data
		 * @param sequences the sequences
		 * @return the byte[]
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#setByteArray(byte[],
		 *      java.lang.Object, long[])
		 */
		@Override
		default byte[] setByteArray(byte[] array, Object data, long... sequences) {
			return proxyArrayField().setByteArray(array, data, sequences);
		}

		/**
		 * Sets the byte array.
		 *
		 * @param array the array
		 * @param data  the data
		 * @return the byte array
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#setByteArray(com.slytechs.jnet.platform.api.util.array.jnet.buffer.ByteArray,
		 *      java.lang.Object)
		 */
		@Override
		default ByteArray setByteArray(ByteArray array, Object data) {
			return proxyArrayField().setByteArray(array, data);
		}

		/**
		 * Sets the byte array.
		 *
		 * @param array     the array
		 * @param data      the data
		 * @param sequences the sequences
		 * @return the byte array
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#setByteArray(com.slytechs.jnet.platform.api.util.array.jnet.buffer.ByteArray,
		 *      java.lang.Object, long[])
		 */
		@Override
		default ByteArray setByteArray(ByteArray array, Object data, long... sequences) {
			return proxyArrayField().setByteArray(array, data, sequences);
		}

		/**
		 * Sets the int array.
		 *
		 * @param array  the array
		 * @param offset the offset
		 * @param length the length
		 * @param data   the data
		 * @return the int[]
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#setIntArray(int[],
		 *      int, int, java.lang.Object)
		 */
		@Override
		default int[] setIntArray(int[] array, int offset, int length, Object data) {
			return proxyArrayField().setIntArray(array, offset, length, data);
		}

		/**
		 * Sets the int array.
		 *
		 * @param array     the array
		 * @param offset    the offset
		 * @param length    the length
		 * @param data      the data
		 * @param sequences the sequences
		 * @return the int[]
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#setIntArray(int[],
		 *      int, int, java.lang.Object, long[])
		 */
		@Override
		default int[] setIntArray(int[] array, int offset, int length, Object data, long... sequences) {
			return proxyArrayField().setIntArray(array, offset, length, data, sequences);
		}

		/**
		 * Sets the int array.
		 *
		 * @param array the array
		 * @param data  the data
		 * @return the int[]
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#setIntArray(int[],
		 *      java.lang.Object)
		 */
		@Override
		default int[] setIntArray(int[] array, Object data) {
			return proxyArrayField().setIntArray(array, data);
		}

		/**
		 * Sets the int array.
		 *
		 * @param array     the array
		 * @param data      the data
		 * @param sequences the sequences
		 * @return the int[]
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#setIntArray(int[],
		 *      java.lang.Object, long[])
		 */
		@Override
		default int[] setIntArray(int[] array, Object data, long... sequences) {
			return proxyArrayField().setIntArray(array, data, sequences);
		}

		/**
		 * Sets the long array.
		 *
		 * @param array  the array
		 * @param offset the offset
		 * @param length the length
		 * @param data   the data
		 * @return the long[]
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#setLongArray(long[],
		 *      int, int, java.lang.Object)
		 */
		@Override
		default long[] setLongArray(long[] array, int offset, int length, Object data) {
			return proxyArrayField().setLongArray(array, offset, length, data);
		}

		/**
		 * Sets the long array.
		 *
		 * @param array     the array
		 * @param offset    the offset
		 * @param length    the length
		 * @param data      the data
		 * @param sequences the sequences
		 * @return the long[]
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#setLongArray(long[],
		 *      int, int, java.lang.Object, long[])
		 */
		@Override
		default long[] setLongArray(long[] array, int offset, int length, Object data, long... sequences) {
			return proxyArrayField().setLongArray(array, offset, length, data, sequences);
		}

		/**
		 * Sets the long array.
		 *
		 * @param array the array
		 * @param data  the data
		 * @return the long[]
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#setLongArray(long[],
		 *      java.lang.Object)
		 */
		@Override
		default long[] setLongArray(long[] array, Object data) {
			return proxyArrayField().setLongArray(array, data);
		}

		/**
		 * Sets the long array.
		 *
		 * @param array     the array
		 * @param data      the data
		 * @param sequences the sequences
		 * @return the long[]
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#setLongArray(long[],
		 *      java.lang.Object, long[])
		 */
		@Override
		default long[] setLongArray(long[] array, Object data, long... sequences) {
			return proxyArrayField().setLongArray(array, data, sequences);
		}

		/**
		 * Sets the short array.
		 *
		 * @param array  the array
		 * @param offset the offset
		 * @param length the length
		 * @param data   the data
		 * @return the short[]
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#setShortArray(short[],
		 *      int, int, java.lang.Object)
		 */
		@Override
		default short[] setShortArray(short[] array, int offset, int length, Object data) {
			return proxyArrayField().setShortArray(array, offset, length, data);
		}

		/**
		 * Sets the short array.
		 *
		 * @param array     the array
		 * @param offset    the offset
		 * @param length    the length
		 * @param data      the data
		 * @param sequences the sequences
		 * @return the short[]
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#setShortArray(short[],
		 *      int, int, java.lang.Object, long[])
		 */
		@Override
		default short[] setShortArray(short[] array, int offset, int length, Object data, long... sequences) {
			return proxyArrayField().setShortArray(array, offset, length, data, sequences);
		}

		/**
		 * Sets the short array.
		 *
		 * @param array the array
		 * @param data  the data
		 * @return the short[]
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#setShortArray(short[],
		 *      java.lang.Object)
		 */
		@Override
		default short[] setShortArray(short[] array, Object data) {
			return proxyArrayField().setShortArray(array, data);
		}

		/**
		 * Sets the short array.
		 *
		 * @param array     the array
		 * @param data      the data
		 * @param sequences the sequences
		 * @return the short[]
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#setShortArray(short[],
		 *      java.lang.Object, long[])
		 */
		@Override
		default short[] setShortArray(short[] array, Object data, long... sequences) {
			return proxyArrayField().setShortArray(array, data, sequences);
		}

		/**
		 * Size.
		 *
		 * @return the optional long
		 * @see com.slytechs.jnet.platform.api.memory.layout.BinaryField#size()
		 */
		@Override
		default OptionalLong size() {
			return proxyArrayField().size();
		}

		/**
		 * To string.
		 *
		 * @param data      the data
		 * @param sequences the sequences
		 * @return the string
		 * @see com.slytechs.jnet.platform.api.memory.layout.BinaryField#toString(java.lang.Object,
		 *      long[])
		 */
		@Override
		default String toString(Object data, long... sequences) {
			return proxyArrayField().toString(data, sequences);
		}

		/**
		 * Wrap.
		 *
		 * @param data the data
		 * @return the byte array
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#wrap(byte[])
		 */
		@Override
		default ByteArray wrap(byte[] data) {
			return proxyArrayField().wrap(data);
		}

		/**
		 * Wrap.
		 *
		 * @param data      the data
		 * @param sequences the sequences
		 * @return the byte array
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#wrap(byte[],
		 *      long[])
		 */
		@Override
		default ByteArray wrap(byte[] data, long... sequences) {
			return proxyArrayField().wrap(data, sequences);
		}

		/**
		 * Wrap.
		 *
		 * @param data the data
		 * @return the byte array
		 */
		@Override
		default ByteArray wrap(ByteArray data) {
			return proxyArrayField().wrap(data);
		}

		/**
		 * Wrap.
		 *
		 * @param data      the data
		 * @param sequences the sequences
		 * @return the byte array
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField#wrap(com.slytechs.jnet.platform.api.util.array.jnet.buffer.ByteArray,
		 *      long[])
		 */
		@Override
		default ByteArray wrap(ByteArray data, long... sequences) {
			return proxyArrayField().wrap(data, sequences);
		}
	}

	/**
	 * The Interface ArrayFieldFormatter.
	 *
	 * @author Mark Bednarczyk
	 */
	public interface ArrayFieldFormatter {

		/**
		 * Of.
		 *
		 * @param formatString the format string
		 * @return the array field formatter
		 */
		static ArrayFieldFormatter of(String formatString) {
			return (f, d, seq) -> String.format(formatString, f.getByteArray(d, seq), f.fieldName().orElse("field"), d,
					seq);
		}

		/**
		 * Format.
		 *
		 * @param field     the field
		 * @param data      the data
		 * @param sequences the sequences
		 * @return the string
		 */
		String format(ArrayField field, Object data, long... sequences);
	}

	/**
	 * The Class FormattedArrayField.
	 *
	 * @author Mark Bednarczyk
	 */
	class FormattedArrayField implements ArrayField.Proxy {

		/** The proxy. */
		private final ArrayField proxy;

		/** The formatter. */
		private ArrayFieldFormatter formatter;

		/**
		 * Instantiates a new formatted array field.
		 *
		 * @param format the format
		 * @param proxy  the proxy
		 */
		public FormattedArrayField(String format, ArrayField proxy) {
			this(ArrayFieldFormatter.of(format), proxy);
		}

		/**
		 * Instantiates a new formatted array field.
		 *
		 * @param formatter the formatter
		 * @param proxy     the proxy
		 */
		public FormattedArrayField(ArrayFieldFormatter formatter, ArrayField proxy) {
			this.proxy = proxy;
		}

		/**
		 * Proxy array field.
		 *
		 * @return the array field
		 * @see com.slytechs.jnet.platform.api.memory.layout.BitField.Proxy#proxyBitField()
		 */
		@Override
		public ArrayField proxyArrayField() {
			return proxy;
		}

		/**
		 * To string.
		 *
		 * @param data      the data
		 * @param sequences the sequences
		 * @return the string
		 * @see com.slytechs.jnet.platform.api.memory.layout.ArrayField.Proxy#toString(java.lang.Object,
		 *      long[])
		 */
		@Override
		public String toString(Object data, long... sequences) {
			return formatter.format(proxy, data, sequences);
		}

	}

	/**
	 * Gets the byte array.
	 *
	 * @param array  the array
	 * @param offset the offset
	 * @param length the length
	 * @param data   the data
	 * @return the byte array
	 */
	// @formatter:off
    byte[]  getByteArray(byte[] array, int offset, int length, Object data);
    
    /**
	 * Gets the byte array at.
	 *
	 * @param byteOffset the byte offset
	 * @param array      the array
	 * @param offset     the offset
	 * @param length     the length
	 * @param data       the data
	 * @return the byte array at
	 */
    byte[]  getByteArrayAt(long byteOffset, byte[] array, int offset, int length, Object data);
	        
        	/**
			 * Gets the byte array.
			 *
			 * @param array     the array
			 * @param offset    the offset
			 * @param length    the length
			 * @param data      the data
			 * @param sequences the sequences
			 * @return the byte array
			 */
        	byte[]  getByteArray(byte[] array, int offset, int length, Object data, long... sequences);
	
	/**
	 * Gets the byte array.
	 *
	 * @param array the array
	 * @param data  the data
	 * @return the byte array
	 */
    default byte[]  getByteArray(byte[] array, Object data)                         { return getByteArray(array, 0, array.length, data); }
    
    /**
	 * Gets the byte array at.
	 *
	 * @param byteOffset the byte offset
	 * @param array      the array
	 * @param data       the data
	 * @return the byte array at
	 */
    default byte[]  getByteArrayAt(long byteOffset, byte[] array, Object data)      { return getByteArrayAt(byteOffset, array, 0, array.length, data); }
	
	/**
	 * Gets the byte array.
	 *
	 * @param array     the array
	 * @param data      the data
	 * @param sequences the sequences
	 * @return the byte array
	 */
	default byte[]  getByteArray(byte[] array, Object data, long... sequences)      { return getByteArray(array, 0, array.length, data, sequences); }
	
	/**
	 * Gets the byte array.
	 *
	 * @param array the array
	 * @param data  the data
	 * @return the byte array
	 */
	default ByteArray getByteArray(ByteArray array, Object data)                    { getByteArray(array.array(), array.arrayOffset(), array.arrayLength(), data); return array; } 
	
	/**
	 * Gets the byte array.
	 *
	 * @param array     the array
	 * @param data      the data
	 * @param sequences the sequences
	 * @return the byte array
	 */
	default ByteArray getByteArray(ByteArray array, Object data, long... sequences) { getByteArray(array.array(), array.arrayOffset(), array.arrayLength(), data, sequences); return array; } 

	/**
	 * Gets the byte array.
	 *
	 * @param data the data
	 * @return the byte array
	 */
	default byte[]  getByteArray(Object data)                                       { return getByteArray(new byte[(int) byteSize()], data); }
	
	/**
	 * Gets the byte array at.
	 *
	 * @param byteOffset the byte offset
	 * @param data       the data
	 * @return the byte array at
	 */
	default byte[]  getByteArrayAt(long byteOffset, Object data)                    { return getByteArrayAt(byteOffset, new byte[(int) byteSize()], data); }
	
	/**
	 * Gets the byte array.
	 *
	 * @param data      the data
	 * @param sequences the sequences
	 * @return the byte array
	 */
	default byte[]  getByteArray(Object data, long... sequences)                    { return getByteArray(new byte[(int) byteSize()], data, sequences); }
	
	        /**
			 * Gets the int array.
			 *
			 * @param array  the array
			 * @param offset the offset
			 * @param length the length
			 * @param data   the data
			 * @return the int array
			 */
        	int[]   getIntArray(int[] array, int offset, int length, Object data);
	        
        	/**
			 * Gets the int array.
			 *
			 * @param array     the array
			 * @param offset    the offset
			 * @param length    the length
			 * @param data      the data
			 * @param sequences the sequences
			 * @return the int array
			 */
        	int[]   getIntArray(int[] array, int offset, int length, Object data, long... sequences);
	
	/**
	 * Gets the int array.
	 *
	 * @param array the array
	 * @param data  the data
	 * @return the int array
	 */
	default int[]   getIntArray(int[] array, Object data)                           { return getIntArray(array, 0, array.length, data); }
	
	/**
	 * Gets the int array.
	 *
	 * @param array     the array
	 * @param data      the data
	 * @param sequences the sequences
	 * @return the int array
	 */
	default int[]   getIntArray(int[] array, Object data, long... sequences)        { return getIntArray(array, 0, array.length, data, sequences); }

	/**
	 * Gets the int array.
	 *
	 * @param data the data
	 * @return the int array
	 */
	default int[]   getIntArray(Object data)                                        { return getIntArray(new int[(int) byteSize()], data); }
	
	/**
	 * Gets the int array.
	 *
	 * @param data      the data
	 * @param sequences the sequences
	 * @return the int array
	 */
	default int[]   getIntArray(Object data, long... sequences)                     { return getIntArray(new int[(int) byteSize()], data, sequences); }
	
	        /**
			 * Gets the long array.
			 *
			 * @param array  the array
			 * @param offset the offset
			 * @param length the length
			 * @param data   the data
			 * @return the long array
			 */
        	long[]  getLongArray(long[] array, int offset, int length, Object data);
	        
        	/**
			 * Gets the long array.
			 *
			 * @param array     the array
			 * @param offset    the offset
			 * @param length    the length
			 * @param data      the data
			 * @param sequences the sequences
			 * @return the long array
			 */
        	long[]  getLongArray(long[] array, int offset, int length, Object data, long... sequences);
	
	/**
	 * Gets the long array.
	 *
	 * @param array the array
	 * @param data  the data
	 * @return the long array
	 */
	default long[]  getLongArray(long[] array, Object data)                         { return getLongArray(array, 0, array.length, data); }
	
	/**
	 * Gets the long array.
	 *
	 * @param array     the array
	 * @param data      the data
	 * @param sequences the sequences
	 * @return the long array
	 */
	default long[]  getLongArray(long[] array, Object data, long... sequences)      { return getLongArray(array, 0, array.length, data, sequences); }

	/**
	 * Gets the long array.
	 *
	 * @param data the data
	 * @return the long array
	 */
	default long[]  getLongArray(Object data)                                       { return getLongArray(new long[(int) byteSize()], data); }
	
	/**
	 * Gets the long array.
	 *
	 * @param data      the data
	 * @param sequences the sequences
	 * @return the long array
	 */
	default long[]  getLongArray(Object data, long... sequences)                    { return getLongArray(new long[(int) byteSize()], data, sequences); }
	
	/**
	 * Gets the short array.
	 *
	 * @param data the data
	 * @return the short array
	 */
	default short[] getShortArray(Object data)                                      { return getShortArray(new short[(int) byteSize()], data); }
	
	/**
	 * Gets the short array.
	 *
	 * @param data      the data
	 * @param sequences the sequences
	 * @return the short array
	 */
	default short[] getShortArray(Object data, long... sequences)                   { return getShortArray(new short[(int) byteSize()], data, sequences); }
	        
        	/**
			 * Gets the short array.
			 *
			 * @param array  the array
			 * @param offset the offset
			 * @param length the length
			 * @param data   the data
			 * @return the short array
			 */
        	short[] getShortArray(short[] array, int offset, int length, Object data);
	        
        	/**
			 * Gets the short array.
			 *
			 * @param array     the array
			 * @param offset    the offset
			 * @param length    the length
			 * @param data      the data
			 * @param sequences the sequences
			 * @return the short array
			 */
        	short[] getShortArray(short[] array, int offset, int length, Object data, long... sequences);

	/**
	 * Gets the short array.
	 *
	 * @param array the array
	 * @param data  the data
	 * @return the short array
	 */
	default short[] getShortArray(short[] array, Object data)                       { return getShortArray(new short[(int) byteSize()], data); }
	
	/**
	 * Gets the short array.
	 *
	 * @param array     the array
	 * @param data      the data
	 * @param sequences the sequences
	 * @return the short array
	 */
	default short[] getShortArray(short[] array, Object data, long... sequences)    { return getShortArray(new short[(int) byteSize()], data, sequences); }
	
	        /**
			 * Sets the byte array.
			 *
			 * @param array  the array
			 * @param offset the offset
			 * @param length the length
			 * @param data   the data
			 * @return the byte[]
			 */
        	byte[]  setByteArray(byte[] array, int offset, int length, Object data);
	        
        	/**
			 * Sets the byte array.
			 *
			 * @param array     the array
			 * @param offset    the offset
			 * @param length    the length
			 * @param data      the data
			 * @param sequences the sequences
			 * @return the byte[]
			 */
        	byte[]  setByteArray(byte[] array, int offset, int length, Object data, long... sequences);
	
	/**
	 * Sets the byte array.
	 *
	 * @param array the array
	 * @param data  the data
	 * @return the byte[]
	 */
	default byte[]  setByteArray(byte[] array, Object data)                         { return setByteArray(array, 0, array.length, data); }
	
	/**
	 * Sets the byte array.
	 *
	 * @param array     the array
	 * @param data      the data
	 * @param sequences the sequences
	 * @return the byte[]
	 */
	default byte[]  setByteArray(byte[] array, Object data, long... sequences)      { return setByteArray(array, 0, array.length, data, sequences); }
	
	/**
	 * Sets the byte array.
	 *
	 * @param array the array
	 * @param data  the data
	 * @return the byte array
	 */
	default ByteArray setByteArray(ByteArray array, Object data)                    { setByteArray(array.array(), array.arrayOffset(), array.arrayLength(), data); return array; }
	
	/**
	 * Sets the byte array.
	 *
	 * @param array     the array
	 * @param data      the data
	 * @param sequences the sequences
	 * @return the byte array
	 */
	default ByteArray setByteArray(ByteArray array, Object data, long... sequences) { setByteArray(array.array(), array.arrayOffset(), array.arrayLength(), data, sequences); return array; }
	
            /**
			 * Sets the int array.
			 *
			 * @param array  the array
			 * @param offset the offset
			 * @param length the length
			 * @param data   the data
			 * @return the int[]
			 */
            int[]   setIntArray(int[] array, int offset, int length, Object data);
            
            /**
			 * Sets the int array.
			 *
			 * @param array     the array
			 * @param offset    the offset
			 * @param length    the length
			 * @param data      the data
			 * @param sequences the sequences
			 * @return the int[]
			 */
            int[]   setIntArray(int[] array, int offset, int length, Object data, long... sequences);
	
	/**
	 * Sets the int array.
	 *
	 * @param array the array
	 * @param data  the data
	 * @return the int[]
	 */
	default int[]   setIntArray(int[] array, Object data)                           { return setIntArray(array, 0, array.length, data); }
	
	/**
	 * Sets the int array.
	 *
	 * @param array     the array
	 * @param data      the data
	 * @param sequences the sequences
	 * @return the int[]
	 */
	default int[]   setIntArray(int[] array, Object data, long... sequences)        { return setIntArray(array, 0, array.length, data, sequences); }
	
            /**
			 * Sets the long array.
			 *
			 * @param array  the array
			 * @param offset the offset
			 * @param length the length
			 * @param data   the data
			 * @return the long[]
			 */
            long[]  setLongArray(long[] array, int offset, int length, Object data);
	        
        	/**
			 * Sets the long array.
			 *
			 * @param array     the array
			 * @param offset    the offset
			 * @param length    the length
			 * @param data      the data
			 * @param sequences the sequences
			 * @return the long[]
			 */
        	long[]  setLongArray(long[] array, int offset, int length, Object data, long... sequences);
	
	/**
	 * Sets the long array.
	 *
	 * @param array the array
	 * @param data  the data
	 * @return the long[]
	 */
	default long[]  setLongArray(long[] array, Object data)                         { return setLongArray(array, 0, array.length, data); }
	
	/**
	 * Sets the long array.
	 *
	 * @param array     the array
	 * @param data      the data
	 * @param sequences the sequences
	 * @return the long[]
	 */
	default long[]  setLongArray(long[] array, Object data, long... sequences)      { return setLongArray(array, 0, array.length, data, sequences); }
	
	        /**
			 * Sets the short array.
			 *
			 * @param array  the array
			 * @param offset the offset
			 * @param length the length
			 * @param data   the data
			 * @return the short[]
			 */
        	short[] setShortArray(short[] array, int offset, int length, Object data);
	        
        	/**
			 * Sets the short array.
			 *
			 * @param array     the array
			 * @param offset    the offset
			 * @param length    the length
			 * @param data      the data
			 * @param sequences the sequences
			 * @return the short[]
			 */
        	short[] setShortArray(short[] array, int offset, int length, Object data, long... sequences);
	
	/**
	 * Sets the short array.
	 *
	 * @param array the array
	 * @param data  the data
	 * @return the short[]
	 */
	default short[] setShortArray(short[] array, Object data)                       { return setShortArray(array, 0, array.length, data); }
	
	/**
	 * Sets the short array.
	 *
	 * @param array     the array
	 * @param data      the data
	 * @param sequences the sequences
	 * @return the short[]
	 */
	default short[] setShortArray(short[] array, Object data, long... sequences)    { return setShortArray(array, 0, array.length, data, sequences); }

	/**
	 * Wrap.
	 *
	 * @param data the data
	 * @return the byte array
	 */
	ByteArray wrap(byte[] data);
	
	/**
	 * Wrap.
	 *
	 * @param data      the data
	 * @param sequences the sequences
	 * @return the byte array
	 */
	ByteArray wrap(byte[] data, long...sequences);

	/**
	 * Wrap.
	 *
	 * @param data the data
	 * @return the byte array
	 */
	ByteArray wrap(ByteArray data);
	
	/**
	 * Wrap.
	 *
	 * @param data      the data
	 * @param sequences the sequences
	 * @return the byte array
	 */
	ByteArray wrap(ByteArray data, long...sequences);

	// @formatter:on

	/**
	 * Formatted.
	 *
	 * @param format the format
	 * @return the array field
	 */
	default ArrayField formatted(String format) {
		return new FormattedArrayField(format, this);
	}
}
