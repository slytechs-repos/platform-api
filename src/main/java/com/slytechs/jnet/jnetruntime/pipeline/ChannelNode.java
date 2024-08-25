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
 * @param <T_BASE> the generic type
 */
public interface ChannelNode<T_BASE extends ChannelNode<T_BASE>>
		extends Reconfigurable {

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
	T_BASE enable(boolean b);

	/**
	 * Next node.
	 *
	 * @return the node
	 */
	<N_BASE extends ChannelNode<N_BASE>> N_BASE nextNode();

	/**
	 * Prev node.
	 *
	 * @param <N_BASE> the generic type
	 * @return the n base
	 */
	<N_BASE extends ChannelNode<N_BASE>> N_BASE prevNode();

}