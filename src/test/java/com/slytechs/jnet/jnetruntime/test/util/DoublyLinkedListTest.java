package com.slytechs.jnet.jnetruntime.test.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.slytechs.jnet.platform.api.util.DoublyLinkedElement;
import com.slytechs.jnet.platform.api.util.DoublyLinkedList;

public class DoublyLinkedListTest {

	private DoublyLinkedList<TestElement> list;

	@BeforeEach
	void setUp() {
		list = new DoublyLinkedList<>();
	}

	// Simple implementation of DoublyLinkedElement for testing
	static class TestElement implements DoublyLinkedElement<TestElement> {
		int value;
		TestElement next;
		TestElement prev;

		TestElement(int value) {
			this.value = value;
		}

		@Override
		public TestElement nextElement() {
			return next;
		}

		@Override
		public TestElement prevElement() {
			return prev;
		}

		@Override
		public void nextElement(TestElement e) {
			this.next = e;
		}

		@Override
		public void prevElement(TestElement e) {
			this.prev = e;
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null || getClass() != obj.getClass())
				return false;
			TestElement that = (TestElement) obj;
			return value == that.value;
		}

		@Override
		public int hashCode() {
			return Integer.hashCode(value);
		}
	}

	@Test
	void testAddingElementsAndBasicOperations() {
		list.add(new TestElement(1));
		list.add(new TestElement(2));
		list.add(new TestElement(3));

		assertEquals(3, list.size());
		assertEquals(1, list.getFirst().value);
		assertEquals(3, list.getLast().value);
	}

	@Test
	void testInsertingElementsAtSpecificPositions() {
		list.add(new TestElement(1));
		list.add(new TestElement(2));
		list.add(new TestElement(3));

		list.add(1, new TestElement(4));
		list.addFirst(new TestElement(0));
		list.addLast(new TestElement(5));

		assertEquals(6, list.size());
		assertEquals(0, list.getFirst().value);
		assertEquals(5, list.getLast().value);
		assertEquals(4, list.get(2).value);
	}

	@Test
	void testRemovingElements() {
		list.add(new TestElement(1));
		list.add(new TestElement(2));
		list.add(new TestElement(3));
		list.add(new TestElement(4));

		list.remove(1);
		assertEquals(3, list.size());
		assertEquals(3, list.get(1).value);

		list.removeFirst();
		list.removeLast();
		assertEquals(1, list.size());
		assertEquals(3, list.getFirst().value);
	}

	@Test
	void testIterator() {
		list.add(new TestElement(1));
		list.add(new TestElement(2));
		list.add(new TestElement(3));

		Iterator<TestElement> iterator = list.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(1, iterator.next().value);
		assertEquals(2, iterator.next().value);
		assertEquals(3, iterator.next().value);
		assertFalse(iterator.hasNext());
	}

	@Test
	void testReverseIterator() {
		list.add(new TestElement(1));
		list.add(new TestElement(2));
		list.add(new TestElement(3));

		Iterator<TestElement> reverseIterator = list.descendingIterator();
		assertTrue(reverseIterator.hasNext());
		assertEquals(3, reverseIterator.next().value);
		assertEquals(2, reverseIterator.next().value);
		assertEquals(1, reverseIterator.next().value);
		assertFalse(reverseIterator.hasNext());
	}

	@Test
	void testQueueOperations() {
		list.offer(new TestElement(1));
		list.offer(new TestElement(2));

		assertEquals(2, list.size());
		assertEquals(1, list.poll().value);
		assertEquals(1, list.size());
		assertEquals(2, list.peek().value);
	}

	@Test
	void testStackOperations() {
		list.push(new TestElement(1));
		list.push(new TestElement(2));

		assertEquals(2, list.size());
		assertEquals(2, list.pop().value);
		assertEquals(1, list.size());
		assertEquals(1, list.peek().value);
	}

	@Test
	void testSearchOperations() {
		TestElement element1 = new TestElement(1);
		TestElement element2 = new TestElement(2);
		TestElement element3 = new TestElement(2);

		list.add(element1);
		list.add(element2);
		list.add(element3);

		assertEquals(0, list.indexOf(element1));
		assertEquals(1, list.indexOf(element2));
		assertEquals(2, list.lastIndexOf(new TestElement(2)));
	}

	@Test
	void testReversedListView() {
		list.add(new TestElement(1));
		list.add(new TestElement(2));
		list.add(new TestElement(3));

		System.out.println("Original list: " + list);

		DoublyLinkedList<TestElement> reversedList = list.reversed();

		System.out.println("Reversed list: " + reversedList);

		assertEquals(3, reversedList.size(), "Reversed list size should match original");
		assertNotNull(reversedList.getFirst(), "First element of reversed list should not be null");
		assertNotNull(reversedList.getLast(), "Last element of reversed list should not be null");

		assertEquals(3, reversedList.getFirst().value, "First element of reversed list should be 3");
		assertEquals(1, reversedList.getLast().value, "Last element of reversed list should be 1");

		reversedList.add(new TestElement(4));
		assertEquals(4, list.size(), "Adding to reversed list should increase original list size");
		assertEquals(4, list.getFirst().value, "New element should be at the start of the original list");
	}

	@Test
	void testEdgeCases() {
		assertThrows(NoSuchElementException.class, () -> list.getFirst());
		assertThrows(NoSuchElementException.class, () -> list.removeLast());

		assertTrue(list.isEmpty());
		assertEquals(0, list.size());

		list.add(new TestElement(1));
		assertFalse(list.isEmpty());
		assertEquals(1, list.size());
	}

	@Test
	void testAddAll() {
		List<TestElement> elements = Arrays.asList(
				new TestElement(1), new TestElement(2), new TestElement(3));
		assertTrue(list.addAll(elements));
		assertEquals(3, list.size());
		assertEquals(1, list.getFirst().value);
		assertEquals(3, list.getLast().value);
	}

	@Test
	void testAddAllAtIndex() {
		list.add(new TestElement(1));
		list.add(new TestElement(4));
		List<TestElement> elements = Arrays.asList(
				new TestElement(2), new TestElement(3));
		assertTrue(list.addAll(1, elements));
		assertEquals(4, list.size());
		assertEquals(2, list.get(1).value);
		assertEquals(3, list.get(2).value);
	}

	@Test
	void testClear() {
		list.add(new TestElement(1));
		list.add(new TestElement(2));
		list.clear();
		assertTrue(list.isEmpty());
		assertEquals(0, list.size());
	}

	@Test
	void testContains() {
		TestElement element = new TestElement(2);
		list.add(new TestElement(1));
		list.add(element);
		list.add(new TestElement(3));
		assertTrue(list.contains(element));
		assertFalse(list.contains(new TestElement(4)));
	}

	@Test
	void testContainsAll() {
		list.add(new TestElement(1));
		list.add(new TestElement(2));
		list.add(new TestElement(3));
		List<TestElement> subList = Arrays.asList(
				new TestElement(1), new TestElement(3));
		assertTrue(list.containsAll(subList));
		subList = Arrays.asList(
				new TestElement(1), new TestElement(4));
		assertFalse(list.containsAll(subList));
	}

	@Test
	void testEquals() {
		list.add(new TestElement(1));
		list.add(new TestElement(2));
		DoublyLinkedList<TestElement> list2 = new DoublyLinkedList<>();
		list2.add(new TestElement(1));
		list2.add(new TestElement(2));
		assertEquals(list, list2);
		list2.add(new TestElement(3));
		assertNotEquals(list, list2);
	}

	@Test
	void testHashCode() {
		list.add(new TestElement(1));
		list.add(new TestElement(2));
		DoublyLinkedList<TestElement> list2 = new DoublyLinkedList<>();
		list2.add(new TestElement(1));
		list2.add(new TestElement(2));

		assertEquals(list.hashCode(), list2.hashCode(), "Equal lists should have the same hash code");

		// Test that different lists have different hash codes
		DoublyLinkedList<TestElement> list3 = new DoublyLinkedList<>();
		list3.add(new TestElement(1));
		list3.add(new TestElement(3));
		assertNotEquals(list.hashCode(), list3.hashCode(), "Different lists should have different hash codes");
	}

	@Test
	void testIndexOfWithDuplicates() {
		list.add(new TestElement(1));
		list.add(new TestElement(2));
		list.add(new TestElement(1));
		assertEquals(0, list.indexOf(new TestElement(1)));
		assertEquals(1, list.indexOf(new TestElement(2)));
	}

	@Test
	void testLastIndexOfWithDuplicates() {
		list.add(new TestElement(1));
		list.add(new TestElement(2));
		list.add(new TestElement(1));
		assertEquals(2, list.lastIndexOf(new TestElement(1)));
		assertEquals(1, list.lastIndexOf(new TestElement(2)));
	}

	@Test
	void testListIteratorWithIndex() {
		list.add(new TestElement(1));
		list.add(new TestElement(2));
		list.add(new TestElement(3));
		ListIterator<TestElement> listIterator = list.listIterator(1);
		assertTrue(listIterator.hasPrevious());
		assertEquals(2, listIterator.next().value);
		assertEquals(3, listIterator.next().value);
		assertFalse(listIterator.hasNext());
	}

	@Test
	void testListIteratorRemove() {
		list.add(new TestElement(1));
		list.add(new TestElement(2));
		list.add(new TestElement(3));
		ListIterator<TestElement> listIterator = list.listIterator();
		listIterator.next();
		listIterator.remove();
		assertEquals(2, list.size());
		assertEquals(2, list.getFirst().value);
	}

	@Test
	void testListIteratorAdd() {
		list.add(new TestElement(1));
		list.add(new TestElement(3));
		ListIterator<TestElement> listIterator = list.listIterator(1);
		listIterator.add(new TestElement(2));
		assertEquals(3, list.size());
		assertEquals(2, list.get(1).value);
	}

	@Test
	void testListIteratorSet() {
		list.add(new TestElement(1));
		list.add(new TestElement(2));
		list.add(new TestElement(3));
		ListIterator<TestElement> listIterator = list.listIterator(1);
		listIterator.next();
		listIterator.set(new TestElement(4));
		assertEquals(4, list.get(1).value);
	}

	@Test
	void testRemoveAll() {
		list.add(new TestElement(1));
		list.add(new TestElement(2));
		list.add(new TestElement(3));
		list.add(new TestElement(4));
		List<TestElement> elementsToRemove = Arrays.asList(
				new TestElement(2), new TestElement(4));
		assertTrue(list.removeAll(elementsToRemove));
		assertEquals(2, list.size());
		assertEquals(1, list.getFirst().value);
		assertEquals(3, list.getLast().value);
	}

	@Test
	void testRetainAll() {
		list.add(new TestElement(1));
		list.add(new TestElement(2));
		list.add(new TestElement(3));
		list.add(new TestElement(4));
		List<TestElement> elementsToRetain = Arrays.asList(
				new TestElement(2), new TestElement(3));
		assertTrue(list.retainAll(elementsToRetain));
		assertEquals(2, list.size());
		assertEquals(2, list.getFirst().value);
		assertEquals(3, list.getLast().value);
	}

	@Test
	void testSubList() {
		list.add(new TestElement(1));
		list.add(new TestElement(2));
		list.add(new TestElement(3));
		list.add(new TestElement(4));
		List<TestElement> subList = list.subList(1, 3);
		assertEquals(2, subList.size());
		assertEquals(2, subList.get(0).value);
		assertEquals(3, subList.get(1).value);
	}

	@Test
	void testToArray() {
		list.add(new TestElement(1));
		list.add(new TestElement(2));
		list.add(new TestElement(3));
		Object[] array = list.toArray();
		assertEquals(3, array.length);
		assertEquals(1, ((TestElement) array[0]).value);
		assertEquals(3, ((TestElement) array[2]).value);
	}

	@Test
	void testToArrayWithType() {
		list.add(new TestElement(1));
		list.add(new TestElement(2));
		list.add(new TestElement(3));
		TestElement[] array = list.toArray(new TestElement[0]);
		assertEquals(3, array.length);
		assertEquals(1, array[0].value);
		assertEquals(3, array[2].value);
	}

	@Test
	void testStream() {
		list.add(new TestElement(1));
		list.add(new TestElement(2));
		list.add(new TestElement(3));
		List<Integer> values = list.stream()
				.map(e -> e.value)
				.collect(Collectors.toList());
		assertEquals(Arrays.asList(1, 2, 3), values);
	}

	@Test
	void testDescendingIteratorWithEmptyList() {
		Iterator<TestElement> iterator = list.descendingIterator();
		assertFalse(iterator.hasNext());
	}
}