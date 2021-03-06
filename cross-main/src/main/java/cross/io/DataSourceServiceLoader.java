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
package cross.io;

import cross.Factory;
import cross.IFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Loads available service implementations from the classpath using
 * {@link java.util.ServiceLoader}.
 *
 * @author Nils Hoffmann
 */
public class DataSourceServiceLoader {

    /**
     * Returns the available implementations of {@link IDataSource}.
     *
     * Elements are sorted according to lexical order on their classnames.
     *
     * @param factory the factory to use
     * @return a list of available implementations
     */
    public List<IDataSource> getAvailableCommands(IFactory factory) {
        ServiceLoader<IDataSource> sl = ServiceLoader.load(IDataSource.class);
        HashSet<IDataSource> s = new HashSet<>();
        for (IDataSource ifc : sl) {
            factory.getObjectFactory().configureType(ifc);
            s.add(ifc);
        }
        ArrayList<IDataSource> al = new ArrayList<>();
        al.addAll(s);
        Collections.sort(al, new Comparator<IDataSource>() {
            @Override
            public int compare(IDataSource o1, IDataSource o2) {
                return o1.getClass().getName().compareTo(
                    o2.getClass().getName());
            }
        });
        return al;
    }
}
