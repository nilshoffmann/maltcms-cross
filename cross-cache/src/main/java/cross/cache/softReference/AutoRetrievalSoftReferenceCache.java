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

import cross.cache.ICacheElementProvider;

/**
 * Values are referenced using {
 *
 * @see java.lang.ref.SoftReference}. These may be
 * reclaimed by the garbage collector as soon as the virtual machine requires
 * additional free memory. The keys are strongly referenced.
 *
 * @author Nils Hoffmann
 * @param <K> the key type
 * @param <V> the value type
 */
public class AutoRetrievalSoftReferenceCache<K, V> extends SoftReferenceCache<K, V> {

    private final ICacheElementProvider<K, V> provider;

    /**
     * Creates a new instance
     *
     * @param name            the name of the cache
     * @param elementProvider the element provider
     */
    public AutoRetrievalSoftReferenceCache(String name, ICacheElementProvider<K, V> elementProvider) {
        super(name);
        this.provider = elementProvider;
    }

    /**
     * Returns v from the cache or retrieves it from the cache element provider.
     *
     * @param key the key
     * @return the value
     */
    @Override
    public V get(K key) {
        V v = super.get(key);
        if (v != null) {
            return v;
        }
        v = provider.provide(key);
        put(key, v);
        return v;
    }
}
