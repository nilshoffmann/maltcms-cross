/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maltcms may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Maltcms, you may choose which license to receive the code
 * under. Certain files or entire directories may not be covered by this
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a
 * LICENSE file in the relevant directories.
 *
 * Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package cross.test;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.rules.TestWatcher;

/**
 * TestWatcher to set up slf4j / log4j-backed logging.
 *
 * @author Nils Hoffmann
 */
public class SetupLogging extends TestWatcher {

    private final Properties config;

    /**
     *
     */
    public SetupLogging() {
        this(SetupLogging.class.getResource("/log4j.properties"));
    }

    /**
     * Create new instance with specified properties.
     *
     * @param props the properties
     */
    public SetupLogging(Properties props) {
        this.config = props;
        PropertyConfigurator.configure(config);
    }

    /**
     * Create new instance with specified properties from a URL, e.g. a classpath
     * resource.
     *
     * @param props the properties url
     */
    public SetupLogging(URL props) {
        Properties config = new Properties();
        try {
            config.load(props.openStream());
        } catch (IOException ex) {
            Logger.getLogger(SetupLogging.class.getName()).log(Level.SEVERE, null, ex);
            config.setProperty("log4j.rootLogger", "INFO, A1");
            config.setProperty("log4j.appender.A1",
                "org.apache.log4j.ConsoleAppender");
            config.setProperty("log4j.appender.A1.layout",
                "org.apache.log4j.PatternLayout");
            config.setProperty("log4j.appender.A1.layout.ConversionPattern",
                "%-4r [%t] %-5p %c %x - %m%n");
        }
        this.config = config;
        PropertyConfigurator.configure(config);
    }

    /**
     * Returns the current logging properties configuration.
     *
     * @return the loggin properties
     */
    public Properties getConfig() {
        return config;
    }

    /**
     * Set the log level (log4j) (<code>log4j.category.clazz.getName(), level</code>)
     * for a specific class.
     *
     * @param clazz the class to set the level for
     * @param level the log level, one of OFF, ERROR, WARN, INFO, DEBUG
     */
    public void setLogLevel(Class clazz, String level) {
        getConfig().put("log4j.category." + clazz.getName(), level);
        update();
    }

    /**
     * Set the log level (log4j) (<code>log4j.category.clazz.getName(), level</code>)
     * for a specific class.
     *
     * @param packageOrClass the class or package to set the level for
     * @param level          the log level, one of OFF, ERROR, WARN, INFO, DEBUG
     */
    public void setLogLevel(String packageOrClass, String level) {
        if (packageOrClass.startsWith("log4j.category.")) {
            getConfig().put(packageOrClass, level);
        } else {
            getConfig().put("log4j.category." + packageOrClass, level);
        }
        update();
    }

    /**
     * Update the log4j logging backend with the current configuration.
     */
    public void update() {
        PropertyConfigurator.configure(config);
    }
}
