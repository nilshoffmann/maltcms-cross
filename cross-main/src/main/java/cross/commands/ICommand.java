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
package cross.commands;

import cross.IConfigurable;

/**
 * Interface for objects which represent a command acting on an object of type
 * <code>IN</code> and returning objects of type
 * <code>OUT</code>.
 *
 * @author Nils Hoffmann
 *
 * @param <IN>  input type.
 * @param <OUT> output type.
 */
public interface ICommand<IN, OUT> extends IConfigurable {

    /**
     * Apply a command implementation to an Object of type
     * <code>IN</code>, returning an object of type
     * <code>OUT</code>.
     *
     * @param in
     * @return
     */
    public OUT apply(IN in);
}
