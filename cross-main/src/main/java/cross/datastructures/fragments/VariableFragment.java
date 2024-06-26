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

import cross.Factory;
import cross.datastructures.StatsMap;
import cross.datastructures.tools.ArrayTools;
import cross.datastructures.tools.EvalTools;
import cross.io.misc.ArrayChunkIterator;
import cross.io.misc.Base64;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jdom2.Element;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.IndexIterator;
import ucar.ma2.Range;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;

/**
 * A class representing Variables. A Variable is a meta-info container for
 * existing data stored in an array for example. VariableFragment objects belong
 * to a parent FileFragment, which corresponds to a virtual file structure.
 *
 * @author Nils Hoffmann
 *
 * @see IFileFragment
 * @see FileFragment
 * @see IVariableFragment
 * @see ImmutableVariableFragment
 * @see ImmutableVariableFragment2
 */
@Slf4j
public final class VariableFragment implements IVariableFragment {

    private final Fragment fragment = new Fragment();
    private final String varname;
    private Dimension[] dims;
    private DataType dataType;
    private Range[] ranges;
    private String rep = null;
    private IVariableFragment index = null;
    private IFileFragment parent = null;
    private boolean isModified = false;
    private boolean useCachedList = false;

    private VariableFragment(final IFileFragment ff,
        final IGroupFragment group, final String varname1,
        final Dimension[] dims1, final DataType dt, final Range[] r) {
        EvalTools.notNull(varname1, "String varname was null", this);
        this.varname = varname1;
        this.ranges = r;
        this.parent = ff;
        // this.gf = (group==null)?new
        // NamedGroupFragment(this.parent,null):group;
        // if ((dims == null) && (r != null) && r[0]!=null) {
        // dims = new Dimension[1];
        // dims[0] = new Dimension("default", r[0].length(), true, false,
        // false);
        // }
        if ((dims1 != null) && (r != null)) {
            int i = 0;
            for (Dimension d : dims1) {
                if (d == null) {
                    if ((i < r.length) && (r[i] != null)) {
                        d = new Dimension("default" + i, r[i].length(), true,
                            false, false);
                    }
                } else {
                    if ((i < r.length) && (r[i] != null)) {
                        d.setLength(r[i].length());
                    }
                }
                i++;
            }
        }
        this.dims = dims1;
        this.dataType = dt == null ? DataType.DOUBLE : dt;
        toString();
        this.parent.addChildren(this);
    }

    /**
     * Create a new variable fragment with the given name and parent.
     *
     * @param parent2  the parent file fragment
     * @param varname2 the variable name
     */
    public VariableFragment(final IFileFragment parent2, final String varname2) {
        this(parent2, varname2, null, null, null);
    }

    private IGroupFragment gf = null;
    // private boolean protect = false;
    /**
     * Creates a VariableFragment compatible in type, name and dimensions to vf.
     * Does not copy Range or array data!
     *
     * @param ff the parent file fragment for the new variable fragment
     * @param vf the variable fragment to copy the structure from
     * @return 
     */
    public static IVariableFragment createCompatible(IFileFragment ff,
        IVariableFragment vf) {
        VariableFragment nf = new VariableFragment(ff, vf.getName());
        vf.getParent().getChild(vf.getName(), true);
        Dimension[] d = vf.getDimensions();
        if (d != null) {
            Dimension[] nd = new Dimension[d.length];
            int i = 0;
            for (Dimension dim : d) {
                nd[i++] = new Dimension(dim.getName(), dim);
            }
            nf.setDimensions(nd);
        }
        nf.setDataType(vf.getDataType());
        return nf;
    }

    private VariableFragment(final IFileFragment ff, final String varname1,
        final Dimension[] dims1, final DataType dt, final Range[] ranges1) {
        this(ff, null, varname1, dims1, dt, ranges1);
    }

    /**
     * Create a new variable fragment instance with the given name, parent and
     * index variable fragment.
     *
     * @param parent2  the parent
     * @param varname2 the variable name
     * @param ifrg     the index variable fragment
     */
    public VariableFragment(final IFileFragment parent2, final String varname2,
        final IVariableFragment ifrg) {
        this(parent2, varname2, null, null, null);
        setIndex(ifrg);
    }

    /**
     *
     * @return
     */
    protected Array getArrayRef() {
        List<Array> l = parent.getCache().get(this);
        if (l != null) {
            if (l.size() == 1) {
                return l.get(0);
            } else if (l.size() > 1) {
                log.warn("Glueing array list of arrays! This is very inefficient!");
                return ArrayTools.glue(l);
            }
        }
        return null;
    }

    @Override
    public void appendXML(final Element e) {
        log.debug("Appending xml for variable " + getName());
        final String vname = "variable";
        final Element var = new Element(vname);
        this.fragment.appendXML(var);
        var.setAttribute("name", getName());
        var.setAttribute("dataType", getDataType().getPrimitiveClassType().getName());

        final Dimension[] dims1 = getDimensions();
        if ((dims1 != null) && (dims1.length > 0)) {
            final Element dimensions = new Element("dimensions");
            getParent().addDimensions(dims1);
            for (final Dimension d : dims1) {
                final String name = d.getName();
                final Element dim = new Element("dimension");
                dim.setAttribute("refname", name);
                dimensions.addContent(dim);
            }
            var.addContent(dimensions);
        }
        if (getRange() != null) {
            final Element ranges1 = new Element("ranges");
            for (final Range r : getRange()) {
                if (r != null) {
                    final Element range = new Element("range");
                    if (r.getName() != null) {
                        range.setAttribute("name", r.getName());
                    }
                    range.setAttribute("first", "" + r.first());
                    range.setAttribute("stride", "" + r.stride());
                    range.setAttribute("last", "" + r.last());
                    ranges1.addContent(range);
                }
            }
            var.addContent(ranges1);
        }
        if (this.index != null) {
            var.setAttribute("indexVariable", this.index.getName());
        }
        e.addContent(var);

        if (getArrayRef() != null) {
            final Element data = new Element("data");
            final StringBuilder sb = new StringBuilder(
                getArrayRef().getShape()[0]);
            final IndexIterator ii = getArrayRef().getIndexIterator();
            while (ii.hasNext()) {
                sb.append(ii.getObjectNext() + " ");
            }
            final String b64 = Base64.encodeObject(sb.toString(), Base64.GZIP);
            data.setText(b64);
            e.addContent(data);
        }
    }

    @Override
    public int compare(final IFragment o1, final IFragment o2) {
        return o1.toString().compareTo(o2.toString());
    }

    @Override
    public int compareTo(final Object o) {
        if (o instanceof VariableFragment) {
            final String lhs = getParent().getName() + ">" + getName();
            final String rhs = ((IVariableFragment) o).getParent().getName()
                + ">" + ((IVariableFragment) o).getName();
            // return
            // this.toString().compareTo(((VariableFragment)o).toString());
            return lhs.compareTo(rhs);
        }
        return -1;
    }

    @Override
    public Array getArray() {
        return getArrayRef();
    }

    @Override
    public Attribute getAttribute(final Attribute a) {
        return this.fragment.getAttribute(a);
    }

    @Override
    public Attribute getAttribute(final String name) {
        return this.fragment.getAttribute(name);
    }

    @Override
    public List<Attribute> getAttributes() {
        return this.fragment.getAttributes();
    }

    @Override
    public ArrayChunkIterator getChunkIterator(final int chunksize) {
        return new ArrayChunkIterator(Factory.getInstance(), this, chunksize);
    }

    @Override
    public DataType getDataType() {
        return this.dataType;
    }

    @Override
    public Dimension[] getDimensions() {
        return this.dims;
    }

    @Override
    public IVariableFragment getIndex() {
        return this.index;
    }

    @Override
    public List<Array> getIndexedArray() {
//        if (getIndex() == null) {
//            return Collections.emptyList();
//        }
        List<Array> l = parent.getCache().get(this);
        if (l == null) {
            return Collections.emptyList();
        }
        return l;
    }

    @Override
    public IFileFragment getParent() {
        return this.parent;
    }

    @Override
    public Range[] getRange() {
        return this.ranges;
    }

    @Override
    public StatsMap getStats() {
        return this.fragment.getStats();
    }

    @Override
    public String getVarname() {
        return this.varname;
    }

    @Override
    public boolean hasArray() {
        List<Array> l = parent.getCache().get(this);
        if (l == null || l.isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean hasAttribute(final Attribute a) {
        return this.fragment.hasAttribute(a);
    }

    @Override
    public boolean hasAttribute(final String name) {
        return this.fragment.hasAttribute(name);
    }

    @Override
    public boolean isModified() {
        return this.isModified;
    }

    /**
     * Whether this variable fragment uses a cached list for lazy array
     * retrieval (only applies, if index variable is not null).
     *
     * @return true if this variable fragment uses a cached list, false
     *         otherwise
     */
    public boolean isUseCachedList() {
        return this.useCachedList;
    }

    @Override
    public void setArray(final Array a1) {
        // EvalTools.notNull(a1, this);
        // if (log.isDebugEnabled()) {
        log.debug("Set array on VariableFragment {} as child of {}",
            toString(), getParent().getUri());
        if (a1 == null) {
            //parent.getCache().put(this, null);
            clear();
        } else {
            this.isModified = true;
            ArrayList<Array> list = new ArrayList<>(1);
            list.add(a1);
            parent.getCache().put(this, list);
            setDataType(DataType.getType(a1));
            if (getDimensions() == null) {
                setDimensions(cross.datastructures.tools.ArrayTools.getDefaultDimensions(a1));
            }
        }
    }

    @Override
    public void addAttribute(Attribute a) {
        this.fragment.addAttribute(a);
    }

    @Override
    public void setAttributes(final Attribute... a) {
        this.isModified = true;
        this.fragment.setAttributes(a);
    }

    @Override
    public void setDataType(final DataType dataType1) {
        EvalTools.notNull(dataType1, this);
        this.dataType = dataType1;
    }

    @Override
    public void setDimensions(final Dimension[] dims1) {
        EvalTools.notNull(dims1, this);
        this.dims = dims1;
        getParent().addDimensions(dims1);
    }

    @Override
    public void setIndex(final IVariableFragment index1) {
        if ((index1 != null) && (this.index != null)) {
            log.debug("Setting index from {} to {}", this.index, index1);
        }
        this.index = index1;
        // this.al = null;
    }

    @Override
    public void setIndexedArray(final List<Array> al1) throws IllegalStateException {
        if (al1 != null && this.index == null) {
            throw new IllegalStateException("Please call setIndex first before adding indexed data!");
        }
        log.debug(
            "Set indexed array on VariableFragment {} as child of {}",
            toString(), getParent().getUri());
//        synchronized (this) {
        setIndexedArrayInternal(al1);
//        }
    }

    /**
     *
     * @param al1
     */
    protected void setIndexedArrayInternal(final List<Array> al1) {
        if (al1 != null && !al1.isEmpty()) {
            this.isModified = true;
            parent.getCache().put(this, al1);
            setDataType(DataType.getType(al1.get(0)));
        } else {
            clear();
        }
    }

    @Override
    public void clear() {
        this.isModified = false;
        parent.getCache().put(this, null);
    }

    @Override
    public void setIsModified(final boolean b) {
        this.isModified = b;
    }

    @Override
    public void setRange(final Range[] ranges1) {
        this.ranges = ranges1;
    }

    @Override
    public void setStats(final StatsMap stats1) {
        this.fragment.setStats(stats1);
    }

    /**
     * Whether to use cached list for array retrieval (true) or load array data
     * eagerly (false).
     *
     * @param b whether to use the cached list or not
     */
    public void setUseCachedList(final boolean b) {
        this.useCachedList = b;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        // if (this.rep == null) {
        final StringBuilder sb = new StringBuilder();
        // sb.append(getParent().getAbsolutePath() + ">");
        sb.append(getName());
        if (getRange() != null) {
            for (final Range r : getRange()) {
                if (r != null) {
                    sb.append("[" + r.first() + ":" + r.last() + ":"
                        + r.stride() + "]");
                }
            }
        }
        sb.append(getIndex() == null ? "" : "#" + getIndex().toString());
        // String range = "";
        // sb.append(range);
        this.rep = sb.toString();
        // }
        return this.rep;
    }

    @Override
    public String getName() {
        return this.varname;
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 5;
//        hash = 83 * hash + (this.fragment != null ? this.fragment.hashCode() : 0);
        hash = 83 * hash + (this.varname != null ? this.varname.hashCode() : 0);
        hash = 83 * hash + (this.parent != null ? this.parent.hashCode() : 0);
        return hash;
    }

    /**
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final VariableFragment other = (VariableFragment) obj;
//        if (this.fragment != other.fragment && (this.fragment == null || !this.fragment.equals(other.fragment))) {
//            return false;
//        }
        if ((this.varname == null) ? (other.varname != null) : !this.varname.equals(other.varname)) {
            return false;
        }
        if (this.parent != other.parent && (this.parent == null || !this.parent.equals(other.parent))) {
            return false;
        }
        return true;
    }
}
