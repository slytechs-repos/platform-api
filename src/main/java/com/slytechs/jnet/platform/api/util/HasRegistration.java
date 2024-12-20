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

/**
 * Defines an interface for objects that may have an associated Registration.
 * 
 * <p>
 * This interface provides a method to retrieve an Optional Registration object,
 * allowing implementations to indicate whether they have a registration and to
 * provide access to it if it exists.
 * </p>
 *
 * @author Mark Bednarczyk
 */
public interface HasRegistration {

	/**
	 * Returns an Optional containing the Registration associated with this object,
	 * if one exists.
	 *
	 * @return an Optional<Registration> which is empty if no registration exists,
	 *         or contains the Registration if one is associated with this object
	 */
	Registration registration();
}