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
package cross.cache;

import cross.cache.ehcache.AutoRetrievalEhcacheDelegate;
import cross.cache.ehcache.EhcacheDelegate;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.DiskStoreConfiguration;
import net.sf.ehcache.config.MemoryUnit;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import net.sf.ehcache.config.SizeOfPolicyConfiguration;
import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

/**
 * Static utility class for creation and retrieval of various pre-configured
 * caches.
 *
 * @author Nils Hoffmann
 */
@Slf4j
public class CacheFactory {

    private static File cacheDirectory = new File(System.getProperty("java.io.tmpdir"), "maltcms-default");

    /**
     * Set the cache location for all NEWLY created caches.
     *
     * @param cacheDirectory the cache directory
     */
    public static void setCacheDirectory(File cacheDirectory) {
        CacheFactory.cacheDirectory = cacheDirectory;
    }

    /**
     * Remove the cache from the ehcache cache manager.
     *
     * @param cacheName the name of the cache to remove
     */
    public static void removeCache(String cacheName) {
        try {
            CacheManager.getInstance().removeCache(cacheName);
        } catch (IllegalStateException ise) {
            log.warn("Failed to remove cache " + cacheName, ise.getLocalizedMessage());
        }
    }

    private static CacheManager defaultCacheManager = null;

    /**
     * Returns the default cache manager implementation for ehcache. Creates
     * it, if it has not yet been created.
     *
     * @return the cache manager instance
     */
    public static CacheManager getDefault() {
        if (defaultCacheManager == null) {
            cacheDirectory.mkdirs();
            Configuration cacheManagerConfig = new Configuration()
                .diskStore(new DiskStoreConfiguration()
                    .path(cacheDirectory.getAbsolutePath()));
            cacheManagerConfig.sizeOfPolicy(new SizeOfPolicyConfiguration().maxDepth(10000).maxDepthExceededBehavior(SizeOfPolicyConfiguration.MaxDepthExceededBehavior.ABORT));
//			cacheManagerConfig.setMaxBytesLocalHeap(Math.max(MemoryUnit.MEGABYTES.toBytes(128), Runtime.getRuntime().maxMemory() / 4));
//			cacheManagerConfig.setMaxBytesLocalDisk(Long.MAX_VALUE);
            defaultCacheManager = CacheManager.newInstance(cacheManagerConfig);
            defaultCacheManager.setName("maltcms-default");
        }
        return defaultCacheManager;
    }

    /**
     * Creates a disk-backed cache below the default cacheDirectory.
     *
     * @param <K>                the key type to use, can be non-serializable
     * @param <V>                the value type to use, can be non-serializable
     * @param cacheName          the name of the cache
     * @param megaBytesLocalHeap the amount of memory to allocate on the local heap
     * @param megaBytesLocalDisk the amount of memory to allocate on the local disk
     * @return a cache delegate representing the created cache
     */
    public static <K, V> ICacheDelegate<K, V> createDefaultCache(String cacheName, int megaBytesLocalHeap, int megaBytesLocalDisk) {
        CacheManager cacheManager = getDefault();
        if (cacheManager.cacheExists(cacheName)) {
            return new EhcacheDelegate<>(cacheManager.getCache(cacheName));
        }
        CacheConfiguration cacheConfig = new CacheConfiguration()
            .name(cacheName)
            .maxBytesLocalHeap(megaBytesLocalHeap, MemoryUnit.MEGABYTES)
            .maxBytesLocalDisk(megaBytesLocalDisk, MemoryUnit.GIGABYTES)
            .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU)
            .persistence(new PersistenceConfiguration().strategy(Strategy.LOCALTEMPSWAP))
            .transactionalMode(CacheConfiguration.TransactionalMode.OFF)
            .diskSpoolBufferSizeMB(256);
        Cache cache = new Cache(cacheConfig);
        cacheManager.addCache(cache);
        EhcacheDelegate<K, V> ed = new EhcacheDelegate<>(cache);
        return ed;
    }

    /**
     * Creates a disk-backed cache below a custom cacheDir. Keeps a maximum of Integer.MAX_VALUE
     * entries on disk.
     *
     * @param <K>                 the key type to use, can be non-serializable
     * @param <V>                 the value type to use, can be non-serializable
     * @param cacheDir            custom base directory for on-disk cache storage
     * @param cacheName           the name of the cache
     * @param maxElementsInMemory the maximum number of elements to keep in memory
     * @return a cache delegate representing the created cache
     */
    public static <K, V> ICacheDelegate<K, V> createDefaultCache(File cacheDir, String cacheName, int maxElementsInMemory) {
        CacheManager cacheManager = getDefault();
        if (cacheManager.cacheExists(cacheName)) {
            return new EhcacheDelegate<>(cacheManager.getCache(cacheName));
        }
        CacheConfiguration cacheConfig = new CacheConfiguration()
            .name(cacheName)
            .maxEntriesLocalHeap(maxElementsInMemory)
            .maxEntriesLocalDisk(Integer.MAX_VALUE)
            .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU)
            .persistence(new PersistenceConfiguration().strategy(Strategy.LOCALTEMPSWAP))
            .transactionalMode(CacheConfiguration.TransactionalMode.OFF)
            .diskSpoolBufferSizeMB(256);
        Cache cache = new Cache(cacheConfig);
        cacheManager.addCache(cache);
        EhcacheDelegate<K, V> ed = new EhcacheDelegate<>(cache);
        return ed;
    }

    /**
     * Creates a disk-backed cache with a default capacity of 1000 elements in
     * memory, disk overflow is stored in a file below the provided cacheDir.
     *
     * @param <K>       the key type to use, can be non-serializable
     * @param <V>       the value type to use, can be non-serializable
     * @param cacheDir  custom base directory for on-disk cache storage
     * @param cacheName the name of the cache
     * @return a cache delegate representing the created cache
     */
    public static <K, V> ICacheDelegate<K, V> createDefaultCache(File cacheDir, String cacheName) {
        return createDefaultCache(cacheDir, cacheName, 100);
    }

    /**
     * Creates a disk-backed cache with a default capacity of 1000 elements in
     * memory, disk overflow is stored in a file below the default cacheDirectory.
     *
     * @param <K>       the key type to use, can be non-serializable
     * @param <V>       the value type to use, can be non-serializable
     * @param cacheName the name of the cache
     * @return a cache delegate representing the created cache
     */
    public static <K, V> ICacheDelegate<K, V> createDefaultCache(String cacheName) {
        return createDefaultCache(cacheDirectory, cacheName);
    }

    /**
     * Creates a disk-backed cache, disk overflow is stored in a file below the default cacheDirectory.
     *
     * @param <K>                 the key type to use, can be non-serializable
     * @param <V>                 the value type to use, can be non-serializable
     * @param cacheName           the name of the cache
     * @param maxElementsInMemory the maximum number of elements to keep in memory
     * @return a cache delegate representing the created cache
     */
    public static <K, V> ICacheDelegate<K, V> createDefaultCache(String cacheName, int maxElementsInMemory) {
        return createDefaultCache(cacheDirectory, cacheName, maxElementsInMemory);
    }

    /**
     * Creates a volatile, non persistent cache with a default capacity of 1000 elements in
     * memory. Default time to idle is 30 seconds, default time to live is 60 seconds.
     *
     * @param <K>       the key type to use, can be non-serializable
     * @param <V>       the value type to use, can be non-serializable
     * @param cacheName the name of the cache
     * @return a cache delegate representing the created cache
     */
    public static <K, V> ICacheDelegate<K, V> createVolatileCache(String cacheName) {
        return createVolatileCache(cacheName, 30, 60, 1000, new CacheEventListener[0]);
    }

    /**
     * Creates a volatile, non persistent cache with a default capacity of 1000 elements in
     * memory.
     *
     * @param <K>        the key type to use, can be non-serializable
     * @param <V>        the value type to use, can be non-serializable
     * @param cacheName  the name of the cache
     * @param timeToIdle time until an entry is marked as idle
     * @param timeToLive time until an entry is removed from the cache
     * @return a cache delegate representing the created cache
     */
    public static <K, V> ICacheDelegate<K, V> createVolatileCache(String cacheName, long timeToIdle, long timeToLive) {
        return createVolatileCache(cacheName, timeToIdle, timeToLive, 1000, new CacheEventListener[0]);
    }

    /**
     * Creates a volatile, non persistent cache.
     *
     * @param <K>                 the key type to use, can be non-serializable
     * @param <V>                 the value type to use, can be non-serializable
     * @param cacheName           the name of the cache
     * @param timeToIdle          time until an entry is marked as idle
     * @param timeToLive          time until an entry is removed from the cache
     * @param maxElementsInMemory the maximum number of elements to keep in memory
     * @return a cache delegate representing the created cache
     */
    public static <K, V> ICacheDelegate<K, V> createVolatileCache(String cacheName, long timeToIdle, long timeToLive, int maxElementsInMemory) {
        return createVolatileCache(cacheName, timeToIdle, timeToLive, maxElementsInMemory, new CacheEventListener[0]);
    }

    /**
     * Creates a volatile, non persistent cache.
     *
     * @param <K>                 the key type to use, can be non-serializable
     * @param <V>                 the value type to use, can be non-serializable
     * @param cacheName           the name of the cache
     * @param timeToIdle          time until an entry is marked as idle
     * @param timeToLive          time until an entry is removed from the cache
     * @param maxElementsInMemory the maximum number of elements to keep in memory
     * @param cacheEventListener  a variable number of listeners
     * @return a cache delegate representing the created cache
     */
    public static <K, V> ICacheDelegate<K, V> createVolatileCache(String cacheName, long timeToIdle, long timeToLive, int maxElementsInMemory, CacheEventListener... cacheEventListener) {
        CacheManager cacheManager = getDefault();
        if (cacheManager.cacheExists(cacheName)) {
            return new EhcacheDelegate<>(cacheManager.getCache(cacheName));
        }
        CacheConfiguration cacheConfig = new CacheConfiguration()
            .name(cacheName)
            .maxEntriesLocalHeap(maxElementsInMemory)
            .eternal(false)
            .timeToIdleSeconds(timeToIdle)
            .timeToLiveSeconds(timeToLive)
            .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU)
            .persistence(new PersistenceConfiguration().strategy(Strategy.NONE))
            .transactionalMode(CacheConfiguration.TransactionalMode.OFF);
        Cache cache = new Cache(cacheConfig);
        cacheManager.addCache(cache);
        EhcacheDelegate<K, V> ed = new EhcacheDelegate<>(cache);
        for (CacheEventListener listener : cacheEventListener) {
            cache.getCacheEventNotificationService().registerListener(listener);
        }
        return ed;
    }

    /**
     * Creates a volatile, non persistent auto retrieval cache (self populating), with a default capacity of 1000 elements in
     * memory. Default time to idle is 30 seconds, default time to live is 60 seconds.
     *
     * @param <K>       the key type to use, can be non-serializable
     * @param <V>       the value type to use, can be non-serializable
     * @param cacheName the name of the cache
     * @param provider  the provider mapping keys to elements
     * @return a cache delegate representing the created cache
     */
    public static <K, V> ICacheDelegate<K, V> createAutoRetrievalCache(String cacheName, ICacheElementProvider<K, V> provider) {
        return createVolatileAutoRetrievalCache(cacheName, provider, 30, 60);
    }

    /**
     * Creates a volatile, non persistent auto retrieval cache (self populating), with a default capacity of 1000 elements in
     * memory.
     *
     * @param <K>        the key type to use, can be non-serializable
     * @param <V>        the value type to use, can be non-serializable
     * @param cacheName  the name of the cache
     * @param provider   the provider mapping keys to elements
     * @param timeToIdle time until an entry is marked as idle
     * @param timeToLive time until an entry is removed from the cache
     * @return a cache delegate representing the created cache
     */
    public static <K, V> ICacheDelegate<K, V> createVolatileAutoRetrievalCache(String cacheName, ICacheElementProvider<K, V> provider, long timeToIdle, long timeToLive) {
        CacheManager cacheManager = getDefault();
        if (cacheManager.cacheExists(cacheName)) {
            return new EhcacheDelegate<>(cacheManager.getCache(cacheName));
        }
        CacheConfiguration cacheConfig = new CacheConfiguration()
            .name(cacheName)
            .maxEntriesLocalHeap(1000)
            .eternal(false)
            .timeToIdleSeconds(timeToIdle)
            .timeToLiveSeconds(timeToLive)
            .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU)
            .persistence(new PersistenceConfiguration().strategy(Strategy.NONE))
            .transactionalMode(CacheConfiguration.TransactionalMode.OFF);
        Cache cache = new Cache(cacheConfig);
        cacheManager.addCache(cache);
        AutoRetrievalEhcacheDelegate<K, V> ared = new AutoRetrievalEhcacheDelegate<>(cache, provider);
        return ared;
    }

    /**
     * Returns the ehcache instance associated to the cacheName, or creates it
     * if it does not exist.
     *
     * @param cacheName the cache name
     * @return the ehcache instance
     */
    public static Ehcache getCacheFor(String cacheName) {
        return CacheManager.getInstance().getEhcache(cacheName);
    }
}
