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
 * Annotation to specify DataType lookup classes for pipeline components.
 * 
 * <p>
 * This annotation is used to define the DataType classes that should be
 * considered when looking up data types for pipeline processors or
 * transformers. It can be applied to methods or types (classes/interfaces) to
 * provide context for data type resolution within the pipeline framework.
 * </p>
 *
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>
 * {@code
 * @ATypeLookup({ CustomDataTypes.class,
 * 		StandardDataTypes.class })
 * public class MyPipelineProcessor {
 * 	// Processor implementation
 * }
 * }
 * </pre>
 *
 * <p>
 * In this example, the annotation specifies that the CustomDataTypes and
 * StandardDataTypes classes should be used for DataType lookups related to this
 * processor.
 * </p>
 *
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 */
@Retention(RUNTIME)
@Target({ METHOD,
		TYPE })
public @interface ATypeLookup {

	/**
	 * Specifies the DataType classes to be used for type lookup.
	 * 
	 * <p>
	 * These classes should extend or implement DataType and will be used by the
	 * pipeline framework to resolve data types for annotated elements.
	 * </p>
	 *
	 * @return An array of Class objects extending DataType
	 */
	Class<? extends DataType>[] value() default {};
}