/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.el;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class ExpressionContext extends HashMap<String, Object> {

	private static final long serialVersionUID = -8709294073194083447L;

	private ExpressionContext parent;

	public ExpressionContext() {
	}

	/**
	 * @param parent
	 */
	public ExpressionContext(ExpressionContext parent) {
		this.parent = parent;
	}

	/**
	 * @see java.util.HashMap#get(java.lang.Object)
	 */
	@Override
	public Object get(Object key) {
		Object value = null;

		if (super.containsKey(key)) {
			value = super.get(key);
		} else if (parent != null) {
			value = parent.get(key);
		}

		if (value instanceof ValueBinding) {
			value = ((ValueBinding<?>) value).getValue();
		}

		return value;
	}

	/**
	 * @see java.util.HashMap#containsKey(java.lang.Object)
	 */
	@Override
	public boolean containsKey(Object key) {
		if (super.containsKey(key)) {
			return true;
		} else {
			return parent != null && parent.containsKey(key);
		}
	}

	/**
	 * @see java.util.HashMap#keySet()
	 */
	@Override
	public Set<String> keySet() {
		Set<String> keySet = super.keySet();

		if (parent != null) {
			keySet = new HashSet<String>(keySet);

			Set<String> parentSet = parent.keySet();

			for (String key : parentSet) {
				if (!keySet.contains(key)) {
					keySet.add(key);
				}
			}
		}

		return keySet;
	}

	/**
	 * @see java.util.HashMap#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return super.isEmpty() && (parent == null || parent.isEmpty());
	}

	/**
	 * @see java.util.HashMap#size()
	 */
	@Override
	public int size() {
		return keySet().size();
	}

	/**
	 * @see java.util.HashMap#values()
	 */
	@Override
	public Collection<Object> values() {
		Set<String> keySet = keySet();

		Collection<Object> values = new LinkedList<Object>();

		for (String key : keySet) {
			values.add(get(key));
		}

		return values;
	}

	/**
	 * @see java.util.HashMap#containsValue(java.lang.Object)
	 */
	@Override
	public boolean containsValue(Object value) {
		return values().contains(value);
	}

	/**
	 * @see java.util.HashMap#entrySet()
	 */
	@Override
	public Set<Map.Entry<String, Object>> entrySet() {
		Set<String> keySet = keySet();

		Set<Map.Entry<String, Object>> entries = new HashSet<Map.Entry<String, Object>>(
				keySet.size());

		for (String key : keySet) {
			entries.add(new Entry(key, get(key)));
		}

		return entries;
	}

	static class Entry implements Map.Entry<String, Object> {

		private String key;

		private Object value;

		Entry(String key, Object value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public String getKey() {
			return key;
		}

		@Override
		public Object getValue() {
			return value;
		}

		@Override
		public Object setValue(Object value) {
			this.value = value;

			return value;
		}
	}

	public interface ValueBinding<T> {

		T getValue();
	}
}