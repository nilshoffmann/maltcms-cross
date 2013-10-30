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

/**
 * Interface for objects which are copyable. Objects implementing this interface
 * are expected to create a deep copy of their current state if the <code>copy</code>
 * method is called.
 *
 * @author Nils Hoffmann
 * @param <T> the target type of the copyable
 * @since 1.3.1
 */
public interface ICopyable<T> {

	/**
	 * Create a deep copy of the object implementing ICopyable.
	 * The result may need to be cast to the appropriate type.
	 *
	 * @param <T> the target type of this object
	 * @return the deep copy of this object
	 */
	<T> T copy();

}
