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
public interface Pipeline<T_BASE extends Pipeline<T_BASE>> extends Processor<T_BASE>, Reconfigurable {

	/**
	 * The Interface Mapper.
	 *
	 * @param <T_IN>  the input type
	 * @param <T_OUT> the output type
	 */
	public interface DataMapper<T_IN, T_OUT> extends Reconfigurable {

		/**
		 * Input.
		 *
		 * @return the t1
		 */
		T_IN input();

		/**
		 * Input type.
		 *
		 * @return the data type
		 */
		DataType inputType();

		/**
		 * Output type.
		 *
		 * @return the data type
		 */
		DataType outputType();

		/**
		 * Output.
		 *
		 * @return the t2
		 */
		T_OUT output();

	}

	<D> Optional<Channel<D>> findChannel(D dataReceiver, DataType type);

	List<Channel<?>> listChannels();

	<D> List<Channel<D>> listChannelsOfType(Class<D> dataClass);

	<P extends Processor<P>> Optional<P> findProcessor(Class<P> processorType);

	/**
	 * Input.
	 *
	 * @param <T_IN> the generic type
	 * @param type   the type
	 * @return the t2
	 */
	<T_IN> Optional<T_IN> findInputMapping(Class<T_IN> dataClass, DataType type);

	<T_OUT> Optional<T_OUT> findOutputMapping(Class<T_OUT> dataClass, DataType type);

}
