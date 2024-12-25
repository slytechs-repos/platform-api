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
public interface ThrowableConsumer<T> {

	static <T> ThrowableConsumer<T> of(ThrowableConsumer<T> before) {
		return t -> before.acceptOrThrow(t);
	}

	static <T> CheckedConsumer<T, ?> ofChecked(CheckedConsumer<T, ?> before) {
		return t -> before.acceptOrThrow(t);
	}

	static <T> ThrowableConsumer<T> ofUnchecked(UncheckedConsumer<T> before) {
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
				ThrowableConsumer.this.acceptOrThrow(t);

			} catch (RuntimeException e) {
				throw e;

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	default <E extends Throwable> CheckedConsumer<T, E> asChecked(Class<E> checkedClass) {
		return t -> {
			try {
				ThrowableConsumer.this.acceptOrThrow(t);

			} catch (RuntimeException e) {
				throw CheckedUtils.unwrapCheckedOrThrowRuntime(e, checkedClass);

			} catch (Exception e) {
				throw CheckedUtils.castAsCheckedOrThrowRuntime(e, checkedClass);
			}
		};
	}

	/**
	 * Accept.
	 *
	 * @param t the t
	 * @throws Exception the exception
	 */
	void acceptOrThrow(T t) throws Exception;
}
