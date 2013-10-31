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
package cross.cache;

import java.util.Set;

/**
 * Interface for facades in front of particular cache implementations.
 *
 * Delegates operations to underlying cache but presents a
 * unified interface.
 *
 * @author Nils Hoffmann
 * @param <K> the key type
 * @param <V> the value type
 */
public interface ICacheDelegate<K, V> {

	/**
	 * Place a value in the cache, identified by the key.
	 *
	 * @param key   the key
	 * @param value the value
	 */
	void put(K key, V value);

	/**
	 * Returns the associated value for the key, or null if the key
	 * has no association.
	 *
	 * @param key the key
	 * @return the value associated to the key, or null
	 */
	V get(K key);

	/**
	 * Returns the name of the cache.
	 *
	 * @return the name of the cache
	 */
	String getName();

	/**
	 * Returns the set of currently cached keys.
	 *
	 * @return the key set
	 */
	Set<K> keys();

	/**
	 * Removes all keys in the cache and initiates an orderly shutdown / release
	 * of any resources.
	 */
	void close();

	/**
	 * Returns the type of the cache.
	 *
	 * @return the type of the cache
	 * @see CacheType
	 */
	CacheType getCacheType();
}
