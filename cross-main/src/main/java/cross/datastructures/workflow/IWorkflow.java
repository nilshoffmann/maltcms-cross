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

import cross.Factory;
import cross.IConfigurable;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.pipeline.ICommandSequence;
import cross.datastructures.tuple.TupleND;
import cross.event.IEventSource;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.commons.configuration.Configuration;
import org.jdom.Element;

/**
 * Workflow models a sequence of produced IWorkflowResults, which usually are
 * files created by {@link cross.datastructures.workflow.IWorkflowElement}
 * objects.
 *
 * @author Nils Hoffmann
 *
 */
public interface IWorkflow extends IEventSource<IWorkflowResult>, IConfigurable, Callable<TupleND<IFileFragment>>, Serializable {

    /**
     * Append IWorkflowResult to this IWorkflow instance.
     *
     * @param iwr the workflow result to append to this workflow
     */
    public abstract void append(IWorkflowResult iwr);

    /**
     * Return the active ICommandSequence instance.
     *
     * @return the active command sequence
     */
    public abstract ICommandSequence getCommandSequence();

    /**
     * Returns the currently active configuration for this workflow.
     *
     * @return the active configuration
     */
    public abstract Configuration getConfiguration();

    /**
     * Returns the name of this IWorkflow.
     *
     * @return the name of the workflow
     */
    public abstract String getName();

    /**
     * Returns an iterator over all currently available results.
     *
     * @return an iterator over all workflow results
     */
    public abstract Iterator<IWorkflowResult> getResults();

    /**
     * Returns the results for a specific {@link IFileFragment}.
     *
     * @param iff the file fragment connected to the result
     * @return a list of workflow results, may be empty
     */
    public abstract List<IWorkflowResult> getResultsFor(IFileFragment iff);

    /**
     * Returns the results created by a specific {@link IWorkflowElement}.
     *
     * @param afc the workflow element that created the objects
     * @return a list of workflow results, may be empty
     */
    public abstract List<IWorkflowResult> getResultsFor(IWorkflowElement afc);

    /**
     * Returns the results created by a specific {@link IWorkflowElement} for a given
     * {@link IFileFragment}.
     *
     * @param afc the workflow element that created the objects
     * @param iff the file fragment connected to the result
     * @return a list of workflow results, may be empty
     */
    public abstract List<IWorkflowResult> getResultsFor(IWorkflowElement afc,
        IFileFragment iff);

    /**
     * Returns the list of results matching a given file extension pattern.
     *
     * @param fileExtension the file extension of the desired file objects
     * @return a list of workflow results, may be empty
     */
    public abstract List<IWorkflowResult> getResultsOfType(String fileExtension);

    /**
     * Returns the list of results matching a given file extension pattern
     * created by a given IWorkflowElement implementation.
     *
     * @param afc           the workflow element that created the objects
     * @param fileExtension the file extension of the desired file objects
     * @return a list of workflow results, may be empty
     */
    public abstract List<IWorkflowResult> getResultsOfType(
        IWorkflowElement afc, String fileExtension);

    /**
     * Returns the list of results matching a given class created by a given
     * IWorkflowElement implementation.
     *
     * @param <T> the generic type of result objects
     * @param afc the workflow element that created the objects
     * @param c   the desired generic class type of the result objects
     * @return a list of workflow object results, may be empty
     */
    public abstract <T> List<IWorkflowObjectResult> getResultsOfType(IWorkflowElement afc, Class<? extends T> c);

    /**
     * Return the startup data of this IWorkflow.
     *
     * @return the startup date
     */
    public abstract Date getStartupDate();

    /**
     * Restore state of this IWorkflow instance from an XML document.
     *
     * @param e the xml element to decode
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public abstract void readXML(Element e) throws IOException,
        ClassNotFoundException;

    /**
     * Save this IWorkflow.
     */
    public abstract void save();

    /**
     * Load the previously save xml information of this workflow.
     *
     * @param f the workflow xml file.
     */
    public abstract void load(File f);

    /**
     * Set ics to be the active ICommandSequence instance.
     *
     * @param ics the command sequence
     * @see AFragmentCommand
     */
    public abstract void setCommandSequence(ICommandSequence ics);

    /**
     * Set the currently active configuration.
     *
     * @param configuration the active configuration
     */
    public abstract void setConfiguration(Configuration configuration);

    /**
     * Set the name of this IWorkflow instance.
     *
     * @param name the name of this workflow
     */
    public abstract void setName(String name);

    /**
     * Set the startup date of this IWorkflow instance.
     *
     * @param date the startup date
     */
    public abstract void setStartupDate(Date date);

    /**
     * Write the state of this object to XML.
     *
     * @return the xml element representing this workflow and its members
     * @throws IOException
     */
    public abstract Element writeXML() throws IOException;

    /**
     * Returns the output directory for the given object.
     *
     * @param iwe the workflow element or an arbitray object
     * @return the output directory for the given file
     */
    public abstract File getOutputDirectory(Object iwe);

    /**
     * Returns the base output directory of this workflow.
     *
     * @return the base output directory
     */
    public abstract File getOutputDirectory();

    /**
     * Returns the file location of the workflow's xml file.
     *
     * @return the file location
     */
    public abstract File getWorkflowXmlFile();

    /**
     * Returns the state of execution.
     *
     * @return true if this workflow is running within the local vm, false if it is running in distributed RMI mode
     */
    public abstract boolean isExecuteLocal();

    /**
     * Sets whether this workflow should execute locally or in distributed mode.
     *
     * @param b true if this workflow should run locally, false otherwise
     */
    public abstract void setExecuteLocal(boolean b);

    /**
     * Sets the default output directory of this workflow.
     *
     * @param f the output directory
     */
    public abstract void setOutputDirectory(File f);

    /**
     * Sets the workflow postprocessors which are executed after the workflow has finished.
     *
     * @param workflowPostProcessors the collection of workflow post processors
     */
    public abstract void setWorkflowPostProcessors(List<IWorkflowPostProcessor> workflowPostProcessors);

    /**
     * Returns the list of workflow post processors. These will be run after the workflow has finished execution.
     *
     * @return the list of workflow post processors
     */
    public abstract List<IWorkflowPostProcessor> getWorkflowPostProcessors();

    /**
     * Returns the factory associated to this workflow.
     *
     * @return the factory
     * @since 1.3.1
     */
    public abstract Factory getFactory();

    /**
     * Set the factory associated to this workflow.
     *
     * @param factory
     * @since 1.3.1
     */
    public abstract void setFactory(Factory factory);

    /**
     * Clear all workflow related results.
     *
     * @since 1.3.1
     */
    public abstract void clearResults();
}
