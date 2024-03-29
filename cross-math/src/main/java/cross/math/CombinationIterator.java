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
package cross.math;

import java.util.Iterator;

/**
 * Implementation of {@link Iterator} for <code>int[]</code>, returning, for a
 * given number of {@link Partition}s in increasing order, an array of item
 * indices for each partition for the current iteration. Allows to enumerate
 * all items of supplied partitions of arbitrary size in a defined order.
 *
 * @author Nils Hoffmann
 */
public final class CombinationIterator implements Iterator<int[]> {

    private final Partition[] partitions;
    private long size = -1;
    private long cnt = 0;

    /**
     * Create a new CombinationIterator with given Partitions.
     *
     * @param p
     */
    public CombinationIterator(Partition... p) {
        this.partitions = p;
        this.size = size();
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasNext() {
        if (cnt < size) {
            return true;
        }
        return false;
    }

    /**
     *
     * @return
     */
    @Override
    public int[] next() {
        int[] ret = new int[this.partitions.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = partitions[i].current();
        }
        partitions[this.partitions.length - 1].next();
        cnt++;
        return ret;
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Returns the number of combinations of all partitions and their elements.
     *
     * @return the size
     */
    public long size() {
        if (this.size == -1) {
            long size = 1;
            for (Partition p : partitions) {
                size *= p.size();
            }
            this.size = size;
        }
        return size;
    }

    /**
     * Resets the iterator to initial value.
     */
    public void reset() {
        this.cnt = 0;
        for (Partition p : partitions) {
            p.reset();
        }
    }
}
