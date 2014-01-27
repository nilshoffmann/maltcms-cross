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
package cross.datastructures.fragments;

import cross.cache.CacheType;
import cross.cache.ICacheDelegate;
import cross.cache.none.NoCacheManager;
import cross.cache.softReference.SoftReferenceCacheManager;
import cross.datastructures.cache.VariableFragmentArrayCache;
import java.io.File;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.MemoryUnit;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import ucar.ma2.Array;

/**
 * Utility class for access to fragment data caches.
 *
 * @author Nils Hoffmann
 */
@Slf4j
public final class Fragments {

    private static CacheType fragmentCacheType = CacheType.NONE;
    private static File cacheDirectory = new File(System.getProperty("java.io.tmpdir"), "maltcms-fragments-manager");
    private static CacheManager defaultCacheManager = null;

    /**
     * The cache manager for variable fragments is called
     * 'maltcms-fragments-manager', the cache for fragments is called
     * 'maltcms-fragments'.
     *
     * Defaults are at least 128 MBytes of local heap up to a maximum of one quarter of the
     * available maximum heap memory.
     *
     * Disk storage is currently limited to at most 100GBytes.
     *
     * The default configuration for fragments is to overflow to disk, least frequently
     * accessed elements will be evicted from the heap first.
     *
     * @return the singleton cache manager for variable fragment caching
     */
    public static CacheManager getDefault() {
        if (defaultCacheManager == null) {
            cacheDirectory.mkdirs();
            CacheConfiguration cc = new CacheConfiguration();
            cc.name("maltcms-fragments").
                overflowToDisk(true).
                memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU);
            Configuration config = new Configuration();
            config.setDynamicConfig(true);
            config.setMaxBytesLocalHeap(Math.max(MemoryUnit.MEGABYTES.toBytes(128), Runtime.getRuntime().maxMemory() / 4));
            config.setMaxBytesLocalDisk(MemoryUnit.parseSizeInBytes("100G"));
            config.setDefaultCacheConfiguration(cc);
            config.setName("maltcms-fragments-manager");
            defaultCacheManager = CacheManager.create(config);
        }
        return defaultCacheManager;
    }

    /**
     * Set the cache location for all NEWLY created caches.
     *
     * @param f the cache directory
     */
    public static void setCacheDirectory(File f) {
        Fragments.cacheDirectory = f;
    }

    /**
     * Set the default cache type for new fragment caches.
     *
     * @param fragmentCacheType
     * @see CacheType
     */
    public static void setDefaultFragmentCacheType(CacheType fragmentCacheType) {
        Fragments.fragmentCacheType = fragmentCacheType;
    }

    /**
     * Create a new cache delegate, possibly returning an existing one for the
     * same
     * <code>cacheName</code>.
     *
     * @param cacheDir  the cache directory to use
     * @param cacheName the cache name
     * @return the cache delegate
     */
    public static ICacheDelegate<IVariableFragment, List<Array>> createFragmentCache(File cacheDir, String cacheName) {
        return createFragmentCache(cacheDir, cacheName, fragmentCacheType);
    }

    /**
     * Create a new cache delegate, possibly returning an existing one for the
     * same
     * <code>cacheName</code>, using the default cache directory.
     *
     * @param cacheName the cache name
     * @return the cache delegate
     */
    public static ICacheDelegate<IVariableFragment, List<Array>> createFragmentCache(String cacheName) {
        return createFragmentCache(cacheDirectory, cacheName);
    }

    /**
     * Create a new cache delegate, possibly returning an existing one for the
     * same
     * <code>cacheName</code>, using the provided cache directory and cache
     * type.
     *
     * @param cacheDir  the cache directory
     * @param cacheName the cache name
     * @param cacheType the cache type
     * @return the cache delegate
     */
    public static ICacheDelegate<IVariableFragment, List<Array>> createFragmentCache(File cacheDir, String cacheName, CacheType cacheType) {
        switch (cacheType) {
            case EHCACHE:
                log.debug("Using ehcache {}", cacheName);
                return createDefaultFragmentCache(cacheDir, cacheName);
            case SOFT:
                log.debug("Using soft reference cache {}", cacheName);
                return SoftReferenceCacheManager.getInstance().getCache(cacheName);
            case NONE:
                log.debug("Using no cache {}", cacheName);
                return NoCacheManager.getInstance().getCache(cacheName);
            default:
                log.debug("Using no cache {}", cacheName);
                return NoCacheManager.getInstance().getCache(cacheName);
        }
    }

    /**
     * Create a new cache delegate, possibly returning an existing one for the
     * same
     * <code>cacheName</code>, using the provided cache type.
     *
     * @param cacheName the cache name
     * @param cacheType the cache type
     * @return the cache delegate
     */
    public static ICacheDelegate<IVariableFragment, List<Array>> createFragmentCache(String cacheName, CacheType cacheType) {
        return createFragmentCache(cacheDirectory, cacheName, cacheType);
    }

    /**
     * Create a new, default cache delegate, backed by ehcache, possibly
     * returning an existing one for the same
     * <code>cacheName</code>, using the provided cache type. This cache is
     * configured to overflow to disk, if the local capacity is exceeded
     * (minimum = 128 MBytes, maximum = Runtime.getRuntime().maxMemory()/4).
     *
     * @param cacheDir  the cache directory
     * @param cacheName the cache name
     * @return the cache delegate
     */
    public static ICacheDelegate<IVariableFragment, List<Array>> createDefaultFragmentCache(File cacheDir, String cacheName) {
        CacheManager cm = Fragments.getDefault();
        if (cm.cacheExists(cacheName)) {
            return new VariableFragmentArrayCache(cm.getCache(cacheName));
        }
        Ehcache cache = cm.addCacheIfAbsent(cacheName);
        ICacheDelegate<IVariableFragment, List<Array>> ed = new VariableFragmentArrayCache(cache);
        return ed;
    }
}
