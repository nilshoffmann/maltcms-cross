package cross;
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

import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IFileFragmentFactory;
import cross.datastructures.pipeline.ICommandSequence;
import cross.datastructures.tuple.TupleND;
import cross.io.IDataSourceFactory;
import cross.io.IInputDataFactory;
import cross.vocabulary.ICvResolver;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.event.ConfigurationListener;

/**
 * <p>
 * Factory interface for the creation of processing chains.</p>
 *
 * <p>
 * It should be configured prior to any call to
 * <code>createCommandSequence</code>.
 *
 *
 * @author Nils Hoffmann
 * @see cross.Factory
 * @since 1.3.1
 */
public interface IFactory extends ConfigurationListener, IConfigurable {

    /**
     * Call configure before retrieving an instance of ArrayFactory. This
     * ensures, that the factory is instantiated with a fixed config.
     *
     * @param config the configuration to use
     * @deprecated please use {@link #setConfiguration(Configuration)} instead
     */
    @Deprecated
    void configure(final Configuration config);

    void setConfiguration(final Configuration config);

    /**
     * Returns the name of this factory instance or 'default' if none was assigned.
     *
     * @return the name
     */
    String getName();

    /**
     * Set the name of this factory. Null is not permitted.
     *
     * @param name the name
     */
    void setName(String name);

    /**
     * Build the command sequence, aka pipeline for command execution.
     *
     * @return a command sequence initialized according to current configuration
     */
    ICommandSequence createCommandSequence();

    /**
     * Build the command sequence, aka pipeline for command execution.
     *
     * @return a command sequence initialized according to current configuration
     */
    ICommandSequence createCommandSequence(final TupleND<IFileFragment> t);

    /**
     * Return the current configuration
     *
     * @return the configuration
     */
    Configuration getConfiguration();

    /**
     * Return the current data source factory implementation.
     *
     * @return the data source factory
     * @see cross.io.IDataSourceFactory
     * @see cross.io.DataSourceFactory
     */
    IDataSourceFactory getDataSourceFactory();

    /**
     * Return the current input data factory, responsible for handling of input
     * data.
     *
     * @return the input data factory
     * @see cross.io.IInputDataFactory
     * @see cross.io.InputDataFactory
     */
    IInputDataFactory getInputDataFactory();

    /**
     * Return the current object factory, responsible for creating objects.
     *
     * @return the object factory
     * @see cross.IObjectFactory
     * @see cross.ObjectFactory
     */
    IObjectFactory getObjectFactory();

    /**
     * Return the current file fragment factory, responsible for creating
     * file fragments.
     *
     * @return the file fragment factory
     * @see cross.datastructures.fragments.IFileFragmentFactory
     * @see cross.datastructures.fragments.FileFragmentFactory
     * @since 1.3.1
     */
    IFileFragmentFactory getFileFragmentFactory();

    /**
     * Return the current cv resolver, responsible for resolving cv terms and
     * namespaced variable terms to their clear names.
     *
     * @return the cv resolver
     * @see cross.vocabulary.ICvResolver
     * @see cross.vocabulary.IControlledVocabularyProvider
     * @see cross.vocabulary.CvResolver
     * @since 1.3.1
     */
    ICvResolver getCvResolver();

    /**
     * Shutdown the factory's thread pool.
     *
     */
    void shutdown();

    /**
     * Attempts to shutdown all executing threads immediately, and returns a
     * list of all {@link Runnable} instances that were executing or were
     * waiting to be executed when
     * <code>shutdownNow</code> was called.
     *
     * @return the list of runnables that have not yet finished execution
     */
    List<Runnable> shutdownNow();

    /**
     * Waits for termination of executors for at most the given time.
     *
     * @param time the time to wait for
     * @param u    the time unit
     */
    void awaitTermination(final long time, final TimeUnit u);

    /**
     * Jobs submitted via this method will be run by the auxiliary thread pool.
     *
     * @param c the Callable of any type to submit
     * @return a Future of the same type as the Callable
     */
    Future<?> submitJob(final Callable<?> c);

    /**
     * Submit a Runnable job to the Factory
     *
     * @param r the Runnable to submit
     */
    void submitJob(final Runnable r);

}
