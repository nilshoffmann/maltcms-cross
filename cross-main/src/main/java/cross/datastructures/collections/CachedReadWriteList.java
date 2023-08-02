/*
 * Cross, common runtime object support system.
 * Copyright (C) 2008-2012, The authors of Cross. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Cross may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Cross, you may choose which license to receive the code
 * under. Certain files or entire directories may not be covered by this
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a
 * LICENSE file in the relevant directories.
 *
 * Cross is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package cross.datastructures.collections;

import cross.cache.CacheFactory;
import cross.cache.ICacheDelegate;
import cross.datastructures.cache.ISerializationProxy;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Implementation of a read/write cached list for indexed data access.
 *
 * Backed by a soft-reference cache hash map using LRU cache eviction strategy.
 *
 * A {@link ISerializationProxy} can be supplied at construction to allow for otherwise
 * non-serializable objects to be efficiently serialized and deserialized.
 *
 * This implementation currently only supports single element operations of the implemented
 * List interface. All unsupported methods throw {@link UnsupportedOperationException}.
 *
 * @author Nils Hoffmann
 * @param <T>
 */
public class CachedReadWriteList<T> implements List<T> {

    private ICacheDelegate<Integer, Object> cacheDelegate;
    private ISerializationProxy<T> serializationProxy;

    /**
     *
     * @param name
     * @param maxElementsInMemory
     */
    public CachedReadWriteList(String name, int maxElementsInMemory) {
        cacheDelegate = CacheFactory.createDefaultCache(name, maxElementsInMemory);
    }

    /**
     * Initializes the cache with a maximum of 100 elements in memory.
     *
     * @param name
     * @param serializationProxy
     */
    public CachedReadWriteList(String name, ISerializationProxy<T> serializationProxy) {
        this(name, 100);
        this.serializationProxy = serializationProxy;
    }

    /**
     *
     * @param name
     * @param serializationProxy
     * @param maxElementsInMemory
     */
    public CachedReadWriteList(String name, ISerializationProxy<T> serializationProxy, int maxElementsInMemory) {
        this(name, maxElementsInMemory);
        this.serializationProxy = serializationProxy;
    }

    /**
     *
     * @param t
     * @return
     */
    protected Object convert(T t) {
        if (serializationProxy == null) {
            return t;
        }
        return serializationProxy.convert(t);
    }

    /**
     *
     * @param t
     * @return
     */
    protected T reverseConvert(Object t) {
        if (serializationProxy == null) {
            return (T) t;
        }
        return serializationProxy.reverseConvert(t);
    }

    /**
     *
     * @param index
     * @return
     */
    public Serializable getSerializable(int index) {
        return (Serializable) cacheDelegate.get(index);
    }

    /**
     *
     * @return
     */
    @Override
    public int size() {
        return cacheDelegate.keys().size();
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isEmpty() {
        return cacheDelegate.keys().isEmpty();
    }

    /**
     *
     * @param index
     * @return
     */
    public boolean contains(Integer index) {
        return cacheDelegate.get(index) != null;
    }

    /**
     *
     * @param index
     */
    public void remove(Integer index) {
        cacheDelegate.put(index, null);
    }

    /**
     *
     * @param index
     * @return
     */
    @Override
    public T get(int index) {
        return reverseConvert(cacheDelegate.get(index));
    }

    /**
     *
     * @param index
     * @param element
     * @return
     */
    @Override
    public T set(int index, T element) {
        cacheDelegate.put(index, convert(element));
        return element;
    }

    /**
     *
     * @param index
     * @param element
     */
    @Override
    public void add(int index, T element) {
        cacheDelegate.put(index, convert(element));
    }

    /**
     *
     * @param o
     * @return
     */
    @Override
    public boolean contains(Object o) {
        if (o instanceof Integer) {
            return contains((Integer) o);
        }
        return false;
    }

    private final class CachedReadWriteListIterator implements Iterator<T> {

        private final CachedReadWriteList<T> crwl;
        private final SortedSet<Integer> keys;
        private int counter = 0;

        public CachedReadWriteListIterator(CachedReadWriteList crwl) {
            this.crwl = crwl;
            int size = this.crwl.size();
            keys = new TreeSet<>();
            for (int i = 0; i < size; i++) {
                keys.add(i);
            }
        }

        @Override
        public boolean hasNext() {
            return counter < keys.size();
        }

        @Override
        public T next() {
            return crwl.get(counter++);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

    /**
     *
     * @return
     */
    @Override
    public Iterator<T> iterator() {
        return new CachedReadWriteListIterator(this);
    }

    /**
     *
     * @return
     */
    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     * @param <T>
     * @param a
     * @return
     */
    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     * @param e
     * @return
     */
    @Override
    public boolean add(T e) {
        add(size(), e);
        return true;
    }

    /**
     *
     * @param o
     * @return
     */
    @Override
    public boolean remove(Object o) {
        if (o instanceof Integer) {
            remove((Integer) o);
        }
        return false;
    }

    /**
     *
     * @param c
     * @return
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     * @param c
     * @return
     */
    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     * @param index
     * @param c
     * @return
     */
    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     * @param c
     * @return
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     * @param c
     * @return
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     */
    @Override
    public void clear() {
        int size = cacheDelegate.keys().size();
        for (int i = 0; i < size; i++) {
            cacheDelegate.put(i, null);
        }
    }

    /**
     *
     * @param index
     * @return
     */
    @Override
    public T remove(int index) {
        T t = get(index);
        remove(index);
        return t;
    }

    /**
     *
     * @param o
     * @return
     */
    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     * @param o
     * @return
     */
    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     * @return
     */
    @Override
    public ListIterator<T> listIterator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     * @param index
     * @return
     */
    @Override
    public ListIterator<T> listIterator(int index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     * @param fromIndex
     * @param toIndex
     * @return
     */
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
