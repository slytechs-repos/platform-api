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
package com.slytechs.jnet.jnetruntime.pipeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.slytechs.jnet.jnetruntime.util.Id;
import com.slytechs.jnet.jnetruntime.util.Registration;

/**
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 *
 */
public class AbstractPipeline<T, T_BASE extends Pipeline<T, T_BASE>> implements Pipeline<T, T_BASE> {

	private static final class HeadNode<T> extends AbstractNode<T, HeadNode<T>> implements Head<T, HeadNode<T>> {

		/**
		 * @param priority
		 * @param name
		 * @param type
		 */
		public HeadNode(Pipeline<T, ?> parent, String name, DataType type) {
			super(parent, Pipeline.SOURCE_NODE_PRIORITY, name, type);
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.pipeline2.PipelineMockup.Head#addInput(java.util.function.Supplier,
		 *      com.slytechs.jnet.jnetruntime.util.Id)
		 */
		@Override
		public <T_IN extends Transformer<T_IN, T, ?>> T_IN addInput(Supplier<T_IN> input, Id id) {
			throw new UnsupportedOperationException("not implemented yet");
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.pipeline2.PipelineMockup.Head#getInput(com.slytechs.jnet.jnetruntime.util.Id)
		 */
		@Override
		public <T_IN extends Transformer<T_IN, T, ?>> T_IN getInput(Id id) {
			throw new UnsupportedOperationException("not implemented yet");
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.pipeline2.PipelineMockup.Head#registerInput(java.util.function.Supplier,
		 *      com.slytechs.jnet.jnetruntime.util.Id)
		 */
		@Override
		public <T_IN extends Transformer<T_IN, T, ?>> Registration registerInput(Supplier<T_IN> input, Id id) {
			throw new UnsupportedOperationException("not implemented yet");
		}

	}

	private static final class TailNode<T> extends AbstractNode<T, TailNode<T>> implements Tail<T, TailNode<T>> {

		/**
		 * @param priority
		 * @param name
		 * @param type
		 */
		public TailNode(Pipeline<T, ?> parent, String name, DataType type) {
			super(parent, Pipeline.SINK_NODE_PRIORITY, name, type);
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.pipeline2.PipelineMockup.Tail#addOutput(java.util.function.Supplier,
		 *      com.slytechs.jnet.jnetruntime.util.Id)
		 */
		@Override
		public <T_OUT extends Transformer<T_OUT, T, ?>> T_OUT addOutput(Supplier<T_OUT> output, Id id) {
			throw new UnsupportedOperationException("not implemented yet");
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.pipeline2.PipelineMockup.Tail#registerOutput(java.util.function.Supplier,
		 *      com.slytechs.jnet.jnetruntime.util.Id)
		 */
		@Override
		public <T_OUT extends Transformer<T_OUT, T, ?>> Registration registerOutput(Supplier<T_OUT> output, Id id) {
			throw new UnsupportedOperationException("not implemented yet");
		}

	}

	private final DataType dataType;
	private final HeadNode<T> head;
	private final TailNode<T> tail;
	private String name;
	private boolean enabled = true;
	private final List<PipelineNode<T, ?>> registeredNodeList = new ArrayList<>();
	private final List<PipelineNode<T, ?>> enabledNodeList = new ArrayList<>();
	private final List<Transformer<?, T, ?>> inputTransformerList = new ArrayList<>();
	private final List<Transformer<T, ?, ?>> outputTransformerList = new ArrayList<>();

	private final Map<DataType, Transformer<?, T, ?>> inputTransformerMap = new HashMap<>();
	private final Map<DataType, Transformer<?, T, ?>> outputTransformerMap = new HashMap<>();

	public AbstractPipeline(String name, DataType dataType) {
		this.name = name;
		this.dataType = dataType;
		this.head = new HeadNode<>(this, "%s-head".formatted(name), dataType);
		this.tail = new TailNode<>(this, "%s-tail".formatted(name), dataType);

		enabledNodeList.add(head);
		enabledNodeList.add(tail);
		Collections.sort(registeredNodeList);
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.PipelineNode#enable(boolean)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public synchronized T_BASE enable(boolean b) {
		this.enabled = b;

		return (T_BASE) this;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.PipelineNode#isEnabled()
	 */
	@Override
	public synchronized boolean isEnabled() {
		return this.enabled;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.PipelineNode#name()
	 */
	@Override
	public String name() {
		return name;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.PipelineMockup.Pipeline#dataType()
	 */
	@Override
	public DataType dataType() {
		return dataType;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.PipelineMockup.Pipeline#addNode(int,
	 *      com.slytechs.jnet.jnetruntime.pipeline2.PipelineMockup.PipelineNode)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public synchronized T_BASE addNode(int priority, PipelineNode<T, ?> node) {

		var registration = registerNode(priority, node);

		return (T_BASE) this;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.PipelineMockup.Pipeline#registerNode(int,
	 *      com.slytechs.jnet.jnetruntime.pipeline2.PipelineMockup.PipelineNode)
	 */
	@Override
	public synchronized Registration registerNode(int priority, PipelineNode<T, ?> node) {
		AbstractNode<?, ?> an = (AbstractNode<?, ?>) node;
		if (an.registration != null)
			throw new IllegalStateException("node [%s] already registered with pipe [%s]"
					.formatted(an.name(), this.name()));

		node.priority(priority);

		registeredNodeList.add(node);

		registeredNodeList.sort((a, b) -> a.priority() - b.priority());

		Registration registration = () -> {
			registeredNodeList.remove(node);
			an.registration = null;
		};

		an.registration = registration;

		return registration;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.PipelineMockup.Pipeline#head()
	 */
	@Override
	public synchronized Head<T, ?> head() {
		return head;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.PipelineMockup.Pipeline#tail()
	 */
	@Override
	public synchronized Tail<T, ?> tail() {
		return tail;
	}

	synchronized void onNodeEnable(PipelineNode<T, ?> node) {
		boolean b = node.isEnabled();

		if (b == enabledNodeList.contains(node))
			return;

		if (b)
			enabledNodeList.add(node);
		else
			enabledNodeList.remove(node);

		// List modified, need to relink nodes

		Collections.sort(enabledNodeList);
		relinkAllEnabledNodes();
	}

	@SuppressWarnings({
			"unchecked",
			"rawtypes" })
	synchronized void relinkAllEnabledNodes() {
		AbstractNode<T, ?> prev = new AbstractNode.Dummy<>(dataType());

		for (var node : enabledNodeList) {
			prev.next = (PipelineNode) node;

			prev = (AbstractNode<T, ?>) node;
		}

	}
}
