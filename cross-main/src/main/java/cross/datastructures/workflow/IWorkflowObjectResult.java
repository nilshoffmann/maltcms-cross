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
package cross.datastructures.workflow;

import cross.datastructures.fragments.IFileFragment;

/**
 * A workflow result that is represented by an object.
 *
 * @author Nils Hoffmann
 * @param <T> the generic type of the object result
 * @deprecated
 */
@Deprecated
public interface IWorkflowObjectResult<T> extends IWorkflowResult {

    /**
     * The typed object associated to this result.
     *
     * @return the typed object
     */
    public T getObject();

    /**
     * Set the typed object associated to this result.
     *
     * @param t the typed object
     */
    public void setObject(T t);

    /**
     * Returns the file fragments associated to this result.
     *
     * @return the file fragments
     */
    public IFileFragment[] getResources();

    /**
     * Sets the file fragments associated to this result.
     *
     * @param resources the file fragments
     */
    public void setResources(IFileFragment... resources);
}
