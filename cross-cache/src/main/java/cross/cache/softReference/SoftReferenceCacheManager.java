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
package cross.cache.softReference;

import cross.cache.ICacheDelegate;
import cross.cache.ICacheElementProvider;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache manager for soft-reference-based caching.
 *
 * @author Nils Hoffmann
 */
public class SoftReferenceCacheManager {

    private static Map<String, SoftReferenceCache> caches = new ConcurrentHashMap<>();
    private static SoftReferenceCacheManager instance;

    private SoftReferenceCacheManager() {
        super();
    }

    /**
     * Returns the <code>SoftReferenceCacheManager</code> instance. Creates a new one if none
     * has yet been created.
     *
     * @return the singleton instance
     */
    public static SoftReferenceCacheManager getInstance() {
        if (SoftReferenceCacheManager.instance == null) {
            SoftReferenceCacheManager.instance = new SoftReferenceCacheManager();
        }
        return SoftReferenceCacheManager.instance;
    }

    /**
     * Returns the cache delegate with the given name or creates a new one.
     *
     * @param <K>  the key type
     * @param <V>  the value type
     * @param name the name of the cache
     * @return the cache delegate
     */
    public <K, V> ICacheDelegate<K, V> getCache(String name) {
        SoftReferenceCache<K, V> delegate = caches.get(name);
        if (delegate == null) {
            delegate = new SoftReferenceCache<>(name);
            caches.put(name, delegate);
        }
        return delegate;
    }

    /**
     * Returns the cache delegate with the given name or creates a new one,
     * backed by the provided <code>ICacheElementProvider</code>.
     *
     * @param <K>      the key type
     * @param <V>      the value type
     * @param name     the name of the cache
     * @param provider the element provider
     * @return the cache delegate
     */
    public <K, V> ICacheDelegate<K, V> getAutoRetrievalCache(String name, ICacheElementProvider<K, V> provider) {
        SoftReferenceCache<K, V> delegate = caches.get(name);
        if (delegate == null) {
            delegate = new AutoRetrievalSoftReferenceCache<>(name, provider);
            caches.put(name, delegate);
        }
        return delegate;
    }

    /**
     * Removes the cache delegate with the given name.
     *
     * @param <K>      the key type
     * @param <V>      the value type
     * @param delegate the cache delegate
     */
    public <K, V> void remove(ICacheDelegate<K, V> delegate) {
        SoftReferenceCache<K, V> cache = caches.get(delegate.getName());
        if (cache != null) {
            cache.close();
            caches.remove(delegate.getName());
        }
    }
}
