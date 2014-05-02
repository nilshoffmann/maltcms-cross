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
package cross;

/**
 * Service interface for the retrieval of IFactory instances.
 *
 * @author Nils Hoffmann
 * @since 1.3.1
 */
public interface IFactoryService {

    /**
     * Returns the named factory instance or creates a new instance if the named
     * instance is not known.
     *
     * @param name the name of the factory
     * @return the factory
     */
    IFactory getInstance(String name);
    
    /**
     * Removes the named factory instance if it exists and returns it.
     * Returns null if no factory is known for the given name.
     *
     * @param name the name of the factory
     * @return the factory or null
     */
    IFactory remove(String name);

}
