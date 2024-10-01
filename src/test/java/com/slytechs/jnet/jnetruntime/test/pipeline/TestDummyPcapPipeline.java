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
package com.slytechs.jnet.jnetruntime.test.pipeline;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Function;

import com.slytechs.jnet.jnetruntime.NotFound;
import com.slytechs.jnet.jnetruntime.pipeline.ATypeLookup;
import com.slytechs.jnet.jnetruntime.pipeline.AbstractEntryPoint;
import com.slytechs.jnet.jnetruntime.pipeline.AbstractInput;
import com.slytechs.jnet.jnetruntime.pipeline.AbstractOutput;
import com.slytechs.jnet.jnetruntime.pipeline.AbstractPipeline;
import com.slytechs.jnet.jnetruntime.pipeline.AbstractProcessor;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.InputTransformer;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.InputTransformer.EntryPoint;
import com.slytechs.jnet.jnetruntime.pipeline.DataType;
import com.slytechs.jnet.jnetruntime.pipeline.HeadNode;
import com.slytechs.jnet.jnetruntime.pipeline.Pipeline;
import com.slytechs.jnet.jnetruntime.pipeline.TailNode;
import com.slytechs.jnet.jnetruntime.time.Timestamp;
import com.slytechs.jnet.jnetruntime.util.HasPriority;

/**
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 *
 */
public class TestDummyPcapPipeline {

	enum DataTypes implements DataType {
		RAW_PACKET_PIPE(RawPacketPipe.class, DataTypes::wrapPipeline),
		PACKETREF_HANDLER(PacketRefHandler.class, DataTypes::wrapOutput),
		NATIVE_PCAP_HANDLER(NativePcapHandler.class, DataTypes::wrapNativePcapHandler),

		;

		private final DataSupport<?> dataSupport;

		<T, U> DataTypes(Class<T> arrayFactory) {
			this.dataSupport = new DataSupport<T>(this, arrayFactory);
		}

		<T, U> DataTypes(Class<T> arrayFactory, Function<T[], T> arrayWrapper) {
			this.dataSupport = new DataSupport<T>(this, arrayFactory, null, arrayWrapper);
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.pipeline.DataType#dataSupport()
		 */
		@SuppressWarnings("unchecked")
		@Override
		public <T> DataSupport<T> dataSupport() {
			return (DataSupport<T>) dataSupport;
		}

		static NativePcapHandler wrapNativePcapHandler(NativePcapHandler[] array) {
			return (hdr, data, _) -> {
				for (var out : array)
					out.handleNativePcapPacket(hdr, data, null);
			};
		}

		static RawPacketPipe wrapPipeline(RawPacketPipe[] array) {
			return (h, d) -> {
				for (var out : array)
					out.handleRawPacketPipe(h, d);
			};
		}

		static PacketRefHandler wrapOutput(PacketRefHandler[] array) {
			return (nb, ai, buffer) -> {
				for (var out : array)
					out.handlePacketRef(nb, ai, buffer);
			};
		}

	}

	@ATypeLookup(DataTypes.class)
	private static class PacketRefPipeline extends AbstractPipeline<RawPacketPipe, PacketRefPipeline> {

		public PacketRefPipeline() {
			super("packetref-pipeline", DataTypes.RAW_PACKET_PIPE);
		}
	}

	interface PacketRefHandler {
		void handlePacketRef(PcapHeader pcapHeader, MemorySegment data, ByteBuffer buffer);
	}

	interface RawPacketPipe {

		void handleRawPacketPipe(PcapHeader header, MemorySegment data);
	}

	private static class PreProcessedPacketRefOutput
			extends AbstractOutput<RawPacketPipe, PacketRefHandler, PreProcessedPacketRefOutput>
			implements RawPacketPipe {

		public PreProcessedPacketRefOutput(TailNode<RawPacketPipe> tailNode) {
			this(tailNode, "packetref-output");
		}

		public PreProcessedPacketRefOutput(TailNode<RawPacketPipe> tailNode, String name) {
			super(tailNode, name, DataTypes.RAW_PACKET_PIPE, DataTypes.PACKETREF_HANDLER);
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.test.pipeline.TestDummyPcapPipeline.RawPacketPipe#handleRawPacketPipe(com.slytechs.jnet.jnetruntime.test.pipeline.TestDummyPcapPipeline.PcapHeader,
		 *      java.lang.foreign.MemorySegment)
		 */
		@Override
		public void handleRawPacketPipe(PcapHeader header, MemorySegment data) {
			outputData().handlePacketRef(header, data, data.asByteBuffer().order(ByteOrder.BIG_ENDIAN));
		}

	}

	private static class ToUppercaseProcessor
			extends AbstractProcessor<RawPacketPipe, ToUppercaseProcessor>
			implements RawPacketPipe {

		public static final String NAME = "to_upper";

		public ToUppercaseProcessor(Pipeline<RawPacketPipe, ?> pipeline, int priority) {
			super(pipeline, priority, NAME, DataTypes.RAW_PACKET_PIPE);
		}

		public ToUppercaseProcessor peek(RawPacketPipe peekAction) {
			super.addOutputToNode(peekAction);
			return this;
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.test.pipeline.TestDummyPcapPipeline.RawPacketPipe#handleRawPacketPipe(com.slytechs.jnet.jnetruntime.test.pipeline.TestDummyPcapPipeline.PcapHeader,
		 *      java.lang.foreign.MemorySegment)
		 */
		@Override
		public void handleRawPacketPipe(PcapHeader header, MemorySegment data) {
			outputData().handleRawPacketPipe(header, data);
		}

	}

	private static class ToLowercaseProcessor
			extends AbstractProcessor<RawPacketPipe, ToLowercaseProcessor>
			implements RawPacketPipe {

		public static final String NAME = "to_lower";

		public ToLowercaseProcessor(Pipeline<RawPacketPipe, ?> pipeline, int priority) {
			super(pipeline, priority, NAME, DataTypes.RAW_PACKET_PIPE);
		}

		public ToLowercaseProcessor peek(RawPacketPipe peekAction) {
			super.addOutputToNode(peekAction);
			return this;
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.test.pipeline.TestDummyPcapPipeline.RawPacketPipe#handleRawPacketPipe(com.slytechs.jnet.jnetruntime.test.pipeline.TestDummyPcapPipeline.PcapHeader,
		 *      java.lang.foreign.MemorySegment)
		 */
		@Override
		public void handleRawPacketPipe(PcapHeader header, MemorySegment data) {
			outputData().handleRawPacketPipe(header, data);
		}

	}

	interface NativePcapHandler {

		void handleNativePcapPacket(MemorySegment header, MemorySegment Data, Object user);
	}

	private static class PcapHeader {
		int caplen = 64;
		int wirelen = 128;
		long timestamp = System.currentTimeMillis();
		private MemorySegment headerAddress;

		/**
		 * @param headerAddress
		 */
		public PcapHeader(MemorySegment headerAddress) {
			this.headerAddress = headerAddress;
			// TODO Auto-generated constructor stub
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "PcapHeader"
					+ " [caplen=" + caplen
					+ ", wirelen=" + wirelen
					+ ", timestamp=" + Timestamp.formatTimestampInEpochMilli(timestamp)
					+ "]";
		}

		/**
		 * @return
		 */
		public MemorySegment address() {
			return headerAddress;
		}

	}

	private static class PacketRef {

		private MemorySegment pcapHeader;
		private MemorySegment packetData;

		public PacketRef(MemorySegment pcapHeader, MemorySegment packetData) {
			this.pcapHeader = pcapHeader;
			this.packetData = packetData;
		}

		/**
		 * @return
		 */
		public MemorySegment pcapHeader() {
			return this.pcapHeader;
		}

		/**
		 * @return
		 */
		public MemorySegment data() {
			return this.packetData;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "PacketRef [pcapHeader=" + pcapHeader + ", packetData=" + packetData + "]";
		}

	}

	private static class NetPcap {

		private String device;

		private final Arena arena;

		public NetPcap(String device) {
			this.device = device;
			this.arena = Arena.ofAuto();
		}

		public String getDeviceName() {
			return device;
		}

		public static NetPcap create(String device) {
			return new NetPcap(device);
		}

		NetPcap activate() {
			return this;
		}

		public <T> int loop(NativePcapHandler handler, T opaque) {
			return 0;
		}

		public PacketRef nextEx() {
			return new PacketRef(arena.allocate(24), arena.allocate(128));
		}

	}

	private static class PcapInput extends AbstractInput<NativePcapHandler, RawPacketPipe, PcapInput>
			implements NativePcapHandler {

		private NetPcap pcap;
		private String deviceName;

		public PcapInput(HeadNode<RawPacketPipe> headNode, NetPcap pcap) {
			super(headNode, "pcap:" + pcap.getDeviceName(), DataTypes.NATIVE_PCAP_HANDLER, DataTypes.RAW_PACKET_PIPE);
			this.pcap = pcap;
			this.deviceName = pcap.getDeviceName();
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.test.pipeline.TestDummyPcapPipeline.NativePcapHandler#handleNativePcapPacket(java.lang.foreign.MemorySegment,
		 *      java.lang.foreign.MemorySegment, java.lang.Object)
		 */
		@Override
		public void handleNativePcapPacket(MemorySegment header, MemorySegment Data, Object user) {
			PcapHeader hdr = new PcapHeader(header);

			outputData().handleRawPacketPipe(hdr, Data);
		}

	}

	private static class PacketRefCollector implements PacketRefHandler {

		private Deque<PacketRef> deque = new ArrayDeque<>(100);

		/**
		 * @see com.slytechs.jnet.jnetruntime.test.pipeline.TestDummyPcapPipeline.PacketRefHandler#handlePacketRef(PcapHeader,
		 *      MemorySegment, ByteBuffer)
		 */
		@Override
		public void handlePacketRef(PcapHeader pcapHeader, MemorySegment data, ByteBuffer buffer) {
			put(new PacketRef(pcapHeader.address(), data));
		}

		public boolean isEmpty() {
			return deque.isEmpty();
		}

		public PacketRef get() {
			return deque.getFirst();
		}

		public void put(PacketRef pkt) {
			deque.push(pkt);
		}

	}

	private static class PcapNextExEntryPoint
			extends AbstractEntryPoint<NativePcapHandler> {

		private final PacketRefCollector collector;
		private final NetPcap pcap;

		public PcapNextExEntryPoint(
				InputTransformer<NativePcapHandler> input,
				String id,
				NetPcap pcap,
				PacketRefCollector collector) {
			super((AbstractInput<NativePcapHandler, ?, ?>) input, id);
			this.pcap = pcap;
			this.collector = collector;
		}

		public PacketRef nextEx() {
			if (collector.isEmpty())
				generateInputPacket();

			assert !collector.isEmpty();

			return collector.get();
		}

		private void generateInputPacket() {
			var packetRef = pcap.nextEx();

			NativePcapHandler input = inputData();

			input.handleNativePcapPacket(packetRef.pcapHeader(), packetRef.data(), null);
		}

	}

	public static void main(String[] args) throws NotFound {

		NetPcap pcap = NetPcap.create("en0");
		String deviceName = pcap.getDeviceName();
		String id = deviceName;

		var pipeline = new PacketRefPipeline();
		var packetRefCollector = new PacketRefCollector();

		var processedOutput = pipeline.addOutput(PreProcessedPacketRefOutput::new);

		processedOutput.createEndPoint("dispatcher");

		processedOutput.createEndPoint("collector")
				.priority(HasPriority.MAX_PRIORITY_VALUE)
				.outputData(packetRefCollector);

		pipeline.addProcessor(2, ToUppercaseProcessor::new);

		pipeline.addProcessor(1, ToLowercaseProcessor::new);

		pipeline.enable(true);

		PcapInput pcapInput = pipeline.addInput(id, pcap, PcapInput::new);
		EntryPoint<NativePcapHandler> loopEntryPoint = pcapInput.createEntryPoint("loop");
		PcapNextExEntryPoint nextExEntryPoint = pcapInput.createEntryPoint(
				"nextEx", // id
				pcap, // NetPcap
				packetRefCollector, // Output
				PcapNextExEntryPoint::new);

		System.out.println(pipeline);

		pcap.activate();
		int packetCount = pcap.loop(loopEntryPoint.inputData(), "User message");

		PacketRef packetRef = nextExEntryPoint.nextEx();

		System.out.println(packetRef);
	}

}
