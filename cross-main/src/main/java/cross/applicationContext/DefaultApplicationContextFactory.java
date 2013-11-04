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
package cross.applicationContext;

import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * <p>Creates an application context instance from the configuration files at
 * <code>applicationContextPaths</code>. The paths have to be encoded in a
 * <a
 * href="http://docs.spring.io/spring/docs/3.2.4.RELEASE/spring-framework-reference/html/resources.html#resources-app-ctx"
 * target="_blank">style readable by Spring</a>.</p>
 *
 * <p>The
 * <code>configuration</code> object is used within an instance of
 * {@link ConfiguringBeanPostProcessor} in order to perform post-construction
 * configuration of beans. This used to be the only way of configuring beans in
 * Cross versions 1.0 and below, and is still present for
 * backwards-compatibility. It has not yet been deprecated but will be in a
 * future version.</p>
 *
 * <p>There are some implications: If your bean implements
 * {@link IConfigurable}, and your implementation of the
 * <code>configure</code> method overrides the settings performed by the
 * application context, you may get unexpected runtime behaviour. So be aware
 * that
 * <code>configure</code> is always called <b>after</b> the bean has been
 * constructed and may revert / override the settings given in the xml
 * configuration.</p>
 *
 * @author Nils Hoffmann
 */
@Slf4j
@Data
public class DefaultApplicationContextFactory {

	private final List<String> applicationContextPaths;
	private final Configuration configuration;

	/**
	 * Creates a new application context from the current list of
	 * <code>applicationContextPaths</code>, interpreted as file system
	 * resources, and the current
	 * <code>configuration</code>.
	 *
	 * @return the application context
	 * @throws BeansException if any beans are not configured correctly
	 */
	public ApplicationContext createApplicationContext() throws BeansException {
		AbstractRefreshableApplicationContext context = null;
		try {
			final ConfiguringBeanPostProcessor cbp = new ConfiguringBeanPostProcessor();
			cbp.setConfiguration(configuration);
			context = new FileSystemXmlApplicationContext(applicationContextPaths.toArray(new String[applicationContextPaths.size()]), context);
			context.addBeanFactoryPostProcessor(new BeanFactoryPostProcessor() {
				@Override
				public void postProcessBeanFactory(ConfigurableListableBeanFactory clbf) throws BeansException {
					clbf.addBeanPostProcessor(cbp);
				}
			});
			context.refresh();
		} catch (BeansException e2) {
			throw e2;
		}
		return context;
	}

	/**
	 * Creates a new application context from the current list of
	 * <code>applicationContextPaths</code>, interpreted as class path
	 * resources, and the current <code>configuration</code>.
	 *
	 * @return the application context
	 * @throws BeansException if any beans are not configured correctly
	 */
	public ApplicationContext createClassPathApplicationContext() throws BeansException {
		AbstractRefreshableApplicationContext context = null;
		try {
			final ConfiguringBeanPostProcessor cbp = new ConfiguringBeanPostProcessor();
			cbp.setConfiguration(configuration);
			context = new ClassPathXmlApplicationContext(applicationContextPaths.toArray(new String[applicationContextPaths.size()]), context);
			context.addBeanFactoryPostProcessor(new BeanFactoryPostProcessor() {
				@Override
				public void postProcessBeanFactory(ConfigurableListableBeanFactory clbf) throws BeansException {
					clbf.addBeanPostProcessor(cbp);
				}
			});
			context.refresh();
		} catch (BeansException e2) {
			throw e2;
		}
		return context;
	}
}
