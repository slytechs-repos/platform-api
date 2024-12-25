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

import java.util.function.Consumer;

/**
 * @author Mark Bednarczyk
 *
 */
public interface UncheckedConsumer<T> extends Consumer<T> {

	static <T> UncheckedConsumer<T> of(UncheckedConsumer<T> before) {
		return before;
	}

	static <T> UncheckedConsumer<T> ofThrowable(ThrowableConsumer<T> before) {
		return t -> {
			try {
				before.acceptOrThrow(t);

			} catch (RuntimeException e) {
				throw e;

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	static <T> UncheckedConsumer<T> ofChecked(CheckedConsumer<T, ?> before) {
		return t -> {
			try {
				before.acceptOrThrow(t);

			} catch (RuntimeException e) {
				throw e;

			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		};
	}

	default <E extends Throwable> CheckedConsumer<T, E> asChecked(Class<E> checkedClass) {
		return t -> {
			try {
				UncheckedConsumer.this.accept(t);

			} catch (RuntimeException e) {
				throw CheckedUtils.unwrapCheckedOrThrowRuntime(e, checkedClass);
			}
		};
	}

	default ThrowableConsumer<T> asThrowable(Class<? extends Throwable> throwableClass) {
		return t -> {
			try {
				UncheckedConsumer.this.accept(t);

			} catch (RuntimeException e) {
				throw CheckedUtils.unwrapExceptionOrThrowRuntime(e);
			}
		};
	}

	@Override
	void accept(T t);
}
