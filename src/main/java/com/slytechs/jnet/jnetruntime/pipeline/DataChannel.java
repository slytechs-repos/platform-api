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

import com.slytechs.jnet.jnetruntime.util.Reconfigurable;
import com.slytechs.jnet.jnetruntime.util.Registration;

/**
 * The Interface Channel.
 *
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 * @param <T> the generic type
 */
public interface DataChannel<T> extends DataNode<T>, Reconfigurable {

	/**
	 * Priority.
	 *
	 * @return the int
	 */
	@Override
	int priority();

	/**
	 * Data type.
	 *
	 * @return the data type
	 */
	DataType dataType();

	/**
	 * Adds the.
	 *
	 * @param priority the priority
	 * @param node     the node
	 * @return the registration
	 */
	Registration addNode(int priority, DataNode<T> node);

	<T_IN> Registration addInputMapper(DataMapper<T_IN, T> inputMapper);

	<T_OUT> Registration addOutputMapper(DataMapper<T, T_OUT> outputMapper);

}
