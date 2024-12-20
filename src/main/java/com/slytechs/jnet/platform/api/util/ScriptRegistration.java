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
package com.slytechs.jnet.platform.api.util;

import java.util.OptionalInt;

/**
 * A {@code ScriptRegistration} associates a script source or network filter
 * expression with a {@link Registration} instance. It extends the functionality
 * of a registration by including the script source code used during
 * registration. This is useful for situations where the script or filter
 * expression needs to be retrieved after registration, such as for logging,
 * debugging, or re-registration purposes.
 *
 * <p>
 * This class implements the {@link Registration} interface, and delegates the
 * {@code unregister} operation to the underlying {@code Registration} instance.
 *
 * @author Mark Bednarczyk
 */
public interface ScriptRegistration extends Registration {

	static ScriptRegistration newInstance(String script, int scriptId, Registration delegate) {
		return new ScriptRegistration() {

			@Override
			public void unregister() {
				delegate.unregister();
			}

			@Override
			public String getScriptSource() {
				return script;
			}

			@Override
			public OptionalInt scriptId() {
				return OptionalInt.of(scriptId);
			}
		};
	}

	String getScriptSource();

	default OptionalInt scriptId() {
		return OptionalInt.empty();
	}

}
