/*
 * Sly Technologies Free License
 * 
 * Copyright 2025 Sly Technologies Inc.
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
package com.slytechs.jnet.platform.api.util.function;

/**
 * A specialized interface for tuple types that represent a fixed-size
 * collection of byte values, commonly used for network addresses (MAC, IPv4,
 * IPv6) and other networking-related values.
 * 
 * <p>
 * The record implementations provide standard {@code equals()},
 * {@code hashCode()} and {@code toString()} behavior where:
 * <ul>
 * <li>equals() performs value-based comparison of all components
 * <li>hashCode() combines component hash codes consistently with equals()
 * <li>toString() formats bytes as colon-separated hex values
 * </ul>
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * // Create a MAC address tuple
 * ByteTuple6 mac = ByteTuple6.of((byte) 0x00, (byte) 0x1A, (byte) 0x2B,
 * 		(byte) 0x3C, (byte) 0x4D, (byte) 0x5E);
 * 
 * // Get hex string representation
 * String macStr = mac.toHexString(); // "00:1a:2b:3c:4d:5e"
 * 
 * // Parse from hex string
 * ByteTuple6 parsed = ByteTuple6.fromHexString("00:1a:2b:3c:4d:5e");
 * }</pre>
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc
 */
public interface ByteTuple {

	// Example implementations for a few key sizes - add others similarly
	interface ByteTuple1 extends ByteTuple {

		static ByteTuple1 fromHexString(String hexString) {
			byte[] bytes = ByteTuple.parseHexString(hexString);
			if (bytes.length != 1) {
				throw new IllegalArgumentException("Expected 1 byte, got " + bytes.length);
			}
			return of(bytes[0]);
		}

		static ByteTuple1 of(byte value1) {
			return new Tuple1b(value1);
		}
	}

	interface ByteTuple16 extends ByteTuple {

		static ByteTuple16 fromHexString(String hexString) {
			byte[] bytes = ByteTuple.parseHexString(hexString);
			if (bytes.length != 16) {
				throw new IllegalArgumentException("Expected 16 bytes, got " + bytes.length);
			}
			return of(bytes[0], bytes[1], bytes[2], bytes[3],
					bytes[4], bytes[5], bytes[6], bytes[7],
					bytes[8], bytes[9], bytes[10], bytes[11],
					bytes[12], bytes[13], bytes[14], bytes[15]);
		}

		static ByteTuple16 of(
				byte value1, byte value2, byte value3, byte value4,
				byte value5, byte value6, byte value7, byte value8,
				byte value9, byte value10, byte value11, byte value12,
				byte value13, byte value14, byte value15, byte value16) {
			return new Tuple16b(
					value1, value2, value3, value4, value5, value6, value7, value8,
					value9, value10, value11, value12, value13, value14, value15, value16);
		}
	}

	interface ByteTuple2 extends ByteTuple {

		static ByteTuple2 fromHexString(String hexString) {
			byte[] bytes = ByteTuple.parseHexString(hexString);
			if (bytes.length != 2) {
				throw new IllegalArgumentException("Expected 2 bytes, got " + bytes.length);
			}
			return of(bytes[0], bytes[1]);
		}

		static ByteTuple2 of(byte value1, byte value2) {
			return new Tuple2b(value1, value2);
		}
	}

	interface ByteTuple3 extends ByteTuple {

		static ByteTuple3 fromHexString(String hexString) {
			byte[] bytes = ByteTuple.parseHexString(hexString);
			if (bytes.length != 3) {
				throw new IllegalArgumentException("Expected 3 bytes, got " + bytes.length);
			}
			return of(bytes[0], bytes[1], bytes[2]);
		}

		static ByteTuple3 of(byte value1, byte value2, byte value3) {
			return new Tuple3b(value1, value2, value3);
		}
	}

	interface ByteTuple4 extends ByteTuple {

		static ByteTuple4 fromHexString(String hexString) {
			byte[] bytes = ByteTuple.parseHexString(hexString);
			if (bytes.length != 4) {
				throw new IllegalArgumentException("Expected 4 bytes, got " + bytes.length);
			}
			return of(bytes[0], bytes[1], bytes[2], bytes[3]);
		}

		static ByteTuple4 of(byte value1, byte value2, byte value3, byte value4) {
			return new Tuple4b(value1, value2, value3, value4);
		}
	}

	interface ByteTuple5 extends ByteTuple {

		static ByteTuple5 fromHexString(String hexString) {
			byte[] bytes = ByteTuple.parseHexString(hexString);
			if (bytes.length != 5) {
				throw new IllegalArgumentException("Expected 5 bytes, got " + bytes.length);
			}
			return of(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4]);
		}

		static ByteTuple5 of(byte value1, byte value2, byte value3, byte value4, byte value5) {
			return new Tuple5b(value1, value2, value3, value4, value5);
		}
	}

	// Example of a common network address size (MAC address)
	interface ByteTuple6 extends ByteTuple {

		static ByteTuple6 fromHexString(String hexString) {
			byte[] bytes = ByteTuple.parseHexString(hexString);
			if (bytes.length != 6) {
				throw new IllegalArgumentException("Expected 6 bytes, got " + bytes.length);
			}
			return of(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5]);
		}

		static ByteTuple6 of(
				byte value1, byte value2, byte value3,
				byte value4, byte value5, byte value6) {
			return new Tuple6b(
					value1, value2, value3, value4, value5, value6);
		}
	}

	public record Tuple3b(byte value1, byte value2, byte value3) implements ByteTuple3 {
		@Override
		public byte[] values() {
			return new byte[] {
					value1,
					value2,
					value3
			};
		}

		@Override
		public String toHexString() {
			return ByteTuple.toHexString(values());
		}

		@Override
		public int size() {
			return 3;
		}
	}

	public record Tuple5b(byte value1, byte value2, byte value3, byte value4, byte value5)
			implements ByteTuple5 {
		@Override
		public byte[] values() {
			return new byte[] {
					value1,
					value2,
					value3,
					value4,
					value5
			};
		}

		@Override
		public String toHexString() {
			return ByteTuple.toHexString(values());
		}

		@Override
		public int size() {
			return 5;
		}
	}

	public record Tuple6b(
			byte value1, byte value2, byte value3,
			byte value4, byte value5, byte value6) implements ByteTuple6 {

		@Override
		public byte[] values() {
			return new byte[] {
					value1,
					value2,
					value3,
					value4,
					value5,
					value6
			};
		}

		@Override
		public String toHexString() {
			return ByteTuple.toHexString(values());
		}

		@Override
		public int size() {
			return 6;
		}
	}

	public record Tuple1b(byte value1) implements ByteTuple1 {
		@Override
		public byte[] values() {
			return new byte[] {
					value1
			};
		}

		@Override
		public String toHexString() {
			return ByteTuple.toHexString(values());
		}

		@Override
		public int size() {
			return 1;
		}
	}

	public record Tuple16b(
			byte value1, byte value2, byte value3, byte value4,
			byte value5, byte value6, byte value7, byte value8,
			byte value9, byte value10, byte value11, byte value12,
			byte value13, byte value14, byte value15, byte value16) implements ByteTuple16 {

		@Override
		public byte[] values() {
			return new byte[] {
					value1,
					value2,
					value3,
					value4,
					value5,
					value6,
					value7,
					value8,
					value9,
					value10,
					value11,
					value12,
					value13,
					value14,
					value15,
					value16
			};
		}

		@Override
		public String toHexString() {
			return ByteTuple.toHexString(values());
		}

		@Override
		public int size() {
			return 16;
		}
	}

	public record Tuple2b(byte value1, byte value2) implements ByteTuple2 {
		@Override
		public byte[] values() {
			return new byte[] {
					value1,
					value2
			};
		}

		@Override
		public String toHexString() {
			return ByteTuple.toHexString(values());
		}

		@Override
		public int size() {
			return 2;
		}
	}

	public record Tuple4b(
			byte value1, byte value2,
			byte value3, byte value4) implements ByteTuple4 {

		@Override
		public byte[] values() {
			return new byte[] {
					value1,
					value2,
					value3,
					value4
			};
		}

		@Override
		public String toHexString() {
			return ByteTuple.toHexString(values());
		}

		@Override
		public int size() {
			return 4;
		}
	}

	/**
	 * Parses a colon-separated hex string into a byte array.
	 *
	 * @param hexString the hex string to parse
	 * @return the parsed byte array
	 * @throws IllegalArgumentException if the string is not properly formatted
	 */
	static byte[] parseHexString(String hexString) {
		if (hexString == null || hexString.isEmpty()) {
			return new byte[0];
		}

		String[] parts = hexString.split(":");
		byte[] bytes = new byte[parts.length];

		for (int i = 0; i < parts.length; i++) {
			try {
				bytes[i] = (byte) Integer.parseInt(parts[i], 16);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(
						"Invalid hex value at position " + i + ": " + parts[i]);
			}
		}

		return bytes;
	}

	/**
	 * Converts a sequence of bytes to a colon-separated hex string.
	 *
	 * @param bytes the byte array to convert
	 * @return a colon-separated hex string representation
	 */
	static String toHexString(byte[] bytes) {
		if (bytes == null || bytes.length == 0) {
			return "";
		}

		StringBuilder sb = new StringBuilder(bytes.length * 3 - 1);
		for (int i = 0; i < bytes.length; i++) {
			if (i > 0) {
				sb.append(':');
			}
			if (bytes[i] < 16)
				sb.append('0');

			sb.append(Integer.toHexString(Byte.toUnsignedInt(bytes[i])));
		}
		return sb.toString();
	}

	/**
	 * Creates an appropriate ByteTuple instance based on the length of the input
	 * array.
	 *
	 * @param bytes the byte array to create a tuple from
	 * @return a ByteTuple instance of the appropriate size
	 * @throws IllegalArgumentException if the array length doesn't match any
	 *                                  supported tuple size
	 */
	static ByteTuple valueOf(byte[] bytes) {
		if (bytes == null) {
			throw new IllegalArgumentException("Byte array cannot be null");
		}

		return switch (bytes.length) {
		case 1 -> ByteTuple1.of(bytes[0]);
		case 2 -> ByteTuple2.of(bytes[0], bytes[1]);
		case 3 -> ByteTuple3.of(bytes[0], bytes[1], bytes[2]); // Added
		case 4 -> ByteTuple4.of(bytes[0], bytes[1], bytes[2], bytes[3]);
		case 5 -> ByteTuple5.of(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4]); // Added
		case 6 -> ByteTuple6.of(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5]);
		case 16 -> ByteTuple16.of(bytes[0], bytes[1], bytes[2], bytes[3],
				bytes[4], bytes[5], bytes[6], bytes[7],
				bytes[8], bytes[9], bytes[10], bytes[11],
				bytes[12], bytes[13], bytes[14], bytes[15]);
		default -> throw new IllegalArgumentException(
				"Unsupported byte array length: " + bytes.length);
		};
	}

	/**
	 * Returns the number of elements in this tuple.
	 *
	 * @return the number of elements in this tuple
	 */
	int size();

	/**
	 * Returns a colon-separated hexadecimal string representation of this tuple.
	 *
	 * @return the hex string representation
	 */
	String toHexString();

	// Example IPv4 + Port (6 bytes) could use ByteTuple6
	// Example MAC Address (6 bytes) could use ByteTuple6
	// Example IPv6 Address (16 bytes) could use ByteTuple16
	// Example IPv6 + Port (18 bytes) would use ByteTuple18
	/**
	 * Returns all values of this tuple as a byte array.
	 *
	 * @return an array containing all byte values in this tuple
	 */
	byte[] values();

}