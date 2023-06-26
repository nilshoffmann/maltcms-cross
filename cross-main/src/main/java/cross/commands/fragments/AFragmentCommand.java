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
package cross.commands.fragments;

import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tools.FileTools;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowProgressResult;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowElement;
import cross.datastructures.workflow.IWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.event.EventSource;
import cross.event.IEvent;
import cross.event.IEventSource;
import cross.event.IListener;
import cross.exception.MappingNotAvailableException;
import cross.tools.StringTools;
import cross.vocabulary.CvResolver;
import cross.vocabulary.ICvResolver;
import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.sf.mpaxs.api.ICompletionService;
import net.sf.mpaxs.spi.concurrent.CompletionServiceFactory;
import org.apache.commons.configuration.Configuration;
import org.jdom2.Element;

/**
 * A class providing a default implementation for configuration and a concrete
 * typing of the untyped superclass {@link cross.commands.ICommand}.
 * Additionally, many convenience methods are provided for easier creation of
 * custom commands.
 *
 * Use objects extending this class as commands within a
 * {@link cross.datastructures.pipeline.ICommandSequence}.
 *
 * @author Nils Hoffmann
 */
@Slf4j
@Data
public abstract class AFragmentCommand implements IFragmentCommand {

    private static final long serialVersionUID = -4551167359317007776L;
    @Getter(AccessLevel.NONE)
    private final IEventSource<IWorkflowResult> eventSource = new EventSource<>();
    private IWorkflow workflow = null;
    private DefaultWorkflowProgressResult progress = null;
    private ICvResolver cvResolver = new CvResolver();

    /**
     * Initialize a fragment command as a child command of this one.
     *
     * @param fragmentCommand the fragment command to initalize to use the
     *                        current workflow and configuration
     */
    public void initSubCommand(AFragmentCommand fragmentCommand) {
        fragmentCommand.setWorkflow(workflow);
        fragmentCommand.configure(workflow.getConfiguration());
    }

    /**
     * Post process the results in the completion service and map them by name
     * to the input.
     *
     * @param ics the completion service returning Files to wait on
     * @param t   the input to map to
     * @return the result file fragments linking processing results and input
     */
    public TupleND<IFileFragment> postProcess(ICompletionService<File> ics,
        final TupleND<IFileFragment> t) {
        TupleND<IFileFragment> ret = new TupleND<>();
        try {
            List<File> results = ics.call();
            // expect at least one result
            EvalTools.gt(0, results.size(), this);
            // map input to results
            ret = mapToInput(results, t);
            // append results to workflow for bookkeeping
            addWorkflowResults(ret);
        } catch (Exception ex) {
            log.error("Caught exception while executing workers: ", ex);
            throw new RuntimeException(ex);
        }
        return ret;
    }

    /**
     * Post process the results in the completion service and map them by name
     * to the input.
     *
     * @param ics the completion service returning URIs to wait on
     * @param t   the input to map to
     * @return the result file fragments linking processing results and input
     */
    public TupleND<IFileFragment> postProcessUri(ICompletionService<URI> ics,
        final TupleND<IFileFragment> t) {
        TupleND<IFileFragment> ret = new TupleND<>();
        try {
            List<URI> results = ics.call();
            // expect at least one result
            EvalTools.gt(0, results.size(), this);
            // map input to results
            ret = mapToInputUri(results, t);
            // append results to workflow for bookkeeping
            addWorkflowResults(ret);
        } catch (Exception ex) {
            log.error("Caught exception while executing workers: ", ex);
            throw new RuntimeException(ex);
        }
        return ret;
    }

    @Override
    public void addListener(final IListener<IEvent<IWorkflowResult>> l) {
        this.eventSource.addListener(l);
    }

    @Override
    public void appendXML(final Element e) {
    }

    /**
     * As of release 1.2.2, please use the spring beans based configuration for
     * AFragmentCommand instances. Only configuration of variable name mappings
     * should be performed using configure.
     *
     * @param cfg
     * @deprecated
     */
    @Override
    @Deprecated
    public void configure(final Configuration cfg) {
    }

    @Override
    public void fireEvent(final IEvent<IWorkflowResult> e) {
        this.eventSource.fireEvent(e);
    }

    /**
     * Returns a description of this fragment command and what it does.
     *
     * @return the description
     */
    public abstract String getDescription();

    /**
     * Utility method to create mutable FileFragments from a given tuple of
     * FileFragments.
     *
     * @param t the input fragments
     * @return the new work fragments, referencing the input fragments
     *         (one-to-one)
     */
    public TupleND<IFileFragment> createWorkFragments(TupleND<IFileFragment> t) {
        TupleND<IFileFragment> wt = new TupleND<>();
        for (IFileFragment iff : t) {
            wt.add(createWorkFragment(iff));
        }
        return wt;
    }

    /**
     * Utility method to create a mutable FileFragment to work on.
     *
     * @param iff the file fragment to use as input / source fragment
     * @return the mutable work fragment
     */
    public IFileFragment createWorkFragment(IFileFragment iff) {
        URI uri = new File(getWorkflow().getOutputDirectory(this), iff.getName()).toURI();
        log.info("Work fragment: {}", uri);
        final IFileFragment copy = new FileFragment(uri);
        copy.addSourceFile(iff);
        return copy;
    }

    /**
     * Maps a list of URIs, which resemble processing results of input file
     * fragments to the input file fragments, mainting the order of the input
     * fragments.
     *
     * @param files          the file URIs to map
     * @param inputFragments the input fragments
     * @return the mapped file fragments
     */
    public TupleND<IFileFragment> mapToInputUri(List<URI> files,
        TupleND<IFileFragment> inputFragments) {
        HashMap<String, URI> names = new LinkedHashMap<>();
        for (URI f : files) {
            String filename = FileTools.getFilename(f);
            String basename = StringTools.removeFileExt(filename);
            String ext = StringTools.getFileExtension(filename);
            if (ext.equals(filename)) {
                log.debug("Filename: {}", basename);
            } else {
                log.debug("Filename: {}", basename + "." + ext);
            }
            names.put(basename, f);
        }
        TupleND<IFileFragment> retFragments = new TupleND<>();
        for (IFileFragment fragment : inputFragments) {
            log.debug("InputFragment: " + fragment.getUri());
            String filename = FileTools.getFilename(fragment.getUri());
            String basename = StringTools.removeFileExt(filename);
            String ext = StringTools.getFileExtension(filename).toLowerCase();
            if (ext.equals(filename)) {
                log.debug("Filename: {}", basename);
            } else {
                log.debug("Filename: {}", basename + "." + ext);
            }
            retFragments.add(new FileFragment(names.get(basename)));
        }
        return retFragments;
    }

    /**
     * Maps a list of Files, which resemble processing results of input file
     * fragments to the input file fragments, mainting the order of the input
     * fragments.
     *
     * @param files          the file URIs to map
     * @param inputFragments the input fragments
     * @return the mapped file fragments
     */
    public TupleND<IFileFragment> mapToInput(List<File> files,
        TupleND<IFileFragment> inputFragments) {
        List<URI> uris = new LinkedList<>();
        for (File f : files) {
            log.info("Adding result file {}", f.toURI());
            uris.add(f.toURI());
        }
        return mapToInputUri(uris, inputFragments);
    }

    /**
     * Create a non-blocking completion service for the given service object
     * type.
     *
     * @param <T>               the serializable result type
     * @param serviceObjectType the service object type
     * @return a new completion service with 1000 milliseconds time out for
     *         non-blocking wait
     */
    public <T extends Serializable> ICompletionService<T> createCompletionService(
        Class<? extends T> serviceObjectType) {
        return createNonBlockingCompletionService(serviceObjectType, 1000,
            TimeUnit.MILLISECONDS);
    }

    /**
     * Create a blocking completion service for the given service object type.
     *
     * @param <T>               the serializable result type
     * @param serviceObjectType the service object type
     * @return a new completion service with blocking wait
     */
    public <T extends Serializable> ICompletionService<T> createBlockingCompletionService(
        Class<? extends T> serviceObjectType) {
        ICompletionService<T> ics = null;
        CompletionServiceFactory<T> csf = new CompletionServiceFactory<>();
        csf.setBlockingWait(true);
        if (getWorkflow().isExecuteLocal()) {
            log.info("Creating local completion service!");
            csf.setMaxThreads(workflow.getConfiguration().getInt("cross.Factory.maxthreads", 1));
            ics = csf.newLocalCompletionService();
        } else {
            log.info("Creating mpaxs completion service!");
            ics = csf.newDistributedCompletionService();
        }
        return ics;
    }

    /**
     * Create a non-blocking completion service for the given service object
     * type and time out parameters.
     *
     * @param <T>               the serializable result type
     * @param serviceObjectType the service object type
     * @param timeOut           the time out to wait for results
     * @param timeUnit          the time out unit
     * @return a new completion service with the specified time out for
     *         non-blocking wait
     */
    public <T extends Serializable> ICompletionService<T> createNonBlockingCompletionService(
        Class<? extends T> serviceObjectType, long timeOut,
        TimeUnit timeUnit) {
        ICompletionService<T> ics = null;
        CompletionServiceFactory<T> csf = new CompletionServiceFactory<>();
        csf.setTimeOut(timeOut);
        csf.setTimeUnit(timeUnit);
        if (getWorkflow().isExecuteLocal()) {
            log.info("Creating local completion service!");
            csf.setMaxThreads(workflow.getConfiguration().getInt("cross.Factory.maxthreads", 1));
            ics = csf.newLocalCompletionService();
        } else {
            log.info("Creating mpaxs completion service!");
            ics = csf.newDistributedCompletionService();
        }
        return ics;
    }

    /**
     * Append the given file fragments as workflow results.
     *
     * @param fragments the file fragments
     */
    public void addWorkflowResults(IFileFragment... fragments) {
        for (IFileFragment fragment : fragments) {
            addWorkflowResult(fragment);
        }
    }

    /**
     * Append the given file fragments as workflow results.
     *
     * @param fragments the file fragments
     */
    public void addWorkflowResults(TupleND<IFileFragment> fragments) {
        for (IFileFragment fragment : fragments) {
            addWorkflowResult(fragment);
        }
    }

    /**
     * Append the given file fragment as workflow result.
     *
     * @param fragment the file fragment
     */
    public void addWorkflowResult(IFileFragment fragment) {
        getWorkflow().append(
            new DefaultWorkflowResult(new File(fragment.getUri()),
                this, getWorkflowSlot(), fragment));
    }

    /**
     * Append the given file fragment as workflow result, referencing the given
     * resources.
     *
     * @param fragment  the file fragment
     * @param resources the additional resources referenced by the fragment
     */
    public void addWorkflowResult(IFileFragment fragment,
        IFileFragment... resources) {
        getWorkflow().append(
            new DefaultWorkflowResult(new File(fragment.getUri()),
                this, getWorkflowSlot(), resources));
    }

    /**
     * Append the given file fragment as workflow result, referencing the given
     * resources under the category given by slot.
     *
     * @param fragment  the file fragment
     * @param slot      the workflow slot for the result
     * @param resources the additional resources referenced by the fragment
     */
    public void addWorkflowResult(IFileFragment fragment, WorkflowSlot slot,
        IFileFragment... resources) {
        getWorkflow().append(
            new DefaultWorkflowResult(new File(fragment.getUri()),
                this, slot, resources));
    }

    /**
     * Append the given file fragment as workflow result, referencing the given
     * resources under the category given by slot.
     *
     * @param fragment  the file fragment
     * @param producer  the workflow element that created the result
     * @param slot      the workflow slot for the result
     * @param resources the additional resources referenced by the fragment
     */
    public void addWorkflowResult(IFileFragment fragment,
        IWorkflowElement producer, WorkflowSlot slot,
        IFileFragment... resources) {
        getWorkflow().append(
            new DefaultWorkflowResult(new File(fragment.getUri()),
                producer, slot, resources));
    }

    /**
     * Initialize a {@link DefaultWorkflowProgressResult} object with the given
     * total step size.
     *
     * @param size the number of steps to complete the progress
     */
    public void initProgress(int size) {
        EvalTools.isNull(progress, this);
        setProgress(
            new DefaultWorkflowProgressResult(
                size, this, getWorkflowSlot()));
    }

    @Override
    public void removeListener(final IListener<IEvent<IWorkflowResult>> l) {
        this.eventSource.removeListener(l);
    }

    /**
     * Resolve the given variable name against the cvResolver.
     *
     * @param varname the variable name to resolve, e.g. var.total_intensity ->
     *                total_intensity
     * @return the resolved variable name
     */
    public String resolve(String varname) {
        try {
            String resolved = cvResolver.translate(varname);
            return resolved;
        } catch (MappingNotAvailableException mnae) {
            log.warn("Could not map variable: " + varname, mnae);
            return varname;
        }
    }

    /**
     * Save the given file fragments. Should only be called, if
     * <code>fileFragments</code> have not been saved yet.
     *
     * @param fileFragments
     * @return the saved (immutable) file fragments
     */
    public TupleND<IFileFragment> save(TupleND<IFileFragment> fileFragments) {
        for (IFileFragment f : fileFragments) {
            f.save();
        }
        return fileFragments;
    }

    /**
     * Returns the name of this class.
     *
     * @return the name of this class
     */
    @Override
    public String toString() {
        return getClass().getName();
    }
}
