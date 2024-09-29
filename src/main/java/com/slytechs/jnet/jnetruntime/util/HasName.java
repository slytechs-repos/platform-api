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
 * Defines an interface for objects that have a name attribute.
 * 
 * <p>
 * This interface is typically used to provide a common method for retrieving
 * the name of an object, allowing for consistent naming conventions and
 * identification across different implementations.
 * </p>
 *
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 */
public interface HasName {

	/**
	 * Returns the name of this object.
	 *
	 * @return a String representing the name of the object
	 */
	String name();
}