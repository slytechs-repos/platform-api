package com.slytechs.jnet.jnetruntime.test.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.slytechs.jnet.platform.api.util.DoublyLinkedElement;
import com.slytechs.jnet.platform.api.util.DoublyLinkedPriorityQueue;

/**
 * JUnit test case for the DoublyLinkedPriorityQueue class. This class contains
 * a comprehensive set of tests to verify the correctness of the
 * DoublyLinkedPriorityQueue implementation.
 */
class DoublyLinkedPriorityQueueTest {

	private DoublyLinkedPriorityQueue<TestElement> queue;

	/**
	 * Sets up the test fixture. Called before every test case method.
	 */
	@BeforeEach
	void setUp() {
		queue = new DoublyLinkedPriorityQueue<>();
	}

	/**
	 * Test element class that implements both Comparable and DoublyLinkedElement
	 * interfaces.
	 */
	static class TestElement implements Comparable<TestElement>, DoublyLinkedElement<TestElement> {
		int value;
		TestElement next;
		TestElement prev;

		TestElement(int value) {
			this.value = value;
		}

		@Override
		public int compareTo(TestElement other) {
			return Integer.compare(this.value, other.value);
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
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			TestElement that = (TestElement) o;
			return value == that.value;
		}

		@Override
		public int hashCode() {
			return Objects.hash(value);
		}

		@Override
		public String toString() {
			return "TestElement{value=" + value + "}";
		}
	}

	/**
	 * Tests the offer and poll operations of the queue. Verifies that elements are
	 * properly ordered and can be retrieved in the correct order.
	 */
	@Test
	void testOfferAndPoll() {
		queue.offer(new TestElement(3));
		queue.offer(new TestElement(1));
		queue.offer(new TestElement(2));

		assertEquals(1, queue.poll().value);
		assertEquals(2, queue.poll().value);
		assertEquals(3, queue.poll().value);
		assertNull(queue.poll());
	}

	/**
	 * Tests the peek operation of the queue. Verifies that peek returns the correct
	 * element without removing it.
	 */
	@Test
	void testPeek() {
		assertNull(queue.peek());
		queue.offer(new TestElement(1));
		assertEquals(1, queue.peek().value);
		queue.offer(new TestElement(2));
		assertEquals(1, queue.peek().value);
	}

	/**
	 * Tests the size method of the queue. Verifies that the size is correctly
	 * updated as elements are added and removed.
	 */
	@Test
	void testSize() {
		assertEquals(0, queue.size());
		queue.offer(new TestElement(1));
		assertEquals(1, queue.size());
		queue.offer(new TestElement(2));
		assertEquals(2, queue.size());
		queue.poll();
		assertEquals(1, queue.size());
	}

	/**
	 * Tests the isEmpty method of the queue. Verifies that isEmpty returns the
	 * correct boolean value based on the queue's state.
	 */
	@Test
	void testIsEmpty() {
		assertTrue(queue.isEmpty());
		queue.offer(new TestElement(1));
		assertFalse(queue.isEmpty());
		queue.poll();
		assertTrue(queue.isEmpty());
	}

	/**
	 * Tests the clear method of the queue. Verifies that clear removes all elements
	 * from the queue.
	 */
	@Test
	void testClear() {
		queue.offer(new TestElement(1));
		queue.offer(new TestElement(2));
		assertFalse(queue.isEmpty());
		queue.clear();
		assertTrue(queue.isEmpty());
		assertEquals(0, queue.size());
	}

	/**
	 * Tests the contains method of the queue. Verifies that contains correctly
	 * identifies whether an element is in the queue.
	 */
	@Test
	void testContains() {
		TestElement element = new TestElement(2);
		assertFalse(queue.contains(element));
		queue.offer(element);
		assertTrue(queue.contains(element));
		queue.poll();
		assertFalse(queue.contains(element));
	}

	/**
	 * Tests the iterator of the queue. Verifies that the iterator returns elements
	 * in the correct order.
	 */
	@Test
	void testIterator() {
		queue.offer(new TestElement(3));
		queue.offer(new TestElement(1));
		queue.offer(new TestElement(2));

		Iterator<TestElement> it = queue.iterator();
		assertEquals(1, it.next().value);
		assertEquals(2, it.next().value);
		assertEquals(3, it.next().value);
		assertFalse(it.hasNext());
	}

	/**
	 * Tests the toArray method of the queue. Verifies that toArray returns an array
	 * containing all elements in the correct order.
	 */
	@Test
	void testToArray() {
		queue.offer(new TestElement(3));
		queue.offer(new TestElement(1));
		queue.offer(new TestElement(2));

		Object[] array = queue.toArray();
		assertEquals(3, array.length);
		assertEquals(1, ((TestElement) array[0]).value);
		assertEquals(2, ((TestElement) array[1]).value);
		assertEquals(3, ((TestElement) array[2]).value);
	}

	/**
	 * Tests the toArray(T[] a) method of the queue. Verifies that toArray(T[] a)
	 * returns an array of the correct type containing all elements in the correct
	 * order.
	 */
	@Test
	void testToArrayWithParameter() {
		queue.offer(new TestElement(3));
		queue.offer(new TestElement(1));
		queue.offer(new TestElement(2));

		TestElement[] array = queue.toArray(new TestElement[0]);
		assertEquals(3, array.length);
		assertEquals(1, array[0].value);
		assertEquals(2, array[1].value);
		assertEquals(3, array[2].value);
	}

	/**
	 * Tests the remove method of the queue. Verifies that remove correctly removes
	 * a specific element from the queue.
	 */
	@Test
	void testRemove() {
		TestElement element = new TestElement(2);
		queue.offer(new TestElement(1));
		queue.offer(element);
		queue.offer(new TestElement(3));

		assertTrue(queue.remove(element));
		assertFalse(queue.contains(element));
		assertEquals(2, queue.size());
	}

	/**
	 * Tests the remove method of the queue with a non-existent element. Verifies
	 * that remove returns false when trying to remove an element not in the queue.
	 */
	@Test
	void testRemoveNonExistent() {
		queue.offer(new TestElement(1));
		queue.offer(new TestElement(2));

		assertFalse(queue.remove(new TestElement(3)));
		assertEquals(2, queue.size());
	}

	/**
	 * Tests offering a null element to the queue. Verifies that offering null
	 * throws a NullPointerException.
	 */
	@Test
	void testOfferNull() {
		assertThrows(NullPointerException.class, () -> queue.offer(null));
	}

	/**
	 * Tests polling from an empty queue. Verifies that polling from an empty queue
	 * returns null.
	 */
	@Test
	void testPollEmpty() {
		assertNull(queue.poll());
	}

	/**
	 * Tests peeking at an empty queue. Verifies that peeking at an empty queue
	 * returns null.
	 */
	@Test
	void testPeekEmpty() {
		assertNull(queue.peek());
	}

	/**
	 * Tests the remove operation of the queue's iterator. Verifies that the
	 * iterator's remove method correctly removes elements from the queue.
	 */
	@Test
	void testIteratorRemove() {
		queue.offer(new TestElement(1));
		queue.offer(new TestElement(2));
		queue.offer(new TestElement(3));

		Iterator<TestElement> it = queue.iterator();
		it.next();
		it.remove();
		assertEquals(2, queue.size());
		assertEquals(2, queue.peek().value);
	}

	/**
	 * Tests for concurrent modification exception with the iterator. Verifies that
	 * modifying the queue while iterating throws a ConcurrentModificationException.
	 */
	@Test
	void testIteratorConcurrentModification() {
		queue.offer(new TestElement(1));
		queue.offer(new TestElement(2));

		Iterator<TestElement> it = queue.iterator();
		queue.offer(new TestElement(3));

		assertThrows(ConcurrentModificationException.class, it::next);
	}

	/**
	 * Tests the queue with a custom comparator. Verifies that the queue correctly
	 * orders elements according to the custom comparator.
	 */
	@Test
	void testCustomComparator() {
		DoublyLinkedPriorityQueue<TestElement> customQueue = new DoublyLinkedPriorityQueue<>(
				(a, b) -> Integer.compare(b.value, a.value) // Reverse order
		);

		customQueue.offer(new TestElement(3));
		customQueue.offer(new TestElement(1));
		customQueue.offer(new TestElement(2));

		assertEquals(3, customQueue.poll().value);
		assertEquals(2, customQueue.poll().value);
		assertEquals(1, customQueue.poll().value);
	}

	/**
	 * Tests offering multiple elements and then polling all elements from the
	 * queue. Verifies that elements are retrieved in the correct order.
	 */
	@Test
	void testOfferAllAndPollAll() {
		List<TestElement> elements = Arrays.asList(
				new TestElement(5), new TestElement(2), new TestElement(8),
				new TestElement(1), new TestElement(9), new TestElement(3));

		elements.forEach(queue::offer);

		List<Integer> polledValues = new ArrayList<>();
		while (!queue.isEmpty()) {
			polledValues.add(queue.poll().value);
		}

		assertEquals(Arrays.asList(1, 2, 3, 5, 8, 9), polledValues);
	}

	/**
	 * Tests the removeAll method of the queue. Verifies that removeAll correctly
	 * removes all specified elements from the queue.
	 */
	@Test
	void testRemoveAll() {
		queue.offer(new TestElement(1));
		queue.offer(new TestElement(2));
		queue.offer(new TestElement(3));

		List<TestElement> toRemove = Arrays.asList(new TestElement(1), new TestElement(3));
		assertTrue(queue.removeAll(toRemove));
		assertEquals(1, queue.size());
		assertEquals(2, queue.peek().value);
	}

	/**
	 * Tests the retainAll method of the queue. Verifies that retainAll correctly
	 * retains only the specified elements in the queue.
	 */
	@Test
	void testRetainAll() {
		queue.offer(new TestElement(1));
		queue.offer(new TestElement(2));
		queue.offer(new TestElement(3));

		List<TestElement> toRetain = Arrays.asList(new TestElement(1), new TestElement(2));
		assertTrue(queue.retainAll(toRetain));
		assertEquals(2, queue.size());
		assertTrue(queue.contains(new TestElement(1)));
		assertTrue(queue.contains(new TestElement(2)));
		assertFalse(queue.contains(new TestElement(3)));
	}

	/**
	 * Tests the addAll method of the queue. Verifies that addAll correctly adds all
	 * elements from a collection to the queue.
	 */
	@Test
	void testAddAll() {
		List<TestElement> toAdd = Arrays.asList(new TestElement(3), new TestElement(1), new TestElement(2));
		assertTrue(queue.addAll(toAdd));
		assertEquals(3, queue.size());
		assertEquals(1, queue.peek().value);
	}

	/**
	 * Tests the containsAll method of the queue. Verifies that containsAll
	 * correctly identifies whether the queue contains all elements from a
	 * collection.
	 */
	@Test
	void testContainsAll() {
		queue.offer(new TestElement(1));
		queue.offer(new TestElement(2));
		queue.offer(new TestElement(3));

		List<TestElement> subSet = Arrays.asList(new TestElement(1), new TestElement(3));
		assertTrue(queue.containsAll(subSet));

		subSet = Arrays.asList(new TestElement(1), new TestElement(4));
		assertFalse(queue.containsAll(subSet));
	}

	/**
	 * Tests removing all elements using the iterator. Verifies that all elements
	 * can be removed from the queue using the iterator's remove method.
	 */
	@Test
	void testIteratorRemoveAll() {
		queue.offer(new TestElement(1));
		queue.offer(new TestElement(2));
		queue.offer(new TestElement(3));

		Iterator<TestElement> it = queue.iterator();
		while (it.hasNext()) {
			it.next();
			it.remove();
		}

		assertTrue(queue.isEmpty());
	}

	/**
	 * Tests offering and polling a large number of elements. Verifies that the
	 * queue correctly handles a large number of elements.
	 */
	@Test
	void testOfferAndPollLargeNumber() {
		int n = 10000;
		for (int i = n; i > 0; i--) {
			queue.offer(new TestElement(i));
		}

		for (int i = 1; i <= n; i++) {
			assertEquals(i, queue.poll().value);
		}

		assertTrue(queue.isEmpty());
	}

	/**
	 * Tests the removeIf method of the queue. Verifies that removeIf correctly
	 * removes elements that satisfy the given predicate.
	 */
	@Test
	void testRemoveIfPredicate() {
		queue.offer(new TestElement(1));
		queue.offer(new TestElement(2));
		queue.offer(new TestElement(3));
		queue.offer(new TestElement(4));

		queue.removeIf(e -> e.value % 2 == 0);

		assertEquals(2, queue.size());
		assertEquals(1, queue.poll().value);
		assertEquals(3, queue.poll().value);
	}

	/**
	 * Tests the forEach method of the queue. Verifies that forEach correctly
	 * applies the given action to each element in the queue.
	 */
	@Test
	void testForEachConsumer() {
		queue.offer(new TestElement(3));
		queue.offer(new TestElement(1));
		queue.offer(new TestElement(2));

		List<Integer> values = new ArrayList<>();
		queue.forEach(e -> values.add(e.value));

		assertEquals(Arrays.asList(1, 2, 3), values);
	}

	/**
	 * Tests the spliterator of the queue. Verifies that the spliterator correctly
	 * iterates over all elements in the queue.
	 */
	@Test
	void testSpliterator() {
		queue.offer(new TestElement(3));
		queue.offer(new TestElement(1));
		queue.offer(new TestElement(2));

		Spliterator<TestElement> spliterator = queue.spliterator();
		List<Integer> values = new ArrayList<>();
		spliterator.forEachRemaining(e -> values.add(e.value));

		assertEquals(Arrays.asList(1, 2, 3), values);
	}

	/**
	 * Tests the behavior of the queue when elements are added in reverse order.
	 * Verifies that the queue correctly reorders the elements.
	 */
	@Test
	void testReverseOrderInsertion() {
		for (int i = 10; i > 0; i--) {
			queue.offer(new TestElement(i));
		}

		for (int i = 1; i <= 10; i++) {
			assertEquals(i, queue.poll().value);
		}
	}

	/**
	 * Tests the integrity of the queue's structure after multiple operations. This
	 * test verifies that the queue maintains correct next and prev references.
	 */
	@Test
	void testQueueStructureIntegrity() {
		queue.offer(new TestElement(3));
		queue.offer(new TestElement(1));
		queue.offer(new TestElement(4));
		queue.offer(new TestElement(2));

		assertEquals(4, queue.size(), "Queue should contain 4 elements");

		TestElement first = queue.poll();
		assertEquals(1, first.value, "First element should be 1");
		assertNull(first.prevElement(), "First element's prev should be null");
		assertNull(first.nextElement(), "First element's next should be null after removal");

		TestElement second = queue.poll();
		assertEquals(2, second.value, "Second element should be 2");
		assertNull(second.prevElement(), "Second element's prev should be null");
		assertNull(second.nextElement(), "Second element's next should be null after removal");

		assertEquals(2, queue.size(), "Queue should now contain 2 elements");
	}

	/**
	 * Tests the queue's behavior when elements are added and removed in various
	 * orders. This test helps verify that the queue maintains correct structure and
	 * order.
	 */
	@Test
	void testMixedOperations() {
		queue.offer(new TestElement(3));
		queue.offer(new TestElement(1));
		queue.offer(new TestElement(4));
		assertEquals(1, queue.poll().value);
		queue.offer(new TestElement(2));
		assertEquals(2, queue.poll().value);
		assertEquals(3, queue.poll().value);
		queue.offer(new TestElement(5));
		assertEquals(4, queue.poll().value);
		assertEquals(5, queue.poll().value);
		assertTrue(queue.isEmpty());
	}

	/**
	 * Tests the queue's behavior with a large number of elements. This test helps
	 * verify that the queue can handle a significant number of elements without
	 * issues in its internal structure.
	 */
	@Test
	void testLargeNumberOfElements() {
		int elementCount = 10000;
		for (int i = elementCount; i > 0; i--) {
			queue.offer(new TestElement(i));
		}

		assertEquals(elementCount, queue.size(), "Queue should contain all added elements");

		for (int i = 1; i <= elementCount; i++) {
			TestElement element = queue.poll();
			assertNotNull(element, "Element should not be null");
			assertEquals(i, element.value, "Elements should be polled in ascending order");
		}

		assertTrue(queue.isEmpty(), "Queue should be empty after polling all elements");
	}

	@Test
	void testMultipleOfferSimilarElements() {
		queue.offer(new TestElement(1));
		queue.offer(new TestElement(1));
		queue.offer(new TestElement(1));

		assertEquals(3, queue.size(), "Queue size should be 3 after offering three similar elements");
		assertEquals(1, queue.poll().value, "First polled element should have value 1");
		assertEquals(1, queue.poll().value, "Second polled element should have value 1");
		assertEquals(1, queue.poll().value, "Third polled element should have value 1");
		assertTrue(queue.isEmpty(), "Queue should be empty after polling all elements");
	}

	@Test
	void testEqualPriority() {
		queue.offer(new TestElement(1));
		queue.offer(new TestElement(1));
		queue.offer(new TestElement(1));

		assertEquals(3, queue.size());
		assertEquals(1, queue.poll().value);
		assertEquals(1, queue.poll().value);
		assertEquals(1, queue.poll().value);
	}

	@Test
	void testOfferDuplicates() {
		queue.offer(new TestElement(1));
		queue.offer(new TestElement(1));
		queue.offer(new TestElement(2));

		assertEquals(3, queue.size());
		assertEquals(1, queue.poll().value);
		assertEquals(1, queue.poll().value);
		assertEquals(2, queue.poll().value);
	}

	/**
	 * Tests the behavior of the iterator when the queue is modified during
	 * iteration. Verifies that the iterator throws a
	 * ConcurrentModificationException in this case.
	 */
	@Test
	void testIteratorConcurrentModificationOffer() {
		queue.offer(new TestElement(1));
		queue.offer(new TestElement(2));

		Iterator<TestElement> iterator = queue.iterator();
		iterator.next();
		queue.offer(new TestElement(3));

		assertThrows(ConcurrentModificationException.class, iterator::next);
	}

}