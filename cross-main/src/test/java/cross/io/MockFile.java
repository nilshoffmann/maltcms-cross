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

import cross.datastructures.tools.ArrayTools;
import cross.exception.ResourceNotAvailableException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import ucar.ma2.Array;

/**
 * Mock file for testing of datasources.
 *
 * @author Nils Hoffmann
 */
@Data
public class MockFile {

    private final URI uri;
    private final Map<String, ArrayList<Array>> variableToDataMap = new LinkedHashMap<>();

    /**
     *
     * @param uri
     */
    public MockFile(URI uri) {
        this.uri = uri;
    }

    /**
     *
     * @return
     */
    public Set<String> keys() {
        return variableToDataMap.keySet();
    }

    /**
     *
     * @param name
     * @return
     */
    public Array getChild(String name) {
        if (variableToDataMap.containsKey(name)) {
            return ArrayTools.glue(variableToDataMap.get(name));
        }
        throw new ResourceNotAvailableException("Could not find variable " + name + " for file " + uri);
    }

    /**
     *
     * @param name
     * @return
     */
    public ArrayList<Array> getIndexedChild(String name) {
        if (variableToDataMap.containsKey(name)) {
            return variableToDataMap.get(name);
        }
        throw new ResourceNotAvailableException("Could not find variable " + name + " for file " + uri);
    }

    /**
     *
     * @param name
     * @param c
     */
    public void addChild(String name, ArrayList<Array> c) {
        variableToDataMap.put(name, c);
    }
}
