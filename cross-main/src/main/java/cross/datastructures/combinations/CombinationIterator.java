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
package cross.datastructures.combinations;

import java.util.Iterator;

/**
 * Iterator over parameter combinations that supports lazy instantiation of
 * object from each parameter combination.
 *
 * @author Nils Hoffmann
 * @param <T>
 */
public class CombinationIterator<T> implements Iterator<T> {

    private final CombinationProvider provider;
    private final ITypeFactory<T> factory;
    private long element = 0;

    /**
     * Creates a new combination iterator given a combination provider and type
     * factory.
     *
     * @param provider the combination provider providing parameter combinations
     * @param factory  the factory to create new instances of type T
     */
    public CombinationIterator(CombinationProvider provider, ITypeFactory<T> factory) {
        this.provider = provider;
        this.factory = factory;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasNext() {
        return element < provider.size();
    }

    /**
     *
     * @return
     */
    @Override
    public T next() {
        return factory.create(provider.get(element++));
    }

    /**
     *
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Reset this iterator to initial conditions for re-use.
     */
    public void reset() {
        this.element = 0;
        this.provider.reset();
    }
}
