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
package cross.datastructures.tools;

import cross.Factory;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.tools.StringTools;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class to ease handling of files and directories.
 *
 * @author Nils Hoffmann
 *
 */
@Slf4j
public class FileTools {

    /**
     *
     */
    public static final SimpleDateFormat sdf = new SimpleDateFormat(
        "MM-dd-yyyy_HH-mm-ss", Locale.US);

    /**
     * Append the creator name to the basedir, with the given prefix, and
     * return as directory.
     *
     * @param base    the base directory
     * @param prefix  the prefix
     * @param creator the creator
     * @return the creator directory
     */
    private static File appendCreatorNameToBaseDir(final File base,
        String prefix, final Class<?> creator) {
        File creatordir = base;
        if (creator != null) {
            final String creatorName = creator.getSimpleName();
            if (!creatorName.equals("")) {
                creatordir = new File(base, prefix + creatorName);
            }
        }
        if (!creatordir.exists()) {
            creatordir.mkdirs();
        }
        return creatordir;
    }

    /**
     * Check whether the given file fragment location is readable.
     * Returns the file if the file fragment is readable, throws an {@link IOException}
     * otherwise.
     *
     * @param ff the file fragment to check
     * @return the file representing the file fragment on disk
     * @throws IOException if the file could not be found
     */
    protected static File checkFileReadable(final IFileFragment ff)
        throws IOException {
        log.debug("Trying to locate {}", ff.getName());
        final File outF = new File(ff.getUri()).getCanonicalFile();
        //final File outF = new File(ff.getUri()).getCanonicalFile();
        if (outF.exists()) {
            log.debug("Found {} in directory {}", ff.getName(), outF.getParent());
            // knownFiles.put(outF.getAbsolutePath(), outF);
            return outF;
        } else {
            throw new IOException("File does not exist: "
                + outF.getAbsolutePath());
        }
    }

    /**
     * Create and return a file for the given file fragment.
     *
     * @param f the file fragment
     * @return the file representing the file fragment on disk
     * @throws IOException if the file could not be found
     */
    protected static File createFile(final IFileFragment f) throws IOException {
        File file = null;
        log.debug("File extension: {}", StringTools.getFileExtension(f.getName()));
        IFileFragment ff = f;
        try {
            file = FileTools.findFile(f);
            log.info("File exists, checking, whether we should overwrite or create new file in temporary location!");
            if (Factory.getInstance().getConfiguration().getBoolean(
                "output.overwrite", false)) {
                log.info("Option output.overwrite=true in default.properties is set, overwriting existing file!");
                file.delete();
                file = new File(file.getAbsolutePath());
            } else {
                log.info(
                    "File {} already exists, creating file in temporary location!",
                    f.getUri());
                final String tmpdir = System.getProperty("java.io.tmpdir");
                final File tmp = new File(tmpdir);
                file = new File(tmp, file.getName());
                log.debug("Setting {} as source file of {}", f.getUri(), file.getAbsolutePath());
                ff = new FileFragment(file);
                ff.addSourceFile(f);
            }

            // if(file!=null) {
            // ff.setFile(file.getAbsolutePath());
            // f.setFile(file.getAbsolutePath());
        } catch (final IOException ioex) {
            log.debug(ioex.getLocalizedMessage());
            // create the file and it's parent directories atomically
            log.debug("File does not exist, creating atomically!");
            file = new File(f.getUri());
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        // }
        return file;
    }

    /**
     * Find a file for the given file fragment.
     *
     * @param f the file fragment
     * @return the file representing the file fragment on disk
     * @throws IOException if the file could not be found
     */
    protected static File findFile(final IFileFragment f) throws IOException {
        try {
            final File outF = FileTools.checkFileReadable(f);
            return outF;
        } catch (final IOException ioex) {
            throw ioex;
        }
    }

    /**
     * Find a file for the parent file fragment of the given variable fragment.
     *
     * @param vf the variable fragment
     * @return the file representing the variable fragment's parent file fragment on disk
     * @throws IOException if the file could not be found
     */
    protected static File findFile(final IVariableFragment vf)
        throws IOException {
        return FileTools.findFile(vf.getParent());
    }

    /**
     * Return the default output directory below the given baseDirectory.
     *
     * If <code>Factory.getInstance().getConfiguration().getBoolean("omitUserTimePrefix", false)</code> returns true,
     * the baseDirectory will be returned, or, if baseDirectory is null, the factory configuration property 'output.basedir'.
     *
     * @param baseDirectory the base directory for output, if null, <code>Factory.getInstance().getConfiguration().getString("output.basedir", "")</code> will be used
     * @param d             the startup date, if null, only 'user.name' will be appended
     * @return the output directory below baseDirectory, with 'user.name' and startup date appended.
     */
    public static File getDefaultDirs(final File baseDirectory, final Date d) {
        File outputBasedir = new File(Factory.getInstance().getConfiguration().getString("output.basedir", ""));
        if (baseDirectory != null) {
            outputBasedir = baseDirectory;
        }
        final boolean omitUserTimePrefix = Factory.getInstance().getConfiguration().getBoolean("omitUserTimePrefix", false);
        if (omitUserTimePrefix) {
            return outputBasedir;
        } else if (d == null) {
            final File usernamebasedir = new File(outputBasedir,
                Factory.getInstance().getConfiguration().getString(
                    "user.name", "default"));
            return usernamebasedir;
        } else {
            final File basedir = new File(outputBasedir,
                Factory.getInstance().getConfiguration().getString(
                    "user.name", "default"));
            final File datedir = new File(basedir, FileTools.sdf.format(d));
            return datedir;
        }
    }

    /**
     * Get default directory with the given startup date stamp.
     *
     * @param d the startup date
     * @return the output directory below 'output.basedir' or, if empty, below 'user.dir', with 'user.name' and startup date appended.
     */
    public static File getDefaultDirs(final Date d) {
        return getDefaultDirs(null, d);
    }

    /**
     * Returns the directory name of the given file path
     *
     * @param fullname the file path
     * @return the parent directory of fullname
     */
    public static String getDirname(final String fullname) {
        final File f = new File(fullname);
        return f.getParent();
    }

    /**
     * Find a file for the given file fragment.
     *
     * @param ff the file fragment
     * @return the file representing the file fragment on disk
     * @throws IOException if the file could not be found
     */
    public static File getFile(final IFileFragment ff) throws IOException {
        return FileTools.findFile(ff);
    }

    /**
     * Return the filename from the provided URI.
     *
     * @param u the uri
     * @return the filename (last path component)
     */
    public static String getFilename(final URI u) {
        String pathName = u.getPath();
        if (u.getPath().endsWith("/")) {
            pathName = u.getPath().substring(0, u.getPath().length() - 1);
            log.debug("PathName: {}", pathName);
        }
        return pathName.substring(pathName.lastIndexOf("/") + 1);
//
//        if(u.getPath().endsWith("/")) {
//            return "";
//        }
//        return u.getPath().substring(u.getPath().lastIndexOf("/") + 1);
    }

    /**
     * Returns the file name of the given file path
     *
     * @param fullname the file path
     * @return the file name of fullname
     */
    public static String getFilename(final String fullname) {
        final File f = new File(fullname);
        return f.getName();
    }

    /**
     * Returns a unique non-existing filename for an existing file.
     *
     * @param file the file to get the next free file name for
     * @return the input file, if it does not exist, or the next free file with an increasing suffix number starting from 1
     */
    private static File getNextFreeFileName(final File file) {
        File f = file;
        int i = 1;
        if (Factory.getInstance().getConfiguration().getBoolean(
            "output.overwrite", false)) {
            f.delete();
        }
        if (!f.exists()) {
            return f;
        }
        while (f.exists()) {
            final String ext = StringTools.getFileExtension(f.getAbsolutePath());
            if (ext.equals(f.getName())) {
                log.warn("File has no proper file extension: {}", f);
                final String base = StringTools.removeFileExt(f.getAbsolutePath());
                f = new File(base + "" + i);
            } else {
                final String base = StringTools.removeFileExt(f.getAbsolutePath());
                f = new File(base + "" + i + "." + ext);
            }
            i++;
        }
        return f;
    }

    private static File getNextFreeFileName(final String filename) {
        return FileTools.getNextFreeFileName(new File(filename));
    }

    /**
     * Prepare the output for the provided file fragment, creates parent directories.
     *
     * @param parent the file fragment
     * @return the output file with created parent directories
     * @throws IOException
     */
    public static File prepareOutput(final IFileFragment parent)
        throws IOException {
        // log.debug("Saving file to directory: "
        // + FileTools.getDirname(parent.getAbsolutePath()));
        final File f = FileTools.createFile(parent);
        log.debug("Writing to file " + f.getAbsolutePath() + "\n");
        return f;
    }

    /**
     * Prepare the output for the provided directory and filename. Assumes a default
     * format suffix of 'csv'.
     *
     * @param dir      the output directory path
     * @param filename the filename
     * @return the output file with created parent directories
     */
    public static File prepareOutput(final String dir, final String filename) {
        return prepareOutput(dir, filename, "csv");
    }

    /**
     * Prepare the output for the provided directory, filename, and filetypeSuffix.
     *
     * @param dir            the output directory path
     * @param filename       the filename
     * @param filetypeSuffix the file type suffix
     * @return the output file with created parent directories
     */
    public static File prepareOutput(final String dir, final String filename,
        final String filetypeSuffix) {
        final String basedir = ((dir == null) || dir.isEmpty()) ? Factory.getInstance().getConfiguration().getString("output.basedir")
            : dir;
        final File d = new File(basedir);
        if (!d.exists()) {
            d.mkdirs();
        }

        File f = new File(d, filename + "." + filetypeSuffix);
        f = getNextFreeFileName(f);
        return f;
    }

    /**
     * Prepare the output for the provided directory and filename. Assumes a default
     * format suffix of 'csv'.
     *
     * @param dir      the output directory
     * @param filename the filename
     * @return the output file with created parent directories
     */
    public static File prepareOutput(final File dir, final String filename) {
        return prepareOutput(dir.getAbsolutePath(), filename);
    }

    /**
     * Prepend the given prefix to the directory created by {@link FileTools#getDefaultDirs(java.io.File, java.util.Date) }.
     *
     * @param baseDir the output base directory
     * @param prefix  the prefix to prepend
     * @param creator the creator
     * @param d       the startup date
     * @return the output file with created parent directories and prefix prepended
     */
    public static File prependDefaultDirsWithPrefix(File baseDir, String prefix, final Class<?> creator, final Date d) {
        return FileTools.appendCreatorNameToBaseDir(
            FileTools.getDefaultDirs(baseDir, d), prefix, creator);
    }

    /**
     * Prepend the given prefix to the default output directory created by {@link FileTools#getDefaultDirs(java.util.Date) }.
     *
     * @param prefix  the prefix to prepend
     * @param creator the creator
     * @param d       the startup date
     * @return the output file with created parent directories and prefix prepended
     */
    public static File prependDefaultDirsWithPrefix(String prefix,
        final Class<?> creator, final Date d) {
        return FileTools.appendCreatorNameToBaseDir(
            FileTools.getDefaultDirs(d), prefix, creator);
    }

    /**
     * Resolve the given relative URI against the given base URI.
     *
     * @param base
     * @param relativeURI
     * @return the resolved, abolute URI
     */
    public static URI resolveRelativeUri(URI base, URI relativeURI) {
        return base.resolve(relativeURI);
    }

    /**
     * Returns a relative URI from the given source URI to the given target URI
     *
     * @param from the source URI
     * @param to   the target URI
     * @return the relative URI
     */
    public static URI getRelativeUri(URI from, URI to) {
        // Normalize paths to remove . and .. segments
        from = from.normalize();
        to = to.normalize();

        // Split paths into segments
        String[] bParts = from.getPath().split("\\/");
        String[] cParts = to.getPath().split("\\/");

        // Discard trailing segment of from path
        if (bParts.length > 0 && !from.getPath().endsWith("/")) {
            bParts = Arrays.copyOf(bParts, bParts.length - 1);
        }

        // Remove common prefix segments
        int i = 0;
        while (i < bParts.length && i < cParts.length && bParts[i].equals(cParts[i])) {
            i++;
        }

        // Construct the relative path
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < (bParts.length - i); j++) {
            sb.append("../");
        }
        for (int j = i; j < cParts.length; j++) {
            if (j != i) {
                sb.append("/");
            }
            sb.append(cParts[j]);
        }

        return URI.create(escapeUri(sb.toString()));
    }

    /**
     * Returns a resolved canonical path, where <code>relativeFile</code> is resolved against
     * <code>base</code>.
     *
     * @param base         the source file
     * @param relativeFile the file relative to base
     * @return the resolved, canonical path
     * @throws IOException
     */
    public static String resolveRelativeFile(File base, File relativeFile) throws IOException {
        return new File(base, relativeFile.getPath()).getCanonicalPath();
    }

    /**
     * Returns a relative file from base to target.
     *
     * @param target the target fragment to get the relative path to
     * @param base   the base fragment against which the relative path is calculated
     * @return the relative file from base to target
     * @throws IOException
     */
    public static File getRelativeFile(IFileFragment target, IFileFragment base) throws IOException {
        return new File(getRelativeUri(target.getUri(), base.getUri()));
    }

    /**
     * Returns the path of one File relative to another.
     *
     * @param target the target directory
     * @param base   the base directory
     * @return target's path relative to the base directory
     * @throws IOException if an error occurs while resolving the files'
     *                     canonical names
     */
    public static File getRelativeFile(File target, File base) throws IOException {
        return new File(getRelativeUri(base.toURI(), target.toURI()));
    }

    /**
     * Escape spaces with '%20' in the provided string path.
     *
     * @param path the path to escape spaces in
     * @return the escaped path
     */
    public static String escapeUri(String path) {
        return path.replaceAll(" ", "%20");
    }
}
