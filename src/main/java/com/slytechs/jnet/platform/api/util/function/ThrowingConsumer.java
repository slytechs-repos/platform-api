/*
 * Sly Technologies Free License
 * 
 * Copyright 2025 Sly Technologies Inc.
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
package com.slytechs.jnet.platform.api.util.function;

/**
 * A consumer that can throw checked exceptions.
 *
 * @param <T> the type of the input
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
@FunctionalInterface
public interface ThrowingConsumer<T> {
    
    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     * @throws Exception if unable to process the input
     */
    void accept(T t) throws Exception;
}