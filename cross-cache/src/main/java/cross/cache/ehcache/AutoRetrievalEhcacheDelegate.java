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
import cross.cache.ICacheElementProvider;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.exceptionhandler.CacheExceptionHandler;

/**
 * Transparent cache, which also knows how to create objects of the given type
 * via the
 *
 * {@link cross.cache.ICacheElementProvider}, if their key is not present in the
 * in-memory cache.
 *
 * Please note that Ehcache only allows Serializable objects to be externalized
 * to disk, should the in-memory cache overflow.
 *
 * @author Nils Hoffmann
 * @param <K> the key type
 * @param <V> the value type
 */
@Slf4j
public class AutoRetrievalEhcacheDelegate<K, V> implements ICacheDelegate<K, V> {

    private final ICacheElementProvider<K, V> provider;
    private final Ehcache cache;
    private final Set<K> keys;

    /**
     * Creates a new instance.
     *
     * @param cache the backing cache to use
     * @param provider the provider for values, given a specific key
     */
    public AutoRetrievalEhcacheDelegate(Ehcache cache,
            ICacheElementProvider<K, V> provider) {
        this.provider = provider;
        this.cache = cache;
        cache.setCacheExceptionHandler(new CacheExceptionHandler() {

            @Override
            public void onException(Ehcache ehcache, Object key, Exception exception) {
                if (exception instanceof java.io.NotSerializableException) {
                    log.debug("Exception occured on cache " + ehcache.getName() + ": ", exception);
                } else {
                    throw new RuntimeException("Exception occured on cache " + ehcache.getName() + ": ", exception);
                }
            }
        });
        this.keys = Collections.newSetFromMap(new ConcurrentHashMap<K, Boolean>());
    }

    @Override
    public Set<K> keys() {
        return this.keys;
    }

    @Override
    public void put(final K key, final V value) {
        getCache().put(new Element((Object) key, (Object) value));
        if (value == null) {
            this.keys.remove(key);
        } else {
            this.keys.add(key);
        }
    }

    @Override
    public V get(final K key) {
        Element element = getCache().get((Object) key);
        V v = null;
        if (element != null) {
            v = (V) element.getObjectValue();
            if (v != null) {
                return v;
            }
        }
        v = provider.provide(key);
        put(key, v);
        return v;
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
    public void close() {
        getCache().dispose();
    }

    @Override
    public CacheType getCacheType() {
        return CacheType.EHCACHE;
    }
}
