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
package com.slytechs.jnet.jnetruntime.pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.slytechs.jnet.jnetruntime.pipeline.DataProcessor.DataChangeListener;
import com.slytechs.jnet.jnetruntime.pipeline.DataProcessor.DataChangeSupport;
import com.slytechs.jnet.jnetruntime.pipeline.DataType.DataSupport;
import com.slytechs.jnet.jnetruntime.util.Registration;

/**
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 *
 */
class DataList<T> {

	private final DataType type;
	private final DataSupport<T> support;
	private final Class<T> componentType;
	private final List<T> list = new ArrayList<>();
	private final DataChangeSupport<T> changeListeners = new DataChangeSupport<>();
	private T data;

	public DataList(DataType type) {
		this.type = type;
		this.support = type.dataSupport();
		this.componentType = type.dataClass();
	}

	public DataList(DataType type, DataChangeListener<T> listener) {
		this.type = type;
		this.support = type.dataSupport();
		this.componentType = type.dataClass();

		addChangeListener(listener);
	}

	public DataList(DataType type, List<T> sourceList) {
		this.type = type;
		this.support = type.dataSupport();
		this.componentType = type.dataClass();

		list.addAll(sourceList);
	}

	public Registration addChangeListener(DataChangeListener<T> listener) {
		if (list.isEmpty())
			listener.onDataChange(support.empty());

		return changeListeners.addListener(listener);
	}

	private boolean update(boolean b) {
		data = wrap();

		changeListeners.dispatch(data);

		return b;
	}

	private void validateType(T data) throws IllegalArgumentException {

		if (!type.isCompatibleWith(data.getClass()))
			throw new IllegalArgumentException("data parameter [%s] does not match data type [%s] class [%s]"
					.formatted(data.getClass(), type, type.dataClass()));

	}

	private void validateType(Collection<T> collection) throws IllegalArgumentException {

		if (collection instanceof List<T> list && !list.isEmpty()) {
			validateType(list.get(0));

			return;
		}

		Iterator<T> it = collection.iterator();

		if (it.hasNext())
			validateType(it.next());
	}

	public boolean add(T data) {
		validateType(data);

		return update(list.add(data));
	}

	public void addFirst(T data) {
		validateType(data);

		list.addFirst(data);

		update(true);
	}

	public void addLast(T data) {
		validateType(data);

		list.addLast(data);

		update(true);
	}

	public T data() {
		return this.data;
	}

	public boolean addAll(Collection<T> c) {
		validateType(c);

		return update(list.addAll(c));
	}

	public void clear() {
		list.clear();

		update(false);
	}

	public void forEach(Consumer<T> forEach) {
		list.forEach(forEach);
	}

	public T get(int index) {
		return list.get(index);
	}

	public boolean isEmpty() {
		return list.isEmpty();
	}

	public boolean remove(T data) {
		return update(list.remove(data));
	}

	public int size() {
		return list.size();
	}

	public Stream<T> stream() {
		return list.stream();
	}

	public T[] toArray() {
		T[] array = PipelineUtils.newArray(componentType, list.size());

		for (int i = 0; i < list.size(); i++)
			array[i] = list.get(i);

		return array;
	}

	public T wrap() {
		if (list.size() == 1)
			return list.get(0);

		return support.wrapArray(toArray());
	}
}
