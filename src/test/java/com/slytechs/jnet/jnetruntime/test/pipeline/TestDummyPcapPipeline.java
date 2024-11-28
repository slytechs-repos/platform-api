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
import com.slytechs.jnet.jnetruntime.pipeline.DataType;
import com.slytechs.jnet.jnetruntime.pipeline.HeadNode;
import com.slytechs.jnet.jnetruntime.pipeline.Pipeline;
import com.slytechs.jnet.jnetruntime.pipeline.TailNode;
import com.slytechs.jnet.jnetruntime.time.Timestamp;
import com.slytechs.jnet.jnetruntime.util.HasPriority;

/**
 * The Class TestDummyPcapPipeline.
 */
public class TestDummyPcapPipeline {

	/**
	 * The Enum DataTypes.
	 */
	enum DataTypes implements DataType {

		/** The raw packet pipe. */
		RAW_PACKET_PIPE(RawPacketPipe.class, DataTypes::wrapPipeline),

		/** The packetref handler. */
		PACKETREF_HANDLER(PacketRefHandler.class, DataTypes::wrapOutput),

		/** The native pcap handler. */
		NATIVE_PCAP_HANDLER(NativePcapHandler.class, DataTypes::wrapNativePcapHandler),

		;

		/** The data settingsSupport. */
		private final DataSupport<?> dataSupport;

		/**
		 * Instantiates a new data types.
		 *
		 * @param <T>       the generic type
		 * @param <U>       the generic type
		 * @param dataClass the data class
		 */
		<T, U> DataTypes(Class<T> dataClass) {
			this.dataSupport = new DataSupport<T>(this, dataClass);
		}

		/**
		 * Instantiates a new data types.
		 *
		 * @param <T>          the generic type
		 * @param <U>          the generic type
		 * @param dataClass    the data class
		 * @param arrayWrapper the array wrapper
		 */
		<T, U> DataTypes(Class<T> dataClass, Function<T[], T> arrayWrapper) {
			this.dataSupport = new DataSupport<T>(this, dataClass, arrayWrapper);
		}

		/**
		 * Data settingsSupport.
		 *
		 * @param <T> the generic type
		 * @return the data settingsSupport
		 * @see com.slytechs.jnet.jnetruntime.pipeline.DataType#dataSupport()
		 */
		@SuppressWarnings("unchecked")
		@Override
		public <T> DataSupport<T> dataSupport() {
			return (DataSupport<T>) dataSupport;
		}

		/**
		 * Wrap native pcap handler.
		 *
		 * @param array the array
		 * @return the native pcap handler
		 */
		static NativePcapHandler wrapNativePcapHandler(NativePcapHandler[] array) {
			return (hdr, data, _) -> {
				for (var out : array) {
					out.handleNativePcapPacket(hdr, data, null);
				}
			};
		}

		/**
		 * Wrap pipeline.
		 *
		 * @param array the array
		 * @return the raw packet pipe
		 */
		static RawPacketPipe wrapPipeline(RawPacketPipe[] array) {
			return (h, d) -> {
				for (var out : array) {
					out.handleRawPacketPipe(h, d);
				}
			};
		}

		/**
		 * Wrap output.
		 *
		 * @param array the array
		 * @return the packet ref handler
		 */
		static PacketRefHandler wrapOutput(PacketRefHandler[] array) {
			return (nb, ai, buffer) -> {
				for (var out : array) {
					out.handlePacketRef(nb, ai, buffer);
				}
			};
		}

	}

	/**
	 * The Class PacketRefPipeline.
	 */
	@ATypeLookup(DataTypes.class)
	private static class PacketRefPipeline extends AbstractPipeline<RawPacketPipe, PacketRefPipeline> {

		/**
		 * Instantiates a new packet ref pipeline.
		 */
		public PacketRefPipeline() {
			super("packetref-pipeline", DataTypes.RAW_PACKET_PIPE);
		}
	}

	/**
	 * The Interface PacketRefHandler.
	 */
	interface PacketRefHandler {

		/**
		 * Handle packet ref.
		 *
		 * @param pcapHeader the pcap header
		 * @param data       the data
		 * @param buffer     the buffer
		 */
		void handlePacketRef(PcapHeader pcapHeader, MemorySegment data, ByteBuffer buffer);
	}

	/**
	 * The Interface RawPacketPipe.
	 */
	interface RawPacketPipe {

		/**
		 * Handle raw packet pipe.
		 *
		 * @param header the header
		 * @param data   the data
		 */
		void handleRawPacketPipe(PcapHeader header, MemorySegment data);
	}

	/**
	 * The Class PreProcessedPacketRefOutput.
	 */
	private static class PRefOutput
			extends AbstractOutput<RawPacketPipe, PacketRefHandler, PRefOutput>
			implements RawPacketPipe {

		/**
		 * Instantiates a new pre processed packet ref output.
		 *
		 * @param tailNode the tail node
		 */
		public PRefOutput(TailNode<RawPacketPipe> tailNode) {
			this(tailNode, "packetref-output");
		}

		/**
		 * Instantiates a new pre processed packet ref output.
		 *
		 * @param tailNode the tail node
		 * @param name     the name
		 */
		public PRefOutput(TailNode<RawPacketPipe> tailNode, String name) {
			super(tailNode, name, DataTypes.RAW_PACKET_PIPE, DataTypes.PACKETREF_HANDLER);
		}

		/**
		 * Handle raw packet pipe.
		 *
		 * @param header the header
		 * @param data   the data
		 * @see com.slytechs.jnet.jnetruntime.test.pipeline.TestDummyPcapPipeline.RawPacketPipe#handleRawPacketPipe(com.slytechs.jnet.jnetruntime.test.pipeline.TestDummyPcapPipeline.PcapHeader,
		 *      java.lang.foreign.MemorySegment)
		 */
		@Override
		public void handleRawPacketPipe(PcapHeader header, MemorySegment data) {
			outputData().handlePacketRef(header, data, data.asByteBuffer().order(ByteOrder.BIG_ENDIAN));
		}

	}

	/**
	 * The Class ToUppercaseProcessor.
	 */
	private static class ToUppercaseProcessor
			extends AbstractProcessor<RawPacketPipe, ToUppercaseProcessor>
			implements RawPacketPipe {

		/** The Constant NAME. */
		public static final String NAME = "to_upper";

		/**
		 * Instantiates a new to uppercase processor.
		 *
		 * @param pipeline the pipeline
		 * @param priority the priority
		 */
		public ToUppercaseProcessor(Pipeline<RawPacketPipe, ?> pipeline, int priority) {
			super(pipeline, priority, NAME, DataTypes.RAW_PACKET_PIPE);
		}

		/**
		 * Peek.
		 *
		 * @param peekAction the peek action
		 * @return the to uppercase processor
		 */
		public ToUppercaseProcessor peek(RawPacketPipe peekAction) {
			super.addToOutputList(peekAction);
			return this;
		}

		/**
		 * Handle raw packet pipe.
		 *
		 * @param header the header
		 * @param data   the data
		 * @see com.slytechs.jnet.jnetruntime.test.pipeline.TestDummyPcapPipeline.RawPacketPipe#handleRawPacketPipe(com.slytechs.jnet.jnetruntime.test.pipeline.TestDummyPcapPipeline.PcapHeader,
		 *      java.lang.foreign.MemorySegment)
		 */
		@Override
		public void handleRawPacketPipe(PcapHeader header, MemorySegment data) {
			outputData().handleRawPacketPipe(header, data);
		}

	}

	/**
	 * The Class ToLowercaseProcessor.
	 */
	private static class ToLowercaseProcessor
			extends AbstractProcessor<RawPacketPipe, ToLowercaseProcessor>
			implements RawPacketPipe {

		/** The Constant NAME. */
		public static final String NAME = "to_lower";

		/**
		 * Instantiates a new to lowercase processor.
		 *
		 * @param pipeline the pipeline
		 * @param priority the priority
		 */
		public ToLowercaseProcessor(Pipeline<RawPacketPipe, ?> pipeline, int priority) {
			super(pipeline, priority, NAME, DataTypes.RAW_PACKET_PIPE);
		}

		/**
		 * Peek.
		 *
		 * @param peekAction the peek action
		 * @return the to lowercase processor
		 */
		public ToLowercaseProcessor peek(RawPacketPipe peekAction) {
			super.addToOutputList(peekAction);
			return this;
		}

		/**
		 * Handle raw packet pipe.
		 *
		 * @param header the header
		 * @param data   the data
		 * @see com.slytechs.jnet.jnetruntime.test.pipeline.TestDummyPcapPipeline.RawPacketPipe#handleRawPacketPipe(com.slytechs.jnet.jnetruntime.test.pipeline.TestDummyPcapPipeline.PcapHeader,
		 *      java.lang.foreign.MemorySegment)
		 */
		@Override
		public void handleRawPacketPipe(PcapHeader header, MemorySegment data) {
			outputData().handleRawPacketPipe(header, data);
		}

	}

	/**
	 * The Interface NativePcapHandler.
	 */
	interface NativePcapHandler {

		/**
		 * Handle native pcap packet.
		 *
		 * @param header the header
		 * @param Data   the data
		 * @param user   the user
		 */
		void handleNativePcapPacket(MemorySegment header, MemorySegment Data, Object user);
	}

	/**
	 * The Class PcapHeader.
	 */
	private static class PcapHeader {

		/** The caplen. */
		int caplen = 64;

		/** The wirelen. */
		int wirelen = 128;

		/** The timestamp. */
		long timestamp = System.currentTimeMillis();

		/** The header address. */
		private MemorySegment headerAddress;

		/**
		 * Instantiates a new pcap header.
		 *
		 * @param headerAddress the header address
		 */
		public PcapHeader(MemorySegment headerAddress) {
			this.headerAddress = headerAddress;
			// TODO Auto-generated constructor stub
		}

		/**
		 * To string.
		 *
		 * @return the string
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
		 * Address.
		 *
		 * @return the memory segment
		 */
		public MemorySegment address() {
			return headerAddress;
		}

	}

	/**
	 * The Class PacketRef.
	 */
	private static class PacketRef {

		/** The pcap header. */
		private MemorySegment pcapHeader;

		/** The packet data. */
		private MemorySegment packetData;

		/**
		 * Instantiates a new packet ref.
		 *
		 * @param pcapHeader the pcap header
		 * @param packetData the packet data
		 */
		public PacketRef(MemorySegment pcapHeader, MemorySegment packetData) {
			this.pcapHeader = pcapHeader;
			this.packetData = packetData;
		}

		/**
		 * Pcap header.
		 *
		 * @return the memory segment
		 */
		public MemorySegment pcapHeader() {
			return this.pcapHeader;
		}

		/**
		 * Data.
		 *
		 * @return the memory segment
		 */
		public MemorySegment data() {
			return this.packetData;
		}

		/**
		 * To string.
		 *
		 * @return the string
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "PacketRef [pcapHeader=" + pcapHeader + ", packetData=" + packetData + "]";
		}

	}

	/**
	 * The Class NetPcap.
	 */
	private static class NetPcap {

		/** The device. */
		private String device;

		/** The arena. */
		private final Arena arena;

		/**
		 * Instantiates a new net pcap.
		 *
		 * @param device the device
		 */
		public NetPcap(String device) {
			this.device = device;
			this.arena = Arena.ofAuto();
		}

		/**
		 * Gets the device name.
		 *
		 * @return the device name
		 */
		public String getDeviceName() {
			return device;
		}

		/**
		 * Creates the.
		 *
		 * @param device the device
		 * @return the net pcap
		 */
		public static NetPcap create(String device) {
			return new NetPcap(device);
		}

		/**
		 * Activate.
		 *
		 * @return the net pcap
		 */
		NetPcap activate() {
			return this;
		}

		/**
		 * Loop.
		 *
		 * @param <T>     the generic type
		 * @param handler the handler
		 * @param opaque  the opaque
		 * @return the int
		 */
		public <T> int loop(NativePcapHandler handler, T opaque) {
			return 0;
		}

		/**
		 * Next ex.
		 *
		 * @return the packet ref
		 */
		public PacketRef nextEx() {
			return new PacketRef(arena.allocate(24), arena.allocate(128));
		}

	}

	/**
	 * The Class PcapInput.
	 */
	private static class PcapInput
			extends AbstractInput<NativePcapHandler, RawPacketPipe, PcapInput>
			implements NativePcapHandler {

		/** The pcap. */
		private NetPcap pcap;

		/** The device name. */
		private String deviceName;

		/**
		 * Instantiates a new pcap input.
		 *
		 * @param headNode the head node
		 * @param pcap     the pcap
		 */
		public PcapInput(HeadNode<RawPacketPipe> headNode, NetPcap pcap) {
			super(headNode, "pcap/" + pcap.getDeviceName(), DataTypes.NATIVE_PCAP_HANDLER, DataTypes.RAW_PACKET_PIPE);
			this.pcap = pcap;
			this.deviceName = pcap.getDeviceName();
		}

		/**
		 * Handle native pcap packet.
		 *
		 * @param header the header
		 * @param Data   the data
		 * @param user   the user
		 * @see com.slytechs.jnet.jnetruntime.test.pipeline.TestDummyPcapPipeline.NativePcapHandler#handleNativePcapPacket(java.lang.foreign.MemorySegment,
		 *      java.lang.foreign.MemorySegment, java.lang.Object)
		 */
		@Override
		public void handleNativePcapPacket(MemorySegment header, MemorySegment Data, Object user) {
			PcapHeader hdr = new PcapHeader(header);

			outputData().handleRawPacketPipe(hdr, Data);
		}
	}

	/**
	 * The Class PacketRefCollector.
	 */
	private static class PacketRefCollector implements PacketRefHandler {

		/** The deque. */
		private Deque<PacketRef> deque = new ArrayDeque<>(100);

		/**
		 * Handle packet ref.
		 *
		 * @param pcapHeader the pcap header
		 * @param data       the data
		 * @param buffer     the buffer
		 * @see com.slytechs.jnet.jnetruntime.test.pipeline.TestDummyPcapPipeline.PacketRefHandler#handlePacketRef(PcapHeader,
		 *      MemorySegment, ByteBuffer)
		 */
		@Override
		public void handlePacketRef(PcapHeader pcapHeader, MemorySegment data, ByteBuffer buffer) {
			put(new PacketRef(pcapHeader.address(), data));
		}

		/**
		 * Checks if is empty.
		 *
		 * @return true, if is empty
		 */
		public boolean isEmpty() {
			return deque.isEmpty();
		}

		/**
		 * Gets the.
		 *
		 * @return the packet ref
		 */
		public PacketRef get() {
			return deque.getFirst();
		}

		/**
		 * Put.
		 *
		 * @param pkt the pkt
		 */
		public void put(PacketRef pkt) {
			deque.push(pkt);
		}
	}

	/**
	 * The Class PcapNextExEntryPoint.
	 */
	private static class PcapNextExEntryPoint
			extends AbstractEntryPoint<NativePcapHandler> {

		/** The collector. */
		private final PacketRefCollector collector;

		/** The pcap. */
		private final NetPcap pcap;

		/**
		 * Instantiates a new pcap next ex entry point.
		 *
		 * @param input     the input
		 * @param id        the id
		 * @param pcap      the pcap
		 * @param collector the collector
		 */
		public PcapNextExEntryPoint(
				InputTransformer<NativePcapHandler> input,
				String id,
				NetPcap pcap,
				PacketRefCollector collector) {
			super((AbstractInput<NativePcapHandler, ?, ?>) input, id);
			this.pcap = pcap;
			this.collector = collector;
		}

		/**
		 * Next ex.
		 *
		 * @return the packet ref
		 */
		public PacketRef nextEx() {
			if (collector.isEmpty()) {
				callPcapNextEx();
			}

			assert !collector.isEmpty();

			return collector.get();
		}

		/**
		 * Generate input packet.
		 */
		private void callPcapNextEx() {
			var packetRef = pcap.nextEx();

			NativePcapHandler input = data();
			assert input != null;

			input.handleNativePcapPacket(packetRef.pcapHeader(), packetRef.data(), null);
		}
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws NotFound the not found
	 */
	public static void main(String[] args) throws NotFound {

		NetPcap pcap = NetPcap.create("en0");
		String deviceName = pcap.getDeviceName();
		String id = deviceName;

		var pipeline = new PacketRefPipeline();
		var packetRefCollector = new PacketRefCollector();

		var processedOutput = pipeline.addOutput(PRefOutput::new);

//		processedOutput.createEndPoint("dispatcher");

		processedOutput.createEndPoint("collector")
				.priority(HasPriority.MAX_PRIORITY_VALUE)
				.data(packetRefCollector);

//		System.out.println(pipeline);

//		pipeline.addProcessor(2, ToUppercaseProcessor::new);
//
//		pipeline.addProcessor(1, ToLowercaseProcessor::new);

		PcapInput pcapInput = pipeline.addInput(id, pcap, PcapInput::new);
//		EntryPoint<NativePcapHandler> loopEntryPoint = pcapInput.createEntryPoint("loop");
		PcapNextExEntryPoint nextExEntryPoint = pcapInput.createEntryPoint(
				"nextEx", // id
				pcap, // NetPcap
				packetRefCollector, // Output
				PcapNextExEntryPoint::new);

		System.out.println(pipeline);
		System.out.println();

		pcap.activate();
//		int packetCount = pcap.loop(loopEntryPoint.inputData(), "User message");

		PacketRef packetRef = nextExEntryPoint.nextEx();

		System.out.println(packetRef);
	}

}
