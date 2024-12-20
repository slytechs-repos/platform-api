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

import java.util.function.Supplier;

public interface UncheckedSupplier<T> extends Supplier<T> {

	static <T> UncheckedSupplier<T> of(Supplier<T> before) {
		return () -> before.get();
	}

	static <T> UncheckedSupplier<T> of(UncheckedSupplier<T> before) {
		return before;
	}

	static <T, E extends Throwable> UncheckedSupplier<T> ofChecked(CheckedSupplier<T, E> before,
			Class<E> checkedClass) {

		return () -> {
			try {
				return before.getOrThrow();

			} catch (RuntimeException e) {
				throw e;

			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		};
	}

	static <T> UncheckedSupplier<T> ofThrowable(ThrowableSupplier<T> before) {
		return () -> {
			try {
				return before.getOrThrow();

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
				return UncheckedSupplier.this.get();

			} catch (RuntimeException e) {
				throw CheckedUtils.castAsCheckedOrThrowRuntime(e, checkedClass);
			}
		};
	}

	default ThrowableSupplier<T> asThrowable() {
		return () -> {
			try {
				return UncheckedSupplier.this.get();

			} catch (RuntimeException e) {
				throw CheckedUtils.unwrapException(e);
			}
		};
	}

	@Override
	T get();

}