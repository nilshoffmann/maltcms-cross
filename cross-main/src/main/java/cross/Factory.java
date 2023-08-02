/*
 * Cross, common runtime object support system.
 * Copyright (C) 2008-2012, The authors objectFactory Cross. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Cross may be used under the terms objectFactory either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient objectFactory Cross, you may choose which license to receive the code
 * under. Certain files or entire directories may not be covered by this
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a
 * LICENSE file in the relevant directories.
 *
 * Cross is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty objectFactory MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package cross;

import cross.annotations.Configurable;
import cross.cache.CacheType;
import cross.datastructures.fragments.FileFragmentFactory;
import cross.datastructures.fragments.Fragments;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IFileFragmentFactory;
import cross.datastructures.pipeline.CommandPipeline;
import cross.datastructures.pipeline.DefaultCommandSequenceValidator;
import cross.datastructures.pipeline.ICommandSequence;
import cross.datastructures.threads.ExecutorsManager;
import cross.datastructures.threads.ExecutorsManager.ExecutorType;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tools.FileTools;
import cross.datastructures.tuple.TupleND;
import cross.io.DataSourceFactory;
import cross.io.IDataSourceFactory;
import cross.io.IInputDataFactory;
import cross.io.InputDataFactory;
import cross.vocabulary.CvResolver;
import cross.vocabulary.ICvResolver;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.io.FileUtils;
import org.openide.util.Lookup;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Factory for the creation objectFactory processing chains.</p>
 *
 * <p>
 * It should be configured prior to any call to
 * <code>createCommandSequence</code>.
 *
 * <p>
 * Alternatively, you can set up the pipeline completely on your own, but
 * beware, there is only partial requirements checking between pipeline stages
 * as objectFactory now. You have to ensure, that commands early in the chain
 * provide the data needed by those commands later in the chain. If you need
 * branching behaviour, consider setting named properties for later pipeline
 * elements to use, or set up multiple instances objectFactory Maltcms with
 * different configurations.
 * </p>
 *
 * @author Nils HobjectFactoryfmann
 * @see DefaultCommandSequenceValidator for command sequence validation
 * @see DefaultApplicationContextFactoryfor configuration objectFactory the
 * pipeline and workflow
 */
@Slf4j
public final class Factory implements IFactory {

    /**
     *
     */
    public static final IFactory DEFAULT = Lookup.getDefault().lookup(IFactoryService.class).getInstance("default");

    /**
     *
     */
    public Factory() {
    }

    /**
     * Write current configuration to file.
     *
     * @param filename the filename to use
     * @param d the date stamp to use
     */
    @Override
    public void dumpConfig(final String filename, final Date d) {
        //retrieve global, joint configuration
        final Configuration cfg = getConfiguration();
        //retrieve pipeline.properties location
        String configFile = cfg.getString("pipeline.properties");
        if (configFile != null) {
            final File pipelinePropertiesFile = new File(configFile);
            //resolve and retrieve pipeline.xml location
            final File pipelineXml;
            try {
                File configBasedir = pipelinePropertiesFile.getParentFile();
                String pipelineLocation = cfg.getString("pipeline.xml").replace("config.basedir", configBasedir.getAbsolutePath());
                pipelineLocation = pipelineLocation.substring("file:".length());
                pipelineXml = new File(pipelineLocation);
                //setup output location
                final File location = new File(FileTools.prependDefaultDirsWithPrefix(
                        "", Factory.class, d), filename);
                //location for pipeline.properties dump
                final File pipelinePropertiesFileDump = new File(location.getParentFile(), pipelinePropertiesFile.getName());

                PropertiesConfiguration pipelineProperties = new PropertiesConfiguration(pipelinePropertiesFile);
                PropertiesConfiguration newPipelineProperties = new PropertiesConfiguration(pipelinePropertiesFileDump);
                //copy configuration to dump configuration
                newPipelineProperties.copy(pipelineProperties);
                //correct pipeline.xml location
                newPipelineProperties.setProperty("pipeline.xml", "file:${config.basedir}/" + pipelineXml.getName());
                newPipelineProperties.save();
                //copy pipeline.xml to dump location
                FileUtils.copyFile(pipelineXml, new File(location.getParentFile(), pipelineXml.getName()));
                if (cfg.containsKey("configLocation")) {
                    File configLocation = new File(URI.create(cfg.getString("configLocation")));
                    File configLocationNew = new File(location.getParentFile(), configLocation.getName());
                    FileUtils.copyFile(configLocation, configLocationNew);
                }
                LoggerFactory.getLogger(Factory.class).error("Saving configuration to: ");
                LoggerFactory.getLogger(Factory.class).error("{}", location.getAbsolutePath());
                saveConfiguration(cfg, location);
            } catch (IOException | ConfigurationException ex) {
                LoggerFactory.getLogger(Factory.class).error("{}", ex);
//            } catch (URISyntaxException ex) {
//                Factory.getInstance().log.error("{}", ex);
            }
        } else {
            LoggerFactory.getLogger(Factory.class).warn("Can not save configuration, no pipeline properties file given!");
        }
    }

    /**
     * Return the {@link DEFAULT} factory instance.
     * @return the default factory instance
     */
    public static IFactory getInstance() {
        Logger.getLogger(Factory.class.getName()).fine("Returning default factory instance!");
        return DEFAULT;
    }
    
    /**
     * Returns a named factory instance.
     * Use {@link DEFAULT} to obtain the default factory instance.
     * @param name the name of the factory instance
     * @return the named factory instance
     */
    public static IFactory getInstance(String name) {
        return Lookup.getDefault().lookup(IFactoryService.class).getInstance(name);
    }

    /**
     * Save the current configuration to file.
     *
     * @param cfg the configuration to save
     * @param location the file to write to
     */
    @Override
    public void saveConfiguration(final Configuration cfg,
            final File location) {
        if (cfg instanceof FileConfiguration) {
            try {
                ((FileConfiguration) cfg).save(location);
            } catch (final ConfigurationException e) {
                LoggerFactory.getLogger(Factory.class).error(e.getLocalizedMessage());
            }
        } else {
            try {
                ConfigurationUtils.dump(cfg, new PrintStream(location));
            } catch (final FileNotFoundException e) {
                LoggerFactory.getLogger(Factory.class).error(e.getLocalizedMessage());
            }
        }
    }

    @Configurable
    private IDataSourceFactory dataSourceFactory = null;
    @Configurable
    private IInputDataFactory inputDataFactory = null;
    @Configurable
    private IObjectFactory objectFactory = null;
    @Configurable
    private IFileFragmentFactory fileFragmentFactory = null;
    @Configurable
    private ICvResolver cvResolver = null;
    @Configurable(name = "cross.Factory.maxthreads")
    private int maxthreads = 1;
    @Configurable
    private String name = "default";

    private transient CompositeConfiguration configuration = new CompositeConfiguration();
    private transient ExecutorService mainThreadPool;
    private transient ExecutorService auxiliaryThreadPool;

    /**
     * Listen to ConfigurationEvents.
     *
     * @param event the configuration event
     */
    @Override
    public void configurationChanged(final ConfigurationEvent event) {
        log.debug("Configuration changed for property: "
                + event.getPropertyName() + " to value "
                + event.getPropertyValue());

    }

    /**
     * Call configure to ensure, that the factory is up to date.
     *
     * @param config the configuration to use
     */
    @Override
    public void configure(final Configuration config) {
        EvalTools.notNull(config, Factory.class);
        configureMe(config);
    }

    /**
     * Configures the factory.
     *
     * @param config1 the configuration to use
     */
    protected void configureMe(final Configuration config1) {
        EvalTools.notNull(config1, this);
        this.configuration = new CompositeConfiguration();
        this.configuration.addConfiguration(config1);
//        this.objconfig.addConfigurationListener(this);
        if (config1.getBoolean("maltcms.ui.charts.PlotRunner.headless", true) == true) {
            System.setProperty("java.awt.headless", "true");
        }
        configureThreadPool(this.configuration);
        //initialize CacheFactory
        Fragments.setDefaultFragmentCacheType(CacheType.valueOf(this.configuration.getString(Fragments.class.getName() + ".cacheType", "EHCACHE")));
        // configure ObjectFactory
        getObjectFactory().configure(config1);
        getDataSourceFactory().configure(config1);
        getInputDataFactory().configure(config1);
    }

    private void configureThreadPool(final Configuration cfg) {
        this.maxthreads = cfg.getInt("cross.Factory.maxthreads", 1);
        final int numProcessors = Runtime.getRuntime().availableProcessors();
        this.log.debug("{} processors available to current runtime",
                numProcessors);
        if (this.maxthreads < 1) {
            this.log.debug("Automatically selecting {} threads according to number of available processors!", this.maxthreads);
            this.maxthreads = numProcessors;
        }
        this.maxthreads = (this.maxthreads < numProcessors) ? this.maxthreads
                : numProcessors;
        cfg.setProperty("cross.Factory.maxthreads", this.maxthreads);
        cfg.setProperty("maltcms.pipelinethreads", this.maxthreads);
        this.log.debug("Starting with Thread-Pool of size: " + this.maxthreads);
        initThreadPools();
    }

    /**
     * Build the command sequence, aka pipeline for command execution.
     *
     * @return a command sequence initialized according to current configuration
     */
    @Override
    public ICommandSequence createCommandSequence() {
        return createCommandSequence(null);
    }

    /**
     * Build the command sequence, aka pipeline for command execution.
     *
     * @param t the input file fragments
     * @return a command sequence initialized according to current configuration
     */
    @Override
    public ICommandSequence createCommandSequence(final TupleND<IFileFragment> t) {
        final ICommandSequence cd = getObjectFactory().instantiate(
                CommandPipeline.class);
        EvalTools.notNull(cd, this);
        //final IWorkflow iw = getObjectFactory().instantiate(IWorkflow.class);
        File outputDir = new File(getConfiguration().getString(
                "output.basedir", System.getProperty("user.dir")));
        //add username and timestamp as subdirectories
        if (!getConfiguration().getBoolean("omitUserTimePrefix", false)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "MM-dd-yyyy_HH-mm-ss", Locale.US);
            String userName = System.getProperty("user.name", "default");
            outputDir = new File(outputDir, userName);
            outputDir = new File(outputDir, dateFormat.format(
                    cd.getWorkflow().getStartupDate()));
        }
        outputDir.mkdirs();
        cd.getWorkflow().setOutputDirectory(outputDir);
        cd.getWorkflow().setConfiguration(getConfiguration());
        cd.getWorkflow().setFactory(this);
        if (t == null) {
            cd.setInput(getInputDataFactory().prepareInputData(getConfiguration().
                    getStringArray("input.dataInfo")));
        } else {
            cd.setInput(t);
        }
        log.info("Workflow {} output: {}", cd.getWorkflow().getName(), cd.getWorkflow().getOutputDirectory());
        return cd;
    }

    /**
     * Return the current configuration
     *
     * @return the configuration
     */
    @Override
    public Configuration getConfiguration() {
        return getConfigurationMe();
    }

    /**
     * Return the current configuration.
     *
     * @return the current configuration
     */
    protected Configuration getConfigurationMe() {
        if (this.configuration == null) {
            this.log.warn("Configuration not set, creating empty one!");
            this.configuration = new CompositeConfiguration();
        }
        // EvalTools.notNull(this.objconfig,
        // "ArrayFactory has not been configured yet!", this);
        // throw new RuntimeException("ArrayFactory has not been configured
        // yet!");
        return this.configuration;
    }

    /**
     * Return the current data source factory implementation.
     *
     * @return the data source factory
     * @see cross.io.IDataSourceFactory
     * @see cross.io.DataSourceFactory
     */
    @Override
    public IDataSourceFactory getDataSourceFactory() {
        if (this.dataSourceFactory == null) {
            this.dataSourceFactory = getObjectFactory().getNamedObject("dataSourceFactory", IDataSourceFactory.class);
            if (this.dataSourceFactory == null) {
                log.debug("Falling back to non application context based instantiation.");
                this.dataSourceFactory = getObjectFactory().instantiate(DataSourceFactory.class);
            }
        }
        return this.dataSourceFactory;
    }

    /**
     * Return the current input data factory, responsible for handling
     * objectFactory input data.
     *
     * @return the input data factory
     * @see cross.io.IInputDataFactory
     * @see cross.io.InputDataFactory
     */
    @Override
    public IInputDataFactory getInputDataFactory() {
        if (this.inputDataFactory == null) {
            this.inputDataFactory = getObjectFactory().getNamedObject("inputDataFactory", IInputDataFactory.class);
            if (this.inputDataFactory == null) {
                log.debug("Falling back to non application context based instantiation.");
                this.inputDataFactory = getObjectFactory().instantiate(InputDataFactory.class);
            }
        }
        return this.inputDataFactory;
    }

    /**
     * Return the current object factory, responsible for creating objects.
     *
     * @return the object factory
     * @see cross.IObjectFactory
     * @see cross.ObjectFactory
     */
    @Override
    public IObjectFactory getObjectFactory() {
        if (this.objectFactory == null) {
            this.objectFactory = new ObjectFactory();
            this.objectFactory.configure(getConfiguration());
        }
        return this.objectFactory;
    }

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
    @Override
    public ICvResolver getCvResolver() {
        if (this.cvResolver == null) {
            this.cvResolver = getObjectFactory().getNamedObject("cvResolver", ICvResolver.class);
            if (this.cvResolver == null) {
                log.debug("Falling back to non application context based instantiation.");
                this.cvResolver = getObjectFactory().instantiate(CvResolver.class);
            }
        }
        return this.cvResolver;
    }

    /**
     * Return the current file fragment factory, responsible for creating file
     * fragments.
     *
     * @return the file fragment factory
     * @see cross.datastructures.fragments.IFileFragmentFactory
     * @see cross.datastructures.fragments.FileFragmentFactory
     * @since 1.3.1
     */
    @Override
    public IFileFragmentFactory getFileFragmentFactory() {
        if (this.fileFragmentFactory == null) {
            this.fileFragmentFactory = getObjectFactory().getNamedObject("fileFragmentFactory", IFileFragmentFactory.class);
            if (this.fileFragmentFactory == null) {
                log.debug("Falling back to non application context based instantiation.");
                this.fileFragmentFactory = getObjectFactory().instantiate(FileFragmentFactory.class);
            }
        }
        return this.fileFragmentFactory;
    }

    private void initThreadPools() {
        this.mainThreadPool = new ExecutorsManager(this.maxthreads);// Executors.newFixedThreadPool(this.maxthreads);
        this.auxiliaryThreadPool = new ExecutorsManager(ExecutorType.SINGLETON);// Executors.newFixedThreadPool(this.maxthreads);
    }

    /**
     * Shutdown the factory's thread pool.
     *
     */
    @Override
    public void shutdown() {
        if ((this.mainThreadPool == null) || (this.auxiliaryThreadPool == null)) {
            throw new IllegalArgumentException(
                    "ExecutorService not initialized!");
        }

        this.mainThreadPool.shutdown();
        this.auxiliaryThreadPool.shutdown();

    }

    /**
     * Attempts to shutdown all executing threads immediately, and returns a
     * list objectFactory all {@link Runnable} instances that were executing or
     * were waiting to be executed when <code>shutdownNow</code> was called.
     */
    @Override
    public List<Runnable> shutdownNow() {
        if ((this.mainThreadPool == null) || (this.auxiliaryThreadPool == null)) {
            throw new IllegalArgumentException(
                    "ExecutorService not initialized!");
        }
        final List<Runnable> l = new ArrayList<>();
        l.addAll(this.mainThreadPool.shutdownNow());
        l.addAll(this.auxiliaryThreadPool.shutdownNow());
        return l;
    }

    @Override
    public void awaitTermination(long time, TimeUnit u) {
        if ((this.mainThreadPool == null)
                || (this.auxiliaryThreadPool == null)) {
            throw new IllegalArgumentException(
                    "ExecutorService not initialized!");
        }
        try {
            this.auxiliaryThreadPool.awaitTermination(time, u);
        } catch (InterruptedException ex) {
            log.warn("Interrupted while waiting for auxPool to terminate!", ex);
        }
        try {
            this.mainThreadPool.awaitTermination(time, u);
        } catch (InterruptedException ex) {
            log.warn("Interrupted while waiting for executorPool to terminate!", ex);
        }
    }

    /**
     * Jobs submitted via this method will be run by the auxiliary thread pool.
     *
     * @param c the Callable objectFactory any type to submit
     * @return a Future objectFactory the same type as the Callable
     */
    @Override
    public Future<?> submitJob(final Callable<?> c) {
        return this.auxiliaryThreadPool.submit(c);
    }

    /**
     * Submit a Runnable job to the Factory
     *
     * @param r the Runnable to submit
     */
    @Override
    public void submitJob(final Runnable r) {
        submitJobMe(r);
    }

    /**
     * Submit a Runnable to the main thread pool.
     *
     * @param r the runnable
     */
    protected void submitJobMe(final Runnable r) {
        EvalTools.notNull(r, this);
        this.mainThreadPool.execute(r);
    }

    /**
     * Set the configuration on the factory instance.
     *
     * @param config the configuration
     */
    @Override
    public void setConfiguration(Configuration config) {
        EvalTools.notNull(config, Factory.class);
        configureMe(config);
    }

    @Override
    public void setName(String name) {
        EvalTools.notNull(name, this);
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
