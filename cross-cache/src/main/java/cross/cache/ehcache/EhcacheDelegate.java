/* 
 * Cross, common runtime object support system. 
 * Copyright (C) 2008-2014, The authors of Cross. All rights reserved.
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
package cross.cache.ehcache;

import cross.cache.CacheType;
import cross.cache.ICacheDelegate;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

/**
 *
 * Implementation of a cache delegate for typed caches backed by <a
 * href="http://www.ehcache.org">ehcache</a>.
 *
 * Please note that Ehcache only allows Serializable objects to be externalized
 * to disk, should the in-memory cache overflow.
 *
 * @author Nils Hoffmann
 * @param <K> the key type
 * @param <V> the value type
 */
@Slf4j
public class EhcacheDelegate<K, V> implements ICacheDelegate<K, V> {

    private final Ehcache cache;
    private final Set<K> keys;

    /**
     * Creates a new instance.
     *
     * @param cache the backing cache to use
     */
    public EhcacheDelegate(final Ehcache cache) {
        this.cache = cache;
        this.keys = Collections.newSetFromMap(new ConcurrentHashMap<K, Boolean>());
    }

    @Override
    public Set<K> keys() {
        return this.keys;
    }

    @Override
    public void put(final K key, final V value) {
        try {
            if (value == null) {
                getCache().remove(key);
                keys.remove(key);
            } else {
                getCache().put(new Element(key, value));
                keys.add(key);
            }
        } catch (IllegalStateException se) {
            log.warn("Failed to add element to cache: " + key, se);
        }
    }

    @Override
    public V get(final K key) {
        try {
            Element element = getCache().get(key);
            if (element != null) {
                element.getObjectValue();
                return (V) element.getObjectValue();
            }
            return null;
        } catch (IllegalStateException se) {
            log.warn("Failed to get element from cache: " + key, se);
            return null;
        }
    }

    @Override
    public void close() {
        cache.dispose();
    }

    /**
     * Returns the Ehcache instance backing this cache instance.
     *
     * @return the Ehcache instance
     */
    public Ehcache getCache() {
        return cache;
    }

    @Override
    public String getName() {
        return cache.getName();
    }

    @Override
    public CacheType getCacheType() {
        return CacheType.EHCACHE;
    }
}
