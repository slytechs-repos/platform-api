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
package com.slytechs.jnet.platform.api.internal.layout;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.IntFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.slytechs.jnet.platform.api.util.HasMemoryPointer;
import com.slytechs.jnet.platform.api.util.HexStrings;

/**
 * @author Mark Bednarczyk
 */
public class DebugLayout {

	private static final Pattern SEQUENCE_PATTERN = Pattern.compile("\\[(\\d*)\\]");

	private static PathElement[] parsePath(String... path) {
		return Arrays.stream(path)
				.flatMap(e -> Arrays.stream(e.split("\\.")))
				.map(DebugLayout::parsePathElement)
				.toArray(PathElement[]::new);
	}

	private static PathElement parsePathElement(String element) {
		var matcher = SEQUENCE_PATTERN.matcher(element);
		if (matcher.find()) {
			if (matcher.groupCount() == 1)
				return PathElement.sequenceElement(Long.parseLong(matcher.group(1)));

			return PathElement.sequenceElement();
		}

		return PathElement.groupElement(element);
	}

	private final MemoryLayout layout;
	private final MemorySegment pointer;

	public DebugLayout(MemoryLayout layout) {
		this.layout = layout;
		this.pointer = null;
	}

	public DebugLayout(MemoryLayout layout, MemorySegment pointer) {
		this.layout = layout;
		this.pointer = pointer;
	}

	public DebugLayout bindTo(MemorySegment memory) {
		return new DebugLayout(layout, memory);
	}

	public DebugLayout bindTo(HasMemoryPointer hasPointer) {
		return new DebugLayout(layout, hasPointer.pointer());
	}

	public int getInt(long offset, String... path) {
		long off = offset(path) + offset;

		return pointer().get(ValueLayout.JAVA_INT, off);
	}

	public int getInt(String... path) {
		return getInt(0, path);
	}

	public long getLong(long offset, String... path) {
		long off = offset(path) + offset;

		return pointer().get(ValueLayout.JAVA_LONG, off);
	}

	public long getLong(String... path) {
		return getLong(0, path);
	}

	public String getString(long offset, String... path) {
		return getString(offset, StandardCharsets.UTF_8, path);
	}

	public String getString(long offset, Charset charset, String... path) {
		long off = offset(path) + offset;

		return pointer().getString(off, charset);
	}

	public String getString(String... path) {
		return getString(0, path);
	}

	public MemoryLayout layout() {
		return layout;
	}

	public long offset(String... path) {

		PathElement[] comp = parsePath(path);

		return layout.byteOffset(comp);
	}

	public MemorySegment pointer() {
		if (pointer == null)
			throw new NullPointerException("Layout not bound to MemorySegment");

		return pointer;
	}

	public long printOffset(String... path) {
		PathElement[] elements = parsePath(path);
		long offset = layout.byteOffset(elements);
		String dotPath = toStringWithOffsets(path);

		System.out.printf("%s.%s = offset(%d bytes)%n",
				layout.name().orElse("<no-name>"),
				dotPath,
				offset);

		return offset;
	}

	public DebugLayout printInt(long offset, String... path) {
		String dotPath = toStringWithOffsets(path);
		int value = getInt(offset, path);

		System.out.printf("%s.%s = %d [0x%d, 0b%s] (int-32)%n",
				layout.name().orElse("<no-name>"),
				dotPath,
				value,
				value,
				Integer.toBinaryString(value));

		return this;
	}

	public DebugLayout printInt(String... path) {
		return printInt(0, path);
	}

	public DebugLayout printString(long offset, Charset charset, String... path) {
		String dotPath = toStringWithOffsets(offset, path);
		String value = getString(offset, charset, path);

		System.out.printf("%s.%s = \"%s\" (length=%d, charset=%s)%n",
				layout.name().orElse("<no-name>"),
				dotPath,
				value,
				value.length(),
				charset);

		return this;
	}

	public DebugLayout printString(long offset, String... path) {
		return printString(offset, StandardCharsets.UTF_8, path);

	}

	public DebugLayout printString(String... path) {
		return printString(0, StandardCharsets.UTF_8, path);
	}

	public DebugLayout printString(Charset charset, String... path) {
		return printString(0, charset, path);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DebugLayout [layout=" + layout + "]";
	}

	public String toStringWithOffsets(String... path) {
		PathElement[] pathElements = parsePath(path);

		String[] pathStrings = Arrays.stream(path)
				.flatMap(e -> Arrays.stream(e.split("\\.")))
				.toArray(String[]::new);

		long[] offsets = IntStream.range(0, pathElements.length)
				.mapToLong(i -> layout.byteOffset(Arrays.copyOf(pathElements, i + 1)))
				.toArray();

		return IntStream.range(0, pathElements.length)
				.mapToObj(i -> "%s+%d".formatted(pathStrings[i], offsets[i]))
				.collect(Collectors.joining("."));
	}

	public String toStringWithOffsets(long offset, String... path) {
		PathElement[] pathElements = parsePath(path);

		String[] pathStrings = Arrays.stream(path)
				.flatMap(e -> Arrays.stream(e.split("\\.")))
				.toArray(String[]::new);

		long[] offsets = IntStream.range(0, pathElements.length)
				.mapToLong(i -> layout.byteOffset(Arrays.copyOf(pathElements, i + 1)))
				.toArray();

		if (offset == 0) {
			return IntStream.range(0, pathElements.length)
					.mapToObj(i -> "%s(%+d)".formatted(pathStrings[i], offsets[i]))
					.collect(Collectors.joining("."));

		} else {
			return IntStream.range(0, pathElements.length)
					.mapToObj(i -> "%s(%+d%+d=%+d)".formatted(pathStrings[i], offsets[i], offset, (offsets[i]
							+ offset)))
					.collect(Collectors.joining("."));
		}
	}

	public DebugLayout printHexdump() {
		return printHexdump(0, (int) pointer().byteSize(), HexStrings.DEFAULT_HEXDUMP_PREFIX);
	}

	public DebugLayout printHexdump(IntFunction<String> prefix) {
		return printHexdump(0, (int) pointer().byteSize(), prefix);
	}

	public DebugLayout printHexdump(int offset, int length) {
		return printHexdump(offset, length, HexStrings.DEFAULT_HEXDUMP_PREFIX);
	}

	public DebugLayout printHexdump(int offset, int length, IntFunction<String> prefix) {
		byte[] array = pointer().toArray(ValueLayout.JAVA_BYTE);

		String output = HexStrings.toHexTextDump(
				new StringBuilder(),
				array, offset, length,
				16, prefix

		).toString();

		System.out.println(output);

		return this;
	}
}
