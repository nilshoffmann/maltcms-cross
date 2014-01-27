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
import cross.datastructures.StatsMap;
import cross.exception.ResourceNotAvailableException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.jdom.Element;
import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;

/**
 * Immutable Variant of a FileFragment. All set operations will throw
 * UnsupportedOperationException. All other operations delegate to an instance
 * of IFileFragment, which is provided to the constructor.
 *
 * @author Nils Hoffmann
 *
 * @see IFileFragment
 * @see FileFragment
 *
 */
public final class ImmutableFileFragment implements IFileFragment {

    private IFileFragment frag = null;

    /**
     * Create a new instance from the given URI.
     *
     * @param uri the uri
     */
    public ImmutableFileFragment(final URI uri) {
        this.frag = new FileFragment(uri);
    }

    /**
     * Create a new instance from the given file.
     *
     * @param f the file
     */
    public ImmutableFileFragment(final File f) {
        this.frag = new FileFragment(f);
    }

    /**
     * Create a new instance from the given basedir and name.
     *
     * @param basedir the base directory
     * @param name    the file name
     */
    public ImmutableFileFragment(final File basedir, final String name) {
        this(new File(basedir, name));
    }

    /**
     * Create a new instance by delegating to an existing file fragment.
     *
     * @param f the file fragment
     */
    public ImmutableFileFragment(final IFileFragment f) {
        this.frag = f;
    }

    @Override
    public IVariableFragment addChild(String name) {
        return this.frag.addChild(name);
    }

    @Override
    public void addChildren(final IVariableFragment... fragments) {
        this.frag.addChildren(fragments);
    }

    @Override
    public void addDimensions(final Dimension... dims1) {
        this.frag.addDimensions(dims1);
    }

    @Override
    public void addSourceFile(final Collection<IFileFragment> c) {
        this.frag.addSourceFile(c);
    }

    @Override
    public void addSourceFile(final IFileFragment... ff) {
        this.frag.addSourceFile(ff);
    }

    @Override
    public void appendXML(final Element e) {
        this.frag.appendXML(e);
    }

    @Override
    public void clearArrays() throws IllegalStateException {
        this.frag.clearArrays();
    }

    @Override
    public int compare(final IFragment arg0, final IFragment arg1) {
        return this.frag.compare(arg0, arg1);
    }

    @Override
    public int compareTo(final Object arg0) {
        return this.frag.compareTo(arg0);
    }

    @Override
    public boolean equals(final Object obj) {
        return this.frag.equals(obj);
    }

    @Override
    public String getAbsolutePath() {
        return this.frag.getAbsolutePath();
    }

    @Override
    public Attribute getAttribute(final Attribute a) {
        return this.frag.getAttribute(a);
    }

    @Override
    public Attribute getAttribute(final String name) {
        return this.frag.getAttribute(name);
    }

    @Override
    public List<Attribute> getAttributes() {
        return this.frag.getAttributes();
    }

    @Override
    public ICacheDelegate<IVariableFragment, List<Array>> getCache() {
        return this.frag.getCache();
    }

    @Override
    public IVariableFragment getChild(final String varname)
        throws ResourceNotAvailableException {
        return this.frag.getChild(varname, false);
    }

    @Override
    public IVariableFragment getChild(final String varname,
        final boolean loadStructureOnly)
        throws ResourceNotAvailableException {
        return this.frag.getChild(varname, loadStructureOnly);
    }

    @Override
    public List<IVariableFragment> getImmediateChildren() {
        return this.frag.getImmediateChildren();
    }

    @Override
    public long getID() {
        return this.frag.getID();
    }

    @Override
    public String getName() {
        return this.frag.getName();
    }

    @Override
    public IGroupFragment getParent() {
        return this.frag.getParent();
    }

    @Override
    public int getSize() {
        return this.frag.getSize();
    }

    @Override
    public Collection<IFileFragment> getSourceFiles() {
        final Collection<IFileFragment> c = this.frag.getSourceFiles();
        final ArrayList<IFileFragment> cret = new ArrayList<IFileFragment>();
        for (final IFileFragment ifrg : c) {
            cret.add(new ImmutableFileFragment(ifrg));
        }
        return cret;
    }

    @Override
    public StatsMap getStats() {
        return this.frag.getStats();
    }

    @Override
    public boolean hasAttribute(final Attribute a) {
        return this.frag.hasAttribute(a);
    }

    @Override
    public boolean hasAttribute(final String name) {
        return this.frag.hasAttribute(name);
    }

    @Override
    public boolean hasChild(final IVariableFragment vf) {
        return this.frag.hasChild(vf);
    }

    @Override
    public boolean hasChild(final String varname) {
        return this.frag.hasChild(varname);
    }

    @Override
    public boolean hasChildren(final IVariableFragment... vf) {
        return this.frag.hasChildren(vf);
    }

    @Override
    public boolean hasChildren(final String... s) {
        return this.frag.hasChildren(s);
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public Iterator<IVariableFragment> iterator() {
        final ArrayList<IVariableFragment> al = new ArrayList<IVariableFragment>();
        final Iterator<IVariableFragment> iter = this.frag.iterator();
        while (iter.hasNext()) {
            al.add(new ImmutableVariableFragment(iter.next()));
        }
        return al.iterator();
    }

    @Override
    public long nextGID() {
        return this.frag.nextGID();
    }

    @Override
    public void readStructure() {
        this.frag.readStructure();
    }

    @Override
    public synchronized void readExternal(final ObjectInput in) throws IOException,
        ClassNotFoundException {
        Object o = in.readObject();
        if (o instanceof Long) {
            setID(((Long) o).longValue());
        }
        o = in.readObject();
        if (o instanceof String) {
            this.frag.setFile((String) o);
        }
    }

    @Override
    public void removeChild(final IVariableFragment variableFragment) {
        this.frag.removeChild(variableFragment);
    }

    @Override
    public void removeSourceFile(final IFileFragment ff) {
        this.frag.removeSourceFile(ff);
    }

    @Override
    public void removeSourceFiles() {
        this.frag.removeSourceFiles();
    }

    /**
     * Throws an {@link UnsupportedOperationException} on attempts to save.
     *
     * @return undefined
     * @throws UnsupportedOperationException
     */
    @Override
    public boolean save() {
        throw new UnsupportedOperationException("Can not save immutable fragment!");
    }

    @Override
    public void setAttributes(final Attribute... a) {
        this.frag.setAttributes(a);
    }

    @Override
    public void addAttribute(Attribute a) {
        this.frag.addAttribute(a);
    }

    @Override
    public void setCache(ICacheDelegate<IVariableFragment, List<Array>> persistentCache) {
        this.frag.setCache(persistentCache);
    }

    @Override
    public void setFile(final File f1) {
        this.frag.setFile(f1);
    }

    @Override
    public void setFile(final String file) {
        this.frag.setFile(file);
    }

    @Override
    public void setID(final long id) {
        this.frag.setID(id);
    }

    @Override
    public void setStats(final StatsMap stats1) {
        this.frag.setStats(stats1);
    }

    @Override
    public String toString() {
        return this.frag.toString();
    }

    @Override
    public synchronized void writeExternal(final ObjectOutput out) throws IOException {
        // store id
        out.writeObject(Long.valueOf(getID()));
        // store path to storage
        out.writeObject(getUri().toString());
        out.flush();
    }

    @Override
    public Set<Dimension> getDimensions() {
        return this.frag.getDimensions();
    }

    @Override
    public void clearDimensions() {
        this.frag.clearDimensions();
    }

    @Override
    public URI getUri() {
        return this.frag.getUri();
    }
}
