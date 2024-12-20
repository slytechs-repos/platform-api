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

import com.slytechs.jnet.platform.api.time.TimestampUnit;

/**
 * Represents a captured network frame with associated metadata such as
 * timestamp, capture length, and wire length. This interface provides access to
 * both frame data and allows modification of frame attributes.
 * 
 * <p>
 * Each frame contains:
 * <ul>
 * <li>FrameHeader data in both buffer and memory segment format</li>
 * <li>Timing information with configurable precision</li>
 * <li>Length information including both captured and original wire lengths</li>
 * </ul>
 * </p>
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public interface FrameHeader {

	/**
	 * Provides access to the frame data as a byte buffer.
	 *
	 * @return a byte buffer containing the frame data
	 */
	ByteBuffer asBuffer();

	/**
	 * Provides access to the frame data as a memory segment.
	 *
	 * @return a memory segment containing the frame data
	 */
	MemorySegment asSegment();

	/**
	 * Gets the number of bytes actually captured and stored in this frame. This may
	 * be less than the original packet length if truncation occurred.
	 *
	 * @return the number of captured bytes available in this frame
	 */
	int captureLength();

	/**
	 * Sets a new capture length for this frame.
	 *
	 * @param newLength the new capture length in bytes
	 * @return the previous capture length
	 */
	int captureLength(int newLength);

	/**
	 * Gets the configured timestamp unit used for this pcap frame. This determines
	 * the precision used for timestamp values in the pcap header.
	 *
	 * @return the current timestamp unit
	 */
	TimestampUnit getTimestampUnit();

	/**
	 * Sets the timestamp unit for this frame.
	 *
	 * @param newUnit the new timestamp unit to use
	 * @return this frame instance for method chaining
	 * @throws NullPointerException if newUnit is null
	 */
	PcapFrameHeader setTimestampUnit(TimestampUnit newUnit);

	/**
	 * Gets the frame's timestamp in the currently defined header unit.
	 *
	 * @return the frame timestamp converted to the specified unit
	 */
	long timestamp();

	/**
	 * Sets a new timestamp for this frame.
	 *
	 * @param newTs  the new timestamp value
	 * @param tsUnit the unit of the timestamp value
	 * @return the previous timestamp in the specified unit
	 */
	long timestamp(long newTs, TimestampUnit tsUnit);

	/**
	 * Gets the frame's timestamp in the specified time unit.
	 *
	 * @param tsUnit the desired timestamp unit for the return value
	 * @return the frame timestamp converted to the specified unit
	 */
	long timestamp(TimestampUnit tsUnit);

	/**
	 * Gets the original length of the packet on the wire, before any truncation.
	 * This may be larger than captureLength() if the packet was truncated during
	 * capture.
	 *
	 * @return the original packet length in bytes
	 */
	int wireLength();

	/**
	 * Sets a new wire length for this frame.
	 *
	 * @param newLength the new wire length in bytes
	 * @return the previous wire length
	 */
	int wireLength(int newLength);
}