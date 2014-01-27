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

import cross.cache.ICacheDelegate;
import cross.exception.ResourceNotAvailableException;
import java.io.Externalizable;
import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.jdom.Element;
import ucar.ma2.Array;
import ucar.nc2.Dimension;

/**
 * Interface for partially in-memory files. Supports one-level hierarchy, a file
 * fragment can have instances of {@link IVariableFragment} as children. This is
 * a simplified view of the CDM model used in netcdf without support for groups
 * or nested datastructures.
 *
 * @author Nils Hoffmann
 */
public interface IFileFragment extends IGroupFragment, IFragment, Externalizable {

    /**
     * Add a child of given name or return already existing one.
     *
     * @param name the child to add
     * @return variableFragment the newly created variable fragment, child of
     *         this fragment
     */
    public abstract IVariableFragment addChild(String name);

    /**
     * Add a number of children.
     *
     * @param fragments the children to add
     */
    @Override
    public abstract void addChildren(IVariableFragment... fragments);

    /**
     * Add dimensions to this IFileFragment.
     *
     * @param dims1 the dimensions to add
     */
    public abstract void addDimensions(Dimension... dims1);

    /**
     * Add source FileFragments contained in Collection c to this FileFragment.
     *
     * @param c the source file fragments to add
     */
    public abstract void addSourceFile(Collection<IFileFragment> c);

    /**
     * Add source FileFragments contained in ff to this FileFragment.
     *
     * @param ff the source file fragments to add
     */
    public abstract void addSourceFile(IFileFragment... ff);

    @Override
    public abstract void appendXML(Element e);

    /**
     * Iterates through all VariableFragments, clearing in memory arrays, except
     * for source_files. Throws IllegalStateException if VariableData has been
     * altered.
     *
     * @throws java.lang.IllegalStateException
     */
    public abstract void clearArrays() throws IllegalStateException;

    /**
     * Resets all dimensions of this fragment. Does not set Dimensions to null
     * in IVariableFragments having these Dimensions!
     */
    public abstract void clearDimensions();

    /**
     * Return this FileFragment's storage location as string representation.
     *
     * @deprecated please use {@link #getUri} instead
     * @return the absolute path string
     */
    @Deprecated
    public abstract String getAbsolutePath();

    /**
     * Return a Cache for variable fragment array data for this fragment.
     *
     * @return the cache delegate
     */
    public abstract ICacheDelegate<IVariableFragment, List<Array>> getCache();

    /**
     * Returns the child with name varname. If varname is not found in local
     * structure, try to locate it in sourcefiles. First hit wins. Otherwise
     * throws {@link ResourceNotAvailableException}.
     *
     * @param varname
     * @return the variable fragment
     * @throws ResourceNotAvailableException if the variable with the given name
     *                                       could not be found
     */
    @Override
    public abstract IVariableFragment getChild(String varname)
        throws ResourceNotAvailableException;

    /**
     * Returns the child with name varname. If varname is not found in local
     * structure, try to locate it in sourcefiles. First hit wins. Otherwise
     * throw IllegalArgumentException. If
     * <code>loadStructureOnly</code> is true, only the variable structure is
     * retrieved, not the data.
     *
     * @param varname           the name of the variable
     * @param loadStructureOnly whether only the structure (true) or also the
     *                          data (false) should be loaded
     * @return the variable fragment
     * @throws ResourceNotAvailableException if the variable with the given name
     *                                       could not be found
     */
    public abstract IVariableFragment getChild(String varname,
        boolean loadStructureOnly) throws ResourceNotAvailableException;

    /**
     * Returns the immediate children of this fileFragment. Does not return
     * children that are only found in referenced source files.
     *
     * @return the list of immediate children (not persistent before * calling <code>save()</code>)
     */
    public abstract List<IVariableFragment> getImmediateChildren();

    /**
     * The registered dimensions of this FileFragment.
     *
     * @return the dimensions
     */
    public abstract Set<Dimension> getDimensions();

    /**
     * The unique ID (between runs) of this FileFragment.
     *
     * @return the id
     */
    public abstract long getID();

    /**
     * Return the name of this FileFragment, does not include directory or other
     * prefixed information.
     *
     * @return the name
     */
    @Override
    public abstract String getName();

    @Override
    public abstract IGroupFragment getParent();

    /**
     * Return the number of children of this FileFragment.
     *
     * @return the number of children
     */
    @Override
    public abstract int getSize();

    /**
     * Return all source FileFragments.
     *
     * @return the list of source file fragment
     */
    public abstract Collection<IFileFragment> getSourceFiles();

    /**
     * Return the URI of this FileFragment.
     *
     * @return the uri
     */
    public URI getUri();

    /**
     * Query FileFragment for the given VariableFragments.
     *
     * @param vf the variable fragments to query for
     * @return true if all of the given variables are children of this fragment
     */
    public abstract boolean hasChildren(IVariableFragment... vf);

    /**
     * Query FileFragment for children with the given strings as names.
     *
     * @param s the variable fragment names to query for
     * @return true if all of the given variable names are children of this
     *         fragment
     */
    public abstract boolean hasChildren(String... s);

    public boolean isModified();

    /**
     * Creates an iterator over all children of this FileFragment by the time of
     * creation of the iterator.
     */
    @Override
    public abstract Iterator<IVariableFragment> iterator();

    /**
     * Call the {@link IDataSource} for this FileFragment and load the
     * structural information for this fragment. This includes variable names
     * and shapes as well as attributes.
     */
    public abstract void readStructure();

    /**
     * Remove the given IVariableFragment from the list of this FileFragment's
     * children.
     *
     * @param variableFragment the child to remove
     * @throws ConstraintViolationException to indicate attempted removal of a
     *                                      variable that either is an index variable or uses one
     */
    public abstract void removeChild(IVariableFragment variableFragment);

    /**
     * Remove the given source file.
     *
     * @param ff the source file to remove
     */
    public abstract void removeSourceFile(IFileFragment ff);

    /**
     * Removes all currently associated source files.
     */
    public abstract void removeSourceFiles();

    /**
     * Store this fragment using the {@link IDataSource} responsible for
     * handling this fragment name's extension.
     *
     * @return true if saving succeeded, false otherwise
     */
    public abstract boolean save();

    /**
     * Set a Cache for variable fragment array data for this fragment.
     *
     * May throw an {@link IllegalStateException} if the cache was already
     * initialized to avoid accidental modification or replacement.
     *
     * @param cache the cache
     * @throws IllegalStateException
     */
    public abstract void setCache(ICacheDelegate<IVariableFragment, List<Array>> cache);

    /**
     * Change the filename of this Fragment.
     *
     * @param f1 the file
     */
    public abstract void setFile(File f1);

    /**
     * Change the filename of this Fragment.
     *
     * @param file the file
     */
    public abstract void setFile(String file);

    /**
     * Returns a String representation of this Fragment, containing all it's
     * children with indices and ranges. Can directly be used as input string to
     * the command-line-interface.
     *
     * @return the string representation of this fragment
     */
    @Override
    public abstract String toString();
}
