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
package cross.io.misc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Provides static utility methods for base64 encoding/decoding, checksum
 * calculation, gzipping and unzipping.
 *
 * @author Nils Hoffmann
 *
 */
public class BinaryFileBase64Wrapper {

    /**
     * Decode base64 encoded file in to file out.
     *
     * @param in  the input file
     * @param out the output file
     */
    public static void base64Decode(final File in, final File out) {
        Base64.decodeFileToFile(in.getAbsolutePath(), out.getAbsolutePath());
        // printCompressionRatio(in, out);
    }

    /**
     * Encode file in to file out using base64 encoding.
     *
     * @param in  the input file
     * @param out the output file
     */
    public static void base64Encode(final File in, final File out) {
        Base64.encodeFileToFile(in.getAbsolutePath(), out.getAbsolutePath());
        // printCompressionRatio(in, out);
    }

    /**
     * Calculates a checksum on File.
     *
     * @param a the input file
     * @param c the checksum method object , if null use CRC32 as default checksum algorithm
     * @return the checksum
     * @throws IOException
     */
    public static long calcChecksum(final File a, final Checksum c)
        throws IOException {
        final FileInputStream fis = new FileInputStream(a);
        final CheckedInputStream cis = new CheckedInputStream(fis,
            (c == null) ? new CRC32() : c);
        final BufferedInputStream bis = new BufferedInputStream(cis);
        while (bis.read() != -1) {
        }
        return cis.getChecksum().getValue();
    }

    /**
     * Applies gunzip on in file and writes decompressed result to file out.
     *
     * @param in  the input file
     * @param out the output file
     * @throws FileNotFoundException if input or output file file does not exist
     * @throws IOException
     */
    public static void gunzip(final File in, final File out)
        throws FileNotFoundException, IOException {
        System.out.println("Unzipping to " + out);
        try (GZIPInputStream gzi = new GZIPInputStream(new FileInputStream(in))) {
            final BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(out));
            int b;
            final byte[] data = new byte[1024];
            while ((b = gzi.read(data, 0, data.length)) != -1) {
                bos.write(data, 0, b);
            }
            bos.flush();
            bos.close();
        }
    }

    /**
     * Compress the given input file to the given output file.
     *
     * @param in  the input file
     * @param out the output file
     * @throws FileNotFoundException if input or output file file does not exist
     * @throws IOException
     */
    public static void gzip(final File in, final File out)
        throws FileNotFoundException, IOException {
        try (GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(
                out))) {
            final BufferedInputStream bis = new BufferedInputStream(
                    new FileInputStream(in));
            int b;
            final byte[] data = new byte[1024];
            while ((b = bis.read(data, 0, data.length)) != -1) {
                gos.write(data, 0, b);
            }
            bis.close();
            gos.flush();
        }
    }

    /**
     *
     * @param args
     */
    public static void main(final String[] args) {
        // if (args[0].equals("-d")) {
        // File input = new File(args[1]);
        // String prefix = removeB64FileExtension(input.getName());
        // File output = new File(input.getParentFile(), prefix);
        // base64Decode(input, output);
        // File unzipped = new File(input.getParentFile(),
        // removeGZFileExtension(output.getName()));
        // try {
        // gunzip(output, unzipped);
        // } catch (IOException e) {
        // System.err.println(e.getLocalizedMessage());
        // }
        // } else if (args[0].equals("-e")) {
        // File input = new File(args[1]);
        // try {
        // File gzipf = new File(input.getParentFile(), input.getName()
        // + ".gz");
        // gzip(input,gzipf);
        // File output = new File(gzipf.getParentFile(), gzipf.getName()
        // + ".b64");
        // base64Encode(gzipf, output);
        // } catch (FileNotFoundException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        //
        // } else {
        // System.err.println("Unsupported input parameter " + args[0]);
        // }
        for (final String file : args) {
            File b64enc, input;
            try {
                input = new File(file);
                final File tmpdirin = new File("/tmp/in");
                final File tmpdirout = new File("/tmp/out");
                if (!tmpdirin.exists()) {
                    tmpdirin.mkdirs();
                }
                if (!tmpdirout.exists()) {
                    tmpdirout.mkdirs();
                }
                final File gzipped = new File(tmpdirin, input.getName() + ".gz");
                BinaryFileBase64Wrapper.gzip(input, gzipped);
                b64enc = new File(tmpdirin, gzipped.getName() + ".b64");
                BinaryFileBase64Wrapper.base64Encode(gzipped, b64enc);

                final File b64dec = new File(tmpdirout, input.getName() + ".gz");
                BinaryFileBase64Wrapper.base64Decode(b64enc, b64dec);
                final File gunzipped = new File(tmpdirout, input.getName());
                BinaryFileBase64Wrapper.gunzip(b64dec, gunzipped);
                if (BinaryFileBase64Wrapper.verifyChecksum(input, gunzipped)) {
                    System.out
                        .println("Conversion successful for file " + file);
                } else {
                    System.out.println("Verification of checksums for " + file
                        + " failed!");
                }
            } catch (final IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    /**
     * Print the compression ration between uncompressed and compressed file.
     *
     * @param unc  the uncompressed file
     * @param comp the compressed file
     */
    public static void printCompressionRatio(final File unc, final File comp) {
        double unclen = unc.length();
        double complen = comp.length();
        unclen /= (Math.pow(2.0d, 20.0d));
        complen /= (Math.pow(2.0d, 20.0d));
        System.out.printf("%s : %1.2f %s\n", "Size of input file  " + unc,
            unclen, " MBytes");
        System.out.printf("%s : %1.2f %s\n", "Size of output file  " + comp,
            complen, " MBytes");
        final float compressionRatio = (float) complen / (float) unclen;
        System.out.printf("%s = %1.2f\n", "Compression ratio (output/input)",
            compressionRatio);
    }

    /**
     * Remove file extension ".b64" if existent.
     *
     * @param filename the filename
     * @return the filename with ".b64" removed
     */
    public static String removeB64FileExtension(final String filename) {
        String s = filename;
        if (s.endsWith(".b64")) {
            s = s.replace(".b64", "");
        }
        System.out.println("Without Base64 extension: " + s);
        return s;
    }

    /**
     * Remove file extension ".gz" if existent.
     *
     * @param filename the filename
     * @return the filename with ".gz" removed
     */
    public static String removeGZFileExtension(final String filename) {
        String s = filename;
        if (s.endsWith(".gz")) {
            s = s.replace(".gz", "");
        }
        System.out.println("Without GZ extension " + s);
        return s;
    }

    /**
     * Calculates checksums for both files and compares, whether they match.
     *
     * @param a the first file
     * @param b the second file
     * @return true if checksums match, false otherwise
     */
    public static boolean verifyChecksum(final File a, final File b) {
        try {
            final long csa = BinaryFileBase64Wrapper.calcChecksum(a, null);
            final long csb = BinaryFileBase64Wrapper.calcChecksum(b, null);
            if (csa == csb) {
                System.out.println("Checksums match!");
                return true;
            }
            System.err.println("Checksum mismatch!");
            return false;
        } catch (final IOException e) {
            return false;
        }

    }

    /**
     * Write bytes from byte array to file using a buffer.
     *
     * @param bytes the byte array to write
     * @param out   the output file to write to
     * @throws FileNotFoundException if input or output file file does not exist
     * @throws IOException
     */
    public static void writeBytes(final byte[] bytes, final File out)
        throws FileNotFoundException, IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(out))) {
            final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            int b;
            final byte[] data = new byte[1024];
            while ((b = bais.read(data, 0, data.length)) != -1) {
                bos.write(data, 0, b);
            }
            bais.close();
            bos.flush();
        }
    }
}
