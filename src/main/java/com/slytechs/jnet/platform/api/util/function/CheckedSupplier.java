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
package com.slytechs.jnet.platform.api.util.function;

public interface CheckedSupplier<T, E extends Throwable> {

	static <T, E extends Throwable> CheckedSupplier<T, E> ofUnchecked(
			Class<E> checkedClass,
			UncheckedSupplier<T> before) {

		return () -> {
			try {
				return before.get();
			} catch (RuntimeException e) {
				throw CheckedUtils.castAsCheckedOrThrowRuntime(e, checkedClass);
			}
		};
	}

	static <T, E extends Throwable> CheckedSupplier<T, E> ofChecked(
			Class<E> checkedClass,
			ThrowableSupplier<T> before) {
		return () -> {
			try {
				return before.getOrThrow();
			} catch (RuntimeException e) {
				throw e;
			} catch (Throwable e) {
				throw CheckedUtils.castAsCheckedOrThrowRuntime(e, checkedClass);
			}
		};

	}

	default UncheckedSupplier<T> asUnchecked() {
		return () -> {
			try {
				return getOrThrow();

			} catch (RuntimeException e) {
				throw e;

			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		};
	}

	default ThrowableSupplier<T> asThrowable() {
		return () -> getOrThrow();
	}

	T getOrThrow() throws E;
}