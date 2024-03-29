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
import cross.IFactory;
import cross.cache.ICacheDelegate;
import cross.datastructures.StatsMap;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tools.FileTools;
import cross.datastructures.tools.FragmentTools;
import cross.exception.ConstraintViolationException;
import cross.exception.ResourceNotAvailableException;
import cross.io.IDataSource;
import cross.tools.StringTools;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.jdom2.Element;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;

/**
 * <p>
 * FileFragment are the root element of a Tree of VariableFragments, where
 * each VariableFragment represents one Variable within the (possibly not yet
 * existing) File F.</p>
 *
 * <p>
 * The preferred way to create a FileFragment is by directly invoking its
 * constructor. Please note: once you call
 * <code>save()</code> on the fragment, its contents will be written to disk and
 * all content of the FileFragment will be flushed and / or reset. Attempts to
 * re-save an existing FileFragment will return
 * <code>false</code>, since an existing file will not be overwritten by default
 * (see below on how to override this behaviour). You can however still use the
 * FileFragment instance to lazily load the data that was just written to disk.
 * </p>
 *
 * <p>
 * FileFragments may be created and registered by calling
 * <i>FragmentTools.create(String filename)</i> or appropriate <i>get(...)</i>
 * methods, which create and return a new FileFragment, if not previously
 * existent, or return the Fragment matching the given filename.</p>
 *
 * <p>
 * Alternatively, they can be created using the methods provided in
 * {@link FileFragmentFactory}</p>
 *
 * <p>
 * Filenames can be local or global, since the IO-mechanism decides, whether
 * a file given by filename already exists in input or output location. Files
 * already existing in output location are not overwritten by default, but this
 * behavior can be changed by setting the configuration option
 * <code>output.overwrite</code> to true. Otherwise, the file will be saved in
 * the default location for temporary files.</p>
 *
 * @author Nils Hoffmann
 *
 * @see IDataSource for details on the data source implementation used when calling <code>save</code> and <code>getChild</code>
 */
@Slf4j
public final class FileFragment implements IFileFragment {

    /**
     *
     */
    public static final String NUMBERFORMAT = "%010d";

    /**
     *
     * @param ff
     * @return
     */
    public static String printFragment(final IFileFragment ff) {
        final StringBuffer sb = new StringBuffer();
        final List<Attribute> attrs = ff.getAttributes();
        sb.append("Contents of File ").append(ff.getUri()).append("\n");
        sb.append("Attributes:\n");
        for (final Attribute a : attrs) {
            sb.append("\t").append(a.toString()).append("\n");
        }
        sb.append("Variables and Groups: \n");
        synchronized (ff) {
            for (final IVariableFragment vf : ff) {
                sb.append(vf.toString()).append("(DataType = ").append(vf.getDataType()).append(")" + ":\n");
                sb.append("\tDimensions: ");
                final StringBuffer dims = new StringBuffer();
                dims.append("(");
                final Dimension[] dimA = vf.getDimensions();
                if (dimA != null) {
                    for (final Dimension d : dimA) {
                        sb.append(d.getName()).append(",");
                        dims.append(d.getLength()).append(" x ");
                    }
                    sb.replace(sb.length() - 1, sb.length(), "");
                    dims.replace(dims.length() - 3, dims.length(), "");
                    dims.append(")");
                }
                sb.append(" ").append(dims).append("\n");
            }
        }
        return sb.toString();
    }
    /**
     *
     */
    static long FID = 0;
    private String rep = "";
    private long fID = 0;
    private long nextGID = 0;
    private long gID = 0;
    private final LinkedHashMap<String, Dimension> dims;
    private final Map<String, IVariableFragment> children;
    private final Map<URI, IFileFragment> sourcefiles;
    private final String fileExtension = ".cdf";
    private final Fragment fragment = new Fragment();
    private final BfsVariableSearcher bvs;
    private ICacheDelegate<IVariableFragment, List<Array>> persistentCache = null;
    private URI u;

    /**
     * Create a FileFragment
     */
    public FileFragment() {
        this.sourcefiles = new LinkedHashMap<>();
        this.fID = FileFragment.FID++;
        this.children = new ConcurrentHashMap<>();
        this.dims = new LinkedHashMap<>();
        this.bvs = new BfsVariableSearcher();
        setFile(new File(getDefaultFilename()).toURI());
    }

    /**
     * Create a FileFragment connected to File f.
     *
     * @param f the file
     */
    public FileFragment(final File f) {
        this();
        setFile(f);
    }

    /**
     * Create a FileFragment connected to URI u.
     *
     * @param u the URI
     */
    public FileFragment(final URI u) {
        this();
        setFile(u);
    }

    /**
     * Create a plain FileFragment at basedir with name. If name is null, uses a
     * default filename.
     *
     * @param basedir the base directory
     * @param name    the file name
     */
    public FileFragment(final File basedir, final String name) {
        this();
        String filename = (name == null ? getDefaultFilename() : name);
        setFile(new File(basedir, filename).toURI());
    }

    /**
     * Sets the array cache of this FileFragment as specified if the current
     * cache has not yet been initialized (is null). Throws an
     * {@link IllegalStateException} otherwise to prevent loss of cached data.
     *
     * @param persistentCache
     * @throws IllegalStateException
     */
    @Override
    public void setCache(ICacheDelegate<IVariableFragment, List<Array>> persistentCache) {
        if (this.persistentCache != null) {
            throw new IllegalStateException("Cache already initialized!");
        }
        this.persistentCache = persistentCache;
    }

    @Override
    public IVariableFragment addChild(String name) {
        if (this.children.containsKey(name)) {
            return this.children.get(name);
        }
        IVariableFragment variableFragment = new VariableFragment(this, name);
//        addChildren(variableFragment);
        this.children.put(name, variableFragment);
        if (variableFragment.getDimensions() != null) {
            addDimensions(variableFragment.getDimensions());
        }
        return variableFragment;
    }

    @Override
    public void addChildren(final IVariableFragment... fragments) {
        for (final IVariableFragment vf : fragments) {
            if (this.children.containsKey(vf.getName())) {
                log.debug("VariableFragment " + vf.getName()
                    + " already known!");
                throw new IllegalArgumentException(
                    "Can not add a child more than once, call getImmediateChild() to obtain a reference!");
            }
            // else {
            // IGroupFragment gf = vf.getGroup();
            log.debug("Adding {} {} as child of {} to {}",
                new Object[]{vf.getClass().getSimpleName(), vf.getName(),
                    vf.getParent().getUri(),
                    getUri()});
            this.children.put(vf.getName(), vf);
            if (vf.getParent().getUri().equals(getUri())) {
                log.debug("Parent FileFragment is this!");
            } else {
                log.debug("Parent FileFragment is {}", vf.getParent().getUri());
            }
            // if(!gf.hasChild(vf)) {
            // gf.addChildren(vf);
            // }
            if (vf.getDimensions() != null) {
                addDimensions(vf.getDimensions());
            }
            // }
            // put(vf.getGroup(), vf);
        }
    }

    @Override
    public void addDimensions(final Dimension... dims1) {
        for (final Dimension d : dims1) {
            if (!this.dims.containsKey(d.getName())) {
                this.dims.put(d.getName(), d);
            } else {
                Dimension known = this.dims.get(d.getName());
                if (known.getLength() != d.getLength()) {
                    log.warn("Replacing dimension {} with {}", known, d);
                    this.dims.remove(d.getName());
                    this.dims.put(d.getName(), d);
                }
            }
        }
    }

    @Override
    public void addSourceFile(final Collection<IFileFragment> c) {
        if (c != null) {
            for (final IFileFragment f1 : c) {
                if (f1.getUri().equals(this.getUri())) {
                    throw new IllegalArgumentException(
                        "Cannot reference self as source file!");
                } else {
                    if (this.sourcefiles.containsKey(f1.getUri())) {
                        log.debug(
                            "Sourcefile {} already set, not overwriting!",
                            f1.getName());
                    } else {
                        log.debug(
                            "Adding sourcefile {} to FileFragment {}", f1.getUri(), this.getUri());
                        this.sourcefiles.put(f1.getUri(), f1);
                    }
                }
            }
            setSourceFiles(this.sourcefiles);
        }
    }

    private void setSourceFiles(final Map<URI, IFileFragment> files) {
        if (files.isEmpty()) {
            log.debug(
                "setSourceFiles called for empty source files list on FileFragment {}",
                this);
            return;
        }
        final ArrayChar.D2 a = FragmentTools.createSourceFilesArray(this, files.values());
        final String sfvar = Factory.getInstance().getConfiguration().getString("var.source_files", "source_files");
        IVariableFragment vf = null;
        if (hasChild(sfvar)) {
            log.debug("Source files exist on {}", this);
            vf = getChild(sfvar);
            vf.setArray(a);
        } else {
            log.debug("Setting new source files on {}",
                this);
            vf = new VariableFragment(this, sfvar);
            vf.setArray(a);
        }
        final Dimension d1 = new Dimension("source_file_number", a.getShape()[0], true);
        final Dimension d2 = new Dimension("source_file_max_chars", a.getShape()[1], true);
        vf.setDimensions(new Dimension[]{d1, d2});
    }

    @Override
    public void addSourceFile(final IFileFragment... ff) {
        final List<IFileFragment> l = Arrays.asList(ff);
        addSourceFile(l);
    }

    @Override
    public void appendXML(final Element e) {
        log.debug("Appending xml for fileFragment " + getName());
        final Element fileFragment = new Element("file");
        this.fragment.appendXML(fileFragment);
        final Element sourceFiles = new Element("sourceFiles");
        for (final IFileFragment frag : getSourceFiles()) {
            final Element sfile = new Element("file");
            sfile.setAttribute("filename", frag.getUri().toString());
            sourceFiles.addContent(sfile);
        }
        final Element dimensions = new Element("dimensions");
        int id = 0;
        for (final Dimension d : this.dims.values()) {
            final int length = d.getLength();
            final String name = d.getName();
            final Element dim = new Element("dimension");
            dim.setAttribute("name", name);
            dim.setAttribute("length", "" + length);
            dim.setAttribute("id", "" + id++);
            dim.setAttribute("shared", "" + d.isShared());
            dim.setAttribute("unlimited", "" + d.isUnlimited());
            dim.setAttribute("variableLength", "" + d.isVariableLength());
            dimensions.addContent(dim);
        }
        fileFragment.addContent(dimensions);
        // fileFragment.setAttribute("resourceLocation", );
        fileFragment.setAttribute("filename", getUri().toString());
        // fileFragment.setAttribute("dirname", getDirname());
        fileFragment.setAttribute("size", "" + getSize());
        // fileFragment.setAttribute("id",""+this.fID);
        e.addContent(fileFragment);
        // for(IGroupFragment gf:this.children.values()) {
        // gf.appendXML(fileFragment);
        // }
        for (final IVariableFragment vf : this.children.values()) {
            vf.appendXML(fileFragment);
        }
    }

    @Override
    public void clearArrays() throws IllegalStateException {
        List<IVariableFragment> toRemove = new LinkedList<>();
        for (final IVariableFragment ivf : this.getImmediateChildren()) {
            if (ivf.isModified()) {
                log.warn(
                    "Can not clear arrays for {} on {}, {} was modified!",
                    new Object[]{ivf.getParent().getUri(), ivf.getName(), ivf.getClass().getSimpleName()});
            } else {
                toRemove.add(ivf);
            }
        }
        String sourceFileVarName = Factory.getInstance().getConfiguration().getString("var.source_files", "source_files");
        for (IVariableFragment v : toRemove) {
            if (!sourceFileVarName.equals(v.getName())) {
                v.clear();
                if (persistentCache != null) {
                    persistentCache.put(v, null);
                }
//                removeChild(v);
                children.remove(v.getName());
            }
        }
    }

    @Override
    public void clearDimensions() {
        this.dims.clear();
    }

    @Override
    public int compare(final IFragment arg0, final IFragment arg1) {
        return this.fragment.compare(arg0, arg1);
    }

    @Override
    public int compareTo(final Object arg0) {
        return this.fragment.compareTo(arg0);
    }

    @Override
    public String getAbsolutePath() {
        if (this.u.getScheme() == null || this.u.getScheme().equals("file")) {
            return new File(this.u).getAbsolutePath();
        } else {
            return this.u.toString();
        }
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
    public ICacheDelegate<IVariableFragment, List<Array>> getCache() {
        if (this.persistentCache == null) {
            this.persistentCache = Fragments.createFragmentCache("FileFragmentCache");
        }
        return this.persistentCache;
    }

    @Override
    public IVariableFragment getChild(final String varname)
        throws ResourceNotAvailableException {
        return getChild(varname, false);
    }

    @Override
    public synchronized IVariableFragment getChild(final String varname,
        final boolean loadStructureOnly)
        throws ResourceNotAvailableException {
        // return child if already in memory
        if (this.children.containsKey(varname)) {
            log.debug("Found {} as direct child of {} in memory.", varname,
                this.getUri());
            return getImmediateChild(varname);
        } else {
            String sourceFileVarName = Factory.getInstance().getConfiguration().getString("var.source_files", "source_files");
            if (!varname.equals(sourceFileVarName)) {
                log.info("Trying to load source files from file: {}", this.getUri());
                // loop over all active source_files
                Collection<IFileFragment> parents = bvs.getClosestParent(this, varname);
                if (!parents.isEmpty()) {
                    log.info("Found matches for {} in {}", varname, parents);
                }
                if (parents.size() == 1) {
                    return parents.iterator().next().getChild(varname, loadStructureOnly);
                } else if (parents.size() > 1) {
                    throw new ConstraintViolationException("Found more than one possible source file for variable " + varname + ": " + parents);
                }
            } else {
                //try to update from file
                addSourceFile(FragmentTools.getSourceFiles(this).values());
                return getImmediateChild(sourceFileVarName);
            }
            // if all fails, throw ResourceNotAvailableException
            throw new ResourceNotAvailableException("Failed to find var "
                + varname + " in fragment " + getUri().toString()
                + " and source files " + this.sourcefiles.values());
        }
    }

    @Override
    public List<IVariableFragment> getImmediateChildren() {
        return Collections.unmodifiableList(new ArrayList<>(this.children.values()));
    }

    @Override
    public long getID() {
        return this.fID;
    }

    private IVariableFragment getImmediateChild(final String varname) {
        return this.children.get(varname);
    }

    @Override
    public String getName() {
        String pathName = u.getPath();
        if (u.getPath().endsWith("/")) {
            pathName = u.getPath().substring(0, u.getPath().length() - 1);
            log.debug("PathName: {}", pathName);
        }
        return pathName.substring(pathName.lastIndexOf("/") + 1);
    }

    @Override
    public IGroupFragment getParent() {
        return null;
    }

    @Override
    public int getSize() {
        // int size = 0;
        // for(String key:this.children.keySet()) {
        // size+=this.children.get(key).getSize();
        // }
        return this.children.size();
    }

    /**
     * Use this method with caution! It will only return the list of source
     * files, if either an array from a parent file has been loaded, or if the
     * sourcefiles have been loaded explicitly. If you want to obtain the list
     * of source files, call:
     * <code>tools.FragmentTools.getSourceFiles(IFileFragment f)</code>.
     *
     * @return this list of source files
     */
    @Override
    public Collection<IFileFragment> getSourceFiles() {
        return this.sourcefiles.values();
    }

    @Override
    public StatsMap getStats() {
        return this.fragment.getStats();
    }

    @Override
    public URI getUri() {
        return u;
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
    public synchronized boolean hasChild(final IVariableFragment vf) {
        return hasChild(vf.getName());
    }

    @Override
    public synchronized boolean hasChild(final String varname) {
        if (this.children.containsKey(varname)) {
            log.debug("Variable {} already contained as child of {}",
                varname, this.getUri());
            return true;
        }
        return false;
    }

    @Override
    public boolean hasChildren(final IVariableFragment... vf) {
        for (final IVariableFragment frag : vf) {
            if (!hasChild(frag)) {
                log.warn("Requested variable {} not contained in {}", frag.getName(), getUri());
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasChildren(final String... s) {
        for (final String name : s) {
            if (!hasChild(name)) {
                log.warn("Requested variable {} not contained in {}",
                    name, getUri());
                return false;
            }
        }
        return true;
    }

    private String getDefaultFilename() {
        final StringBuilder sb = new StringBuilder();
        final Formatter formatter = new Formatter(sb);
        formatter.format(FileFragment.NUMBERFORMAT, (this.fID));
        return sb.toString() + this.fileExtension;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isModified() {
        for (final IVariableFragment ivf : getImmediateChildren()) {
            if (ivf.isModified()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<IVariableFragment> iterator() {
        final ArrayList<IVariableFragment> al = new ArrayList<>(
            this.children.size());
        if (this.children.isEmpty()) {
            try {
                Factory.getInstance().getDataSourceFactory().getDataSourceFor(this).readStructure(this);
            } catch (IOException ex) {
                log.error("IOException while loading structure of " + getUri(), ex);
            }
        }
        // for(IGroupFragment vf:this.children.values()) {
        final Iterator<String> iter = this.children.keySet().iterator(); // Must
        // be in
        // block
        while (iter.hasNext()) {
            final IVariableFragment ivf = this.children.get(iter.next());
            al.add(ivf);
        }
        // }
        // return this.children.values().iterator();
        return al.iterator();
    }

    @Override
    public long nextGID() {
        final long id = this.nextGID++;
        return id;
    }

    @Override
    public void readStructure() throws IllegalStateException {
        if (this.isModified()) {
            throw new IllegalStateException("Can not read structure on modified file fragment. Call clearArrays() to revert changes or call save() to persist!");
        }
        try {
            Factory.getInstance().getDataSourceFactory().getDataSourceFor(this).readStructure(this);
            readSourceFiles();
        } catch (IOException ex) {
            log.warn(ex.getLocalizedMessage());
        }
    }

    /**
     *
     * @param in
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Override
    public synchronized void readExternal(final ObjectInput in) throws IOException,
        ClassNotFoundException {
        Object o = in.readObject();
        if (o instanceof Long) {
            this.fID = (Long) o;
        }
        o = in.readObject();
        if (o instanceof String) {
            setFile((String) o);
        }
//        in.close();
        this.sourcefiles.clear();
        this.children.clear();
        this.dims.clear();
    }

    @Override
    public synchronized void removeChild(
        final IVariableFragment variableFragment) {
        if (this.children.containsKey(variableFragment.getName())) {
            log.debug("Removing child " + variableFragment.getName());
            if (variableFragment.getIndex() != null) {
                throw new ConstraintViolationException("Tried to remove a variable that references an index variable: " + variableFragment.getName() + "; index: " + variableFragment.getIndex().getName());
            }
            List<IVariableFragment> indexReferents = new LinkedList<>();
            for (IVariableFragment other : this.children.values()) {
                if (other.getIndex() != null) {
                    if (other.getIndex().getName().equals(variableFragment.getName())) {
                        indexReferents.add(other);
                    }
                }
            }
            if (indexReferents.isEmpty()) {
                this.children.remove(variableFragment.getName());
            } else {
                throw new ConstraintViolationException("Tried to remove index variable " + variableFragment.getName() + ", required by: " + indexReferents);
            }
        } else {
            log.warn("Could not remove {}, no child of {}",
                variableFragment.getName(), this.getUri());
        }
    }

    @Override
    public void removeSourceFile(final IFileFragment ff) {
        this.sourcefiles.remove(ff.getUri());
    }

    @Override
    public void removeSourceFiles() {
        this.sourcefiles.clear();
        removeSourceFilesVariableFragment();
    }

    private void removeSourceFilesVariableFragment() {
        final String sfvar = Factory.getInstance().getConfiguration().getString("var.source_files", "source_files");
        if (hasChild(sfvar)) {
            final IVariableFragment ivf = getChild(sfvar);
            removeChild(ivf);
            log.debug("Removing {} from {}", sfvar, this);
        } else {
            log.warn(
                "Can not remove {}, no such child in {}!", sfvar, this);
        }
    }

    @Override
    public boolean save() {
        if (this.u == null) {
            log.warn("URI for FileFragment was null, using default!");
            setFile(new File(getDefaultFilename()).toURI());
            log.warn("URI now set to " + getUri());
        }

        // FIXME all output currently redirected to netcdf
        String ext = StringTools.getFileExtension(getName());//.toLowerCase();
        if (ext.equals(getName())) {
            log.info("File location did not have a proper extension, setting default: cdf!");
            ext = "cdf";
        } else {
            ext = ext.toLowerCase();
        }
        final String filename = StringTools.removeFileExt(getName());
        String basepath = u.getPath();
        if (basepath.endsWith("/")) {
            log.debug("Resource is a directory, removing trailing slash!");
            basepath = basepath.substring(0, basepath.length() - 1);
        }
        basepath = basepath.substring(0, basepath.lastIndexOf("/") + 1);
        log.debug("extension: " + ext);
        log.debug("filename: " + filename);
        log.debug("basepath: " + basepath);
        log.debug("uri: " + u.toString());
        //FIXME this should be configured more centrally
        final String[] netcdfExts = new String[]{"nc", "nc.gz", "nc.z", "nc.zip", "nc.gzip", "nc.bz2", "cdf", "cdf.gz", "cdf.z", "cdf.zip", "cdf.gzip", "cdf.bz2"};
        log.debug("Looking for file extension: {} in {}", ext, Arrays.toString(netcdfExts));
        boolean cdfFile = false;
        for (String key : netcdfExts) {
            if (key.equals(ext)) {
                cdfFile = true;
            }
        }
        if (!cdfFile) {
            try {
                log.debug("Did not find extension!");
                URI newLocation = new URI(this.u.getScheme(), this.u.getUserInfo(), this.u.getHost(), this.u.getPort(), basepath + filename + ".cdf", this.u.getQuery(), this.u.getFragment());
                setFile(newLocation);
            } catch (URISyntaxException ex) {
                log.warn("Failed to set new location: ", ex);
                return false;
            }
        } else {
            log.debug("Found extension!");
        }
        //add source file variable
        setSourceFiles(this.sourcefiles);
        if (Factory.getInstance().getDataSourceFactory().getDataSourceFor(this).write(this)) {
            log.debug("Save of {} succeeded, clearing arrays!", getName());
            for (IVariableFragment frag : getImmediateChildren()) {
                frag.setIsModified(false);
            }
            clearArrays();
            getCache().close();
            removeSourceFiles();
            this.persistentCache = null;
//            FileFragment.fileMap.remove(u);
//            FileFragment.fileMap.remove(u.toString());
            return true;
        }
        return false;
    }

    @Override
    public void setAttributes(final Attribute... a) {
        this.fragment.setAttributes(a);
    }

    @Override
    public void addAttribute(Attribute a) {
        this.fragment.addAttribute(a);
    }

    @Override
    public void setFile(final File f1) {
        if (!f1.isAbsolute()) {
            log.warn("File must be absolute! Was: " + f1);
        }
        setFile(f1.toURI());
    }

    /**
     *
     * @param uri
     */
    protected void setFile(final URI uri) {
        setFile(uri.toString());
    }

    /**
     *
     * @param path
     * @return
     */
    protected boolean isFile(final String path) {
        File f = new File(path);
        return f.isFile();
    }

    /**
     *
     * @param path
     * @return
     */
    protected boolean isURI(final String path) {
        URI uri = URI.create(FileTools.escapeUri(path));
        if (uri.getScheme() == null || uri.getScheme().isEmpty()) {
            return false;
        }
        return uri.isAbsolute();
    }

    @Override
    public void setFile(final String file) {
        EvalTools.notNull(file, this);
        log.info("Setting resource location to: {}", file);
        URI u = URI.create(FileTools.escapeUri(file));
        if (u.getScheme() == null) {
            throw new ConstraintViolationException("URI scheme must not be null for " + this.u.toString());
        }
        if (u.getPath().contains("file:")) {
            throw new ConstraintViolationException("Illegal URI: scheme must not occur in path for " + this.u.toString());
        }
        this.u = u;
    }

    @Override
    public void setID(final long id) {
        this.gID = id;
    }

    @Override
    public void setStats(final StatsMap stats1) {
        this.fragment.setStats(stats1);
    }

    /**
     * Returns a string containing all VariableNames and Ranges.
     * @return 
     */
    protected String structureToString() {
        final StringBuilder sb = new StringBuilder();
        if (this.u == null) {
            sb.append(getID());
        } else {
            sb.append(this.u.toString());
        }
        sb.append(">");
        int i = 0;
        // for(IGroupFragment vf:this.children.values()) {
        synchronized (this.children) {
            final Iterator<String> iter = this.children.keySet().iterator(); // Must
            // be in
            // block
            while (iter.hasNext()) {
                final IVariableFragment ivf = this.children.get(iter.next());
                final String v = ivf.toString();
                sb.append(v);

                if (i < this.children.size() - 1) {
                    sb.append("&");
                }
                i++;
            }
        }
        // }
        // Iterator<String> iter = this.children.keySet().iterator();
        // for (int i = 0; i < this.children.size(); i++) {
        // String v = this.children.get(iter.next()).toString();
        // int lim = v.indexOf(">");
        // sb.append(v.substring(lim + 1, v.length()));
        // if (i < this.children.size() - 1) {// only append, if this is
        // // not the last child
        // sb.append("&");
        // }
        // }
        this.rep = sb.toString();
        // }
        return this.rep;
    }

    @Override
    public String toString() {
        return structureToString();
    }

    /**
     *
     * @param out
     * @throws IOException
     */
    @Override
    public synchronized void writeExternal(final ObjectOutput out) throws IOException {
        if (isModified()) {
            // bring memory state into sync with storage representation
            save();
        }
        // store id
        out.writeObject(this.fID);
        // store path to storage
        out.writeObject(this.u.toString());
        out.flush();
//        out.close();
    }

    @Override
    public Set<Dimension> getDimensions() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(this.dims.values()));
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + (this.u != null ? this.u.hashCode() : 0);
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
        final FileFragment other = (FileFragment) obj;
        if (this.u != other.u && (this.u == null || !this.u.equals(other.u))) {
            return false;
        }
        return true;
    }

    private void readSourceFiles() {
        Map<URI, IFileFragment> map = FragmentTools.getSourceFiles(this);
        log.info("Adding sourcefiles {} to file: {}", map.values(), this.getUri());
        for (URI uri : map.keySet()) {
            if (this.sourcefiles.containsKey(uri)) {
                log.debug("Source file already known!");
            } else {
                this.sourcefiles.put(uri, map.get(uri));
            }
        }
    }
}
