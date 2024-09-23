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
import java.util.List;
import java.util.function.Supplier;

import com.slytechs.jnet.jnetruntime.pipeline.DataProcessor.ProcessorFactory;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.PipelineInput;
import com.slytechs.jnet.jnetruntime.util.Id;
import com.slytechs.jnet.jnetruntime.util.Registration;

/**
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 *
 */
public class AbstractPipeline<T, T_PIPE extends Pipeline<T, T_PIPE>>
		extends AbstractComponent<T_PIPE>
		implements Pipeline<T, T_PIPE> {

	private static final class HeadNode<T>
			extends AbstractProcessor<T, HeadNode<T>>
			implements Head<T, HeadNode<T>> {

		private final List<AbstractTransformer<?, T, ?>> inputList = new ArrayList<>();

		/**
		 * @param priority
		 * @param name
		 * @param type
		 */
		public HeadNode(Pipeline<T, ?> parent, String name, DataType type) {
			super(parent, Pipeline.SOURCE_NODE_PRIORITY, name, type);

			enable(true);
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.pipeline2.PipelineMockup.Head#addInput(java.util.function.Supplier,
		 *      com.slytechs.jnet.jnetruntime.util.Id)
		 */
		@Override
		public <T_IN extends DataTransformer<T_IN, T, ?>> T_IN addInputGet(Supplier<T_IN> input, Id id) {
			throw new UnsupportedOperationException("not implemented yet");
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.pipeline2.PipelineMockup.Head#getInput(com.slytechs.jnet.jnetruntime.util.Id)
		 */
		@Override
		public <T_IN extends DataTransformer<T_IN, T, ?>> T_IN getInput(Id id) {
			throw new UnsupportedOperationException("not implemented yet");
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.pipeline.AbstractProcessor#nextProcessor(com.slytechs.jnet.jnetruntime.pipeline.AbstractProcessor)
		 */
		@Override
		void nextProcessor(AbstractProcessor<T, ?> next) {
			super.nextProcessor(next);

			T out = next == null
					? null
					: next.data();

			inputList.forEach(t -> t.output(out));
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.pipeline2.PipelineMockup.Head#registerInput(java.util.function.Supplier,
		 *      com.slytechs.jnet.jnetruntime.util.Id)
		 */
		@Override
		public <T_IN extends DataTransformer<T_IN, T, ?>> Registration registerInputGet(Supplier<T_IN> input, Id id) {
			throw new UnsupportedOperationException("not implemented yet");
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.pipeline.Pipeline.Head#addInput(com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.PipelineInput)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public Registration addInput(PipelineInput<?> input) {
			if (!(input instanceof AbstractTransformer transformer))
				throw new IllegalArgumentException("unsupported input type [%s]"
						.formatted(input));

			inputList.add(transformer);

			return () -> {
				transformer.enable(false);

				inputList.remove(input);
			};
		}

	}

	private static final class TailNode<T>
			extends AbstractProcessor<T, TailNode<T>>
			implements Tail<T, TailNode<T>> {

		/**
		 * @param priority
		 * @param name
		 * @param type
		 */
		public TailNode(Pipeline<T, ?> parent, String name, DataType type) {
			super(parent, Pipeline.SINK_NODE_PRIORITY, name, type);

			enable(true);
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.pipeline2.PipelineMockup.Tail#registerOutput(java.util.function.Supplier,
		 *      com.slytechs.jnet.jnetruntime.util.Id)
		 */
		@Override
		public <T_OUT extends DataTransformer<T_OUT, T, ?>> Registration registerOutputGet(Supplier<T_OUT> output,
				Id id) {
			throw new UnsupportedOperationException("not implemented yet");
		}

	}

	private final DataType dataType;
	private final HeadNode<T> head;
	private final TailNode<T> tail;

	private final List<AbstractProcessor<T, ?>> initializedList = new ArrayList<>();
	private final List<AbstractProcessor<T, ?>> activeList = new ArrayList<>();

	public AbstractPipeline(String name, DataType dataType) {
		super(name);

		this.dataType = dataType;
		this.head = new HeadNode<>(this, "%s-head".formatted(name), dataType);
		this.tail = new TailNode<>(this, "%s-tail".formatted(name), dataType);

		initializedList.add(head);
		initializedList.add(tail);
		Collections.sort(initializedList);
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Pipeline#addProcessor(int,
	 *      java.lang.String,
	 *      com.slytechs.jnet.jnetruntime.pipeline.DataProcessor.ProcessorFactory)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T_PROC extends DataProcessor<T, T_PROC>> T_PROC addProcessor(int priority, String name,
			ProcessorFactory<T, T_PROC> factory) {
		T_PROC processor = factory.newInstance(this, priority, name);

		registerProcessor((AbstractProcessor<T, ?>) processor);

		return processor;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.PipelineMockup.Pipeline#dataType()
	 */
	@Override
	public DataType dataType() {
		return dataType;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.PipelineMockup.Pipeline#head()
	 */
	@Override
	public synchronized Head<T, ?> head() {
		return head;
	}

	synchronized void linkProcessor(AbstractProcessor<T, ?> processor) {
		if (activeList.contains(processor))
			throw new IllegalStateException("processor [%s] already active in channel [%s]"
					.formatted(processor.name(), name()));

		activeList.add(processor);
		Collections.sort(activeList);
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Pipeline#newInputBuilder(com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.PipelineInput.Factory)
	 */
	@Override
	public <T_DATA, T_BUILDER extends PipelineInput<T_DATA>> T_BUILDER newInputBuilder(
			PipelineInput.Factory<T_DATA, T_BUILDER> factory) {
		T_BUILDER b = factory.newInputBuilder();

		return b;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Pipeline#newInputData(java.lang.Object,
	 *      com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.PipelineInput.Factory1Arg)
	 */
	@Override
	public <T_DATA, T_ARG1> T_DATA newInputData(T_ARG1 arg1, PipelineInput.Factory1Arg<T_DATA, T_ARG1> factory) {
		PipelineInput<T_DATA> b = factory.newInputBuilder(arg1);

		return b.inputData();
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Pipeline#newInputData(java.lang.Object,
	 *      java.lang.Object,
	 *      com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.PipelineInput.Factory1Arg)
	 */
	@Override
	public <T_DATA, T_ARG1, T_ARG2> T_DATA newInputData(T_ARG1 arg1, T_ARG2 arg2,
			PipelineInput.Factory2Args<T_DATA, T_ARG1, T_ARG2> factory) {
		PipelineInput<T_DATA> b = factory.newInputBuilder(arg1, arg2);

		return b.inputData();
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.AbstractComponent#onEnable(boolean)
	 */
	@Override
	protected void onEnable(boolean newValue) {

		initializedList.stream()
				.filter(PipeComponent::isEnabled)
				.forEach(activeList::add);

		relinkActiveNodes();
	}

	synchronized void onNodeEnable(DataProcessor<T, ?> processor) {
		if (!(processor instanceof AbstractProcessor<T, ?> aprocessor))
			throw new IllegalStateException("invalid processor implementation [%s]"
					.formatted(processor.name()));

		boolean b = aprocessor.isEnabled();

		if (b == activeList.contains(aprocessor))
			return;

		if (b)
			linkProcessor(aprocessor);
		else
			unlinkProcessor(aprocessor);

		// List modified, need to relink nodes
		Collections.sort(activeList);
		relinkActiveNodes();
	}

	private Registration registerProcessor(AbstractProcessor<T, ?> processor) {
		if (initializedList.contains(processor))
			throw new IllegalStateException("processor [%s] already added to pipeline [%s]"
					.formatted(processor.name(), this.name()));

		initializedList.add(processor);

		Registration r = () -> unregisterProcessor(processor);
		processor.setRegistration(r);

		return r;
	}

	synchronized void relinkActiveNodes() {
		AbstractProcessor<T, ?> prev = new AbstractProcessor.Dummy<>(dataType());

		for (AbstractProcessor<T, ?> activeProcessor : activeList) {
			prev.nextProcessor(activeProcessor);

			prev = activeProcessor;
		}
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.PipelineMockup.Pipeline#tail()
	 */
	@Override
	public synchronized Tail<T, ?> tail() {
		return tail;
	}

	synchronized void unlinkProcessor(AbstractProcessor<T, ?> processor) {
		processor.nextProcessor(null);
		activeList.remove(processor);

		relinkActiveNodes();
	}

	void unregisterProcessor(AbstractProcessor<T, ?> processor) {
		if (!initializedList.contains(processor) || processor.registration().isEmpty())
			throw new IllegalStateException("Processor [%s] is not registered"
					.formatted(name()));

		processor.enable(false);
		initializedList.remove(processor);

		// Reset processor registration
		processor.setRegistration(null);
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Pipeline#addInput(com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.PipelineInput)
	 */
	@Override
	public Registration addInput(PipelineInput<?> input) {
		return head.addInput(input);
	}

}
