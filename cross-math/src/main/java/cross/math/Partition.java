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
 * Partition mimics a collection of indexed elements of given size. Iteration
 * over the partition corresponds to returning the number of each item in the
 * partition in increasing order. When the last item's number is returned, the
 * iterator can not be further iterated. Calling <code>reset()</code> resets the
 * item index to 0 and allows reuse of the Iterator.
 *
 * @author Nils Hoffmann
 */
public class Partition implements Iterator<Integer> {

    private final int size;
    private int count = 0;
    private int max;
    private Partition neighbor = null;

    /**
     * Creates a new Partition.
     *
     * @param size the size of the partition
     */
    public Partition(int size) {
        this.size = size;
        this.max = this.size;
    }

    /**
     * Creates a new Partition with the given neighbor, whose counter is increased, once
     * the internal <code>count</code> exceeds <code>size</code>.
     *
     * @param neighbor the next neighbor
     * @param size     the size of the partition
     */
    public Partition(Partition neighbor, int size) {
        this(size);
        this.neighbor = neighbor;
    }

    @Override
    public boolean hasNext() {
        return this.count < max;
    }

    @Override
    public Integer next() {
        //overflow/carry to next neighbor
        if (count + 1 == this.max) {
            //this.max--;
            this.count = 0;
            if (this.neighbor != null) {
                this.neighbor.next();
            }
            return this.count;
        } else {//increase counter
            return Integer.valueOf(count++);
        }
    }

    /**
     * Returns the current internal index of the partition.
     *
     * @return the current count value
     */
    public Integer current() {
        return this.count;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Returns the size of the partition.
     *
     * @return the partition size
     */
    public int size() {
        return this.size;
    }

    /**
     * Resets the size of this partition.
     */
    public void reset() {
        this.count = 0;
    }
}
