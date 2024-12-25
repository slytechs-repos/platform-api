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
package com.slytechs.jnet.platform.api.data;

/**
 * Common interface for data processing flow control. Defines the core behavior
 * for managing how data moves through processing stages.
 * 
 * <p>
 * Controls:
 * <ul>
 * <li>Data flow direction (forward, reverse)
 * <li>Flow state (started, stopped, paused)
 * <li>Error handling and recovery
 * <li>Performance monitoring
 * </ul>
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc
 * @since 1.0.0
 * @see DataProcessor
 */
public interface DataFlow {

	boolean isTransformable();
}