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

/**
 * The Interface Node.
 *
 * @param <T> the generic type
 */
public interface DataNode<T> extends Reconfigurable {

	/**
	 * Priority.
	 *
	 * @return the int
	 */
	int priority();

	/**
	 * Checks if is enabled.
	 *
	 * @return true, if is enabled
	 */
	boolean isEnabled();

	/**
	 * Enable.
	 *
	 * @param b the b
	 */
	void enable(boolean b);

	/**
	 * Data.
	 *
	 * @return the t
	 */
	T data();
	
	DataType dataType();

	/**
	 * Next node.
	 *
	 * @return the node
	 */
	<N extends DataNode<T>> N nextNode();

}