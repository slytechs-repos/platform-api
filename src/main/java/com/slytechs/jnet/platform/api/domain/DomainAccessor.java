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
package com.slytechs.jnet.platform.api.domain;

/**
 * 
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public interface DomainAccessor {
	
	/**
	 * Resolve a domain attribute (field/stat/metadata) by name or path. e.g.
	 * "ethernet.dst" or "packet.frameNo" or "stats.errorCount"
	 *
	 * @param attributeName The name or path to the desired attribute.
	 * @param context       An optional context object (e.g., the current Packet).
	 * @return The resolved value (could be int, String, byte[], etc.) or null if
	 *         not found.
	 */
	Object resolve(String attributeName, Object context);

}
