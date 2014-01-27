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

import cross.annotations.AnnotationInspector;
import cross.applicationContext.DefaultApplicationContextFactory;
import cross.datastructures.tools.EvalTools;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.openide.util.lookup.ServiceProvider;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

/**
 * <p>
 * Implementation of {@link IObjectFactory}, backed by a Spring application
 * context. The resource location of the context xml file is referenced in the
 * provided configuration file by the key
 * <code>pipeline.xml = file:path/to/myPipeline.xml</code>.</p>
 *
 * <p>
 * The location can be combined with an environment variable, so that
 * pipelines can be placed below a common base directory:
 * <code>pipeline.xml = file:${config.basedir}/path/to/myPipeline.xml</code>,
 * where
 * <code>${config.basedir}</code> is a system property that can be set when
 * cross is invoked:
 * <code>-Dconfig.basedir=/path/to/my/configuration</code>.</p>
 *
 * <p>
 * Multiple locations can be defined, by appending them as values for key pipeline.xml, separated
 * by a ',' (comma): <code>pipeline.xml = file:path/to/component1.xml,file:path/to/component2.xml,file:path/to/myPipeline.xml</code></p>
 *
 * <p>
 * Beans in any of these configurations can reference other beans in other or the same configuration by id</p>
 *
 * <p>
 * The default workflow xml file is expected at
 * <code>file:${maltcms.home}/cfg/pipelines/xml/workflowDefaults.xml</code>.
 * That file is used to set up defaults for the workflow.</p>
 *
 * <p>
 * The property <code>cross.applicationContext.defaultLocations=location1,location2,location3</code>,
 * holds a list of default application context xml resources to load, apart from the one for the current pipeline.
 * The default locations are added before the user-defined context locations.</p>
 *
 * @author Nils Hoffmann
 */
@Slf4j
@ServiceProvider(service = IObjectFactory.class)
public class ObjectFactory implements IObjectFactory {

    private Configuration cfg = new PropertiesConfiguration();
    private ApplicationContext context = null;
    public static final String CONTEXT_LOCATION_KEY = "pipeline.xml";

    @Override
    public void configure(final Configuration cfg) {
        this.cfg = cfg;
        String[] contextLocations = null;
        if (this.cfg.containsKey(CONTEXT_LOCATION_KEY)) {
            log.debug("Using user-defined location: {}", (Object[]) this.cfg.getStringArray(CONTEXT_LOCATION_KEY));
            contextLocations = cfg.getStringArray(CONTEXT_LOCATION_KEY);
        }
        if (contextLocations == null) {
            log.debug("No pipeline configuration found! Please define! Example: -c cfg/pipelines/chroma.mpl");
            return;
        }
        log.debug("Using context locations: {}",
            Arrays.toString(contextLocations));
        try {
            if (cfg.containsKey("maltcms.home")) {
                File f = new File(new File(cfg.getString("maltcms.home")), "cfg/pipelines/xml/workflowDefaults.xml");
                if (f.exists()) {
                    log.info("Using workflow defaults at: {}", f);
                    cfg.setProperty("cross.applicationContext.workflowDefaults", cfg.getString("cross.applicationContext.workflowDefaults.file"));
                }
            } else {
                log.info("Using workflow defaults from classpath.");
            }
            String[] defaultLocations = cfg.getStringArray("cross.applicationContext.defaultLocations");
            log.debug("Using default context locations: {}", Arrays.toString(defaultLocations));
            LinkedList<String> applicationContextLocations = new LinkedList<String>(Arrays.asList(defaultLocations));
            applicationContextLocations.addAll(Arrays.asList(contextLocations));
            context = new DefaultApplicationContextFactory(applicationContextLocations, this.cfg).
                createApplicationContext();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public <T> void configureType(final T t) {
        configureType(t, this.cfg);
    }

    private <T> T configureType(final T t, final Configuration cfg) {
        if (t instanceof IConfigurable) {
            if (cfg == null || cfg.isEmpty()) {
                log.warn(
                    "ObjectFactory's configuration is null or empty! Skipping configuration of {}",
                    t.getClass().getName());
                return t;
            }
            log.debug("Instance of type {} is configurable!", t.getClass().
                toString());
            final Collection<String> requiredKeys = AnnotationInspector.
                getRequiredConfigKeys(t);
            log.debug("Required keys for class {}", t.getClass());
            log.debug("{}", requiredKeys);
            log.debug("Configuring with full configuration!");
            ((IConfigurable) t).configure(cfg);
        }
        return t;
    }

    @Override
    public <T> T instantiate(final Class<T> c) {
        if (context != null) {
            try {
                T t = context.getBean(c);
                log.info("Retrieved bean {} from context!", t.getClass().getName());
                return t;
            } catch (NoSuchBeanDefinitionException nsbde) {
                log.debug("Could not create bean {} from context! Reason:\n {}",
                    c.getName(), nsbde.getLocalizedMessage());
            } catch (BeansException be) {
                log.debug("Could not create bean {} from context! Reason:\n {}",
                    c.getName(), be.getLocalizedMessage());
            }
        }
        log.info("Using regular configuration mechanism on instance of type " + c.getName());
        return configureType(instantiateType(c), this.cfg);
    }

    private <T> T instantiateType(final Class<T> c) {
        try {
            return c.newInstance();
        } catch (final InstantiationException e) {
            log.error(e.getLocalizedMessage());
        } catch (final IllegalAccessException e) {
            log.error(e.getLocalizedMessage());
        }
        throw new IllegalArgumentException("Could not instantiate class "
            + c.getName());
    }

    @Override
    public <T> T instantiate(final String classname, final Class<T> cls) {
        EvalTools.notNull(classname, "Class name of type " + cls.getName()
            + " was null!", Factory.class);
        final Class<?> c = loadClass(classname);
        final Class<? extends T> t = c.asSubclass(cls);
        return instantiate(t);
    }

    @Override
    public <T> T instantiate(final String classname, final Class<T> cls,
        final String configurationFile) {
        CompositeConfiguration cc = new CompositeConfiguration();
        try {
            File configFileLocation = new File(configurationFile);
            cc.addConfiguration(new PropertiesConfiguration(configFileLocation.
                getAbsolutePath()));
        } catch (ConfigurationException e) {
            log.warn(e.getLocalizedMessage());
        }
        cc.addConfiguration(this.cfg);

        return instantiate(classname, cls, cc);
    }

    @Override
    public <T> T instantiate(final String classname, final Class<T> cls,
        final Configuration config) {
        return configureType(instantiate(classname, cls), config);
    }

    /**
     * Load a class by its name. Tries to locate the given class name on the
     * user class path and on the default java class path. Currently only
     * supports loading of classes from local storage.
     *
     * @param name the fully qualified name of the class
     * @return the loaded class or null if any exception is encountered
     */
    protected Class<?> loadClass(final String name) {
        EvalTools.notNull(name, ObjectFactory.class);
        Class<?> cls = null;
        try {
            log.debug("Loading class {}", name);
            try {
                cls = this.getClass().getClassLoader().loadClass(name);
                EvalTools.notNull(cls, ObjectFactory.class);
                return cls;
            } catch (final NullPointerException npe) {
                log.error("Could not load class with name " + name
                    + "! Check for typos!");
            }
        } catch (final ClassNotFoundException e) {
            log.error(e.getLocalizedMessage());
        }
        return cls;
    }

    @Override
    public <T> Map<String, T> getObjectsOfType(Class<T> cls) {
        return context.getBeansOfType(cls);
    }

    @Override
    public <T> T getNamedObject(String name,
        Class<T> cls) {
        return context.getBean(name, cls);
    }
}
