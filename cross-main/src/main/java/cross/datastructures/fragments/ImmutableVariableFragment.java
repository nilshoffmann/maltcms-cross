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

import cross.datastructures.StatsMap;
import cross.datastructures.tools.EvalTools;
import cross.io.misc.ArrayChunkIterator;
import java.util.Collections;
import java.util.List;
import org.jdom.Element;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Range;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;

/**
 * Immutable Variant of a VariableFragment. All set operations will throw an
 * UnsupportedOperationException. All other operations delegate to an instance
 * of IVariableFragment, which is provided to the constructor.
 *
 * @author Nils Hoffmann
 * @see UnsupportedOperationException
 * @see IVariableFragment
 * @see VariableFragment
 */
public final class ImmutableVariableFragment implements IVariableFragment {

	private final IVariableFragment vf;

	/**
	 * Create a new immutable version of the provided variable fragment.
	 * @param vf2 the variable fragment to delegate to
	 */
	public ImmutableVariableFragment(final IVariableFragment vf2) {
		this.vf = vf2;
		EvalTools.notNull(vf2, this);
	}

	@Override
	public void appendXML(final Element e) {
		this.vf.appendXML(e);
	}

	@Override
	public int compare(final IFragment o1, final IFragment o2) {
		return this.vf.compare(o1, o2);
	}

	@Override
	public int compareTo(final Object o) {
		return this.vf.compareTo(o);
	}

	@Override
	public boolean equals(final Object obj) {
		return this.vf.equals(obj);
	}

	@Override
	public Array getArray() {
		return this.vf.getArray().copy();
	}

	@Override
	public Attribute getAttribute(final Attribute a) {
		return this.vf.getAttribute(a);
	}

	@Override
	public Attribute getAttribute(final String name) {
		return this.vf.getAttribute(name);
	}

	@Override
	public List<Attribute> getAttributes() {
		return this.vf.getAttributes();
	}

	@Override
	public ArrayChunkIterator getChunkIterator(final int chunksize) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DataType getDataType() {
		return this.vf.getDataType();
	}

	@Override
	public Dimension[] getDimensions() {
		return this.vf.getDimensions();
	}

	@Override
	public IVariableFragment getIndex() {
		return this.vf.getIndex();
	}

	@Override
	public List<Array> getIndexedArray() {
		return Collections.unmodifiableList(this.vf.getIndexedArray());
	}

	@Override
	public IFileFragment getParent() {
		return this.vf.getParent();
	}

	@Override
	public Range[] getRange() {
		return this.vf.getRange();
	}

	@Override
	public StatsMap getStats() {
		return this.vf.getStats();
	}

	@Override
	public String getVarname() {
		return this.vf.getName();
	}

	@Override
	public boolean hasArray() {
		return this.vf.hasArray();
	}

	@Override
	public boolean hasAttribute(final Attribute a) {
		return this.vf.hasAttribute(a);
	}

	@Override
	public boolean hasAttribute(final String name) {
		return this.vf.hasAttribute(name);
	}

	@Override
	public int hashCode() {
		return this.vf.hashCode();
	}

	@Override
	public boolean isModified() {
		return false;
	}

	@Override
	public void setArray(final Array a1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addAttribute(Attribute a) {
		this.vf.addAttribute(a);
	}

	@Override
	public void setAttributes(final Attribute... a) {
		this.vf.setAttributes(a);
	}

	@Override
	public void setDataType(final DataType dataType1) {
		this.vf.setDataType(dataType1);
	}

	@Override
	public void setDimensions(final Dimension[] dims1) {
		this.vf.setDimensions(dims1);
	}

	@Override
	public void setIndex(final IVariableFragment index1) {
		this.vf.setIndex(index1);
	}

	@Override
	public void setIndexedArray(final List<Array> al1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setIsModified(final boolean b) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setRange(final Range[] ranges1) {
		this.vf.setRange(ranges1);
	}

	@Override
	public void setStats(final StatsMap stats1) {
		this.vf.setStats(stats1);
	}

	@Override
	public String toString() {
		return this.vf.toString();
	}

	@Override
	public String getName() {
		return this.vf.getName();
	}

	@Override
	public void clear() {
		this.vf.clear();
	}
}
