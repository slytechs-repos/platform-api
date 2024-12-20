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
package com.slytechs.jnet.platform.api.frame;

import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.util.Objects;

import com.slytechs.jnet.platform.api.MemoryBinding;
import com.slytechs.jnet.platform.api.time.TimestampUnit;

/**
 * A libpcap-specific FrameHeader implementation that provides access to
 * captured packet data stored in the pcap file format. This class handles the
 * standard pcap header structure which includes timestamp, capture length, and
 * wire length fields.
 * 
 * <p>
 * The pcap header structure layout varies based on platform architecture:
 * 
 * <pre>
 * Compact Layout (32-bit alignment):
 * - struct timeval ts;     // 0: timestamp seconds and microseconds (8 bytes)
 * - uint32_t caplen;      // 8: length of captured portion
 * - uint32_t len;         // 12: original packet length
 * Total: 16 bytes
 * 
 * Padded Layout (64-bit alignment):
 * - struct timeval ts;     // 0: timestamp seconds and microseconds (16 bytes)
 * - uint32_t caplen;      // 16: length of captured portion
 * - uint32_t len;         // 20: original packet length
 * Total: 24 bytes
 * </pre>
 * </p>
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 * @see <a href="https://www.tcpdump.org/manpages/pcap-savefile.5.txt">PCAP File
 *      Format</a>
 */
public class PcapFrameHeader extends MemoryBinding implements FrameHeader {

	/** Standard pcap header offsets for 32-bit/compact layout */
	private static final long TV_SEC_OFFSET_COMPACT = 0;
	private static final long TV_USEC_OFFSET_COMPACT = 4;
	private static final long CAPLEN_OFFSET_COMPACT = 8;
	private static final long WIRELEN_OFFSET_COMPACT = 12;

	/** pcap header offsets for 64-bit/padded layout */
	private static final long TV_SEC_OFFSET_PADDED = 0;
	private static final long TV_USEC_OFFSET_PADDED = 8;
	private static final long CAPLEN_OFFSET_PADDED = 16;
	private static final long WIRELEN_OFFSET_PADDED = 20;

	/**
	 * Gets the capture length from a ByteBuffer using the specified ABI
	 * configuration.
	 *
	 * @param header buffer containing frame header
	 * @param offset start offset within the buffer
	 * @param abi    ABI configuration for memory layout
	 * @return captured packet length in bytes
	 */
	public static int captureLength(ByteBuffer header, int offset, FrameABI abi) {
		offset += abi.offset(CAPLEN_OFFSET_COMPACT, CAPLEN_OFFSET_PADDED);

		int len = header.getInt(offset);

		return len;
	}

	/**
	 * Sets the capture length in a ByteBuffer containing a pcap packet header.
	 *
	 * @param header   buffer containing pcap packet header
	 * @param offset   start offset within the buffer to the header
	 * @param abi      ABI configuration for memory layout
	 * @param newValue new capture length value to set
	 * @return the value that was set
	 */
	public static int captureLength(ByteBuffer header, int offset, FrameABI abi, int newValue) {
		offset += abi.offset(CAPLEN_OFFSET_COMPACT, CAPLEN_OFFSET_PADDED);

		header.putInt(offset, newValue);

		return newValue;
	}

	/**
	 * Gets the capture length from a MemorySegment containing a pcap packet header.
	 *
	 * @param header memory segment containing pcap packet header
	 * @param offset start offset within the segment to the header
	 * @param abi    ABI configuration for memory layout
	 * @return number of bytes actually captured and saved in the file
	 */
	public static int captureLength(MemorySegment header, long offset, FrameABI abi) {
		offset += abi.offset(CAPLEN_OFFSET_COMPACT, CAPLEN_OFFSET_PADDED);

		int len = header.get(abi.JAVA_INT, offset);

		return len;
	}

	/**
	 * Sets the capture length in a MemorySegment containing a pcap packet header.
	 *
	 * @param header   memory segment containing pcap packet header
	 * @param offset   start offset within the segment to the header
	 * @param abi      ABI configuration for memory layout
	 * @param newValue new capture length value to set
	 * @return the value that was set
	 */
	public static int captureLength(MemorySegment header, long offset, FrameABI abi, int newValue) {
		offset += abi.offset(CAPLEN_OFFSET_COMPACT, CAPLEN_OFFSET_PADDED);

		header.set(abi.JAVA_INT, offset, newValue);

		return newValue;
	}

	/**
	 * Gets the timestamp seconds field from a ByteBuffer containing a pcap packet
	 * header.
	 *
	 * @param header buffer containing pcap packet header
	 * @param offset start offset within the buffer to the header
	 * @param abi    ABI configuration for memory layout
	 * @return seconds value from the header's timestamp
	 */
	public static int tvSec(ByteBuffer header, int offset, FrameABI abi) {
		offset += abi.offset(TV_SEC_OFFSET_COMPACT, TV_SEC_OFFSET_PADDED);

		int len = header.getInt(offset);

		return len;
	}

	/**
	 * Sets the timestamp seconds field in a ByteBuffer containing a pcap packet
	 * header.
	 *
	 * @param header   buffer containing pcap packet header
	 * @param offset   start offset within the buffer to the header
	 * @param abi      ABI configuration for memory layout
	 * @param newValue new seconds value to set
	 * @return the value that was set
	 */
	public static int tvSec(ByteBuffer header, int offset, FrameABI abi, int newValue) {
		offset += abi.offset(TV_SEC_OFFSET_COMPACT, TV_SEC_OFFSET_PADDED);

		header.putInt(offset, newValue);

		return newValue;
	}

	/**
	 * Gets the timestamp seconds field from a MemorySegment containing a pcap
	 * packet header.
	 *
	 * @param header memory segment containing pcap packet header
	 * @param offset start offset within the segment to the header
	 * @param abi    ABI configuration for memory layout
	 * @return seconds value from the header's timestamp
	 */
	public static int tvSec(MemorySegment header, long offset, FrameABI abi) {
		offset += abi.offset(TV_SEC_OFFSET_COMPACT, TV_SEC_OFFSET_PADDED);

		int len = header.get(abi.JAVA_INT, offset);

		return len;
	}

	/**
	 * Sets the timestamp seconds field in a MemorySegment containing a pcap packet
	 * header.
	 *
	 * @param header   memory segment containing pcap packet header
	 * @param offset   start offset within the segment to the header
	 * @param abi      ABI configuration for memory layout
	 * @param newValue new seconds value to set
	 * @return the value that was set
	 */
	public static int tvSec(MemorySegment header, long offset, FrameABI abi, int newValue) {
		offset += abi.offset(TV_SEC_OFFSET_COMPACT, TV_SEC_OFFSET_PADDED);

		header.set(abi.JAVA_INT, offset, newValue);

		return newValue;
	}

	/**
	 * Gets the timestamp microseconds field from a ByteBuffer containing a pcap
	 * packet header.
	 *
	 * @param header buffer containing pcap packet header
	 * @param offset start offset within the buffer to the header
	 * @param abi    ABI configuration for memory layout
	 * @return microseconds value from the header's timestamp
	 */
	public static int tvUSec(ByteBuffer header, int offset, FrameABI abi) {
		offset += abi.offset(TV_USEC_OFFSET_COMPACT, TV_USEC_OFFSET_PADDED);

		int len = header.getInt(offset);

		return len;
	}

	/**
	 * Sets the timestamp microseconds field in a ByteBuffer containing a pcap
	 * packet header.
	 *
	 * @param header   buffer containing pcap packet header
	 * @param offset   start offset within the buffer to the header
	 * @param abi      ABI configuration for memory layout
	 * @param newValue new microseconds value to set
	 * @return the value that was set
	 */
	public static int tvUSec(ByteBuffer header, int offset, FrameABI abi, int newValue) {
		offset += abi.offset(TV_USEC_OFFSET_COMPACT, TV_USEC_OFFSET_PADDED);

		header.putInt(offset, newValue);

		return newValue;
	}

	/**
	 * Gets the timestamp microseconds field from a MemorySegment containing a pcap
	 * packet header.
	 *
	 * @param header memory segment containing pcap packet header
	 * @param offset start offset within the segment to the header
	 * @param abi    ABI configuration for memory layout
	 * @return microseconds value from the header's timestamp
	 */
	public static int tvUSec(MemorySegment header, long offset, FrameABI abi) {
		offset += abi.offset(TV_USEC_OFFSET_COMPACT, TV_USEC_OFFSET_PADDED);

		int len = header.get(abi.JAVA_INT, offset);

		return len;
	}

	/**
	 * Sets the timestamp microseconds field in a MemorySegment containing a pcap
	 * packet header.
	 *
	 * @param header   memory segment containing pcap packet header
	 * @param offset   start offset within the segment to the header
	 * @param abi      ABI configuration for memory layout
	 * @param newValue new microseconds value to set
	 * @return the value that was set
	 */
	public static int tvUSec(MemorySegment header, long offset, FrameABI abi, int newValue) {
		offset += abi.offset(TV_USEC_OFFSET_COMPACT, TV_USEC_OFFSET_PADDED);

		header.set(abi.JAVA_INT, offset, newValue);

		return newValue;
	}

	/**
	 * Gets the wire length from a ByteBuffer containing a pcap packet header.
	 *
	 * @param header buffer containing pcap packet header
	 * @param offset start offset within the buffer to the header
	 * @param abi    ABI configuration for memory layout
	 * @return length of the packet as it appeared on the network
	 */
	public static int wireLength(ByteBuffer header, int offset, FrameABI abi) {
		offset += abi.offset(WIRELEN_OFFSET_COMPACT, WIRELEN_OFFSET_PADDED);

		int len = header.getInt(offset);

		return len;
	}

	/**
	 * Sets the wire length in a ByteBuffer containing a pcap packet header.
	 *
	 * @param header   buffer containing pcap packet header
	 * @param offset   start offset within the buffer to the header
	 * @param abi      ABI configuration for memory layout
	 * @param newValue new wire length value to set
	 * @return the value that was set
	 */
	public static int wireLength(ByteBuffer header, int offset, FrameABI abi, int newValue) {
		offset += abi.offset(WIRELEN_OFFSET_COMPACT, WIRELEN_OFFSET_PADDED);

		header.putInt(offset, newValue);

		return newValue;
	}

	/**
	 * Gets the wire length from a MemorySegment containing a pcap packet header.
	 *
	 * @param header memory segment containing pcap packet header
	 * @param offset start offset within the segment to the header
	 * @param abi    ABI configuration for memory layout
	 * @return length of the packet as it appeared on the network
	 */
	public static int wireLength(MemorySegment header, long offset, FrameABI abi) {
		offset += abi.offset(WIRELEN_OFFSET_COMPACT, WIRELEN_OFFSET_PADDED);

		int len = header.get(abi.JAVA_INT, offset);

		return len;
	}

	/**
	 * Sets the wire length in a MemorySegment containing a pcap packet header.
	 *
	 * @param header   memory segment containing pcap packet header
	 * @param offset   start offset within the segment to the header
	 * @param abi      ABI configuration for memory layout
	 * @param newValue new wire length value to set
	 * @return the value that was set
	 */
	public static int wireLength(MemorySegment header, long offset, FrameABI abi, int newValue) {
		offset += abi.offset(WIRELEN_OFFSET_COMPACT, WIRELEN_OFFSET_PADDED);

		header.set(abi.JAVA_INT, offset, newValue);

		return newValue;
	}

	private FrameABI abi = FrameABI.nativeABI();

	private TimestampUnit tsUnit = TimestampUnit.PCAP_MICRO;

	/**
	 * Constructs a new PcapFrameHeader with default ABI configuration (native) and
	 * default timestamp unit (microseconds).
	 */
	public PcapFrameHeader() {
	}

	/**
	 * Constructs a new PcapFrameHeader with the specified ABI configuration and
	 * default timestamp unit (microseconds).
	 *
	 * @param abi the ABI configuration for memory layout and byte order
	 */
	public PcapFrameHeader(FrameABI abi) {
		this.abi = abi;
	}

	/**
	 * Constructs a new PcapFrameHeader with the specified ABI configuration and
	 * timestamp unit.
	 *
	 * @param abi           the ABI configuration for memory layout and byte order
	 * @param pcapTimestamp the timestamp precision unit for this frame
	 */
	public PcapFrameHeader(FrameABI abi, TimestampUnit pcapTimestamp) {
		this.abi = abi;
		this.tsUnit = pcapTimestamp;
	}

	/**
	 * Gets the current ABI configuration.
	 *
	 * @return the current ABI configuration
	 */
	public FrameABI abi() {
		return abi;
	}

	/**
	 * @see com.slytechs.jnet.platform.api.frame.FrameHeader#asBuffer()
	 */
	@Override
	public ByteBuffer asBuffer() {
		return super.buffer();
	}

	/**
	 * @see com.slytechs.jnet.platform.api.frame.FrameHeader#asSegment()
	 */
	@Override
	public MemorySegment asSegment() {
		return super.memorySegment();
	}

	/**
	 * @see com.slytechs.jnet.platform.api.frame.FrameHeader#captureLength()
	 */
	@Override
	public int captureLength() {
		return captureLength(memorySegment(), 0, abi);
	}

	/**
	 * @see com.slytechs.jnet.platform.api.frame.FrameHeader#captureLength(int)
	 */
	@Override
	public int captureLength(int newLength) {
		return captureLength(memorySegment(), 0, abi, newLength);
	}

	/**
	 * Gets the configured timestamp unit used for this pcap frame. This determines
	 * the precision used for timestamp values in the pcap header.
	 *
	 * @return the current timestamp unit
	 */
	@Override
	public TimestampUnit getTimestampUnit() {
		return this.tsUnit;
	}

	/**
	 * Sets the ABI configuration for this frame.
	 *
	 * @param newABI the new ABI configuration to use
	 * @return this frame instance for method chaining
	 * @throws NullPointerException if newABI is null
	 */
	public PcapFrameHeader setAbi(FrameABI newABI) {
		this.abi = Objects.requireNonNull(newABI);

		return this;
	}

	/**
	 * Sets the timestamp unit for this frame.
	 *
	 * @param newUnit the new timestamp unit to use
	 * @return this frame instance for method chaining
	 * @throws NullPointerException if newUnit is null
	 */
	@Override
	public PcapFrameHeader setTimestampUnit(TimestampUnit newUnit) {
		this.tsUnit = Objects.requireNonNull(newUnit);

		return this;
	}

	/**
	 * @see com.slytechs.jnet.platform.api.frame.FrameHeader#timestamp()
	 */
	@Override
	public long timestamp() {
		return timestamp(this.tsUnit);
	}

	/**
	 * @see com.slytechs.jnet.platform.api.frame.FrameHeader#timestamp(long,
	 *      com.slytechs.jnet.platform.api.time.TimestampUnit)
	 */
	@Override
	public long timestamp(long newTs, TimestampUnit timestampUnit) {
		long pcapTs = this.tsUnit.convert(newTs, timestampUnit);

		int tvSec = (int) tsUnit.toEpochSecond(pcapTs);
		int tvUSec = (int) tsUnit.toEpochSecondFraction(pcapTs);

		tvSec(tvSec);
		tvUSec(tvUSec);

		return newTs;
	}

	/**
	 * @see com.slytechs.jnet.platform.api.frame.FrameHeader#timestamp(com.slytechs.jnet.platform.api.time.TimestampUnit)
	 */
	@Override
	public long timestamp(TimestampUnit tsUnit) {
		long pcapTs = tsUnit.ofSecond(tvSec(), tvUSec());

		return tsUnit.convert(pcapTs, tsUnit);
	}

	/**
	 * Gets the timestamp seconds component.
	 *
	 * @return seconds since epoch
	 */
	public int tvSec() {
		return tvSec(memorySegment(), 0, abi);
	}

	/**
	 * Sets the timestamp seconds component.
	 *
	 * @param newValue new seconds value
	 * @return the new value that was set
	 */
	public int tvSec(int newValue) {
		return tvSec(memorySegment(), 0, abi, newValue);
	}

	/**
	 * Gets the timestamp microseconds component.
	 *
	 * @return microseconds within the second
	 */
	public int tvUSec() {
		return tvUSec(memorySegment(), 0, abi);
	}

	/**
	 * Sets the timestamp microseconds component.
	 *
	 * @param newValue new microseconds value
	 * @return the new value that was set
	 */
	public int tvUSec(int newValue) {
		return tvUSec(memorySegment(), 0, abi, newValue);
	}

	/**
	 * @see com.slytechs.jnet.platform.api.frame.FrameHeader#wireLength()
	 */
	@Override
	public int wireLength() {
		return wireLength(memorySegment(), 0, abi);
	}

	/**
	 * @see com.slytechs.jnet.platform.api.frame.FrameHeader#wireLength(int)
	 */
	@Override
	public int wireLength(int newLength) {
		return wireLength(memorySegment(), 0, abi, newLength);
	}

}
