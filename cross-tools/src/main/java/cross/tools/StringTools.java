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
package cross.tools;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class with methods to ease some String based operations.
 *
 * @author Nils Hoffmann
 *
 */
public class StringTools {

    /**
     * Replaces all whitespace with hyphens
     *
     * @param s the input string
     * @return the replaced string
     */
    public static String deBlank(final String s) {
        return StringTools.deBlank(s, "-");
    }

    /**
     * Replaces all whitespace with replacement.
     *
     * @param s           the input string
     * @param replacement the replacement string
     * @return the replaced string
     */
    public static String deBlank(final String s, final String replacement) {
        return s.replaceAll("\\s", replacement);
    }

    /**
     * Returns the suffix of a file or s if no dot '.' is contained in s. If
     * multiple dots are contained in the string, the suffix following the first
     * dot is returned.
     *
     * @param s the input string
     * @return the replaced string
     */
    public static String getFileExtension(final String s) {
        final int lastIndexOfDot = s.lastIndexOf(".");
        if (lastIndexOfDot == -1) {
            return s;
        }
        //handle multiple dots
        final int firstIndexOfDot = s.indexOf(".");
        if (firstIndexOfDot < lastIndexOfDot) {
            return s.substring(firstIndexOfDot + 1, s.length());
        }
        return s.substring(lastIndexOfDot + 1, s.length());
    }

    /**
     * Returns that part of a string before first occurrence of a dot, if a dot
     * is contained in s, otherwise, s is returned. If multiple dots are
     * contained in the string, the prefix of the string up to the first dot
     * position (exclusive) is returned.
     *
     * @param s the input string
     * @return the modified string
     */
    public static String removeFileExt(final String s) {
        final int lastIndexOfDot = s.lastIndexOf(".");
        //handle multiple dots
        final int firstIndexOfDot = s.indexOf(".");
        if (firstIndexOfDot < lastIndexOfDot) {
            String fileExtension = getFileExtension(s);
            return s.substring(0, s.length() - fileExtension.length() - 1);
        } else {
            return s.substring(0,
                (lastIndexOfDot < 0 ? s.length() : lastIndexOfDot));
        }
    }

    /**
     * Convert an untyped list of Strings to a typed one.
     *
     * @param list the input list
     * @return the typed list
     * @throws IllegalArgumentException if an object in the input list is not a
     *                                  string
     */
    public static ArrayList<String> toStringList(final List<?> list) {
        final ArrayList<String> al = new ArrayList<>();
        for (final Object o : list) {
            if (o instanceof String) {
                al.add((String) o);
            } else {
                throw new IllegalArgumentException("Expected object of type String, got: " + o.getClass().getName());
            }
        }
        return al;
    }

    /**
     * Joins the elements of the given object array using the provided
     * joinString. Each element is appended by calling its
     * <code>toString()</code> method.
     *
     * Returns an empty string if s is empty.
     *
     * @param s          the object array
     * @param joinString the string to separate elements
     * @return the joined string
     */
    public static String join(Object[] s, String joinString) {
        if (s.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length - 1; i++) {
            sb.append(s[i].toString());
            sb.append(joinString);
        }
        sb.append(s[s.length - 1]);
        return sb.toString();
    }
}
