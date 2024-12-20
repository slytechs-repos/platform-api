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
package com.slytechs.jnet.platform.api.internal.util.function;

public interface ThrowableSupplier<T> {

	static <T> ThrowableSupplier<T> ofUnchecked(UncheckedSupplier<T> before) {
		return () -> {
			try {
				return before.get();
			} catch (RuntimeException e) {
				throw CheckedUtils.unwrapException(e);
			}
		};
	}

	static <T, E extends Throwable> ThrowableSupplier<T> ofChecked(CheckedSupplier<T, E> before) {
		return before.asThrowable();
	}

	default UncheckedSupplier<T> asUnchecked() {

		return () -> {
			try {
				return ThrowableSupplier.this.getOrThrow();
			} catch (RuntimeException e) {
				throw e;
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		};

	}

	default <E extends Throwable> CheckedSupplier<T, E> asChecked(Class<E> checkedClass) {
		return () -> {
			try {
				return getOrThrow();
			} catch (Throwable e) {
				throw CheckedUtils.castAsCheckedOrThrowRuntime(e, checkedClass);
			}
		};
	}

	T getOrThrow() throws Throwable;

	default <E extends Throwable> T getOrThrowChecked(Class<E> checkedClass) throws E {
		try {
			return getOrThrow();
		} catch (Throwable e) {
			throw CheckedUtils.castAsCheckedOrThrowRuntime(e, checkedClass);
		}
	}
}