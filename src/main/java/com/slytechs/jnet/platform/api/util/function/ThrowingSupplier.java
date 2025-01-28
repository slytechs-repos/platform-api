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

import java.util.function.Supplier;

/**
 * A supplier that may throw an unchecked exception.
 *
 * @param <T> the type of value supplied
 */
@FunctionalInterface
public interface ThrowingSupplier<T> {

	static <T> Supplier<T> lift(ThrowingSupplier<T> supplier) {
		return () -> {
			try {
				return supplier.get();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	static <T> ThrowingSupplier<T> of(ThrowingSupplier<T> supplier) {
		return supplier;
	}

	T get() throws Exception;

}