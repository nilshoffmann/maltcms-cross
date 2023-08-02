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
package cross.datastructures.tuple;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Abstract class for N-Tuples of type T, providing different utility methods,
 * to return all non-identical pairs (Ai,Aj), i!=j. (Ai,Aj) = (Aj,Ai) => i<j =>
 * (Ai,Aj) is preferred.
 *
 * @author Nils Hoffmann
 *
 * @param <T>
 */
public class TupleND<T extends Serializable> implements Collection<T>,
    Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 3369621175169080132L;
    private final List<T> c;

    /**
     *
     * @param c1
     */
    public TupleND(final Collection<T> c1) {
        this.c = new ArrayList<>(c1);
    }

    /**
     *
     */
    public TupleND() {
        this.c = new ArrayList<>();
    }

    /**
     *
     * @param ts
     */
    public TupleND(final T... ts) {
        this.c = new ArrayList<>(ts.length);
        for (final T t : ts) {
            this.c.add(t);
        }
    }

    /**
     *
     * @param e
     * @return
     */
    @Override
    public synchronized boolean add(final T e) {
        return this.c.add(e);
    }

    /**
     *
     * @param c1
     * @return
     */
    @Override
    public synchronized boolean addAll(final Collection<? extends T> c1) {
        return this.c.addAll(c1);
    }

    /**
     *
     */
    @Override
    public void clear() {
        this.c.clear();
    }

    /**
     *
     * @param o
     * @return
     */
    @Override
    public boolean contains(final Object o) {
        return this.c.contains(o);
    }

    /**
     *
     * @param c1
     * @return
     */
    @Override
    public boolean containsAll(final Collection<?> c1) {
        return this.c.containsAll(c1);
    }

    /**
     *
     * @param n
     * @return
     */
    public T get(final int n) {
        return this.c.get(n);
    }

    /**
     *
     * @return
     */
    public Iterator<T> getIterator() {
        return this.c.iterator();
    }

    /**
     *
     * @return
     */
    public ListIterator<T> getListIterator() {
        return this.c.listIterator();
    }

    /**
     *
     * @param index
     * @return
     */
    public ListIterator<T> getListIterator(final int index) {
        return this.c.listIterator(index);
    }

    /**
     *
     * @return
     */
    public int getNumberOfPairs() {
        return getSize() * (getSize() - 1) / 2;
    }

    /**
     * Returns all unique pairs without repetition. For example the collection
     * [A,B,C,D] will return the following pairs: [A,B],[A,C],[A,D],[B,C],[B,D],[C,D].
     *
     * @return the unique pairs without repetition
     */
    public List<Tuple2D<T, T>> getPairs() {
        final ArrayList<Tuple2D<T, T>> al = new ArrayList<>(
            getNumberOfPairs());
        // int size = getNumberOfPairs();
        int cnt = 1;
        for (int i = 0; i < this.c.size() - 1; i++) {
            for (int j = i + 1; j < this.c.size(); j++) {
                // System.out.println("Adding pair " + cnt + " of " + size);
                al.add(new Tuple2D<>(this.c.get(i), this.c.get(j)));
                cnt++;
            }
        }

        return Collections.unmodifiableList(al);
    }

    /**
     * Returns all pairs of elements with the first element, excluding the first element.
     * For example the collection [A,B,C,D] will return the following pairs: [A,B],[A,C],[A,D].
     *
     * @return the unique pairs without repetition
     */
    public List<Tuple2D<T, T>> getPairsWithFirstElement() {
        final T first = this.c.get(0);
        final ArrayList<Tuple2D<T, T>> al = new ArrayList<>(this.c
            .size() - 1);
        // int size = this.c.size() - 1;
        for (int i = 1; i < this.c.size(); i++) {
            // System.out.println("Adding pair " + i + " of " + size);
            al.add(new Tuple2D<>(first, this.c.get(i)));
        }

        return Collections.unmodifiableList(al);
    }

    /**
     * Returns all pairs of elements with the first element, excluding the first element.
     * For example the collection [A,B,C,D] will return the following pairs: [D,A],[D,B],[D,C].
     *
     * @return the unique pairs without repetition
     */
    public List<Tuple2D<T, T>> getPairsWithLastElement() {
        final T last = this.c.get(this.c.size() - 1);
        final ArrayList<Tuple2D<T, T>> al = new ArrayList<>(this.c
            .size() - 1);
        final int size = this.c.size() - 1;
        for (int i = 0; i < size; i++) {
            // System.out.println("Adding pair " + i + " of " + size);
            al.add(new Tuple2D<>(last, this.c.get(i)));
        }

        return Collections.unmodifiableList(al);
    }

    /**
     *
     * @return
     */
    public int getSize() {
        return this.c.size();
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isEmpty() {
        return this.c.isEmpty();
    }

    /**
     *
     * @return
     */
    @Override
    public Iterator<T> iterator() {
        return getIterator();
    }

    /**
     *
     * @param n
     * @return
     */
    public T remove(final int n) {
        return this.c.remove(n);
    }

    /**
     *
     * @param o
     * @return
     */
    @Override
    public boolean remove(final Object o) {
        return this.c.remove(o);
    }

    /**
     *
     * @param c1
     * @return
     */
    @Override
    public boolean removeAll(final Collection<?> c1) {
        return this.c.removeAll(c1);
    }

    /**
     *
     * @param c1
     * @return
     */
    @Override
    public boolean retainAll(final Collection<?> c1) {
        return this.c.retainAll(c1);
    }

    /**
     * Adds the pair elements contained in <code>coll</code> to this
     * n-tuple in the order that they appear in. For example the collection [[A,B],[A,C],[A,D]]
     * will create the following elements in order: [A,B,C,D].
     *
     * @param coll the pair collection
     */
    public void setPairs(final Collection<Tuple2D<T, T>> coll) {
//		System.out.println("Clearing collection!");
        this.c.clear();
        final Iterator<Tuple2D<T, T>> iter = coll.iterator();
        final HashSet<T> hm = new HashSet<>();
//		System.out.println("Adding new Pairs!");
        while (iter.hasNext()) {
            final Tuple2D<T, T> tuple = iter.next();
            if (!hm.contains(tuple.getFirst())) {
                this.c.add(tuple.getFirst());
                hm.add(tuple.getFirst());
            }
            if (!hm.contains(tuple.getSecond())) {
                this.c.add(tuple.getSecond());
                hm.add(tuple.getSecond());
            }
        }
    }

    /**
     *
     * @return
     */
    @Override
    public int size() {
        return this.getSize();
    }

    /**
     *
     * @return
     */
    @Override
    public Object[] toArray() {
        return this.c.toArray();
    }

    /**
     *
     * @param <T1>
     * @param a
     * @return
     */
    @Override
    public <T1> T1[] toArray(final T1[] a) {
        return this.c.toArray(a);
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        int last = size() - 1;
        int i = 0;
        for (T t : this) {
            sb.append(t.toString() + (i == last ? "" : ", "));
            i++;
        }
        sb.append("]");
        return sb.toString();
    }
}
