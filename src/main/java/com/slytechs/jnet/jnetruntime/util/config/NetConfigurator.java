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
package com.slytechs.jnet.jnetruntime.util.config;

import com.slytechs.jnet.jnetruntime.util.Registration;
import com.slytechs.jnet.jnetruntime.util.config.NetConfigurator.NetPropertyChange.BoolChange;
import com.slytechs.jnet.jnetruntime.util.config.NetConfigurator.NetPropertyChange.NumberChange;
import com.slytechs.jnet.jnetruntime.util.config.NetConfigurator.NetPropertyChange.StringChange;

/**
 * @author Mark Bednarczyk
 */
public interface NetConfigurator {

	interface NetConfigChange {
		void onNetConfigChange(NetConfig<?, ?> changedConfig);
	}

	interface NetEnumRegistration<T> {
		Registration register(NetConfigurator cfg, NetProperty property, NetPropertyChange<T> action);
	}

	interface NetEnumPropertyChange<T1, T2> {
		void execute(T1 instance, T2 newValue);

		default NetPropertyChange<T2> asPropertyChange(T1 instance) {
			return v -> execute(instance, v);
		}
	}

	interface NetPropertyChange<T> {

		interface BoolChange extends NetPropertyChange<Boolean> {
		}

		interface StringChange extends NetPropertyChange<String> {
		}

		interface NumberChange extends NetPropertyChange<Number> {
		}

		default void onNetPropertyChange(NetConfig<?, ?> config, NetProperty property, T oldValue, T newValue) {
			onNetPropertyChange(newValue);
		}

		void onNetPropertyChange(T newValue);

	}

	String name();

	void applyConfigChanges(NetConfig<?, ?> config);

	<T> void applyPropertyChanges(NetConfig<?, ?> config, NetProperty property, T oldValue, T newValue);

	Registration registerConfigListener(NetConfig<?, ?> configInstance, NetConfigChange action);

	Registration registerConfigListener(String configPath, NetConfigChange action);

	Registration onPropertyChange(NetProperty property, NetPropertyChange<?> action);

	default Registration onBooleanChange(NetProperty property, BoolChange action) {
		return onPropertyChange(property, action);
	}

	default Registration onStringChange(NetProperty property, StringChange action) {
		return onPropertyChange(property, action);
	}

	default Registration onNumberChange(NetProperty property, NumberChange action) {
		return onPropertyChange(property, action);
	}

	<T> Registration registerAllPropertyListeners(T targetInstance, NetProperty[] propertyTable);

	<T> void applyAllFieldPropertySetters(T targetInstance, NetProperty[] propertyTable);
}
