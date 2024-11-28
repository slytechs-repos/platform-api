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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.slytechs.jnet.jnetruntime.util.settings.Property.Action;

/**
 * A flexible and extensible settings management system that provides type-safe
 * property handling with support for various data types, default values, and
 * change notifications.
 * 
 * <p>
 * The Settings class serves as a central repository for application
 * configuration, allowing for dynamic property management with type safety and
 * change tracking. It supports various property types including primitives,
 * enums, strings, and lists, with the ability to register listeners for
 * property changes.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>
 * class MySettings extends Settings {
 * 	private final IntProperty port = ofInt("server.port", 8080)
 * 			.on((newValue, source) -> System.out.println("Port changed to: " + newValue));
 * 
 * 	public MySettings() {
 * 		super("MyServerSettings");
 * 		enableUpdates(true);
 * 	}
 * }
 * </pre>
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public class Settings {
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

	// Private fields
	private String name = getClass().getSimpleName();
	private final SettingsSupport settingsSupport = new SettingsSupport();
	private final List<Property<?, ?>> properties = new ArrayList<>();

	/**
	 * Creates a new Settings instance with updates disabled by default.
	 */
	public Settings() {
		settingsSupport.enableFireEvents = false;
	}

	/**
	 * Creates a new Settings instance with the specified name and updates disabled
	 * by default.
	 *
	 * @param name the name of this settings instance, used for identification and
	 *             logging
	 */
	public Settings(String name) {
		this.name = name;
		settingsSupport.enableFireEvents = false;
	}

	/**
	 * Clears all properties in this settings instance, resetting them to their
	 * unset state. This operation removes all current values, effectively returning
	 * properties to their default values if specified.
	 */
	public void clear() {
		for (var p : properties) {
			p.clear();
		}
	}

	/**
	 * Enables or disables property update notifications. When enabled, property
	 * changes will trigger registered listeners.
	 *
	 * @param b true to enable update notifications, false to disable them
	 */
	public synchronized void enableUpdates(boolean b) {
		settingsSupport.enableEventDispatching(b);
	}

	/**
	 * Locates a property by its name in the properties collection.
	 *
	 * @param name the name of the property to find
	 * @return the found property or null if not found
	 */
	@SuppressWarnings("unchecked")
	private <T, P_BASE extends Property<T, P_BASE>> P_BASE findProperty(String name) {
		for (var p : properties) {
			if (p.name().equals(name))
				return (P_BASE) p;
		}
		return null;
	}

	/**
	 * Fires a property change event for the specified property.
	 *
	 * @param property the property for which to fire the change event
	 */
	private void fireForProperty(Property<?, ?> property) {
		settingsSupport.fireValueChange(property.name(), null, property.getValue(), "<refresh>");
	}

	/**
	 * Merges the values from another Settings instance into this one. Only
	 * properties that exist in both instances and have non-null values in the
	 * source instance will be merged.
	 *
	 * @param settingsToBeMerged the source Settings instance whose values should be
	 *                           merged into this one
	 */
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

	/**
	 * Returns the name of this settings instance.
	 *
	 * @return the name of this settings instance
	 */
	public String name() {
		return name;
	}

	/**
	 * Creates or retrieves a property with the specified name using a factory
	 * function.
	 *
	 * @param <T>      the type of the property value
	 * @param <P_BASE> the type of the property
	 * @param name     the name of the property
	 * @param factory  the factory function to create the property if it doesn't
	 *                 exist
	 * @return the existing or newly created property
	 */
	public <T, P_BASE extends Property<T, P_BASE>> P_BASE of(String name, Function<String, P_BASE> factory) {
		P_BASE p = findProperty(name);
		if (p != null)
			return p;

		P_BASE property = Property.of(name, factory);
		property.settingsSupport = this.settingsSupport;
		properties.add(property);
		return property;
	}

	/**
	 * Creates or retrieves a property with the specified name and default value.
	 *
	 * @param <T>          the type of the property value
	 * @param <P_BASE>     the type of the property
	 * @param name         the name of the property
	 * @param defaultValue the default value for the property
	 * @return the existing or newly created property
	 */
	public <T, P_BASE extends Property<T, P_BASE>> P_BASE of(String name, T defaultValue) {
		P_BASE p = findProperty(name);
		if (p != null)
			return p;

		P_BASE property = Property.of(name, defaultValue);
		property.settingsSupport = this.settingsSupport;
		properties.add(property);
		return property;
	}

	/**
	 * Creates or retrieves a property with the specified name, default value, and
	 * factory.
	 *
	 * @param <T>          the type of the property value
	 * @param <P_BASE>     the type of the property
	 * @param name         the name of the property
	 * @param defaultValue the default value for the property
	 * @param factory      the factory function to create the property
	 * @return the existing or newly created property
	 */
	public <T, P_BASE extends Property<T, P_BASE>> P_BASE of(String name, T defaultValue,
			BiFunction<String, T, P_BASE> factory) {
		P_BASE p = findProperty(name);
		if (p != null)
			return p;

		P_BASE property = Property.of(name, defaultValue, factory);
		property.settingsSupport = this.settingsSupport;
		properties.add(property);
		return property;
	}

	/**
	 * Creates a new byte property with the specified name.
	 *
	 * @param name the name of the property
	 * @return a new ByteProperty instance
	 */
	public ByteProperty ofByte(String name) {
		return of(name, ByteProperty::new);
	}

	/**
	 * Creates a new byte property with the specified name and action.
	 *
	 * @param name   the name of the property
	 * @param action the action to be performed when the property changes
	 * @return a new ByteProperty instance
	 */
	public ByteProperty ofByte(String name, Action<Byte> action) {
		return of(name, ByteProperty::new).on(action);
	}

	/**
	 * Creates a new byte property with the specified name and default value.
	 *
	 * @param name         the name of the property
	 * @param defaultValue the default value for the property
	 * @return a new ByteProperty instance
	 */
	public ByteProperty ofByte(String name, byte defaultValue) {
		return of(name, defaultValue, ByteProperty::new);
	}

	/**
	 * Creates a new byte property with the specified name, default value, and
	 * action.
	 *
	 * @param name         the name of the property
	 * @param defaultValue the default value for the property
	 * @param action       the action to be performed when the property changes
	 * @return a new ByteProperty instance
	 */
	public ByteProperty ofByte(String name, byte defaultValue, Action<Byte> action) {
		return of(name, defaultValue, ByteProperty::new).on(action);
	}

	/**
	 * Creates a new double property with the specified name.
	 *
	 * @param name the name of the property
	 * @return a new DoubleProperty instance
	 */
	public DoubleProperty ofDouble(String name) {
		return of(name, DoubleProperty::new);
	}

	/**
	 * Creates a new double property with the specified name and action.
	 *
	 * @param name   the name of the property
	 * @param action the action to be performed when the property changes
	 * @return a new DoubleProperty instance
	 */
	public DoubleProperty ofDouble(String name, Action<Double> action) {
		return of(name, DoubleProperty::new).on(action);
	}

	/**
	 * Creates a new double property with the specified name and default value.
	 *
	 * @param name         the name of the property
	 * @param defaultValue the default value for the property
	 * @return a new DoubleProperty instance
	 */
	public DoubleProperty ofDouble(String name, double defaultValue) {
		return of(name, defaultValue, DoubleProperty::new);
	}

	/**
	 * Creates a new double property with the specified name, default value, and
	 * action.
	 *
	 * @param name         the name of the property
	 * @param defaultValue the default value for the property
	 * @param action       the action to be performed when the property changes
	 * @return a new DoubleProperty instance
	 */
	public DoubleProperty ofDouble(String name, double defaultValue, Action<Double> action) {
		return of(name, defaultValue, DoubleProperty::new).on(action);
	}

	/**
	 * Creates a new enum property with the specified name and enum type.
	 *
	 * @param <E>      the enum type
	 * @param name     the name of the property
	 * @param enumType the Class object of the enum type
	 * @return a new EnumProperty instance
	 */
	public <E extends Enum<E>> EnumProperty<E> ofEnum(String name, Class<E> enumType) {
		return of(name, n -> new EnumProperty<E>(name, enumType));
	}

	/**
	 * Creates a new enum property with the specified name, enum type, and action.
	 *
	 * @param <E>      the enum type
	 * @param name     the name of the property
	 * @param enumType the Class object of the enum type
	 * @param action   the action to be performed when the property changes
	 * @return a new EnumProperty instance
	 */
	public <E extends Enum<E>> EnumProperty<E> ofEnum(String name, Class<E> enumType, Action<E> action) {
		return of(name, n -> new EnumProperty<E>(name, enumType))
				.on(action);
	}

	/**
	 * Creates a new enum property with the specified name and default value.
	 *
	 * @param <E>          the enum type
	 * @param name         the name of the property
	 * @param defaultValue the default enum value
	 * @return a new EnumProperty instance
	 */
	public <E extends Enum<E>> EnumProperty<E> ofEnum(String name, E defaultValue) {
		return of(name, n -> new EnumProperty<E>(name, defaultValue));
	}

	/**
	 * Creates a new enum property with the specified name, default value, and
	 * action.
	 *
	 * @param <E>          the enum type
	 * @param name         the name of the property
	 * @param defaultValue the default enum value
	 * @param action       the action to be performed when the property changes
	 * @return a new EnumProperty instance
	 */
	public <E extends Enum<E>> EnumProperty<E> ofEnum(String name, E defaultValue, Action<E> action) {
		return of(name, n -> new EnumProperty<E>(name, defaultValue))
				.on(action);
	}

	/**
	 * Creates a new float property with the specified name.
	 *
	 * @param name the name of the property
	 * @return a new FloatProperty instance
	 */
	public FloatProperty ofFloat(String name) {
		return of(name, FloatProperty::new);
	}

	/**
	 * Creates a new float property with the specified name and action.
	 *
	 * @param name   the name of the property
	 * @param action the action to be performed when the property changes
	 * @return a new FloatProperty instance
	 */
	public FloatProperty ofFloat(String name, Action<Float> action) {
		return of(name, FloatProperty::new).on(action);
	}

	/**
	 * Creates a new float property with the specified name and default value.
	 *
	 * @param name         the name of the property
	 * @param defaultValue the default value for the property
	 * @return a new FloatProperty instance
	 */
	public FloatProperty ofFloat(String name, float defaultValue) {
		return of(name, defaultValue, FloatProperty::new);
	}

	/**
	 * Creates a new float property with the specified name, default value, and
	 * action.
	 *
	 * @param name         the name of the property
	 * @param defaultValue the default value for the property
	 * @param action       the action to be performed when the property changes
	 * @return a new FloatProperty instance
	 */
	public FloatProperty ofFloat(String name, float defaultValue, Action<Float> action) {
		return of(name, defaultValue, FloatProperty::new).on(action);
	}

	/**
	 * Creates a new integer property with the specified name.
	 *
	 * @param name the name of the property
	 * @return a new IntProperty instance
	 */
	public IntProperty ofInt(String name) {
		return of(name, IntProperty::new);
	}

	/**
	 * Creates a new integer property with the specified name and action.
	 *
	 * @param name   the name of the property
	 * @param action the action to be performed when the property changes
	 * @return a new IntProperty instance
	 */
	public IntProperty ofInt(String name, Action<Integer> action) {
		return of(name, IntProperty::new).on(action);
	}

	/**
	 * Creates a new integer property with the specified name and default value.
	 *
	 * @param name         the name of the property
	 * @param defaultValue the default value for the property
	 * @return a new IntProperty instance
	 */
	public IntProperty ofInt(String name, int defaultValue) {
		return of(name, defaultValue, IntProperty::new);
	}

	/**
	 * Creates a new integer property with the specified name, default value, and
	 * action.
	 *
	 * @param name         the name of the property
	 * @param defaultValue the default value for the property
	 * @param action       the action to be performed when the property changes
	 * @return a new IntProperty instance with the specified configuration
	 */
	public IntProperty ofInt(String name, int defaultValue, Action<Integer> action) {
		return of(name, defaultValue, IntProperty::new).on(action);
	}

	/**
	 * Creates a new long property with the specified name.
	 *
	 * @param name the name of the property
	 * @return a new LongProperty instance
	 */
	public LongProperty ofLong(String name) {
		return of(name, LongProperty::new);
	}

	/**
	 * Creates a new long property with the specified name and action.
	 *
	 * @param name   the name of the property
	 * @param action the action to be performed when the property changes
	 * @return a new LongProperty instance
	 */
	public LongProperty ofLong(String name, Action<Long> action) {
		return of(name, LongProperty::new).on(action);
	}

	/**
	 * Creates a new long property with the specified name and default value.
	 *
	 * @param name         the name of the property
	 * @param defaultValue the default value for the property
	 * @return a new LongProperty instance
	 */
	public LongProperty ofLong(String name, long defaultValue) {
		return of(name, defaultValue, LongProperty::new);
	}

	/**
	 * Creates a new long property with the specified name, default value, and
	 * action.
	 *
	 * @param name         the name of the property
	 * @param defaultValue the default value for the property
	 * @param action       the action to be performed when the property changes
	 * @return a new LongProperty instance
	 */
	public LongProperty ofLong(String name, long defaultValue, Action<Long> action) {
		return of(name, defaultValue, LongProperty::new).on(action);
	}

	/**
	 * Creates a new short property with the specified name.
	 *
	 * @param name the name of the property
	 * @return a new ShortProperty instance
	 */
	public ShortProperty ofShort(String name) {
		return of(name, ShortProperty::new);
	}

	/**
	 * Creates a new short property with the specified name and action.
	 *
	 * @param name   the name of the property
	 * @param action the action to be performed when the property changes
	 * @return a new ShortProperty instance
	 */
	public ShortProperty ofShort(String name, Action<Short> action) {
		return of(name, ShortProperty::new).on(action);
	}

	/**
	 * Creates a new short property with the specified name and default value.
	 *
	 * @param name         the name of the property
	 * @param defaultValue the default value for the property
	 * @return a new ShortProperty instance
	 */
	public ShortProperty ofShort(String name, short defaultValue) {
		return of(name, defaultValue, ShortProperty::new);
	}

	/**
	 * Creates a new short property with the specified name, default value, and
	 * action.
	 *
	 * @param name         the name of the property
	 * @param defaultValue the default value for the property
	 * @param action       the action to be performed when the property changes
	 * @return a new ShortProperty instance
	 */
	public ShortProperty ofShort(String name, short defaultValue, Action<Short> action) {
		return of(name, defaultValue, ShortProperty::new).on(action);
	}

	/**
	 * Creates a new string property with the specified name.
	 *
	 * @param name the name of the property
	 * @return a new StringProperty instance
	 */
	public StringProperty ofString(String name) {
		return of(name, StringProperty::new);
	}

	/**
	 * Creates a new string property with the specified name and action.
	 *
	 * @param name   the name of the property
	 * @param action the action to be performed when the property changes
	 * @return a new StringProperty instance
	 */
	public StringProperty ofString(String name, Action<String> action) {
		return of(name, StringProperty::new).on(action);
	}

	/**
	 * Creates a new string property with the specified name and default value.
	 *
	 * @param name         the name of the property
	 * @param defaultValue the default value for the property
	 * @return a new StringProperty instance
	 */
	public StringProperty ofString(String name, String defaultValue) {
		return of(name, defaultValue, StringProperty::new);
	}

	/**
	 * Creates a new string property with the specified name, default value, and
	 * action.
	 *
	 * @param name         the name of the property
	 * @param defaultValue the default value for the property
	 * @param action       the action to be performed when the property changes
	 * @return a new StringProperty instance
	 */
	public StringProperty ofString(String name, String defaultValue, Action<String> action) {
		return of(name, defaultValue, StringProperty::new).on(action);
	}

	/**
	 * Creates a new list property with the specified name and parser function.
	 *
	 * @param <E>    the type of elements in the list
	 * @param name   the name of the property
	 * @param parser the function to parse string values into list elements
	 * @return a new ListProperty instance
	 */
	public <E> ListProperty<E> ofList(String name, Function<String, E> parser) {
		return of(name, n -> new ListProperty<E>(name, parser));
	}

	/**
	 * Creates a new list property with the specified name, parser function, and
	 * action.
	 *
	 * @param <E>    the type of elements in the list
	 * @param name   the name of the property
	 * @param parser the function to parse string values into list elements
	 * @param action the action to be performed when the property changes
	 * @return a new ListProperty instance
	 */
	public <E> ListProperty<E> ofList(String name, Function<String, E> parser, Action<List<E>> action) {
		return of(name, n -> new ListProperty<E>(name, parser)).on(action);
	}

	/**
	 * Creates a new list property with the specified name, parser function, and
	 * default value.
	 *
	 * @param <E>          the type of elements in the list
	 * @param name         the name of the property
	 * @param parser       the function to parse string values into list elements
	 * @param defaultValue the default list value
	 * @return a new ListProperty instance
	 */
	public <E> ListProperty<E> ofList(String name, Function<String, E> parser, List<E> defaultValue) {
		return of(name, n -> new ListProperty<E>(name, parser, defaultValue));
	}

	/**
	 * Creates a new list property with the specified name, parser function, default
	 * value, and action.
	 *
	 * @param <E>          the type of elements in the list
	 * @param name         the name of the property
	 * @param parser       the function to parse string values into list elements
	 * @param defaultValue the default list value
	 * @param action       the action to be performed when the property changes
	 * @return a new ListProperty instance
	 */
	public <E> ListProperty<E> ofList(String name, Function<String, E> parser, List<E> defaultValue,
			Action<List<E>> action) {
		return of(name, n -> new ListProperty<E>(name, parser, defaultValue));
	}

	/**
	 * Creates a new list property with the specified name, parser function, and
	 * variable number of default values.
	 *
	 * @param <E>          the type of elements in the list
	 * @param name         the name of the property
	 * @param parser       the function to parse string values into list elements
	 * @param defaultValue the default values as varargs
	 * @return a new ListProperty instance
	 */
	@SafeVarargs
	public final <E> ListProperty<E> ofList(String name, Function<String, E> parser, E... defaultValue) {
		return of(name, n -> new ListProperty<E>(name, parser, Arrays.asList(defaultValue)));
	}

	/**
	 * Creates a new list property with the specified name, parser function, action,
	 * and variable number of default values.
	 *
	 * @param <E>          the type of elements in the list
	 * @param name         the name of the property
	 * @param parser       the function to parse string values into list elements
	 * @param action       the action to be performed when the property changes
	 * @param defaultValue the default values as varargs
	 * @return a new ListProperty instance
	 */
	@SafeVarargs
	public final <E> ListProperty<E> ofList(String name, Function<String, E> parser,
			Action<List<E>> action, E... defaultValue) {
		return of(name, n -> new ListProperty<E>(name, parser, Arrays.asList(defaultValue)));
	}

	/**
	 * Refreshes all property values by firing change notifications for all
	 * non-empty properties. This can be used to reinitialize listeners after
	 * enabling updates. Only fires events if updates are enabled.
	 */
	public synchronized void refreshAllProperties() {
		if (settingsSupport.enableFireEvents == false)
			return;

		properties.stream()
				.filter(Property::isPresent)
				.forEach(this::fireForProperty);
	}

	/**
	 * Returns a string representation of this Settings instance, including its name
	 * and a subset of its properties (limited to 10 for readability).
	 *
	 * @return a string representation of this Settings instance
	 */
	@Override
	public String toString() {
		final int maxLen = 10;
		return name()
				+ " [properties=" + (properties != null ? properties.subList(0, Math.min(properties.size(),
						maxLen)) : null) + "]";
	}
}