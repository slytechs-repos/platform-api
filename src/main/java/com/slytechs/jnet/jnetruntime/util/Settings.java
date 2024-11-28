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
package com.slytechs.jnet.jnetruntime.util;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.slytechs.jnet.jnetruntime.util.Settings.Property.Action;

/**
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public class Settings {

	public static final class BooleanProperty extends Property<Boolean, BooleanProperty> {

		/**
		 * @param base
		 */
		public BooleanProperty(String name) {
			super(name);
		}

		/**
		 * @param name
		 * @param base
		 * @param value
		 */
		public BooleanProperty(String name, boolean value) {
			super(name, value);
		}

		public boolean getBoolean() {
			return getValue();
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.util.Settings.Property#parseValue(java.lang.String)
		 */
		@Override
		public BooleanProperty parseValue(String newValue) {
			return setValue(newValue == null ? null : Boolean.parseBoolean(newValue));
		}

		public BooleanProperty setBoolean(boolean newValue) {
			return setValue(newValue);
		}
	}

	public static final class ByteProperty extends Property<Byte, ByteProperty> {

		/**
		 * @param base
		 */
		public ByteProperty(String name) {
			super(name);
		}

		/**
		 * @param base
		 * @param value
		 */
		public ByteProperty(String name, byte value) {
			super(name, value);
		}

		public byte getByte() {
			return getValue();
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.util.Settings.Property#parseValue(java.lang.String)
		 */
		@Override
		public ByteProperty parseValue(String newValue) {
			return setValue(newValue == null ? null : Byte.parseByte(newValue));
		}

		public ByteProperty setByte(byte newValue) {
			return setValue(newValue);
		}

		public Optional<Byte> toShortOptional() {
			if (isPresent())
				return Optional.of(getValue());

			return Optional.empty();
		}
	}

	public static final class DoubleProperty extends Property<Double, DoubleProperty> {

		/**
		 * @param base
		 */
		public DoubleProperty(String name) {
			super(name);
		}

		/**
		 * @param base
		 * @param value
		 */
		public DoubleProperty(String name, double value) {
			super(name, value);
		}

		public double getDouble() {
			return getValue();
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.util.Settings.Property#parseValue(java.lang.String)
		 */
		@Override
		public DoubleProperty parseValue(String newValue) {
			return setValue(newValue == null ? null : Double.parseDouble(newValue));
		}

		public DoubleProperty setDouble(double newValue) {
			return setValue(newValue);
		}

		public OptionalDouble toDoubleOptional() {
			if (isPresent())
				return OptionalDouble.of(getValue());

			return OptionalDouble.empty();
		}
	}

	private static final class EmptyProperty<T, T_BASE extends Property<T, T_BASE>> extends Property<T, T_BASE> {

		private EmptyProperty(String name) {
			super(name);
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.util.Settings.Property#getValue()
		 */
		@Override
		public T getValue() {
			throw new UnsupportedOperationException("");
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.util.Settings.Property#map(java.util.function.Function)
		 */
		@Override
		public <U, P_BASE extends Property<U, P_BASE>> P_BASE map(Function<? super T, ? extends U> action) {
			throw new UnsupportedOperationException("not implemented yet");
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.util.Settings.Property#parseValue(java.lang.String)
		 */
		@Override
		public T_BASE parseValue(String newValue) {
			throw new UnsupportedOperationException("not implemented yet");
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.util.Settings.Property#setValue(java.lang.Object)
		 */
		@Override
		public T_BASE setValue(T newValue) {
			throw new UnsupportedOperationException("");
		}

	}

	public static final class EnumProperty<E extends Enum<E>> extends Property<E, EnumProperty<E>> {

		private final Class<E> enumType;

		/**
		 * @param base
		 */
		public EnumProperty(String name, Class<E> enumType) {
			super(name);
			this.enumType = enumType;
		}

		/**
		 * @param base
		 * @param value
		 */
		@SuppressWarnings("unchecked")
		public EnumProperty(String name, E value) {
			super(name, value);
			this.enumType = (Class<E>) value.getClass();
		}

		public E getEnum() {
			return getValue();
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.util.Settings.Property#parseValue(java.lang.String)
		 */
		@Override
		public EnumProperty<E> parseValue(String newValue) {
			return setValue(Enums.getEnumOrThrow(enumType, newValue, () -> new IllegalArgumentException(newValue)));
		}

		public EnumProperty<E> setEnum(E newValue) {
			return super.setValue(newValue);
		}

	}

	public static final class FloatProperty extends Property<Float, FloatProperty> {

		/**
		 * @param base
		 */
		public FloatProperty(String name) {
			super(name);
		}

		/**
		 * @param base
		 * @param value
		 */
		public FloatProperty(String name, float value) {
			super(name, value);
		}

		public float getFloat() {
			return getValue();
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.util.Settings.Property#parseValue(java.lang.String)
		 */
		@Override
		public FloatProperty parseValue(String newValue) {
			return setValue(newValue == null ? null : Float.parseFloat(newValue));
		}

		public FloatProperty setFloat(float newValue) {
			return setValue(newValue);
		}

		public Optional<Float> toFloatOptional() {
			if (isPresent())
				return Optional.of(getValue());

			return Optional.empty();
		}
	}

	public static final class IntProperty extends Property<Integer, IntProperty> {

		/**
		 * @param base
		 */
		public IntProperty(String name) {
			super(name);
		}

		/**
		 * @param base
		 * @param value
		 */
		public IntProperty(String name, int value) {
			super(name, value);
		}

		public int getInt() {
			return getValue();
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.util.Settings.Property#parseValue(java.lang.String)
		 */
		@Override
		public IntProperty parseValue(String newValue) {
			return setValue(newValue == null ? null : Integer.parseInt(newValue));
		}

		public IntProperty setInt(int newValue) {
			return setValue(newValue);
		}

		public OptionalInt toIntOptional() {
			if (isPresent())
				return OptionalInt.of(getValue());

			return OptionalInt.empty();
		}
	}

	public static final class ListProperty<E> extends Property<List<E>, ListProperty<E>> {

		private final Function<String, E> componentParser;

		/**
		 * @param base
		 */
		public ListProperty(String name, Function<String, E> componentParser) {
			super(name);
			this.componentParser = componentParser;
		}

		/**
		 * @param base
		 * @param value
		 */
		public ListProperty(String name, Function<String, E> componentParser, List<E> value) {
			super(name, value);
			this.componentParser = componentParser;
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.util.Settings.Property#parseValue(java.lang.String)
		 */
		@Override
		public ListProperty<E> parseValue(String newValue) {
			String[] array = newValue.split(",");
			List<E> list = new ArrayList<>(array.length);

			for (String element : array) {
				E e = componentParser.apply(element);

				list.add(e);
			}

			return setValue(list);
		}

		public List<E> getList() {
			return getValue();
		}

	}

	public static final class LongProperty extends Property<Long, LongProperty> {

		/**
		 * @param base
		 */
		public LongProperty(String name) {
			super(name);
		}

		/**
		 * @param base
		 * @param value
		 */
		public LongProperty(String name, long value) {
			super(name, value);
		}

		public long getLong() {
			return getValue();
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.util.Settings.Property#parseValue(java.lang.String)
		 */
		@Override
		public LongProperty parseValue(String newValue) {
			return setValue(newValue == null ? null : Long.parseLong(newValue));
		}

		public LongProperty setLong(int newValue) {
			return super.setValue((long) newValue);
		}

		public LongProperty setLong(long newValue) {
			return super.setValue(newValue);
		}

		public LongProperty setLong(String newValue) {
			return super.setValue(Long.parseLong(newValue));
		}

		public OptionalLong toOptionalLong() {
			if (isPresent())
				return OptionalLong.of(getValue());

			return OptionalLong.empty();
		}
	}

	public static sealed abstract class Property<T, T_BASE extends Property<T, T_BASE>>
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

		interface Action<T> {

			static <T> Action<T> ofAction(BiConsumer<T, Object> before, Object source) {
				return new Action<T>() {
					/**
					 * @see com.slytechs.jnet.jnetruntime.util.Settings.Property.Action#canFireFromSource(java.lang.Object)
					 */
					@Override
					public boolean canFireFromSource(Object src) {
						return source == null || source != src;
					}

					/**
					 * @see com.slytechs.jnet.jnetruntime.util.Settings.Property.Action#propertyChangeAction(java.lang.String,
					 *      java.lang.Object, java.lang.Object, java.lang.Object)
					 */
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

			default Action<T> andThen(Action<T> after) {
				Action<T> before = this;
				return new Action<T>() {
					/**
					 * @see com.slytechs.jnet.jnetruntime.util.Settings.Property.Action#canFireFromSource(java.lang.Object)
					 */
					@Override
					public boolean canFireFromSource(Object source) {
						return before.canFireFromSource(source);
					}

					/**
					 * @see com.slytechs.jnet.jnetruntime.util.Settings.Property.Action#propertyChangeAction(java.lang.String,
					 *      java.lang.Object, java.lang.Object, java.lang.Object)
					 */
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

			default boolean canFireFromSource(Object source) {
				return true;
			}

			default void propertyChangeAction(String name, T newValue) {
				propertyChangeAction(newValue);
			}

			default void propertyChangeAction(String name, T oldValue, T newValue) {
				propertyChangeAction(name, newValue);
			}

			default void propertyChangeAction(String name, T oldValue, T newValue, Object source) {
				propertyChangeAction(name, oldValue, newValue);
			}

			void propertyChangeAction(T newValue);
		}

		public static <U, P_BASE extends Property<U, P_BASE>> P_BASE empty(String name) {

			@SuppressWarnings("unchecked")
			P_BASE empty = (P_BASE) new EmptyProperty<>(name);

			return empty;
		}

		public static <T, T_BASE extends Property<T, T_BASE>> T_BASE of(String name,
				Function<String, T_BASE> factory) {
			return factory.apply(name);
		}

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

		public static <T, T_BASE extends Property<T, T_BASE>> T_BASE of(String name, T value,
				BiFunction<String, T, T_BASE> factory) {
			return factory.apply(name, value);
		}

		public static <T, T_BASE extends Property<T, T_BASE>> T_BASE ofSystem(String name,
				Function<String, T_BASE> factory) {

			var property = factory.apply(name)
					.systemProperty();

			return property;
		}

		public static <T, T_BASE extends Property<T, T_BASE>> T_BASE ofSystem(String name, T defaultValue) {
			T_BASE property = Property.<T, T_BASE>of(name, defaultValue)
					.systemProperty();

			return property;
		}

		public static <T, T_BASE extends Property<T, T_BASE>> T_BASE ofSystem(String name, T defaultValue,
				BiFunction<String, T, T_BASE> factory) {
			T_BASE property = factory.apply(name, defaultValue)
					.systemProperty();

			return property;
		}

		Support support = new Support();

		@SuppressWarnings("unchecked")
		private final T_BASE us = (T_BASE) this;

		private T value;

		private String formatString = "%s";

		private final String name;

		protected Property(String name) {
			Objects.requireNonNull(name);
			this.name = name;
		}

		protected Property(String name, T value) {
			Objects.requireNonNull(name);
			this.name = name;
			this.value = value;
		}

		protected void checkBounds(T value) throws IllegalArgumentException {
			// No bounds check by default, override to implement a check
		}

		public T_BASE clear() {
			if (value != null)
				support.fireValueChange(name(), null, this.value, "<clear>");

			this.value = null;

			return us;
		}

		public String getFormat() {
			return formatString;
		}

		public T_BASE getSystemProperty(T defaultValue) {
			String newValue = System.getProperty(name());
			if (newValue != null)
				return parseValue(newValue);

			else if (isEmpty())
				return setValue(defaultValue);

			return us;
		}

		public T getValue() {
			if (isEmpty())
				throw noValueException();

			return value;
		}

		public T_BASE ifEmpty(Runnable action) {
			if (value == null)
				action.run();

			return us;
		}

		public void ifPresent(Consumer<? super T> action) {
			if (value != null)
				action.accept(value);
		}

		public boolean isEmpty() {
			return value == null;
		}

		public boolean isPresent() {
			return value != null;
		}

		public <U, P_BASE extends Property<U, P_BASE>> P_BASE map(Function<? super T, ? extends U> action) {
			if (isPresent()) {
				U mappedValue = action.apply(value);

				return Property.of(name(), mappedValue);
			}

			return empty(name());
		}

		public StringProperty mapToFormattedString() {
			return new StringProperty(name(), toFormattedValue());
		}

		public final String name() {
			return this.name;
		}

		protected final IllegalStateException noValueException() {
			return new IllegalStateException("no value");
		}

		public T_BASE on(Action<T> action) {

			if (isPresent())
				support.fireValueChange(action, name, value, value, null);

			support.addAction(name(), action);

			return us;
		}

		public T_BASE on(Action<T> action, Consumer<Registration> registration) {

			if (isPresent())
				support.fireValueChange(action, name, value, value, null);

			var reg = support.addAction(name(), action);
			registration.accept(reg);

			return us;
		}

		public T_BASE or(Supplier<? extends T_BASE> supplier) {
			Objects.requireNonNull(supplier);

			if (isPresent()) {
				return us;

			} else {
				T_BASE r = supplier.get();
				return Objects.requireNonNull(r);
			}
		}

		public T orElse(T altValue) {
			return altValue;
		}

		public T_BASE orElseAction(Runnable action) {
			if (value == null)
				action.run();

			return us;
		}

		public T orElseGet(Supplier<? extends T> supplier) {
			return value != null ? value : supplier.get();
		}

		/**
		 * If a value is present, returns the value, otherwise throws
		 * {@code NoSuchElementException}.
		 *
		 * @return the non-{@code null} value described by this {@code Optional}
		 * @throws NoSuchElementException if no value is present
		 */
		public T orElseThrow() {
			if (value == null) {
				throw new NoSuchElementException("No value present");
			}
			return value;
		}

		/**
		 * If a value is present, returns the value, otherwise throws an exception
		 * produced by the exception supplying function.
		 *
		 * @apiNote A method reference to the exception constructor with an empty
		 *          argument list can be used as the supplier. For example,
		 *          {@code IllegalStateException::new}
		 *
		 * @param <X>               Type of the exception to be thrown
		 * @param exceptionSupplier the supplying function that produces an exception to
		 *                          be thrown
		 * @return the value, if present
		 * @throws X                    if no value is present
		 * @throws NullPointerException if no value is present and the exception
		 *                              supplying function is {@code null}
		 */
		public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
			if (value != null) {
				return value;
			} else {
				throw exceptionSupplier.get();
			}
		}

		public abstract T_BASE parseValue(String newValue);

		public T_BASE setFormat(String formatString) {
			this.formatString = formatString;

			return us;
		}

		public T_BASE setValue(T newValue) {
			return setValue(newValue, null);
		}

		public T_BASE setValue(T newValue, Object source) {
			if (this.value == newValue)
				return us;

			if (newValue != null)
				checkBounds(newValue);

			this.value = newValue;

			support.fireValueChange(name(), this.value, newValue, source);

			return us;
		}

		public Stream<T> stream() {
			if (isEmpty())
				return Stream.empty();

			else
				return Stream.of(value);
		}

		public T_BASE systemProperty() {
			String newValue = System.getProperty(name());
			if (newValue != null)
				return parseValue(newValue);

			return us;
		}

		public String toFormattedValue() {
			if (isEmpty())
				return "<empty>";

			return formatString.formatted(value);
		}

		public Optional<T> toOptional() {
			return Optional.ofNullable(value);
		}

		@Override
		public String toString() {
			if (isEmpty())
				return name + " = " + "<empty>";

			return name + " = " + String.valueOf(value);
		}

		protected final IllegalArgumentException valueOutOfBoundsException(T value, T min, T max) {
			return new IllegalArgumentException("value %s [min=%s, max=%s]".formatted(value, min, max));
		}
	}

	public static final class ShortProperty extends Property<Short, ShortProperty> {

		/**
		 * @param base
		 */
		public ShortProperty(String name) {
			super(name);
		}

		/**
		 * @param base
		 * @param value
		 */
		public ShortProperty(String name, short value) {
			super(name, value);
		}

		public short getShort() {
			return getValue();
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.util.Settings.Property#parseValue(java.lang.String)
		 */
		@Override
		public ShortProperty parseValue(String newValue) {
			return setValue(newValue == null ? null : Short.parseShort(newValue));
		}

		public ShortProperty setShort(short newValue) {
			return setValue(newValue);
		}

		public Optional<Short> toShortOptional() {
			if (isPresent())
				return Optional.of(getValue());

			return Optional.empty();
		}
	}

	public static final class StringProperty extends Property<String, StringProperty> {

		/**
		 * @param base
		 */
		public StringProperty(String name) {
			super(name);
		}

		/**
		 * @param base
		 * @param value
		 */
		public StringProperty(String name, String value) {
			super(name, value);
		}

		public String getString() {
			return getValue();
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.util.Settings.Property#parseValue(java.lang.String)
		 */
		@Override
		public StringProperty parseValue(String newValue) {
			return setValue(newValue);
		}

		public StringProperty setString(String newValue) {
			return super.setValue(newValue);
		}

	}

	static class Support {
		Map<String, List<Reference<Property.Action<?>>>> actions = new HashMap<>();

		private boolean enableFireEvents = true;

		public synchronized <T> Registration addAction(String name, Property.Action<T> action) {
			List<Reference<Property.Action<?>>> list = actions.computeIfAbsent(name,
					_ -> new ArrayList<Reference<Property.Action<?>>>());

			Reference<Property.Action<?>> weak = new WeakReference<>(action);
			list.add(weak);

			return () -> remove(name, weak);
		}

		/**
		 * @param b
		 */
		public void enableEventDispatching(boolean b) {
			this.enableFireEvents = b;
		}

		/**
		 * @param <T>
		 * @param name
		 * @param value
		 * @param object
		 */
		public synchronized <T> void fireValueChange(Action<T> action, String name, T oldValue, T newValue,
				Object source) {

			if (!enableFireEvents)
				return;

			action.propertyChangeAction(name, oldValue, newValue, source);
		}

		/**
		 * @param <T>
		 * @param name
		 * @param value
		 * @param object
		 */
		public synchronized <T> void fireValueChange(String name, T oldValue, T newValue) {
			fireValueChange(name, oldValue, newValue, null);
		}

		/**
		 * @param <T>
		 * @param name
		 * @param value
		 * @param object
		 */
		public synchronized <T> void fireValueChange(String name, T oldValue, T newValue, Object source) {
			if (!actions.containsKey(name) || !enableFireEvents)
				return;

			boolean needsCleanup = false;

			for (var ref : actions.get(name)) {
				@SuppressWarnings("unchecked")
				Property.Action<T> action = (Action<T>) ref.get();

				if (action == null) {
					needsCleanup = true;
					continue;
				}

				if (action.canFireFromSource(source))
					action.propertyChangeAction(name, oldValue, newValue, source);
			}

			if (needsCleanup)
				pruneUnreferencedActions();
		}

		private synchronized void pruneUnreferencedActions() {
			for (Iterator<String> iterator = actions.keySet().iterator(); iterator.hasNext();) {
				pruneUnreferencedActions(iterator.next());
			}
		}

		private synchronized void pruneUnreferencedActions(String key) {
			List<Reference<Property.Action<?>>> list = actions.get(key);
			if (list == null || list.isEmpty())
				return;

			for (Iterator<Reference<Property.Action<?>>> iterator = list.iterator(); iterator.hasNext();) {
				Reference<Property.Action<?>> reference = iterator.next();
				if (reference.get() == null)
					iterator.remove();
			}

			if (list.isEmpty())
				actions.remove(key);

		}

		private synchronized void remove(String key, Reference<Property.Action<?>> value) {
			List<Reference<Property.Action<?>>> list = actions.get(key);
			if (list == null || list.isEmpty())
				return;

			Property.Action<?> action = value.get(); // Lock it in
			if (action != null)
				list.remove(value);

			pruneUnreferencedActions();
		}

	}

	public static final class UnsignedByteProperty extends Property<Integer, UnsignedByteProperty> {

		public static final int MIN_VALUE = 0;
		public static final int MAX_VALUE = (1 << 8) - 1;

		/**
		 * @param base
		 */
		public UnsignedByteProperty(String name) {
			super(name);
		}

		/**
		 * @param base
		 * @param unsignedValue
		 */
		public UnsignedByteProperty(String name, byte unsignedValue) {
			super(name, Byte.toUnsignedInt(unsignedValue));
		}

		/**
		 * @param base
		 * @param unsignedValue
		 */
		public UnsignedByteProperty(String name, int unsignedValue) {
			super(name, unsignedValue);
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.util.Settings.Property#checkBounds(java.lang.Object)
		 */
		@Override
		protected void checkBounds(Integer unsignedValue) throws IllegalArgumentException {
			if (unsignedValue < MIN_VALUE || unsignedValue > MAX_VALUE)
				throw valueOutOfBoundsException(unsignedValue, MIN_VALUE, MAX_VALUE);
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.util.Settings.Property#parseValue(java.lang.String)
		 */
		@Override
		public UnsignedByteProperty parseValue(String newValue) {
			return setValue(newValue == null ? null : Integer.parseInt(newValue));
		}

		public UnsignedByteProperty setUnsignedByte(byte newValue) {
			return super.setValue(Byte.toUnsignedInt(newValue));
		}

		public UnsignedByteProperty setUnsignedByte(int newValue) {
			return super.setValue(newValue);
		}

		public OptionalInt toOptionalUnsignedByte() {
			if (isPresent())
				return OptionalInt.of(getValue());

			return OptionalInt.empty();
		}
	}

	public static final class UnsignedIntProperty extends Property<Long, UnsignedIntProperty> {

		public static final long MIN_VALUE = 0;
		public static final long MAX_VALUE = (1L << 32) - 1L;

		/**
		 * @param base
		 */
		public UnsignedIntProperty(String name) {
			super(name);
		}

		/**
		 * @param base
		 * @param unsignedValue
		 */
		public UnsignedIntProperty(String name, long unsignedValue) {
			super(name, unsignedValue);
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.util.Settings.Property#checkBounds(java.lang.Object)
		 */
		@Override
		protected void checkBounds(Long value) throws IllegalArgumentException {
			if (value < MIN_VALUE || value > MAX_VALUE)
				throw valueOutOfBoundsException(value, MIN_VALUE, MAX_VALUE);
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.util.Settings.Property#parseValue(java.lang.String)
		 */
		@Override
		public UnsignedIntProperty parseValue(String newValue) {
			return setValue(newValue == null ? null : Long.parseLong(newValue));
		}

		public UnsignedIntProperty setUnsignedInt(int newValue) {
			return super.setValue(Integer.toUnsignedLong(newValue));
		}

		public UnsignedIntProperty setUnsignedInt(long newValue) {
			return super.setValue(newValue);
		}

		public OptionalLong toOptionalUnsignedInt() {
			if (isPresent())
				return OptionalLong.of(getValue());

			return OptionalLong.empty();
		}
	}

	public static final class UnsignedShortProperty extends Property<Integer, UnsignedShortProperty> {

		public static final int MIN_VALUE = 0;
		public static final int MAX_VALUE = (1 << 16) - 1;

		/**
		 * @param base
		 */
		public UnsignedShortProperty(String name) {
			super(name);
		}

		/**
		 * @param base
		 * @param unsignedValue
		 */
		public UnsignedShortProperty(String name, int unsignedValue) {
			super(name, unsignedValue);
		}

		/**
		 * @param base
		 * @param unsignedValue
		 */
		public UnsignedShortProperty(String name, short unsignedValue) {
			super(name, Short.toUnsignedInt(unsignedValue));
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.util.Settings.Property#checkBounds(java.lang.Object)
		 */
		@Override
		protected void checkBounds(Integer unsignedValue) throws IllegalArgumentException {
			if (unsignedValue < MIN_VALUE || unsignedValue > MAX_VALUE)
				throw valueOutOfBoundsException(unsignedValue, MIN_VALUE, MAX_VALUE);
		}

		/**
		 * @see com.slytechs.jnet.jnetruntime.util.Settings.Property#parseValue(java.lang.String)
		 */
		@Override
		public UnsignedShortProperty parseValue(String newValue) {
			return setValue(newValue == null ? null : Integer.parseInt(newValue));
		}

		public UnsignedShortProperty setUnsignedShort(int newValue) {
			return super.setValue(newValue);
		}

		public UnsignedShortProperty setUnsignedShort(short newValue) {
			return super.setValue(Short.toUnsignedInt(newValue));
		}

		public OptionalInt toOptionalUnsignedShort() {
			if (isPresent())
				return OptionalInt.of(getValue());

			return OptionalInt.empty();
		}
	}

	/**
	 * For testing and demonstation purposes.
	 * 
	 * @param __ ignored
	 */
	public static void main(String[] __) {
		class MySettings extends Settings {

			private final IntProperty x = ofInt("property.x", 10, Action.ofAction(this::setX, this))
					.systemProperty();

			private final EnumProperty<TimeUnit> e = ofEnum("property.e", TimeUnit.MICROSECONDS, Action.ofAction(
					this::setTimeUnit, this));

			private final ListProperty<TimeUnit> l = ofList("property.list", TimeUnit::valueOf, TimeUnit.MICROSECONDS);

			MySettings(String name) {
				super(name);

				enableUpdates(true);
				refreshAllProperties();
			}

			public int getX() {
				return x.getInt();
			}

			public void setTimeUnit(TimeUnit unit, Object source) {
				System.out.println(unit + ", source=" + source);
			}

			public MySettings setX(int newValue, Object source) {
				if (source != this)
					x.setValue(newValue, this);

				System.out.println(name() + "::setX x=" + x.getInt() + ", source=" + source);

				return this;
			}

			public void setY(int newValue) {
				System.out.println(name() + "::setY x=" + newValue);
			}

		}

		StringProperty property = Property.of("property.id", 11)
				.on(newValue -> System.out.println("on:: property.id = " + newValue))
				.setFormat("0x%08X")
				.map(i -> String.valueOf(i))
				.mapToFormattedString()

		;

		System.out.println("main:: " + property);

		MySettings s1 = new MySettings("s1")
//				.setX(20, null)
		;

		MySettings s2 = new MySettings("s2")
				.setX(30, null);

		System.out.println("---");
		s1.merge(s2);

		s2.clear();
	}

	private String name = getClass().getSimpleName();

	private final Support support = new Support();

	private final List<Property<?, ?>> properties = new ArrayList<>();

	public Settings() {
		support.enableFireEvents = false;
	}

	public Settings(String name) {
		this.name = name;
		support.enableFireEvents = false;
	}

	public void clear() {
		for (var p : properties) {
			p.clear();
		}
	}

	public synchronized void enableUpdates(boolean b) {
		support.enableEventDispatching(b);
	}

	@SuppressWarnings("unchecked")
	private <T, P_BASE extends Property<T, P_BASE>> P_BASE findProperty(String name) {
		for (var p : properties) {
			if (p.name().equals(name))
				return (P_BASE) p;
		}

		return null;
	}

	private void fireForProperty(Property<?, ?> property) {
		support.fireValueChange(property.name(), null, property.getValue(), "<refresh>");
	}

	@SuppressWarnings("unchecked")
	public void merge(Settings settingsToBeMerged) {
		var ours = this.properties;

		for (@SuppressWarnings("rawtypes")
		Property our : ours) {
			var their = settingsToBeMerged.findProperty(our.name());
			if (their != null && their.isPresent()) {
				Object value = their.getValue();

				our.setValue(value, "<merge>");
			}

		}
	}

	public String name() {
		return name;
	}

	public <T, P_BASE extends Property<T, P_BASE>> P_BASE of(String name, Function<String, P_BASE> factory) {
		P_BASE p = findProperty(name);
		if (p != null)
			return p;

		P_BASE property = Property.of(name, factory);
		property.support = this.support;

		properties.add(property);

		return property;
	}

	public <T, P_BASE extends Property<T, P_BASE>> P_BASE of(String name, T defaultValue) {
		P_BASE p = findProperty(name);
		if (p != null)
			return p;

		P_BASE property = Property.of(name, defaultValue);
		property.support = this.support;

		properties.add(property);

		return property;
	}

	public <T, P_BASE extends Property<T, P_BASE>> P_BASE of(String name, T defaultValue,
			BiFunction<String, T, P_BASE> factory) {
		P_BASE p = findProperty(name);
		if (p != null)
			return p;

		P_BASE property = Property.of(name, defaultValue, factory);
		property.support = this.support;

		properties.add(property);

		return property;
	}

	public ByteProperty ofByte(String name) {
		return of(name, ByteProperty::new);
	}

	public ByteProperty ofByte(String name, Action<Byte> action) {
		return of(name, ByteProperty::new).on(action);
	}

	public ByteProperty ofByte(String name, byte defaultValue) {
		return of(name, defaultValue, ByteProperty::new);
	}

	public ByteProperty ofByte(String name, byte defaultValue, Action<Byte> action) {
		return of(name, defaultValue, ByteProperty::new).on(action);
	}

	public DoubleProperty ofDouble(String name) {
		return of(name, DoubleProperty::new);
	}

	public DoubleProperty ofDouble(String name, Action<Double> action) {
		return of(name, DoubleProperty::new).on(action);
	}

	public DoubleProperty ofDouble(String name, double defaultValue) {
		return of(name, defaultValue, DoubleProperty::new);
	}

	public DoubleProperty ofDouble(String name, double defaultValue, Action<Double> action) {
		return of(name, defaultValue, DoubleProperty::new).on(action);
	}

	public <E extends Enum<E>> EnumProperty<E> ofEnum(String name, Class<E> enumType) {
		return of(name, n -> new EnumProperty<E>(name, enumType));
	}

	public <E extends Enum<E>> EnumProperty<E> ofEnum(String name, Class<E> enumType, Action<E> action) {
		return of(name, n -> new EnumProperty<E>(name, enumType))
				.on(action);
	}

	public <E extends Enum<E>> EnumProperty<E> ofEnum(String name, E defaultValue) {
		return of(name, n -> new EnumProperty<E>(name, defaultValue));
	}

	public <E extends Enum<E>> EnumProperty<E> ofEnum(String name, E defaultValue, Action<E> action) {
		return of(name, n -> new EnumProperty<E>(name, defaultValue))
				.on(action);
	}

	public FloatProperty ofFloat(String name) {
		return of(name, FloatProperty::new);
	}

	public FloatProperty ofFloat(String name, Action<Float> action) {
		return of(name, FloatProperty::new).on(action);
	}

	public FloatProperty ofFloat(String name, float defaultValue) {
		return of(name, defaultValue, FloatProperty::new);
	}

	public FloatProperty ofFloat(String name, float defaultValue, Action<Float> action) {
		return of(name, defaultValue, FloatProperty::new).on(action);
	}

	public IntProperty ofInt(String name) {
		return of(name, IntProperty::new);
	}

	public IntProperty ofInt(String name, Action<Integer> action) {
		return of(name, IntProperty::new).on(action);
	}

	public IntProperty ofInt(String name, int defaultValue) {
		return of(name, defaultValue, IntProperty::new);
	}

	public IntProperty ofInt(String name, int defaultValue, Action<Integer> action) {
		return of(name, defaultValue, IntProperty::new).on(action);
	}

	public LongProperty ofLong(String name) {
		return of(name, LongProperty::new);
	}

	public LongProperty ofLong(String name, Action<Long> action) {
		return of(name, LongProperty::new).on(action);
	}

	public LongProperty ofLong(String name, long defaultValue) {
		return of(name, defaultValue, LongProperty::new);
	}

	public LongProperty ofLong(String name, long defaultValue, Action<Long> action) {
		return of(name, defaultValue, LongProperty::new).on(action);
	}

	public ShortProperty ofShort(String name) {
		return of(name, ShortProperty::new);
	}

	public ShortProperty ofShort(String name, Action<Short> action) {
		return of(name, ShortProperty::new).on(action);
	}

	public ShortProperty ofShort(String name, short defaultValue) {
		return of(name, defaultValue, ShortProperty::new);
	}

	public ShortProperty ofShort(String name, short defaultValue, Action<Short> action) {
		return of(name, defaultValue, ShortProperty::new).on(action);
	}

	public StringProperty ofString(String name) {
		return of(name, StringProperty::new);
	}

	public StringProperty ofString(String name, Action<String> action) {
		return of(name, StringProperty::new).on(action);
	}

	public StringProperty ofString(String name, String defaultValue) {
		return of(name, defaultValue, StringProperty::new);
	}

	public StringProperty ofString(String name, String defaultValue, Action<String> action) {
		return of(name, defaultValue, StringProperty::new).on(action);
	}

	public <E> ListProperty<E> ofList(String name, Function<String, E> parser) {
		return of(name, n -> new ListProperty<E>(name, parser));
	}

	public <E> ListProperty<E> ofList(String name, Function<String, E> parser, Action<List<E>> action) {
		return of(name, n -> new ListProperty<E>(name, parser)).on(action);
	}

	public <E> ListProperty<E> ofList(String name, Function<String, E> parser, List<E> defaultValue) {
		return of(name, n -> new ListProperty<E>(name, parser, defaultValue));
	}

	public <E> ListProperty<E> ofList(String name, Function<String, E> parser, List<E> defaultValue,
			Action<List<E>> action) {
		return of(name, n -> new ListProperty<E>(name, parser, defaultValue));
	}

	@SuppressWarnings("unchecked")
	public <E> ListProperty<E> ofList(String name, Function<String, E> parser, E... defaultValue) {
		return of(name, n -> new ListProperty<E>(name, parser, Arrays.asList(defaultValue)));
	}

	@SuppressWarnings("unchecked")
	public <E> ListProperty<E> ofList(String name, Function<String, E> parser,
			Action<List<E>> action, E... defaultValue) {
		return of(name, n -> new ListProperty<E>(name, parser, Arrays.asList(defaultValue)));
	}

	public synchronized void refreshAllProperties() {
		if (support.enableFireEvents == false)
			return;

		properties.stream()
				.filter(Property::isPresent)
				.forEach(this::fireForProperty);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final int maxLen = 10;
		return name()
				+ " [properties=" + (properties != null ? properties.subList(0, Math.min(properties.size(),
						maxLen)) : null) + "]";
	}
}
