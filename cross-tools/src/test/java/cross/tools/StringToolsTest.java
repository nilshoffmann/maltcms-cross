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

import cross.test.LogMethodName;
import cross.test.SetupLogging;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 *
 * @author Nils Hoffmann
 */
public class StringToolsTest {

    /**
     *
     */
    @Rule
    public LogMethodName logMethodName = new LogMethodName();

    /**
     *
     */
    @Rule
    public SetupLogging logging = new SetupLogging();

    /**
     * Test of deBlank method, of class StringTools.
     */
    @Test
    public void testDeBlank_String() {
        Assert.assertEquals("has-two-blanks", StringTools.deBlank("has two blanks"));
    }

    /**
     * Test of deBlank method, of class StringTools.
     */
    @Test
    public void testDeBlank_String_String() {
        Assert.assertEquals("hasMtwoMblanks", StringTools.deBlank("has two blanks", "M"));
    }

    /**
     * Test of getFileExtension method, of class StringTools.
     */
    @Test
    public void testGetFileExtension() {
        String singleEx = "blabla.cdf";
        Assert.assertEquals("cdf", StringTools.getFileExtension(singleEx));
        String doubleEx = "blabla.cdf.gz";
        Assert.assertEquals("cdf.gz", StringTools.getFileExtension(doubleEx));
        String tripleEx = "blabla.great.cdf.gz";
        Assert.assertEquals("great.cdf.gz", StringTools.getFileExtension(tripleEx));
    }

    /**
     * Test of removeFileExt method, of class StringTools.
     */
    @Test
    public void testRemoveFileExt() {
        String singleEx = "blabla.cdf";
        Assert.assertEquals("blabla", StringTools.removeFileExt(singleEx));
        String doubleEx = "blabla.cdf.gz";
        Assert.assertEquals("blabla", StringTools.removeFileExt(doubleEx));
        String tripleEx = "blabla.great.cdf.gz";
        Assert.assertEquals("blabla", StringTools.removeFileExt(tripleEx));
    }

    /**
     * Test of toStringList method, of class StringTools.
     */
    @Test
    public void testToStringList() {
        List<Object> l = new ArrayList<>();
        l.add("one");
        l.add("two");
        l.add("three");
        ArrayList<String> al = StringTools.toStringList(l);
        Assert.assertEquals("one", al.get(0));
        Assert.assertEquals("two", al.get(1));
        Assert.assertEquals("three", al.get(2));
    }

    /**
     * Test of join method, of class StringTools.
     */
    @Test
    public void testJoin() {
        Object[] o = new Object[]{"one", "two", "three"};
        String result = StringTools.join(o, "-");
        Assert.assertEquals("one-two-three", result);
    }
}
