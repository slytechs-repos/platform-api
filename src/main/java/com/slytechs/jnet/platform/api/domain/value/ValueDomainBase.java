package com.slytechs.jnet.platform.api.domain.value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import com.slytechs.jnet.platform.api.domain.Domain;
import com.slytechs.jnet.platform.api.domain.DomainBase;
import com.slytechs.jnet.platform.api.domain.DomainPath;
import com.slytechs.jnet.platform.api.util.Registration;

/**
 * Implementation of ValueDomain that provides hierarchical value management.
 */
public class ValueDomainBase extends DomainBase implements ValueDomain {

	private final Map<String, Value> values;
	private final Map<String, ValueFolder> valueFolders;

	/**
	 * Creates a new ValueDomainBase with the specified name and parent domain.
	 *
	 * @param name   the domain name
	 * @param parent the parent domain, can be null for root domains
	 */
	public ValueDomainBase(String name, Domain parent) {
		super(name, parent);
		this.values = new LinkedHashMap<>();
		this.valueFolders = new LinkedHashMap<>();
	}

	/**
	 * Adds a value to this domain.
	 *
	 * @param <T>      the value type
	 * @param newValue the value to add
	 * @return the added value
	 */
	public <T extends Value> T addValue(T newValue) {
		Objects.requireNonNull(newValue, "newValue cannot be null");
		values.put(newValue.name(), newValue);
		return newValue;
	}

	/**
	 * Adds a value with registration for removal.
	 *
	 * @param <T>          the value type
	 * @param newValue     the value to add
	 * @param registration the registration callback
	 * @return the added value
	 */
	public <T extends Value> T addValue(T newValue, Consumer<Registration> registration) {
		Objects.requireNonNull(registration, "registration cannot be null");
		registration.accept(() -> removeValue(newValue.name()));
		return addValue(newValue);
	}

	/**
	 * Removes a value by name.
	 *
	 * @param name the name of the value to remove
	 * @return the removed value
	 */
	public Value removeValue(String name) {
		return values.remove(name);
	}

	@Override
	public boolean containsValue(DomainPath path, Object... params) {
		if (path == null || path.isRoot())
			return false;

		String[] elements = path.elements();
		if (elements.length == 1)
			return values.containsKey(elements[0]);

		// Navigate through the folders
		ValueFolder current = valueFolders.get(elements[0]);
		for (int i = 1; i < elements.length - 1 && current != null; i++) {
			final String element = elements[i]; // Create effectively final variable for lambda
			current = current.folders().stream()
					.filter(f -> f.name().equals(element))
					.findFirst()
					.map(f -> (ValueFolder) f)
					.orElse(null);
		}

		return current != null && current.containsValue(elements[elements.length - 1]);
	}

	/**
	 * Gets a value by name.
	 *
	 * @param <T>  the expected value type
	 * @param name the name of the value
	 * @return the value, or null if not found
	 */
	@SuppressWarnings("unchecked")
	public <T extends Value> T getValue(String name) {
		return (T) values.get(name);
	}

	/**
	 * Lists all values in this domain.
	 *
	 * @return unmodifiable list of values
	 */
	public List<Value> listValues() {
		return Collections.unmodifiableList(new ArrayList<>(values.values()));
	}

	/**
	 * @see com.slytechs.jnet.platform.api.domain.value.ValueDomain#unionSet(java.lang.String,
	 *      java.lang.String, java.lang.Object[])
	 */
	@Override
	public Set<?> unionSet(String path1, String path2, Object... params) {
		Object resolved1 = resolveValue(DomainPath.of(path1));
		Object resolved2 = resolveValue(DomainPath.of(path2));

		if (resolved1 instanceof Set<?> set1 && resolved2 instanceof Set<?> set2) {
			Set<Object> union = new HashSet<>(set1);
			union.addAll(set2);
			return union;
		}
		throw new UnsupportedOperationException("Union is only supported for sets");
	}

	/**
	 * @see com.slytechs.jnet.platform.api.domain.value.ValueDomain#intersectionSet(java.lang.String,
	 *      java.lang.String, java.lang.Object[])
	 */
	@Override
	public Set<?> intersectionSet(String path1, String path2, Object... params) {
		Object resolved1 = resolveValue(DomainPath.of(path1));
		Object resolved2 = resolveValue(DomainPath.of(path2));

		if (resolved1 instanceof Set<?> set1 && resolved2 instanceof Set<?> set2) {
			Set<Object> intersectionSet = new HashSet<>(set1);
			intersectionSet.retainAll(set2);
			return intersectionSet;
		}
		throw new UnsupportedOperationException("Intersection is only supported for sets");
	}

	/**
	 * @see com.slytechs.jnet.platform.api.domain.value.ValueDomain#differenceSet(java.lang.String,
	 *      java.lang.String, java.lang.Object[])
	 */
	@Override
	public Set<?> differenceSet(String path1, String path2, Object... params) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	/**
	 * @see com.slytechs.jnet.platform.api.domain.value.ValueDomain#resolveValue(com.slytechs.jnet.platform.api.domain.DomainPath,
	 *      java.lang.Object[])
	 */
	@Override
	public Object resolveValue(DomainPath path, Object... params) {
		Object current = path;
		int paramIndex = 0;

		for (String element : path.elements()) {
			if (element.endsWith("[]")) { // List handling
				if (current instanceof List<?> list) {
					if (paramIndex >= params.length || !(params[paramIndex] instanceof Integer)) {
						throw new IllegalArgumentException("Invalid index for list");
					}
					int index = (Integer) params[paramIndex++];
					current = list.get(index);
				} else {
					throw new ClassCastException("Expected List for element: " + element);
				}
			} else if (element.endsWith("{}")) { // Map handling
				if (current instanceof Map<?, ?> map) {
					if (paramIndex >= params.length) {
						throw new IllegalArgumentException("Missing key for map");
					}
					Object key = params[paramIndex++];
					current = map.get(key);
				} else {
					throw new ClassCastException("Expected Map for element: " + element);
				}
			} else {
				if (current instanceof Map<?, ?> map) {
					current = map.get(element);
				} else {
					throw new ClassCastException("Cannot resolve element: " + element);
				}
			}
		}

		return current;
	}

}