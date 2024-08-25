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

import java.util.List;
import java.util.Optional;

import com.slytechs.jnet.jnetruntime.util.Reconfigurable;

/**
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 */
public interface Pipeline<T_BASE extends Pipeline<T_BASE>>
		extends Reconfigurable {

	<D> Optional<DataChannel<D>> findChannel(D dataReceiver, DataType type);

	List<DataChannel<?>> listChannels();

	<D> List<DataChannel<D>> listChannelsOfType(Class<D> dataClass);

	<P extends DataProcessor<P, T>, T> Optional<P> findProcessor(Class<P> processorType);

	/**
	 * Find input mapping.
	 *
	 * @param <T_IN> the generic type
	 * @param type   the type
	 * @return the optional
	 */
	<T_IN> Optional<T_IN> findInputMapping(DataType type);

	/**
	 * Find output mapping.
	 *
	 * @param <T_OUT> the generic type
	 * @param type    the type
	 * @return the optional
	 */
	<T_OUT> Optional<T_OUT> findOutputMapping(DataType type);

}
