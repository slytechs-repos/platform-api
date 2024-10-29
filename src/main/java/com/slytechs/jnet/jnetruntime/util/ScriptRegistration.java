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
package com.slytechs.jnet.jnetruntime.util;

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
public class ScriptRegistration implements Registration {

	private final String scriptSource;
	private final Registration registration;

	/**
	 * Constructs a new {@code ScriptRegistration} with the specified script source
	 * and underlying {@link Registration}.
	 *
	 * @param scriptSource the script source code or network filter expression used
	 *                     for registration
	 * @param registration the underlying registration associated with this script
	 */
	public ScriptRegistration(String scriptSource, Registration registration) {
		this.scriptSource = scriptSource;
		this.registration = registration;
	}

	/**
	 * Returns the script source code or network filter expression associated with
	 * this registration.
	 *
	 * @return the script source code or network filter expression
	 */
	public String getScriptSource() {
		return scriptSource;
	}

	/**
	 * Unregisters the underlying registration associated with this script. This
	 * method delegates the unregistration to the underlying {@link Registration}.
	 *
	 * @see Registration#unregister()
	 */
	@Override
	public void unregister() {
		this.registration.unregister();
	}
}
