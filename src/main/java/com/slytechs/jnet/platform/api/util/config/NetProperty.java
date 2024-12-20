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
package com.slytechs.jnet.platform.api.util.config;

import java.util.function.Function;

import com.slytechs.jnet.platform.api.util.config.NetConfigurator.NetEnumPropertyChange;
import com.slytechs.jnet.platform.api.util.config.SystemProperties.SystemPropertyGetter;

/**
 * @author Mark Bednarczyk
 */
public interface NetProperty {

	String NET_PROPERTY_PATH_SEPARATOR = ".";

	static NetProperty matchName(String path, NetProperty[] table) {
		for (var p : table) {
			if (path.endsWith(p.name()))
				return p;
		}

		return null;
	}

	default String canonicalName() {
		var b = new StringBuilder(prefix())
				.append(NET_PROPERTY_PATH_SEPARATOR)
				.append(name());

		return b.toString();
	}

	<T> T defaultValue();

	default <T, T_BASE> NetEnumPropertyChange<T_BASE, T> fieldPropertySetter() {
		return (instance, newValue) -> System.out.printf("NetProperty [name=%s, newValue=%s%n]", name(), newValue);
	}

	default Function<Object, String> formatter() {
		return String::valueOf;
	}

	String name();

	default <T> Function<String, T> parser() {
		throw new UnsupportedOperationException("property [%s] value parser not provided"
				.formatted(name()));
	}

	String prefix();

	default <T> SystemPropertyGetter<T> systemPropertyGetter() {
		return (name, defaultValue) -> defaultValue;
	}

	<T> Class<T> valueType();
}
