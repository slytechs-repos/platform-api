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

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import com.slytechs.jnet.jnetruntime.pipeline.PipelineNode.NamedNodeFactory;
import com.slytechs.jnet.jnetruntime.pipeline.PipelineNode.NodeFactory;
import com.slytechs.jnet.jnetruntime.util.HasName;
import com.slytechs.jnet.jnetruntime.util.Id;
import com.slytechs.jnet.jnetruntime.util.Registration;

/**
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 *
 */
public interface Pipeline<T, T_BASE extends Pipeline<T, T_BASE>> extends HasName {

	interface Head<T, T_BASE extends Head<T, T_BASE>> extends PipelineNode<T, T_BASE> {

		default <T_IN extends Transformer<T_IN, T, ?>> T_IN addInput(T_IN input, Id id) {
			return addInput(() -> input, id);
		}

		<T_IN extends Transformer<T_IN, T, ?>> T_IN addInput(Supplier<T_IN> input, Id id);

		<T_IN extends Transformer<T_IN, T, ?>> T_IN getInput(Id id);

		default <T_IN extends Transformer<T_IN, T, ?>> Registration registerInput(T_IN input, Id id) {
			return registerInput(() -> input, id);
		}

		<T_IN extends Transformer<T_IN, T, ?>> Registration registerInput(Supplier<T_IN> input, Id id);
	}

	interface Tail<T, T_BASE extends Tail<T, T_BASE>> extends PipelineNode<T, T_BASE> {
		default <T_OUT extends Transformer<T_OUT, T, ?>> T_OUT addOutput(T_OUT output, Id id) {
			return addOutput(() -> output, id);
		}

		<T_OUT extends Transformer<T_OUT, T, ?>> T_OUT addOutput(Supplier<T_OUT> output, Id id);

		default <T_OUT extends Transformer<T_OUT, T, ?>> Registration registerOutput(T_OUT output, Id id) {
			return registerOutput(() -> output, id);
		}

		<T_OUT extends Transformer<T_OUT, T, ?>> Registration registerOutput(Supplier<T_OUT> output, Id id);
	}

	static int SOURCE_NODE_PRIORITY = -1;
	static int SINK_NODE_PRIORITY = Integer.MAX_VALUE;

	Head<T, ?> head();

	Tail<T, ?> tail();

	T_BASE enable(boolean b);

	default T_BASE enable(BooleanSupplier b) {
		return enable(b.getAsBoolean());
	}

	boolean isEnabled();

	DataType dataType();

	T_BASE addNode(int priority, PipelineNode<T, ?> node);

	default <T_NODE extends PipelineNode<T, T_NODE>> T_BASE addNode(int priority,
			NodeFactory<T, T_NODE> factory) {
		return addNode(priority, factory.newInstance(this, priority));
	}

	default <T_NODE extends PipelineNode<T, T_NODE>> T_BASE addNamedNode(int priority, String name,
			NamedNodeFactory<T, T_NODE> factory) {
		return addNode(priority, factory.newInstance(this, priority, name));
	}

	Registration registerNode(int priority, PipelineNode<T, ?> node);
}
