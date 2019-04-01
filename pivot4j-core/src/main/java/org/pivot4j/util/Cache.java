/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.util;

import java.util.Map;

import org.apache.commons.collections.map.ReferenceMap;

public class Cache<K, V> {

    @SuppressWarnings("unchecked")
    private Map<K, V> data = new ReferenceMap();

    private Cache<K, V> parent;

    public Cache() {
    }

    public Cache(Cache<K, V> parent) {
        this.parent = parent;
    }

    /**
     * @return parent
     */
    public Cache<K, V> getParent() {
        return parent;
    }

    public void clear() {
        data.clear();

        if (parent != null) {
            parent.clear();
        }
    }

    /**
     * @param key
     * @return
     */
    public boolean containsKey(K key) {
        return data.containsKey(key)
                || (parent != null && parent.containsKey(key));
    }

    /**
     * @param key
     * @return
     */
    public V get(K key) {
        V result = data.get(key);

        if (result == null && parent != null) {
            result = parent.get(key);
        }

        return result;
    }

    /**
     * @param key
     * @param value
     */
    public void put(K key, V value) {
        if (value == null) {
            remove(key);
        } else {
            data.put(key, value);
        }
    }

    /**
     * @param key
     */
    public void remove(K key) {
        data.remove(key);

        if (parent != null) {
            parent.remove(key);
        }
    }
}
