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

import com.slytechs.jnet.jnetruntime.util.HasName;
import com.slytechs.jnet.jnetruntime.util.HasRegistration;

public interface DataProcessor<T, T_BASE extends DataProcessor<T, T_BASE>>
		extends HasName, HasRegistration, PipeComponent<T_BASE>, Comparable<DataProcessor<?, ?>> {

	interface DataBypassable<T, T_BASE extends DataBypassable<T, T_BASE>> {
		default boolean isBypassed() {
			return bypassData() != null;
		}

		T_BASE bypass(boolean b);

		T_BASE bypass(BooleanSupplier b);

		T bypassData();

		T_BASE bypass(T bypassData);
	}

	interface ProcessorFactory<T, T_NODE extends DataProcessor<T, T_NODE>> {
		T_NODE newInstance(Pipeline<T, ?> parent, int priority, String name);
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	default int compareTo(DataProcessor<?, ?> o) {
		return o.priority() - this.priority();
	}

	T data();

	DataType dataType();

	@Override
	boolean isEnabled();

	int priority();

	T_BASE priority(int newPriority);

}