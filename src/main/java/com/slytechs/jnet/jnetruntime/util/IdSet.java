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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Represents a collection of port identifiers with settingsSupport for various set
 * operations. This interface provides methods to create and manipulate
 * collections of port IDs and supports operations such as union, intersection,
 * difference, and symmetric difference.
 */
public interface IdSet<T_BASE extends IdSet<T_BASE>> extends Iterable<Integer> {
	int[] EMPTY_ARRAY = {};

	default int at(int index) {
		var array = toArray();
		Objects.checkIndex(index, array.length);

		return array[index];
	}

	default T_BASE clear() {
		return fromArray(EMPTY_ARRAY);
	}

	default boolean contains(int id) {
		for (int i : toArray())
			if (i == id)
				return true;

		return false;
	}

	/**
	 * Returns the difference of this {@link IdSet} and another {@link IdSet}. The
	 * resulting set contains the ports that are in this set but not in the other.
	 *
	 * @param other the other {@link IdSet} to subtract from this set.
	 * @return a new {@link IdSet} representing the difference of the two sets.
	 */
	default T_BASE difference(T_BASE other) {
		Set<Integer> differenceSet = new HashSet<>();
		for (int port : this.toArray()) {
			if (Arrays.stream(other.toArray()).noneMatch(p -> p == port)) {
				differenceSet.add(port);
			}
		}
		return fromArray(differenceSet.stream().mapToInt(Integer::intValue).toArray());
	}

	/**
	 * Must override and return the BASE type of this subclassed interface.
	 * 
	 * @param ids the ids to return the set of
	 * @return the implementing subclass for this IdSet
	 */
	T_BASE fromArray(int... ids);

	default int idAtHashIndex(int hashValue) {
		var array = toArray();
		int indx = hashValue % array.length;

		return array[indx];
	}

	/**
	 * Returns the intersection of this {@link IdSet} and another {@link IdSet}. The
	 * resulting set contains only the ports that are present in both sets.
	 *
	 * @param other the other {@link IdSet} to intersect with this set.
	 * @return a new {@link IdSet} representing the intersection of the two sets.
	 */
	default T_BASE intersection(T_BASE other) {
		Set<Integer> intersectionSet = new HashSet<>();
		for (int port : this.toArray()) {
			if (Arrays.stream(other.toArray()).anyMatch(p -> p == port)) {
				intersectionSet.add(port);
			}
		}
		return fromArray(intersectionSet.stream().mapToInt(Integer::intValue).toArray());
	}

	default boolean isEmpty() {
		return toArray().length == 0;
	}

	/**
	 * Returns an iterator over the ports in this {@link IdSet}. The iteration order
	 * corresponds to the order of ports in the underlying array.
	 *
	 * @return an iterator over the port IDs.
	 */
	@Override
	default Iterator<Integer> iterator() {
		return new Iterator<Integer>() {
			int[] ports = IdSet.this.toArray();
			int i = 0;

			@Override
			public boolean hasNext() {
				return i < ports.length;
			}

			@Override
			public Integer next() {
				return ports[i++];
			}
		};
	}

	default int length() {
		return toArray().length;
	}

	default List<Integer> list() {
		List<Integer> list = new ArrayList<>(length());

		for (var i : this)
			list.add(i);

		return list;
	}

	default <T> Iterable<T> map(List<T> from) {
		var it = iterator();

		return () -> new Iterator<T>() {

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public T next() {
				return from.get(it.next());
			}
		};
	}

	default <T> Iterable<T> map(T[] from) {
		var it = iterator();

		return () -> new Iterator<T>() {

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public T next() {
				return from[it.next()];
			}
		};
	}

	default <T> List<T> mapToList(List<T> from) {
		List<T> newList = new ArrayList<>(length());

		for (var i : this)
			newList.add(from.get(i));

		return newList;
	}

	default <T> List<T> mapToList(T[] from) {
		return Arrays.asList(toArray(from));
	}

	default <T> Set<T> mapToSet(T[] from) {
		return new HashSet<T>(mapToList(from));
	}

	default Set<Integer> set() {
		Set<Integer> set = new HashSet<>(length());

		for (var i : this)
			set.add(i);

		return set;
	}

	default <T> Set<T> set(List<T> from) {
		Set<T> newSet = new HashSet<>(length());

		for (var i : this)
			newSet.add(from.get(i));

		return newSet;
	}

	default IntStream stream() {
		return IntStream.of(toArray());
	}

	default <T> Stream<T> stream(List<T> from) {
		return mapToList(from).stream();
	}

	default <T> Stream<T> stream(T[] from) {
		return Stream.of(toArray(from));
	}

	/**
	 * Returns the symmetric difference of this {@link IdSet} and another
	 * {@link IdSet}. The resulting set contains the ports that are in either of the
	 * sets but not in both.
	 *
	 * @param other the other {@link IdSet} to calculate the symmetric difference
	 *              with.
	 * @return a new {@link IdSet} representing the symmetric difference of the two
	 *         sets.
	 */
	default T_BASE symmetricDifference(T_BASE other) {
		Set<Integer> symmetricSet = new HashSet<>();
		for (int port : this.toArray()) {
			if (Arrays.stream(other.toArray()).noneMatch(p -> p == port)) {
				symmetricSet.add(port);
			}
		}
		for (int port : other.toArray()) {
			if (Arrays.stream(this.toArray()).noneMatch(p -> p == port)) {
				symmetricSet.add(port);
			}
		}
		return fromArray(symmetricSet.stream().mapToInt(Integer::intValue).toArray());
	}

	/**
	 * Converts the {@link IdSet} to an array of integers.
	 *
	 * @return an array containing all port IDs in this {@link IdSet}.
	 */
	int[] toArray();

	default <T> T[] toArray(T[] from) {
		var newArray = Arrays.copyOf(from, length());

		int indx = 0;
		for (var i : this)
			newArray[indx++] = from[i];

		return newArray;
	}

	/**
	 * Returns the union of this {@link IdSet} and another {@link IdSet}. The
	 * resulting set contains all unique ports from both sets.
	 *
	 * @param other the other {@link IdSet} to combine with this set.
	 * @return a new {@link IdSet} representing the union of the two sets.
	 */
	default T_BASE union(T_BASE other) {
		Set<Integer> unionSet = new HashSet<>();
		for (int port : this.toArray()) {
			unionSet.add(port);
		}
		for (int port : other.toArray()) {
			unionSet.add(port);
		}
		return fromArray(unionSet.stream().mapToInt(Integer::intValue).toArray());
	}
}
