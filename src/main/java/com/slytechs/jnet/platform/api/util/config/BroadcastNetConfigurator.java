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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.slytechs.jnet.platform.api.util.Registration;

/**
 * @author Mark Bednarczyk
 */
public class BroadcastNetConfigurator implements NetConfigurator {

	private class ConfigEntry {

		final NetConfigChange action;

		public ConfigEntry(NetConfigChange action) {
			this.action = action;
		}

	}

	private class PropertyEntry {

		final NetPropertyChange<?> action;

		public PropertyEntry(NetPropertyChange<?> action) {
			this.action = action;
		}
	}

	private final Map<String, List<ConfigEntry>> configMap = new HashMap<>();
	private final Map<String, List<PropertyEntry>> propertyMap = new HashMap<>();
	private final Lock readLock;
	private final Lock writeLock;
	private final String name;

	/**
	 * 
	 */
	public BroadcastNetConfigurator(String name) {
		this.name = name;

		var rwLock = new ReentrantReadWriteLock();
		this.readLock = rwLock.readLock();
		this.writeLock = rwLock.writeLock();
	}

	/**
	 * @see com.slytechs.jnet.platform.api.util.config.NetConfigurator#applyConfigChanges(com.slytechs.jnet.platform.api.util.config.NetConfig)
	 */
	@Override
	public void applyConfigChanges(NetConfig<?, ?> config) {
		readLock.lock();
		try {
			if (!configMap.containsKey(config.canonicalName()))
				return;

			var list = configMap.get(config.canonicalName());

			for (var e : list)
				e.action.onNetConfigChange(config);

		} finally {
			readLock.unlock();
		}
	}

	/**
	 * @see com.slytechs.jnet.platform.api.util.config.NetConfigurator#applyPropertyChanges(com.slytechs.jnet.platform.api.util.config.NetConfig,
	 *      com.slytechs.jnet.platform.api.util.config.NetProperty, java.lang.Object,
	 *      java.lang.Object)
	 */
	@SuppressWarnings({
			"unchecked",
			"rawtypes"
	})
	@Override
	public <T> void applyPropertyChanges(NetConfig<?, ?> config, NetProperty property, T oldValue, T newValue) {
		readLock.lock();
		try {
			if (!propertyMap.containsKey(property.canonicalName()))
				return;

			var list = propertyMap.get(property.canonicalName());

			for (var e : list)
				((NetPropertyChange) e.action).onNetPropertyChange(config, property, oldValue, newValue);

		} finally {
			readLock.unlock();
		}
	}

	/**
	 * @see com.slytechs.jnet.platform.api.util.config.NetConfigurator#name()
	 */
	@Override
	public String name() {
		return name;
	}

	/**
	 * @see com.slytechs.jnet.platform.api.util.config.NetConfigurator#registerConfigListener(com.slytechs.jnet.platform.api.util.config.NetConfig,
	 *      com.slytechs.jnet.platform.api.util.config.NetConfigurator.NetConfigChange)
	 */
	@Override
	public Registration registerConfigListener(NetConfig<?, ?> configInstance, NetConfigChange action) {
		return registerConfigListener(configInstance.canonicalName(), action);
	}

	/**
	 * @see com.slytechs.jnet.platform.api.util.config.NetConfigurator#registerConfigListener(java.lang.String,
	 *      com.slytechs.jnet.platform.api.util.config.NetConfigurator.NetPropertyChange)
	 */
	@Override
	public Registration registerConfigListener(String configPath, NetConfigChange action) {
		writeLock.lock();
		try {
			if (!configMap.containsKey(configPath)) {
				var list = new ArrayList<ConfigEntry>();

				configMap.put(configPath, list);
			}

			var list = configMap.get(configPath);
			var entry = new ConfigEntry(action);

			list.add(entry);

			return () -> unregisterConfig(configPath, list, entry);

		} finally {
			writeLock.unlock();
		}
	}

	private void unregisterConfig(String path, List<ConfigEntry> list, ConfigEntry entry) {
		writeLock.lock();
		try {
			list.remove(entry);

			// Clean up if no more registrations
			if (list.isEmpty())
				configMap.remove(path);

		} finally {
			writeLock.unlock();
		}
	}

	private void unregisterProperty(String path, List<PropertyEntry> list, PropertyEntry entry) {
		writeLock.lock();
		try {
			list.remove(entry);

			// Clean up if no more registrations
			if (list.isEmpty())
				propertyMap.remove(path);

		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * @see com.slytechs.jnet.platform.api.util.config.NetConfigurator#onPropertyChange(com.slytechs.jnet.platform.api.util.config.NetProperty,
	 *      com.slytechs.jnet.platform.api.util.config.NetConfigurator.NetPropertyChange)
	 */
	@Override
	public Registration onPropertyChange(NetProperty property, NetPropertyChange<?> action) {
		String path = property.canonicalName();

		writeLock.lock();
		try {
			if (!propertyMap.containsKey(path)) {
				var list = new ArrayList<PropertyEntry>();

				propertyMap.put(path, list);
			}

			var list = propertyMap.get(path);
			var entry = new PropertyEntry(action);

			list.add(entry);

			return () -> unregisterProperty(path, list, entry);

		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * @see com.slytechs.jnet.platform.api.util.config.NetConfigurator#registerAllPropertyListeners(java.lang.Object,
	 *      com.slytechs.jnet.platform.api.util.config.NetProperty[])
	 */
	@Override
	public <T> Registration registerAllPropertyListeners(T targetInstance, NetProperty[] propertyTable) {
		var list = new ArrayList<Registration>();

		for (var p : propertyTable) {
			var r = onPropertyChange(p, p.fieldPropertySetter().asPropertyChange(targetInstance));
			list.add(r);
		}

		/*
		 * Return a single registration that unregisteres all, using thread safe
		 * implementation
		 */
		return () -> {
			writeLock.lock(); // We're modifying the configurator's registrations
			try {
				for (var r : list)
					r.unregister();

			} finally {
				writeLock.unlock();
			}
		};
	}

	/**
	 * @see com.slytechs.jnet.platform.api.util.config.NetConfigurator#applyAllFieldPropertySetters(java.lang.Object,
	 *      com.slytechs.jnet.platform.api.util.config.NetProperty[])
	 */
	@Override
	public <T> void applyAllFieldPropertySetters(T targetInstance, NetProperty[] propertyTable) {
		for (var p : propertyTable) {
			var defaultValue = p.systemPropertyGetter()
					.getProperty(p.canonicalName(), p.defaultValue());

			p.fieldPropertySetter().execute(targetInstance, defaultValue);
		}
	}

}
