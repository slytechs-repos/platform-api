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

/**
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 *
 */
public interface PipeComponent<T_BASE extends PipeComponent<T_BASE>> extends HasName, HasRegistration {

	public enum NodeState {
		INITIALIZED,
		ACTIVE,
		BYPASSED
	}

	T_BASE enable(boolean b);

	T_BASE bypass(boolean b);

	boolean isEnabled();

	boolean isBypassed();

	T_BASE name(String newName);

	T_BASE enable(BooleanSupplier b);

	T_BASE bypass(BooleanSupplier b);

}
