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
package cross.datastructures.cache;

import java.io.Serializable;

/**
 * Interface for proxys to support serialization of objects, that were
 * originally not marked as {@link Serializable}.
 *
 * @author Nils Hoffmann
 * @param <T>
 */
public interface ISerializationProxy<T> extends Serializable {

    /**
     * Convert the given object to a Serializable instance.
     *
     * @param t the runtime object
     * @return the Serializable proxy
     */
    public Serializable convert(T t);

    /**
     * Invert the conversion from the Serializable / Object, to the
     * original type.
     *
     * @param o the object to reverse convert
     * @return the object with its original type
     */
    public T reverseConvert(Object o);
}
