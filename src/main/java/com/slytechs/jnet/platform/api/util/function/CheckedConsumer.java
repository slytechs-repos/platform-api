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

/**
 * The Interface CheckedConsumer.
 *
 * @param <T> the generic type
 * @author Mark Bednarczyk
 */
public interface CheckedConsumer<T, E extends Throwable> {

	static <T, E extends Throwable> CheckedConsumer<T, E> of(CheckedConsumer<T, E> before) {
		return before;
	}

	static <T, E extends Exception> CheckedConsumer<T, E> ofThrowable(ThrowableConsumer<T> before,
			Class<E> checkedClass) {
		return t -> {
			try {
				before.acceptOrThrow(t);
			} catch (RuntimeException e) {
				throw e;

			} catch (Exception e) {
				throw CheckedUtils.castAsCheckedOrThrowRuntime(e, checkedClass);
			}
		};
	}

	static <T, E extends Throwable> CheckedConsumer<T, E> ofUnchecked(UncheckedConsumer<T> before) {
		return t -> before.accept(t);
	}

	/**
	 * Wrap a consumer for checked exceptions. Any checked exception will be
	 * re-thrown as a unchecked exception and processing will stop.
	 *
	 * @param <T>             the generic type
	 * @param checkedConsumer the checked consumer
	 * @return the consumer
	 */
	default UncheckedConsumer<T> asUnchecked() {
		return t -> {
			try {
				CheckedConsumer.this.acceptOrThrow(t);

			} catch (RuntimeException e) {
				throw e;

			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		};
	}

	default ThrowableConsumer<T> asThrowable() {
		return t -> {
			try {
				CheckedConsumer.this.acceptOrThrow(t);
			} catch (RuntimeException e) {
				throw CheckedUtils.unwrapExceptionOrThrowRuntime(e);

			} catch (Throwable e) {
				throw (Exception) e;
			}
		};
	}

	/**
	 * Accept.
	 *
	 * @param t the t
	 * @throws Exception the exception
	 */
	void acceptOrThrow(T t) throws E;
}
