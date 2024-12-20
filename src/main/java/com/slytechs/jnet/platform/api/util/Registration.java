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
package com.slytechs.jnet.platform.api.util;

import java.util.Objects;

/**
 * Represents a registration that can be unregistered. This interface provides a
 * stateless way to manage the unregistration of handlers or resources.
 * 
 * <p>
 * Implementations of this interface define the specific behavior for
 * unregistration, allowing for flexible and context-specific cleanup
 * operations. Registrations are useful when dealing with resources that need to
 * be explicitly released, such as listeners or subscriptions, ensuring proper
 * lifecycle management.
 * </p>
 * 
 * @author Mark Bednarczyk
 */
public interface Registration {

	interface AutoRegistration extends Registration, AutoCloseable {

		@Override
		default void close() {
			unregister();
		}
	}

	/**
	 * A registration that throws a checked exception upon unregistration.
	 * 
	 * @param <E> the type of checked exception that might be thrown
	 */
	interface CheckedRegistration<E extends Throwable> {
		/**
		 * Unregisters the resource, potentially throwing a checked exception.
		 * 
		 * @throws E if an error occurs during unregistration
		 */
		void unregister() throws E;
	}

	/**
	 * A registration that throws a generic {@link Throwable} upon unregistration.
	 */
	interface ThrowableRegistration {
		/**
		 * Unregisters the resource, potentially throwing a {@link Throwable}.
		 * 
		 * @throws Throwable if an error occurs during unregistration
		 */
		void unregister() throws Throwable;
	}

	/**
	 * Creates a {@link Registration} from a {@link CheckedRegistration}, converting
	 * checked exceptions to runtime exceptions.
	 *
	 * <p>
	 * This method provides a way to wrap a {@link CheckedRegistration} so that it
	 * can be used as a regular {@link Registration}, ensuring that checked
	 * exceptions are handled gracefully.
	 * </p>
	 * 
	 * @param <E>        the type of checked exception thrown by the registration
	 * @param checkClass the class of the checked exception
	 * @param checked    the checked registration to wrap
	 * @return a {@link Registration} that wraps the given checked registration
	 */
	static <E extends Throwable> Registration checked(Class<E> checkClass, CheckedRegistration<E> checked) {
		return () -> {
			try {
				checked.unregister();
			} catch (RuntimeException e) {
				throw e;
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		};
	}

	/**
	 * Creates a {@link Registration} from a {@link ThrowableRegistration},
	 * converting any {@link Throwable} into a runtime exception.
	 * 
	 * <p>
	 * This method wraps a {@link ThrowableRegistration} and ensures that any thrown
	 * {@link Throwable} is rethrown as a {@link RuntimeException}.
	 * </p>
	 * 
	 * @param throwable the throwable registration to wrap
	 * @return a {@link Registration} that wraps the given throwable registration
	 */
	static Registration throwable(ThrowableRegistration throwable) {
		return () -> {
			try {
				throwable.unregister();
			} catch (RuntimeException e) {
				throw e;
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		};
	}

	/**
	 * Combines this registration with another, creating a new registration that
	 * unregisters both when invoked.
	 *
	 * <p>
	 * This method allows for chaining multiple registrations together, enabling the
	 * creation of composite unregistration operations. When the resulting
	 * registration is unregistered, it will unregister both this registration and
	 * the next one in sequence.
	 * </p>
	 *
	 * @param next the next registration to be chained with this one
	 * @return a new {@link Registration} that unregisters both this registration
	 *         and the next one when its {@code unregister()} method is called
	 * @throws NullPointerException if the next registration is null
	 */
	default Registration andThen(Registration next) throws NullPointerException {
		Objects.requireNonNull(next, "next");
		return () -> {
			unregister();
			next.unregister();
		};
	}

	/**
	 * Unregisters this registration.
	 * 
	 * <p>
	 * Implementations should define the specific behavior for unregistration, which
	 * may include releasing resources, removing event listeners, or performing
	 * other cleanup operations. This method is invoked to ensure proper cleanup
	 * when the registration is no longer needed.
	 * </p>
	 */
	void unregister();
}
