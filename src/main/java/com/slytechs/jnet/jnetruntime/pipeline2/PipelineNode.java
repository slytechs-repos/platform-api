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
package com.slytechs.jnet.jnetruntime.pipeline2;

import java.util.function.BooleanSupplier;

import com.slytechs.jnet.jnetruntime.pipeline.DataType;
import com.slytechs.jnet.jnetruntime.util.HasName;

public interface PipelineNode<T, T_BASE extends PipelineNode<T, T_BASE>> extends HasName,
		Comparable<PipelineNode<?, ?>> {

	interface NodeFactory<T, T_NODE extends PipelineNode<T, T_NODE>> {

		T_NODE newInstance(Pipeline<T, ?> parent, int priority);
	}

	interface NamedNodeFactory<T, T_NODE extends PipelineNode<T, T_NODE>> {
		T_NODE newInstance(Pipeline<T, ?> parent, int priority, String name);
	}

	PipelineNode<T, T_BASE> nextNode();

	PipelineNode<T, T_BASE> enable(boolean b);

	default PipelineNode<T, T_BASE> enable(BooleanSupplier b) {
		return enable(b.getAsBoolean());
	}

	T getData();

	DataType getDataType();

	int priority();

	T_BASE priority(int newPriority);

	boolean isEnabled();

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	default int compareTo(PipelineNode<?, ?> o) {
		return this.priority() - o.priority();
	}

}