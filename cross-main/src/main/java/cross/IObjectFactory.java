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

import java.util.Map;
import org.apache.commons.configuration.Configuration;

/**
 * An interface for the creation of objects and for their
 * configuration.
 *
 * @author Nils Hoffmann
 */
public interface IObjectFactory extends IConfigurable {

	/**
	 * Configure the given object, if it implements {@link IConfigurable}.
	 *
	 * @param <T> the generic type
	 * @param t   the object to be configured
	 */
	<T> void configureType(final T t);

	/**
	 * Create a new Instance of c, configure automatically, if c is an instance
	 * of IConfigurable
	 *
	 * @param <T> the generic type
	 * @param c   the desired runtime class
	 * @return a new, configured instance of the requested class
	 */
	<T> T instantiate(final Class<T> c);

	/**
	 * Instantiate a class, given by a classname and the class of Type T.
	 *
	 * @param <T>       the generic type
	 * @param classname the fully qualified name of the class
	 * @param cls       the desired runtime class
	 * @return a new, configured instance of the requested class
	 */
	<T> T instantiate(final String classname, final Class<T> cls);

	/**
	 * Instantiate a class, given a classname and the class of Type t and
	 * configure with configuration from configurationFile.
	 *
	 * @param <T>               the generic type
	 * @param classname         the fully qualified name of the class
	 * @param cls               the requested target class
	 * @param configurationFile the configuration file location to use
	 * @return a new, configured instance of the requested class
	 */
	<T> T instantiate(final String classname, final Class<T> cls, final String configurationFile);

	/**
	 * Instantiate a class, given a classname and the class of Type t and
	 * configure with configuration from config.
	 *
	 * @param <T>       the generic type
	 * @param classname the fully qualified name of the class
	 * @param cls       the requested target class
	 * @param config    the configuration to use
	 * @return a new, configured instance of the requested class
	 */
	<T> T instantiate(final String classname, final Class<T> cls, final Configuration config);

	/**
	 * Returns objects of the given type, or none if not object of that class is known.
	 *
	 * @param <T> the generic type
	 * @param cls the requested target class
	 * @return the objects of the given class type
	 */
	<T> Map<String, T> getObjectsOfType(final Class<T> cls);

	/**
	 * Returns an object by name, or none if no object of that name is known.
	 *
	 * @param <T>  the generic type
	 * @param name the name of the requestd class object
	 * @param cls  the requested target class
	 * @return the object with the given name and class type
	 */
	<T> T getNamedObject(final String name, final Class<T> cls);
}
