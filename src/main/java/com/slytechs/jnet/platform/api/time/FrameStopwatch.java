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
package com.slytechs.jnet.platform.api.time;

import static com.slytechs.jnet.platform.api.time.NanoTime.*;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import com.slytechs.jnet.platform.api.frame.FrameHeader;

/**
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public class FrameStopwatch implements AutoCloseable {

	private final static TimestampUnit INTERNAL_TS_UNIT = TimestampUnit.EPOCH_NANO;

	private long captureTsLast;
	private long startTsLast;
	private long captureTs;
	private long startTs;
	private long offset;

	public FrameStopwatch startPcapNano(long captureTsSec, long captureTsNsec) {
		long ts = TimestampUnit.PCAP_NANO.ofSecond(captureTsSec, captureTsNsec);

		return start(ts, TimestampUnit.PCAP_NANO);
	}

	public FrameStopwatch startPcapMicro(long captureTsSec, long captureTsUsec) {
		long ts = TimestampUnit.PCAP_MICRO.ofSecond(captureTsSec, captureTsUsec);

		return start(ts, TimestampUnit.PCAP_MICRO);
	}
	
	public FrameStopwatch start(FrameHeader header) {
		return start(header.timestamp(), header.getTimestampUnit());
	}

	public FrameStopwatch start(long captureTs, TimestampUnit tsUnit) {
		this.captureTs = tsUnit.toEpochNano(captureTs);
		this.startTs = currentTimeNanos();

		this.offset = startTs - captureTs;

		return this;
	}

	public long computeIfg(Duration ifg) {
		return computeIfg(ifg.toNanos());
	}

	public long computeIfg(long ifg, TimeUnit unit) {
		return computeIfg(unit.toNanos(ifg));
	}

	public long computeIfg(long ifgNanos) {
		if (captureTsLast == 0 || ifgNanos == 0) // We're the first frame
			return 0;

		long soFar = currentTimeNanos() - startTsLast;
		if (soFar < ifgNanos)
			return ifgNanos - soFar; // Remaining to min IFG

		return 0; // We're passed the min IFG gap
	}

	public void delayIfg(Duration ifg) throws InterruptedException {
		delayIfg(ifg.toNanos());
	}

	public void delayIfg(long ifg, TimeUnit unit) throws InterruptedException {
		delayIfg(unit.toNanos(ifg));
	}

	public void delayIfg(long ifgNanos) throws InterruptedException {
		long delayNs = computeIfg(ifgNanos);

		if (delayNs > 0)
			NanoTime.delay(delayNs);
	}

	public long newCaptureTs(TimestampUnit tsUnit) {
		long ts = currentTimeNanos() - offset;

		return tsUnit.convert(ts, INTERNAL_TS_UNIT);
	}

	public long newCaptureTsNanos() {
		long ts = currentTimeNanos() - offset;

		return ts;
	}

	public long newTs(TimestampUnit tsUnit) {
		return tsUnit.convert(currentTimeNanos(), INTERNAL_TS_UNIT);
	}

	public long newTsNanos() {
		return currentTimeNanos();
	}

	@Override
	public void close() {
		captureTsLast = captureTs;
		startTsLast = startTs;
	}
}
