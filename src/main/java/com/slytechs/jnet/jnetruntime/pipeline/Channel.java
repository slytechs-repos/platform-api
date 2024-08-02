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
public interface Channel<T> extends Reconfigurable {

	/**
	 * The Interface Node.
	 *
	 * @param <T> the generic type
	 */
	public interface Node<T> extends Reconfigurable {

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

		/**
		 * Next node.
		 *
		 * @return the node
		 */
		Node<T> nextNode();

	}

	/**
	 * Priority.
	 *
	 * @return the int
	 */
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
	Registration add(int priority, Node<T> node);

}
