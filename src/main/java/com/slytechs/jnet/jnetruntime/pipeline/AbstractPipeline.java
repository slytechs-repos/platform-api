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
import java.util.stream.Collectors;

import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.OutputEndPoint;
import com.slytechs.jnet.jnetruntime.util.Registration;

/**
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 *
 */
public class AbstractPipeline<T, T_PIPE extends Pipeline<T, T_PIPE>>
		extends AbstractComponent<T_PIPE>
		implements Pipeline<T, T_PIPE> {

	private final DataType dataType;
	private final HeadNode<T> head;
	private final TailNode<T> tail;

	private final List<AbstractProcessor<T, ?>> initializedList = new ArrayList<>();
	private final List<AbstractProcessor<T, ?>> activeList = new ArrayList<>();

	public AbstractPipeline(String name, DataType dataType) {
		super(name);

		this.dataType = dataType;
		this.head = new HeadNode<>(this, "head".formatted(name), dataType);
		this.tail = new TailNode<>(this, "tail".formatted(name), dataType);

		initializedList.add(head);
		initializedList.add(tail);
		Collections.sort(initializedList);
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
		if (!isEnabled())
			return; // Not enabled, nothing to do

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

	synchronized void onOutputChange(AbstractProcessor<T, ?> processor) {

	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Pipeline#registerOutput(com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.OutputEndPoint)
	 */
	@Override
	public <T_OUT> Registration registerOutput(OutputEndPoint<T_OUT> output) {
		return tail.registerOutput(output);
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

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Pipeline#registerProcessor(int,
	 *      com.slytechs.jnet.jnetruntime.pipeline.DataProcessor)
	 */
	@Override
	public Registration registerProcessor(int priority, DataProcessor<T, ? extends DataProcessor<T, ?>> processor) {
		if (!(processor instanceof AbstractProcessor<T, ?> aprocessor))
			throw new IllegalArgumentException("invalid processor implementation [%s]"
					.formatted(processor.getClass()));

		registerProcessor(aprocessor);

		return () -> unregisterProcessor(aprocessor);
	}

	private synchronized void relinkActiveNodes() {
		System.out.println(toString());

		AbstractProcessor<T, ?> prev = null;

		for (AbstractProcessor<T, ?> p : activeList) {
			if (prev != null)
				prev.nextProcessor(p);

			prev = p;
		}

		reRegisterActiveNodes();
	}

	private void reRegisterActiveNodes() {
		for (AbstractProcessor<T, ?> p : activeList.reversed()) {
			p.register();
		}
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.PipelineMockup.Pipeline#tail()
	 */
	@Override
	public synchronized Tail<T, ?> tail() {
		return tail;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		String initStr = initializedList.stream()
				.map(p -> "%s".formatted(p.name()))
				.collect(Collectors.joining("->", "[", "]"));

		String activeStr = activeList.stream()
				.map(p -> "%s".formatted(p.name()))
				.collect(Collectors.joining("->", "[", "]"));

		return ""
				+ getClass().getSimpleName()
				+ " [initializedList=" + initStr
				+ ", actveList=" + activeStr
				+ "]";
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

}
