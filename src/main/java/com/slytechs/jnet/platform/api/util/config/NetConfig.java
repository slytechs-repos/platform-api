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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

import com.slytechs.jnet.platform.api.util.Unit;

/**
 * @author Mark Bednarczyk
 */
public abstract class NetConfig<T extends NetProperty, T_BASE extends NetConfig<T, T_BASE>>
		implements AutoCloseable {

	public interface NetConfigFactory<T_CFG extends NetConfig<?, T_CFG>> {
		T_CFG newInstance(NetConfig<?, ?> superconfig);
	}

	private final static String NAME = "name";

	private final Properties properties;
	private final String prefix;
	private final T[] propertyTable;

	private final NetConfig<?, ?> superconfig;

	private final NetConfigurator configurator;
	private final Collection<Reference<NetConfig<?, ?>>> commitStack = new HashSet<>();

	public NetConfig(NetConfig<?, ?> superconfig, String prefix, T[] propertyTable) {

		this.superconfig = superconfig;
		this.prefix = prefix;
		this.propertyTable = propertyTable;
		this.properties = new Properties(superconfig.properties());
		this.configurator = superconfig.configurator;

		// Used to call commit on hierarchy of configs, list is cleared after each
		// commit
		superconfig.commitStack.add(new WeakReference<NetConfig<?, ?>>(this));

		propertyDefaults();
	}

	public NetConfig(NetConfigurator configurator, String prefix, T[] propertyTable) {
		this.configurator = configurator;
		this.prefix = prefix;
		this.propertyTable = propertyTable;
		this.properties = new Properties();
		this.superconfig = null;

		propertyDefaults();
	}

	private void propertyDefaults() {
		for (var prop : propertyTable) {
			String name = prop.canonicalName();
			String formatted = prop.formatter().apply(prop.defaultValue());

			SystemProperties.stringValue(name, formatted, properties::setProperty);
		}
	}

	public String canonicalName() {
		var b = new StringBuilder(prefix);

		return b.toString();
	}

	protected final String stringValue(String propertyName) {
		return properties.getProperty(propertyName);
	}

	protected final String stringValue(String propertyName, String newValue) throws NetConfigException {
		var oldValue = (String) properties.setProperty(propertyName, newValue);

		var p = findProperty(propertyName);
		configurator.applyPropertyChanges(this, p, oldValue, newValue);

		return oldValue;
	}

	protected final boolean boolValue(String propertyName, boolean newValue) throws NetConfigException {
		String oldString = (String) properties.setProperty(propertyName, Boolean.toString(newValue));
		boolean oldValue = Boolean.parseBoolean(oldString);

		var p = findProperty(propertyName);

		configurator.applyPropertyChanges(this, p, oldValue, newValue);

		return oldValue;
	}

	protected final boolean boolValue(String propertyName) {
		String old = properties.getProperty(propertyName);

		return Boolean.parseBoolean(old);
	}

	private NetProperty findProperty(String path) throws NetConfigException {
		var p = NetProperty.matchName(path, propertyTable);
		if (p == null)
			throw new NetConfigException("property for path [%s] not found".formatted(path));

		return p;
	}

	public final void commit() throws NetConfigException {
		prepareForCommit();

		configurator.applyConfigChanges(this);

		for (var ent : properties.entrySet()) {

			var p = findProperty((String) ent.getKey());
			var v = ent.getValue();

			System.out.printf("NetConfig::commit path=%s, p=%s, v=%s, map=%s%n",
					ent.getKey(),
					p,
					v,
					properties);

			configurator.applyPropertyChanges(this, p, v, v);
		}

		var exceptionList = new ArrayList<NetConfigException>();
		commitStack.forEach(t -> {
			try {
				var cfg = t.get();
				if (cfg != null)
					cfg.commit();

			} catch (NetConfigException e) {
				exceptionList.add(e);
			}
		});

		if (!exceptionList.isEmpty())
			throw exceptionList.get(0);
	}

	protected void prepareForCommit() throws InvalidConfigParameter {
	}

	public T_BASE enable(boolean b) {
		return us();
	}

	public T_BASE bypass(boolean b) {
		return us();
	}

	public T_BASE name(String newName) {
		properties.setProperty(prefix + NAME, newName);

		return us();
	}

	public String name() {
		return properties.getProperty(prefix + NAME);
	}

	@SuppressWarnings("unchecked")
	private T_BASE us() {
		return (T_BASE) this;
	}

	/**
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public final void close() throws NetConfigException {
		commit();
	}

	public int write(File filename) throws IOException {
		return write(new FileWriter(filename));
	}

	public int read(File filename) throws IOException {
		return read(new FileReader(filename));
	}

	public int write(OutputStream out) throws IOException {
		return write(new OutputStreamWriter(out));
	}

	public int read(InputStream in) throws IOException {
		return read(new InputStreamReader(in));
	}

	public int write(Writer out) throws IOException {
		throw new UnsupportedOperationException();
	}

	public int read(Reader in) throws IOException {
		throw new UnsupportedOperationException();
	}

	Properties properties() {
		return properties;
	}

	protected T_BASE str(T property, String newValue) {
		return us();
	}

	protected T_BASE bool(T property, boolean newValue) {
		return us();
	}

	protected <V extends Number> T_BASE num(T property, V newValue) {
		return us();
	}

	protected <V extends Number> T_BASE num(T property, V newValue, Unit unit) {
		return us();
	}

	protected <T_CFG extends NetConfig<?, T_CFG>> T_CFG newConfig(NetConfigFactory<T_CFG> factory) {
		return factory.newInstance(this);
	}

}
