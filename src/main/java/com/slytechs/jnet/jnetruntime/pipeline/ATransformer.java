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
 * Annotation to mark and configure transformer methods within a pipeline.
 * 
 * <p>This annotation is used to designate methods as transformers in a data processing pipeline.
 * It provides configuration options for priority, input and output types, enabling status, and naming.
 * The annotation can be used by runtime processing tools to set up and manage pipeline transformers.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * @Transformer(priority = 1, in = String.class, out = Integer.class, enable = true, name = "stringToInt")
 * public Integer transformStringToInt(String input) {
 *     return Integer.parseInt(input);
 * }
 * }
 * </pre>
 *
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface ATransformer {

    /**
     * Specifies whether the transformer should be enabled by default.
     *
     * @return true if the transformer should be enabled, false otherwise
     */
    boolean enable() default true;

    /**
     * Specifies the input type of the transformer.
     *
     * @return the Class object representing the input type
     */
    Class<?> in();

    /**
     * Specifies a custom name for the transformer.
     * If not provided, the method name will typically be used.
     *
     * @return the custom name of the transformer
     */
    String name() default "";

    /**
     * Specifies the output type of the transformer.
     *
     * @return the Class object representing the output type
     */
    Class<?> out();

    /**
     * Specifies the priority of the transformer in the pipeline.
     * Lower values indicate higher priority.
     *
     * @return the priority of the transformer
     */
    int priority() default 0;
}