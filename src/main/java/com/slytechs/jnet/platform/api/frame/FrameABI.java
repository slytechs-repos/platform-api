package com.slytechs.jnet.platform.api.frame;

import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;

import com.slytechs.jnet.platform.api.memory.foreign.NativeABI;

/**
 * Defines Application Binary Interface (ABI) configurations for accessing
 * network frame data in native memory. The ABI configuration determines how the
 * binary data is structured and aligned in memory.
 * 
 * <p>
 * Two main factors affect the ABI configuration:
 * <ul>
 * <li>Memory Layout:
 * <ul>
 * <li>COMPACT - 32-bit field sizes remain 32-bit</li>
 * <li>PADDED - 32-bit fields are expanded to 64-bit on 64-bit
 * architectures</li>
 * </ul>
 * </li>
 * <li>Byte Order: Little-Endian (LE) vs Big-Endian (BE) for cross-platform
 * compatibility</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Network frames captured on systems with different endianness (BE/LE) can be
 * properly interpreted by using the appropriate ABI configuration. Direct
 * memory access is used for efficiency instead of MemoryLayouts.
 * </p>
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public enum FrameABI {

	/** Compact layout (32-bit fields) with little-endian byte order */
	COMPACT_LE("CL", ByteOrder.LITTLE_ENDIAN),

	/** Compact layout (32-bit fields) with big-endian byte order */
	COMPACT_BE("CB", ByteOrder.BIG_ENDIAN),

	/** Padded layout (32-bit expanded to 64-bit) with little-endian byte order */
	PADDED_LE("PL", ByteOrder.LITTLE_ENDIAN),

	/** Padded layout (32-bit expanded to 64-bit) with big-endian byte order */
	PADDED_BE("PB", ByteOrder.BIG_ENDIAN);

	/** Minimum allowed frame size in bytes */
	public static final int MIN_FRAME_SIZE = 14;

	/** Maximum allowed frame size in bytes */
	public static final int MAX_FRAME_SIZE = 64 * 1024;

	/** The native ABI configuration for the current platform */
	private static final FrameABI NATIVE_ABI = calcNativeABI();

	/**
	 * Determines the native ABI configuration based on platform architecture and
	 * byte order. On 32-bit platforms uses COMPACT layout, on 64-bit platforms uses
	 * PADDED layout to accommodate expanded field sizes.
	 *
	 * @return the native ABI configuration for the current platform
	 */
	private static FrameABI calcNativeABI() {
		var littleEndian = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;

		if (NativeABI.is32bit())
			return littleEndian ? COMPACT_LE : COMPACT_BE;

		return littleEndian ? PADDED_LE : PADDED_BE;
	}

	/**
	 * Returns the native ABI configuration for the current platform. This is
	 * determined based on the platform's architecture (32/64-bit) and native byte
	 * order.
	 *
	 * @return the native ABI configuration
	 */
	public static FrameABI nativeABI() {
		return NATIVE_ABI;
	}

	/**
	 * Creates a FrameABI instance with the specified memory layout and byte order.
	 * 
	 * @param isCompact true for compact layout (32-bit fields), false for padded
	 *                  layout (32-bit fields expanded to 64-bit)
	 * @param order     the desired byte order (big-endian or little-endian)
	 * @return the appropriate FrameABI instance based on the specified parameters
	 */
	public static FrameABI valueOf(boolean isCompact, ByteOrder order) {
		return isCompact
				? COMPACT_LE.withOrder(order)
				: PADDED_LE.withOrder(order);
	}

	/** Java layout for 8-bit byte values, configured for consistency */
	public final ValueLayout.OfByte JAVA_BYTE;

	/** Java layout for 16-bit short values, configured for this ABI's byte order */
	public final ValueLayout.OfShort JAVA_SHORT;

	/**
	 * Java layout for 32-bit integer values, configured for this ABI's byte order
	 */
	public final ValueLayout.OfInt JAVA_INT;

	/** Java layout for 64-bit long values, configured for this ABI's byte order */
	public final ValueLayout.OfLong JAVA_LONG;

	/** Java layout for 32-bit float values, configured for this ABI's byte order */
	public final ValueLayout.OfFloat JAVA_FLOAT;

	/**
	 * Java layout for 64-bit double values, configured for this ABI's byte order
	 */
	public final ValueLayout.OfDouble JAVA_DOUBLE;

	private final String abbr;
	private final ByteOrder byteOrder;
	private final boolean isCompact;

	/**
	 * Constructs a new FrameABI enum constant.
	 *
	 * @param abbr  the abbreviated name of the ABI configuration
	 * @param order the byte order used by this ABI
	 */
	FrameABI(String abbr, ByteOrder order) {
		this.abbr = abbr;
		this.byteOrder = order;
		this.isCompact = name().startsWith("COMPACT");

		this.JAVA_BYTE = ValueLayout.JAVA_BYTE.withOrder(order);
		this.JAVA_SHORT = ValueLayout.JAVA_SHORT.withOrder(order);
		this.JAVA_INT = ValueLayout.JAVA_INT.withOrder(order);
		this.JAVA_LONG = ValueLayout.JAVA_LONG.withOrder(order);
		this.JAVA_FLOAT = ValueLayout.JAVA_FLOAT.withOrder(order);
		this.JAVA_DOUBLE = ValueLayout.JAVA_DOUBLE.withOrder(order);
	}

	/**
	 * Returns the abbreviated name of this ABI configuration.
	 *
	 * @return a two-character string representing the ABI configuration: first
	 *         character C(ompact) or P(added), second character L(ittle-endian) or
	 *         B(ig-endian)
	 */
	public String abbr() {
		return abbr;
	}

	/**
	 * Determines if this ABI uses compact layout where 32-bit fields remain 32-bit
	 * on all platforms.
	 *
	 * @return true if this ABI uses compact layout, false if it uses padded layout
	 *         (32-bit fields expanded to 64-bit)
	 */
	public boolean isCompact() {
		return isCompact;
	}

	/**
	 * Returns the appropriate memory offset based on the ABI's layout type. The
	 * method selects between two possible offset values depending on whether the
	 * ABI uses compact or padded layout.
	 *
	 * @param compactOffset the memory offset to use when fields are 32-bit (compact
	 *                      layout)
	 * @param paddedOffset  the memory offset to use when 32-bit fields are expanded
	 *                      to 64-bit (padded layout)
	 * @return compactOffset if this ABI uses compact layout, paddedOffset if it
	 *         uses padded layout
	 */
	public long offset(long compactOffset, long paddedOffset) {
		if (isCompact)
			return compactOffset;

		return paddedOffset;
	}

	/**
	 * Returns the byte order used by this ABI configuration.
	 *
	 * @return the ByteOrder (LITTLE_ENDIAN or BIG_ENDIAN)
	 */
	public ByteOrder order() {
		return byteOrder;
	}

	/**
	 * Returns the ABI configuration with the opposite byte order while maintaining
	 * the same layout (compact/padded). This is useful when processing network
	 * frames captured on systems with different endianness than the current
	 * platform.
	 *
	 * @return the ABI configuration with swapped byte order
	 */
	public FrameABI swapped() {
		return switch (this) {
		case COMPACT_BE -> COMPACT_LE;
		case COMPACT_LE -> COMPACT_BE;
		case PADDED_BE -> PADDED_LE;
		case PADDED_LE -> PADDED_BE;
		};
	}

	/**
	 * Returns a FrameABI instance with the specified byte order while maintaining
	 * the same memory layout (compact/padded). If the current instance already has
	 * the requested byte order, returns this instance.
	 *
	 * @param order the desired byte order (big-endian or little-endian)
	 * @return a FrameABI with the requested byte order - either this instance if
	 *         the byte order matches, or the swapped instance if it differs
	 */
	public FrameABI withOrder(ByteOrder order) {
		return this.byteOrder == order
				? this
				: swapped();
	}
}