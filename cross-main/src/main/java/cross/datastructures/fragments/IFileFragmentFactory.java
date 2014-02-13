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

import cross.IConfigurable;
import java.io.File;
import java.net.URI;
import java.util.Collection;

/**
 * Interface for factories creating IFileFragments.
 *
 * @author Nils Hoffmann
 */
public interface IFileFragmentFactory extends IConfigurable {

    /**
     * Create a new file fragment from the given file.
     *
     * @param f the file
     * @return the new file fragment
     */
    IFileFragment create(final File f);

    /**
     * Create a new file fragment from the given file and source files.
     *
     * @param f           the file
     * @param sourceFiles the source files
     * @return the new file fragment
     */
    IFileFragment create(final File f, final IFileFragment... sourceFiles);

    /**
     * Create a new file fragment from the given directory name and file name.
     *
     * @param dirname  the directory name
     * @param filename the file name
     * @return the new file fragment
     */
    IFileFragment create(final String dirname, final String filename);

    /**
     * Create a new file fragment from the given directory name, file name and source files.
     *
     * @param dirname     the directory name
     * @param filename    the file name
     * @param sourceFiles the source files
     * @return the new file fragment
     */
    IFileFragment create(final String dirname, final String filename, final IFileFragment... sourceFiles);

    /**
     * Create a new file fragment from the given directory, file name and source files.
     *
     * @param dir         the directory
     * @param filename    the file name
     * @param sourceFiles the source files
     * @return the new file fragment
     */
    IFileFragment create(final File dir, final String filename, final IFileFragment... sourceFiles);

    /**
     * Create a new file fragment from the given directory name, file name and source files.
     *
     * @param dirname     the directory name
     * @param filename    the file name
     * @param sourceFiles the source files
     * @return the new file fragment
     */
    IFileFragment create(final String dirname, final String filename, final Collection<IFileFragment> sourceFiles) throws IllegalArgumentException;

    /**
     * Creates a new file fragment with default name. Both original file fragments
     * are added as source files to the new file fragment. The name of the created file fragment
     * will be derived from the two given file fragments.
     *
     * @param f1        the first source file fragment
     * @param f2        the second source file fragment
     * @param outputdir the outputdir where to store the file
     * @return the new file fragment
     */
    IFileFragment create(final IFileFragment f1, final IFileFragment f2, final File outputdir);

    /**
     * Create a new file fragment from the given file path.
     *
     * @param s the file path
     * @return the new file fragment
     */
    IFileFragment create(final String s);

    /**
     * Create a new file fragment from the given uniform resource identifier.
     *
     * @param uri the uniform resource identifier
     * @return the new file fragment
     * @since 1.3.1
     */
    IFileFragment create(final URI uri);

    /**
     * Create a FileFragment and possibly associated VariableFragments by
     * passing the structure encoded in the <code>dataInfo</code> string.
     *
     * @param dataInfo the one-line file fragment structure
     * @return the new file fragment
     *
     * @see cross.io.misc.FragmentStringParser
     */
    IFileFragment fromString(final String dataInfo);
}
