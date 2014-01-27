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

import cross.io.xml.IXMLSerializable;
import java.io.Serializable;

/**
 * A result of a {@link cross.datastructures.workflow.IWorkflowElement}, linking
 * to a created file or immediate resources (e.g. statistics).
 *
 * @author Nils Hoffmann
 *
 */
public interface IWorkflowResult extends IXMLSerializable, Serializable {

    /**
     * Returns the workflow element associated to this result.
     *
     * @return the workflow element
     */
    public IWorkflowElement getWorkflowElement();

    /**
     * Returns the workflow slot / category associated to this result.
     *
     * @return the workflow slot
     */
    public WorkflowSlot getWorkflowSlot();

    /**
     * Sets the workflow element that created / reported this result.
     *
     * @param iwe the workflow element
     */
    public void setWorkflowElement(IWorkflowElement iwe);

    /**
     * Sets the workflow slot / category.
     *
     * @param ws the workflow slot
     */
    public void setWorkflowSlot(WorkflowSlot ws);
}
