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

import cross.IConfigurable;
import cross.datastructures.pipeline.ICommandSequence;
import java.util.Date;
import org.apache.commons.configuration.Configuration;

/**
 * A factory for workflows.
 *
 * @author Nils Hoffmann
 */
public interface IWorkflowFactory extends IConfigurable {

	/**
	 * Returns the current workflow instance as created by this workflow factory instance.
	 * @return the last created workflow
	 */
	IWorkflow getCurrentWorkflowInstance();

	/**
	 * Create a new IWorkflow instance with default name workflow.
	 *
	 * @param startup the startup date
	 * @param ics the command sequence
	 * @return an initialized workflow
	 */
	IWorkflow getDefaultWorkflowInstance(final Date startup, final ICommandSequence ics);

	/**
	 * Create a new DefaultWorkflow instance with custom name, if it does not
	 * exist already. Otherwise returns the existing instance. Use this, if you
	 * will only create one Workflow per VM instance. Otherwise use
	 * getNewWorkflowInstance to create a new DefaultWorkflow instance with
	 * custom configuration.
	 *
	 * @param startup the startup date 
	 * @param name the workflow name
	 * @param ics the command sequence
	 * @return an initialized workflow with a custom name
	 */
	IWorkflow getDefaultWorkflowInstance(final Date startup, final String name, final ICommandSequence ics);

	/**
	 * Create a new workflow instance with a custom configuration.
	 * 
	 * @param startup the startup date
	 * @param name the custom name
	 * @param ics the command sequence
	 * @param cfg the custom configuration
	 * @return an initialized workflow with a custom name and configuration
	 */
	IWorkflow getNewWorkflowInstance(final Date startup, final String name, final ICommandSequence ics, final Configuration cfg);
}
