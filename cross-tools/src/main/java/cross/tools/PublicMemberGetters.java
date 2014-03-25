/* Cross, common runtime object support system.
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
package cross.tools;

import cross.annotations.NoFeature;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author Nils Hoffmann
 * @param <T>
 */
public class PublicMemberGetters<T> {

    /**
     *
     */
    protected HashMap<String, Method> hm;

    /**
     *
     * @param c
     */
    public PublicMemberGetters(Class<?> c) {
        this(c, new String[]{});
    }

    /**
     *
     * @param c
     * @param suffixesToExclude
     */
    public PublicMemberGetters(Class<?> c, String... suffixesToExclude) {
        hm = new HashMap<>();
        Method[] m = c.getMethods();
        for (Method method : m) {
            if (!method.isAnnotationPresent(NoFeature.class)) {
                if (method.getName().startsWith("get")
                    && !method.getName().equals("getClass")) {
                    // look for method name (after get) in suffixes to exclude
                    int idx = Arrays.binarySearch(suffixesToExclude, method.
                        getName().substring(3));
                    // if idx < 0, suffix is not contained, so add method to
                    // pool
                    if (idx < 0) {
                        hm.put(method.getName().substring(3), method);
                    }
                }
            }
        }
    }

    /**
     *
     * @param t
     */
    public PublicMemberGetters(T t) {
        this(t.getClass());
    }

    /**
     *
     * @param s
     * @return
     */
    public Method getMethodForGetterName(String s) {
        String name = s;
        if (name.startsWith("get")) {
            name = name.substring(3);
        }
        if (hm.containsKey(name)) {
            return hm.get(name);
        } else {
            return null;
        }
    }

    /**
     *
     * @param s
     * @return
     */
    public Method getMethodForFieldName(String s) {
        String name = s;
        if (name.startsWith("get")) {
            name = name.substring(3);
        }
        if (name.substring(0, 1).startsWith(name.substring(0, 1).toLowerCase())) {
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        if (hm.containsKey(name)) {
            return hm.get(name);
        } else {
            return null;
        }
    }

    /**
     *
     * @param s
     * @return
     */
    public String[] getGetterNames(String[] s) {
        ArrayList<String> al = new ArrayList<>(s.length);
        for (String method : s) {
            if (getMethodForGetterName(method) != null) {
                al.add(method);
            }
        }
        return al.toArray(new String[al.size()]);
    }

    /**
     *
     * @return
     */
    public String[] getGetterNames() {
        String[] names = new String[hm.size()];
        int i = 0;
        for (String s : hm.keySet()) {
            names[i] = s;
            i++;
        }
        Arrays.sort(names);
        return names;
    }
}
