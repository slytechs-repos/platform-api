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
 * A specialized list implementation for managing data in the pipeline
 * framework.
 * 
 * <p>
 * This class provides functionality for storing, retrieving, and manipulating
 * data of a specific type within a pipeline. It also supports change
 * notifications and data wrapping based on the associated DataType.
 * </p>
 *
 * @param <T> The type of data stored in this list
 * @author Mark Bednarczyk
 */
class DataList<T> {

	/** The type. */
	private final DataType type;

	/** The settingsSupport. */
	private final DataSupport<T> support;

	/** The component type. */
	private final Class<T> componentType;

	/** The list. */
	private final List<T> list = new ArrayList<>();

	/** The change listeners. */
	private final DataChangeSupport<T> changeListeners = new DataChangeSupport<>();

	/** The data. */
	private T data;

	/**
	 * Constructs a new DataList with the specified DataType.
	 *
	 * @param type The DataType associated with this list
	 */
	public DataList(DataType type) {
		this.type = type;
		this.support = type.dataSupport();
		this.componentType = type.dataClass();
	}

	/**
	 * Constructs a new DataList with the specified DataType and initial change
	 * listener.
	 *
	 * @param type     The DataType associated with this list
	 * @param listener The initial DataChangeListener to be added
	 */
	public DataList(DataType type, DataChangeListener<T> listener) {
		this.type = type;
		this.support = type.dataSupport();
		this.componentType = type.dataClass();
		addChangeListener(listener);
	}

	/**
	 * Constructs a new DataList with the specified DataType and initial data.
	 *
	 * @param type       The DataType associated with this list
	 * @param sourceList The initial list of data to be added
	 */
	public DataList(DataType type, List<T> sourceList) {
		this.type = type;
		this.support = type.dataSupport();
		this.componentType = type.dataClass();
		list.addAll(sourceList);
	}

	/**
	 * Adds a new element to the list.
	 *
	 * @param data The element to be added
	 * @return true if the element was added successfully
	 * @throws IllegalArgumentException if the data type is incompatible
	 */
	public boolean add(T data) {
		validateType(data);
		return update(list.add(data));
	}

	/**
	 * Adds all elements from the specified collection to this list.
	 *
	 * @param c The collection of elements to be added
	 * @return true if the list changed as a result of the call
	 * @throws IllegalArgumentException if any element in the collection has an
	 *                                  incompatible type
	 */
	public boolean addAll(Collection<T> c) {
		validateType(c);
		return update(list.addAll(c));
	}

	/**
	 * Adds a change listener to this DataList.
	 *
	 * @param listener The DataChangeListener to be added
	 * @return A Registration object for unregistering the listener
	 */
	public Registration addChangeListener(DataChangeListener<T> listener) {
//		if (list.isEmpty())
//			listener.onDataChange(settingsSupport.empty());
		return changeListeners.addListener(listener);
	}

	/**
	 * Adds a new element to the beginning of the list.
	 *
	 * @param data The element to be added
	 * @throws IllegalArgumentException if the data type is incompatible
	 */
	public void addFirst(T data) {
		validateType(data);
		list.addFirst(data);
		update(true);
	}

	/**
	 * Adds a new element to the end of the list.
	 *
	 * @param data The element to be added
	 * @throws IllegalArgumentException if the data type is incompatible
	 */
	public void addLast(T data) {
		validateType(data);
		list.addLast(data);
		update(true);
	}

	/**
	 * Removes all elements from this list.
	 */
	public void clear() {
		list.clear();
		update(false);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.List#contains(java.lang.Object)
	 */
	public boolean contains(T o) {
		return list.contains(o);
	}

	/**
	 * Returns the current wrapped data.
	 *
	 * @return The current wrapped data
	 */
	public T data() {
		return this.data;
	}

	/**
	 * Performs the given action for each element of the list.
	 *
	 * @param forEach The action to be performed for each element
	 */
	public void forEach(Consumer<T> forEach) {
		list.forEach(forEach);
	}

	/**
	 * Returns the element at the specified position in this list.
	 *
	 * @param index The index of the element to return
	 * @return The element at the specified position in this list
	 * @throws IndexOutOfBoundsException if the index is out of range
	 */
	public T get(int index) {
		return list.get(index);
	}

	/**
	 * Returns true if this list contains no elements.
	 *
	 * @return true if this list contains no elements
	 */
	public boolean isEmpty() {
		return list.isEmpty();
	}

	/**
	 * Removes the first occurrence of the specified element from this list, if it
	 * is present.
	 *
	 * @param data The element to be removed from this list, if present
	 * @return true if this list contained the specified element
	 */
	public boolean remove(T data) {
		return update(list.remove(data));
	}

	/**
	 * Returns the number of elements in this list.
	 *
	 * @return The number of elements in this list
	 */
	public int size() {
		return list.size();
	}

	/**
	 * Returns a sequential Stream with this collection as its source.
	 *
	 * @return A sequential Stream over the elements in this collection
	 */
	public Stream<T> stream() {
		return list.stream();
	}

	/**
	 * Returns an array containing all of the elements in this list in proper
	 * sequence.
	 *
	 * @return An array containing all of the elements in this list in proper
	 *         sequence
	 */
	public T[] toArray() {
		T[] array = PipelineUtils.newArray(componentType, list.size());
		for (int i = 0; i < list.size(); i++)
			array[i] = list.get(i);
		return array;
	}

	/**
	 * Update.
	 *
	 * @param b the b
	 * @return true, if successful
	 */
	private boolean update(boolean b) {
		data = wrap();
		changeListeners.dispatch(data);
		return b;
	}

	/**
	 * Validate type.
	 *
	 * @param collection the collection
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	private void validateType(Collection<T> collection) throws IllegalArgumentException {
		if (collection instanceof List<T> list && !list.isEmpty()) {
			validateType(list.get(0));
			return;
		}
		Iterator<T> it = collection.iterator();
		if (it.hasNext())
			validateType(it.next());
	}

	/**
	 * Validate type.
	 *
	 * @param data the data
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	private void validateType(T data) throws IllegalArgumentException {
		if (!type.isCompatibleWith(data.getClass()))
			throw new IllegalArgumentException("data parameter [%s] does not match data type [%s] class [%s]"
					.formatted(data.getClass(), type, type.dataClass()));
	}

	/**
	 * Wraps the current list data based on the associated DataType.
	 *
	 * @return The wrapped data
	 */
	public T wrap() {
		if (list.size() == 1)
			return list.get(0);
		return support.wrapArray(toArray());
	}
}