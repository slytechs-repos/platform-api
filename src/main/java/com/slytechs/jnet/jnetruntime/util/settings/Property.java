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
package com.slytechs.jnet.jnetruntime.util.settings;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.slytechs.jnet.jnetruntime.util.Registration;

/**
 * A sealed abstract base class for type-safe property handling within the
 * settings framework. This class provides a comprehensive foundation for
 * managing configuration properties with support for value validation, change
 * notifications, optional handling, and system property integration.
 * 
 * <p>
 * The Property class is sealed and can only be extended by specific property
 * types defined in the permits clause. Each property type provides type-safe
 * operations for its specific data type while inheriting common functionality
 * from this base class.
 * </p>
 * 
 * <p>
 * Key features include:
 * </p>
 * <ul>
 * <li>Type-safe value handling with generic type parameters</li>
 * <li>Optional value support with null safety</li>
 * <li>Change notification through action listeners</li>
 * <li>System property integration</li>
 * <li>Value formatting and string conversion</li>
 * <li>Functional programming support with map/filter operations</li>
 * </ul>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>
 * IntProperty port = Property.of("server.port", 8080, IntProperty::new)
 * 		.on((newValue, source) -> System.out.println("Port changed to: " + newValue))
 * 		.setFormat("Port: %d");
 * 
 * port.setValue(9090);
 * port.systemProperty(); // Load from system properties if available
 * 
 * OptionalInt optPort = port.toIntOptional();
 * </pre>
 *
 * @param <T>      the type of value stored in this property
 * @param <T_BASE> the specific property type extending this class
 * 
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc
 */
public sealed abstract class Property<T, T_BASE extends Property<T, T_BASE>>
		permits EmptyProperty,
		ByteProperty,
		ShortProperty,
		IntProperty,
		LongProperty,
		FloatProperty,
		DoubleProperty,
		StringProperty,
		EnumProperty,
		BooleanProperty,
		UnsignedByteProperty,
		UnsignedShortProperty,
		UnsignedIntProperty,
		ListProperty {

	/**
	 * Interface defining actions that can be performed when property values change.
	 * Actions can be chained and can filter based on the source of the change.
	 *
	 * @param <T> the type of value handled by this action
	 */
	interface Action<T> {

		/**
		 * Creates an Action that executes the given BiConsumer when property values
		 * change, with optional source filtering.
		 *
		 * @param <T>    the type of value handled by this action
		 * @param before the action to execute on property change
		 * @param source the source object to filter changes (may be null for no
		 *               filtering)
		 * @return a new Action instance
		 */
		static <T> Property.Action<T> ofAction(BiConsumer<T, Object> before, Object source) {
			return new Property.Action<T>() {
				@Override
				public boolean canFireFromSource(Object src) {
					return source == null || source != src;
				}

				@Override
				public void propertyChangeAction(String name, T oldValue, T newValue, Object src) {
					before.accept(newValue, src);
				}

				@Override
				public void propertyChangeAction(T newValue) {
					throw new UnsupportedOperationException();
				}
			};
		}

		/**
		 * Chains this action with another action to be executed after this one.
		 *
		 * @param after the action to execute after this one
		 * @return a new Action that executes both actions in sequence
		 */
		default Property.Action<T> andThen(Property.Action<T> after) {
			Property.Action<T> before = this;
			return new Property.Action<T>() {
				@Override
				public boolean canFireFromSource(Object source) {
					return before.canFireFromSource(source);
				}

				@Override
				public void propertyChangeAction(String name, T oldValue, T newValue, Object src) {
					before.propertyChangeAction(name, oldValue, newValue, src);
					after.propertyChangeAction(name, oldValue, newValue, src);
				}

				@Override
				public void propertyChangeAction(T newValue) {
					throw new UnsupportedOperationException();
				}
			};
		}

		/**
		 * Determines if the action should be executed for changes from the given
		 * source.
		 *
		 * @param source the source of the property change
		 * @return true if the action should be executed, false otherwise
		 */
		default boolean canFireFromSource(Object source) {
			return true;
		}

		/**
		 * Called when a property value changes, with just the property name and new
		 * value.
		 *
		 * @param name     the name of the property that changed
		 * @param newValue the new value of the property
		 */
		default void propertyChangeAction(String name, T newValue) {
			propertyChangeAction(newValue);
		}

		/**
		 * Called when a property value changes, with the property name, old value, and
		 * new value.
		 *
		 * @param name     the name of the property that changed
		 * @param oldValue the previous value of the property
		 * @param newValue the new value of the property
		 */
		default void propertyChangeAction(String name, T oldValue, T newValue) {
			propertyChangeAction(name, newValue);
		}

		/**
		 * Called when a property value changes, with complete change information.
		 *
		 * @param name     the name of the property that changed
		 * @param oldValue the previous value of the property
		 * @param newValue the new value of the property
		 * @param source   the source of the change
		 */
		default void propertyChangeAction(String name, T oldValue, T newValue, Object source) {
			propertyChangeAction(name, oldValue, newValue);
		}

		/**
		 * The base property change action method that must be implemented.
		 *
		 * @param newValue the new value of the property
		 */
		void propertyChangeAction(T newValue);
	}

	/**
	 * Creates an empty property with the specified name.
	 *
	 * @param <U>      the type of value the property would hold
	 * @param <P_BASE> the specific property type
	 * @param name     the name for the empty property
	 * @return a new empty property instance
	 */
	public static <U, P_BASE extends Property<U, P_BASE>> P_BASE empty(String name) {
		@SuppressWarnings("unchecked")
		P_BASE empty = (P_BASE) new EmptyProperty<>(name);
		return empty;
	}

	/**
	 * Creates a property using the provided factory function.
	 *
	 * @param <T>      the type of value the property will hold
	 * @param <T_BASE> the specific property type
	 * @param name     the name for the property
	 * @param factory  the factory function to create the property
	 * @return a new property instance
	 */
	public static <T, T_BASE extends Property<T, T_BASE>> T_BASE of(String name,
			Function<String, T_BASE> factory) {
		return factory.apply(name);
	}

	/**
	 * Creates a property with a specified name and value, automatically determining
	 * the appropriate property type based on the value's class.
	 *
	 * @param <T>      the type of value the property will hold
	 * @param <T_BASE> the specific property type
	 * @param name     the name for the property
	 * @param value    the initial value for the property
	 * @return a new property instance of the appropriate type
	 * @throws IllegalArgumentException if the value type is not supported
	 */
	public static <T, T_BASE extends Property<T, T_BASE>> T_BASE of(String name, T value) {
		@SuppressWarnings("unchecked")
		T_BASE property = (T_BASE) switch (value) {
		case null -> new EmptyProperty<>(name);
		case Integer ivalue -> new IntProperty(name, ivalue);
		case Long lvalue -> new LongProperty(name, lvalue);
		case String svalue -> new StringProperty(name, svalue);
		case Boolean bvalue -> new BooleanProperty(name, bvalue);
		default -> throw new IllegalArgumentException("unknown property type for value of " + value);
		};

		return property;
	}

	/**
	 * Creates a property using a factory function that takes both name and initial
	 * value.
	 *
	 * @param <T>      the type of value the property will hold
	 * @param <T_BASE> the specific property type
	 * @param name     the name for the property
	 * @param value    the initial value for the property
	 * @param factory  the factory function to create the property
	 * @return a new property instance
	 */
	public static <T, T_BASE extends Property<T, T_BASE>> T_BASE of(String name, T value,
			BiFunction<String, T, T_BASE> factory) {
		return factory.apply(name, value);
	}

	/**
	 * Creates a property that is initialized from a system property if available.
	 *
	 * @param <T>      the type of value the property will hold
	 * @param <T_BASE> the specific property type
	 * @param name     the name for the property (also used as system property name)
	 * @param factory  the factory function to create the property
	 * @return a new property instance initialized from system property if available
	 */
	public static <T, T_BASE extends Property<T, T_BASE>> T_BASE ofSystem(String name,
			Function<String, T_BASE> factory) {
		var property = factory.apply(name)
				.systemProperty();
		return property;
	}

	/**
	 * Creates a property with a default value that is initialized from a system
	 * property if available.
	 *
	 * @param <T>          the type of value the property will hold
	 * @param <T_BASE>     the specific property type
	 * @param name         the name for the property (also used as system property
	 *                     name)
	 * @param defaultValue the default value if no system property is found
	 * @return a new property instance
	 */
	public static <T, T_BASE extends Property<T, T_BASE>> T_BASE ofSystem(String name, T defaultValue) {
		T_BASE property = Property.<T, T_BASE>of(name, defaultValue)
				.systemProperty();
		return property;
	}

	/**
	 * Creates a property using a factory function and initializes it from a system
	 * property if available.
	 *
	 * @param <T>          the type of value the property will hold
	 * @param <T_BASE>     the specific property type
	 * @param name         the name for the property (also used as system property
	 *                     name)
	 * @param defaultValue the default value if no system property is found
	 * @param factory      the factory function to create the property
	 * @return a new property instance
	 */
	public static <T, T_BASE extends Property<T, T_BASE>> T_BASE ofSystem(String name, T defaultValue,
			BiFunction<String, T, T_BASE> factory) {
		T_BASE property = factory.apply(name, defaultValue)
				.systemProperty();
		return property;
	}

	/** Support class for managing property change notifications */
	SettingsSupport settingsSupport = new SettingsSupport();

	/** Reference to this property instance cast to its specific type */
	@SuppressWarnings("unchecked")
	private final T_BASE us = (T_BASE) this;

	/** The current value of the property */
	private T value;

	/** Format string for value representation */
	private String formatString = "%s";

	/** The name of the property */
	private final String name;

	/**
	 * Creates a new Property with the specified name and no initial value.
	 *
	 * @param name the name of the property (must not be null)
	 * @throws NullPointerException if name is null
	 */
	protected Property(String name) {
		Objects.requireNonNull(name);
		this.name = name;
	}

	/**
	 * Creates a new Property with the specified name and initial value.
	 *
	 * @param name  the name of the property (must not be null)
	 * @param value the initial value for the property
	 * @throws NullPointerException if name is null
	 */
	protected Property(String name, T value) {
		Objects.requireNonNull(name);
		this.name = name;
		this.value = value;
	}

	/**
	 * Validates that a value is within acceptable bounds for this property type.
	 * Base implementation performs no validation. Subclasses should override this
	 * method to implement type-specific validation.
	 *
	 * @param value the value to validate
	 * @throws IllegalArgumentException if the value is outside acceptable bounds
	 */
	protected void checkBounds(T value) throws IllegalArgumentException {
		// No bounds check by default, override to implement a check
	}

	/**
	 * Clears the current value of the property, setting it to null. Fires a
	 * property change event if the property had a value.
	 *
	 * @return this property instance for method chaining
	 */
	public T_BASE clear() {
		if (value != null)
			settingsSupport.fireValueChange(name(), null, this.value, "<clear>");

		this.value = null;

		return us;
	}

	/**
	 * Gets the current format string used for value representation.
	 *
	 * @return the current format string
	 */
	public String getFormat() {
		return formatString;
	}

	/**
	 * Attempts to load a value from system properties, using the provided default
	 * if no system property is found.
	 *
	 * @param defaultValue the default value to use if no system property exists
	 * @return this property instance for method chaining
	 */
	public T_BASE getSystemProperty(T defaultValue) {
		String newValue = System.getProperty(name());
		if (newValue != null)
			return parseValue(newValue);
		else if (isEmpty())
			return setValue(defaultValue);

		return us;
	}

	/**
	 * Gets the current value of the property.
	 *
	 * @return the current value
	 * @throws IllegalStateException if the property has no value
	 */
	public T getValue() {
		if (isEmpty())
			throw noValueException();

		return value;
	}

	/**
	 * Executes the provided action if the property has no value.
	 *
	 * @param action the action to execute if the property is empty
	 * @return this property instance for method chaining
	 */
	public T_BASE ifEmpty(Runnable action) {
		if (value == null)
			action.run();

		return us;
	}

	/**
	 * Executes the provided action if the property has a value.
	 *
	 * @param action the action to execute with the current value
	 */
	public void ifPresent(Consumer<? super T> action) {
		if (value != null)
			action.accept(value);
	}

	/**
	 * Checks if the property has no value.
	 *
	 * @return true if the property has no value, false otherwise
	 */
	public boolean isEmpty() {
		return value == null;
	}

	/**
	 * Checks if the property has a value.
	 *
	 * @return true if the property has a value, false otherwise
	 */
	public boolean isPresent() {
		return value != null;
	}

	/**
	 * Maps this property's value to a new property of a different type using the
	 * provided transformation function.
	 *
	 * @param <U>      the type of the new property value
	 * @param <P_BASE> the type of the new property
	 * @param action   the transformation function to apply to the value
	 * @return a new property containing the transformed value, or an empty property
	 *         if this property is empty
	 */
	public <U, P_BASE extends Property<U, P_BASE>> P_BASE map(Function<? super T, ? extends U> action) {
		if (isPresent()) {
			U mappedValue = action.apply(value);
			return Property.of(name(), mappedValue);
		}
		return empty(name());
	}

	/**
	 * Creates a new StringProperty containing this property's value formatted
	 * according to the current format string.
	 *
	 * @return a new StringProperty containing the formatted value
	 */
	public StringProperty mapToFormattedString() {
		return new StringProperty(name(), toFormattedValue());
	}

	/**
	 * Returns the name of this property.
	 *
	 * @return the property name
	 */
	public final String name() {
		return this.name;
	}

	/**
	 * Creates a standard exception for when a value is requested but none exists.
	 *
	 * @return an IllegalStateException with appropriate message
	 */
	protected final IllegalStateException noValueException() {
		return new IllegalStateException("no value");
	}

	/**
	 * Registers an action to be executed when this property's value changes.
	 *
	 * @param action the action to execute on value changes
	 * @return this property instance for method chaining
	 */
	public T_BASE on(Property.Action<T> action) {
		if (isPresent())
			settingsSupport.fireValueChange(action, name, value, value, null);

		settingsSupport.addAction(name(), action);
		return us;
	}

	/**
	 * Registers an action to be executed when this property's value changes and
	 * provides registration tracking through a consumer.
	 *
	 * @param action       the action to execute on value changes
	 * @param registration consumer to receive the registration object
	 * @return this property instance for method chaining
	 */
	public T_BASE on(Property.Action<T> action, Consumer<Registration> registration) {
		if (isPresent())
			settingsSupport.fireValueChange(action, name, value, value, null);

		var reg = settingsSupport.addAction(name(), action);
		registration.accept(reg);

		return us;
	}

	/**
	 * Registers a simple action to be executed when this property's value changes,
	 * with optional source filtering. This is a convenience method that creates an
	 * Action from the provided BiConsumer.
	 * 
	 * <p>
	 * The BiConsumer receives both the new value and the source of the change. If a
	 * source object is provided, the action will only be executed for changes that
	 * come from a different source.
	 * </p>
	 *
	 * <p>
	 * Example usage:
	 * </p>
	 * 
	 * <pre>
	 * property.on((newValue, source) -> System.out.println("Value changed to: " + newValue),
	 * 		this); // Won't fire for changes from 'this'
	 * </pre>
	 *
	 * @param before the action to execute, receiving the new value and change
	 *               source
	 * @param source optional source object for filtering changes, or null for no
	 *               filtering
	 * @return this property instance for method chaining
	 */
	public T_BASE on(BiConsumer<T, Object> before, Object source) {
		return on(Action.ofAction(before, source));
	}

	/**
	 * Returns an alternative property if this one is empty.
	 *
	 * @param supplier the supplier of the alternative property
	 * @return this property if it has a value, otherwise the supplied alternative
	 * @throws NullPointerException if the supplier or its result is null
	 */
	public T_BASE or(Supplier<? extends T_BASE> supplier) {
		Objects.requireNonNull(supplier);
		if (isPresent()) {
			return us;
		} else {
			T_BASE r = supplier.get();
			return Objects.requireNonNull(r);
		}
	}

	/**
	 * Returns the value if present, otherwise returns the alternative value.
	 *
	 * @param altValue the value to return if this property is empty
	 * @return the value of this property if present, otherwise altValue
	 */
	public T orElse(T altValue) {
		return altValue;
	}

	/**
	 * Executes an action if this property is empty and returns the property.
	 *
	 * @param action the action to execute if the property is empty
	 * @return this property instance
	 */
	public T_BASE orElseAction(Runnable action) {
		if (value == null)
			action.run();
		return us;
	}

	/**
	 * Returns the value if present, otherwise returns the result of the supplier.
	 *
	 * @param supplier the supplier of the alternative value
	 * @return the value if present, otherwise the result of the supplier
	 */
	public T orElseGet(Supplier<? extends T> supplier) {
		return value != null ? value : supplier.get();
	}

	/**
	 * Returns the value if present, otherwise throws NoSuchElementException.
	 *
	 * @return the non-null value
	 * @throws NoSuchElementException if no value is present
	 */
	public T orElseThrow() {
		if (value == null) {
			throw new NoSuchElementException("No value present");
		}
		return value;
	}

	/**
	 * Returns the value if present, otherwise throws an exception produced by the
	 * supplier.
	 *
	 * @param <X>               type of exception to throw
	 * @param exceptionSupplier supplier of the exception to throw
	 * @return the value if present
	 * @throws X                    if no value is present
	 * @throws NullPointerException if no value is present and the exception
	 *                              supplier is null
	 */
	public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
		if (value != null) {
			return value;
		} else {
			throw exceptionSupplier.get();
		}
	}

	/**
	 * Parses a string value and sets the property's value accordingly. Must be
	 * implemented by concrete subclasses to provide type-specific parsing.
	 *
	 * @param newValue the string value to parse
	 * @return this property instance for method chaining
	 */
	public abstract T_BASE parseValue(String newValue);

	/**
	 * Sets the format string used for value representation.
	 *
	 * @param formatString the new format string to use
	 * @return this property instance for method chaining
	 */
	public T_BASE setFormat(String formatString) {
		this.formatString = formatString;
		return us;
	}

	/**
	 * Sets the value of this property.
	 *
	 * @param newValue the new value to set
	 * @return this property instance for method chaining
	 */
	public T_BASE setValue(T newValue) {
		return setValue(newValue, null);
	}

	/**
	 * Sets the value of this property with a source identifier.
	 *
	 * @param newValue the new value to set
	 * @param source   the source of the value change
	 * @return this property instance for method chaining
	 */
	public T_BASE setValue(T newValue, Object source) {
		if (this.value == newValue)
			return us;

		if (newValue != null)
			checkBounds(newValue);

		this.value = newValue;
		settingsSupport.fireValueChange(name(), this.value, newValue, source);
		return us;
	}

	/**
	 * Returns a stream containing the property value if present, or an empty stream
	 * if the property is empty.
	 *
	 * @return a stream containing zero or one elements
	 */
	public Stream<T> stream() {
		if (isEmpty())
			return Stream.empty();
		else
			return Stream.of(value);
	}

	/**
	 * Attempts to load this property's value from system properties.
	 *
	 * @return this property instance for method chaining
	 */
	public T_BASE systemProperty() {
		String newValue = System.getProperty(name());
		if (newValue != null)
			return parseValue(newValue);
		return us;
	}

	/**
	 * Returns the property value formatted according to the current format string.
	 *
	 * @return the formatted value string, or "<empty>" if the property is empty
	 */
	public String toFormattedValue() {
		if (isEmpty())
			return "<empty>";
		return formatString.formatted(value);
	}

	/**
	 * Converts this property to an Optional containing its value if present.
	 *
	 * @return an Optional describing the value of this property
	 */
	public Optional<T> toOptional() {
		return Optional.ofNullable(value);
	}

	/**
	 * Returns a string representation of this property.
	 *
	 * @return a string representation including the property name and value
	 */
	@Override
	public String toString() {
		if (isEmpty())
			return name + " = " + "<empty>";
		return name + " = " + String.valueOf(value);
	}

	/**
	 * Creates an exception for when a value is outside the allowed bounds.
	 *
	 * @param value the value that was out of bounds
	 * @param min   the minimum allowed value
	 * @param max   the maximum allowed value
	 * @return an IllegalArgumentException with appropriate message
	 */
	protected final IllegalArgumentException valueOutOfBoundsException(T value, T min, T max) {
		return new IllegalArgumentException("value %s [min=%s, max=%s]".formatted(value, min, max));
	}
}
