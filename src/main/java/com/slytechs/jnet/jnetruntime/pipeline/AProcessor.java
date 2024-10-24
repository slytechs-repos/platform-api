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

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation to mark and configure processor methods or classes within a
 * pipeline.
 * 
 * <p>
 * This annotation is used to designate methods or classes as processors in a
 * data processing pipeline. It provides configuration options for priority,
 * data type, enabling status, and naming. The annotation can be used by runtime
 * processing tools to set up and manage pipeline processors.
 * </p>
 *
 * <p>
 * Usage example for a method:
 * </p>
 * 
 * <pre>
 * {@code
 * @AProcessor(priority = 1, value = String.class, enable = true, name = "stringProcessor")
 * public void processString(String input) {
 * 	// Processing logic here
 * }
 * }
 * </pre>
 *
 * <p>
 * Usage example for a class:
 * </p>
 * 
 * <pre>
 * {@code
 * @AProcessor(value = Integer.class, priority = 2, name = "integerProcessor")
 * public class IntegerProcessor {
 * 	// Processor implementation here
 * }
 * }
 * </pre>
 *
 */
@Retention(RUNTIME)
@Target({ METHOD,
		TYPE })
public @interface AProcessor {

	/**
	 * Specifies whether the processor should be enabled by default.
	 *
	 * @return true if the processor should be enabled, false otherwise
	 */
	boolean enable() default true;

	/**
	 * Specifies a custom name for the processor. If not provided, the method or
	 * class name will typically be used.
	 *
	 * @return the custom name of the processor
	 */
	String name() default "";

	/**
	 * Specifies the priority of the processor in the pipeline. Lower values
	 * indicate higher priority.
	 *
	 * @return the priority of the processor
	 */
	int priority() default 0;

	/**
	 * Specifies the data type that this processor handles.
	 *
	 * @return the Class object representing the data type
	 */
	Class<?> value();
}