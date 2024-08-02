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

import java.util.Objects;

/**
 * Represents a registration that can be unregistered. This interface provides a
 * stateless way to manage the unregistration of handlers or resources.
 *
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 */
public interface Registration {

	/**
	 * Unregisters this registration. Implementations should define the specific
	 * behavior for unregistration.
	 */
	void unregister();

	/**
	 * Combines this registration with another, creating a new registration that
	 * unregisters both when invoked. This method allows for chaining multiple
	 * registrations together.
	 *
	 * @param next the next registration to be chained with this one
	 * @return a new Registration that unregisters both this registration and the
	 *         next one
	 * @throws NullPointerException if the next registration is null
	 */
	default Registration andThen(Registration next) {
		Objects.requireNonNull(next, "next");

		return () -> {
			unregister();
			next.unregister();
		};
	}
}