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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 *
 * @author Nils Hoffmann
 */
public class MathToolsTest {

	@Rule
	public LogMethodName logMethodName = new LogMethodName();
	@Rule
	public SetupLogging logging = new SetupLogging();

	/**
	 * Test of average method, of class MathTools.
	 */
	@Test
	public void testAverage() {
		double[] values = new double[]{1, 2, 3, 4, 5};
		double average = MathTools.average(values, 0, values.length - 1);
		Assert.assertEquals((1 + 2 + 3 + 4 + 5) / 5.0d, average, 10.0e-10);
		double average1 = MathTools.average(values, 1, values.length - 1);
		Assert.assertEquals((2 + 3 + 4 + 5) / 4.0d, average1, 10.0e-10);
		double average2 = MathTools.average(values, 1, values.length - 2);
		Assert.assertEquals((2 + 3 + 4) / 3.0d, average2, 10.0e-10);
		double average3 = MathTools.average(values, 0, values.length - 2);
		Assert.assertEquals((1 + 2 + 3 + 4) / 4.0d, average3, 10.0e-10);
	}

	/**
	 * Test of averageOfSquares method, of class MathTools.
	 */
	@Test
	public void testAverageOfSquares() {
		double[] values = new double[]{1, 2, 3, 4, 5};
		double average = MathTools.averageOfSquares(values, 0, values.length);
		Assert.assertEquals(((1 * 1) + (2 * 2) + (3 * 3) + (4 * 4) + (5 * 5)) / 5.0d, average, 10.0e-10);
		double average1 = MathTools.averageOfSquares(values, 1, values.length - 1);
		Assert.assertEquals(((2 * 2) + (3 * 3) + (4 * 4) + (5 * 5)) / 4.0d, average1, 10.0e-10);
		double average2 = MathTools.averageOfSquares(values, 1, values.length - 2);
		Assert.assertEquals(((2 * 2) + (3 * 3) + (4 * 4)) / 3.0d, average2, 10.0e-10);
		double average3 = MathTools.averageOfSquares(values, 0, values.length - 2);
		Assert.assertEquals(((1 * 1) + (2 * 2) + (3 * 3) + (4 * 4)) / 4.0d, average3, 10.0e-10);
	}

	/**
	 * Test of binCoeff method, of class MathTools.
	 */
	@Test
	public void testBinCoeff() {
		//n over 1, n over 2, n over 3, n over 4, n over 5
		double[] binCoeffExpected = new double[]{1, 2, 6,};
		double[] binCoeffCalculated = MathTools.binCoeff(3);
		//3 over 1
		//3 over 2
		//3 over 3
	}

	/**
	 * Test of binomial method, of class MathTools.
	 */
	@Test
	public void testBinomial() {
	}

	/**
	 * Test of diff method, of class MathTools.
	 */
	@Test
	public void testDiff() {
	}

	/**
	 * Test of faculty method, of class MathTools.
	 */
	@Test
	public void testFaculty() {
		double value = MathTools.faculty(5);
		Assert.assertEquals(120.0d, value, 10.0e-10);
		//last exact value
		double value2 = MathTools.faculty(170);
		Assert.assertEquals(7.257415615307994E306, value2, 10.0e-100);
		try {
			double value3 = MathTools.faculty(180);
			Assert.fail("Should not reach this point!");
		} catch (IllegalArgumentException iae) {
		}
	}

	/**
	 * Test of getLinearInterpolatedY method, of class MathTools.
	 */
	@Test
	public void testGetLinearInterpolatedY() {
	}

	/**
	 * Test of max method, of class MathTools.
	 */
	@Test
	public void testMax_doubleArr() {
		double[] values = new double[]{1234.123, 8098.314, 897.624};
		Assert.assertEquals(8098.314, MathTools.max(values), 10.0e-100);
	}

	/**
	 * Test of max method, of class MathTools.
	 */
	@Test
	public void testMax_3args_1() {
		double[] values = new double[]{1234.123, 8098.314, 897.624, 87123.2235};
		Assert.assertEquals(87123.2235, MathTools.max(values, 0, values.length - 1), 10.0e-100);
		Assert.assertEquals(8098.314, MathTools.max(values, 0, values.length - 2), 10.0e-100);
		Assert.assertEquals(87123.2235, MathTools.max(values, 2, values.length - 1), 10.0e-100);
	}

	/**
	 * Test of max method, of class MathTools.
	 */
	@Test
	public void testMax_intArr() {
		int[] values = new int[]{1234, 8098, 897};
		Assert.assertEquals(8098, MathTools.max(values));
	}

	/**
	 * Test of max method, of class MathTools.
	 */
	@Test
	public void testMax_3args_2() {
		int[] values = new int[]{1234, 8098, 897, 87123};
		Assert.assertEquals(87123, MathTools.max(values, 0, values.length - 1));
		Assert.assertEquals(8098, MathTools.max(values, 0, values.length - 2), 10.0e-100);
		Assert.assertEquals(87123, MathTools.max(values, 2, values.length - 1), 10.0e-100);
	}

	/**
	 * Test of median method, of class MathTools.
	 */
	@Test
	public void testMedian_Collection() {
		//uneven number of elements -> select center value
		List<Integer> l1 = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
		Collections.shuffle(l1);
		double median1 = MathTools.median(l1);
		Assert.assertEquals(5.0d, median1, 10.0e-100);

		//even number of elements -> average of two values next to center
		List<Integer> l2 = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);
		Collections.shuffle(l2);
		double median2 = MathTools.median(l2);
		Assert.assertEquals(4.5d, median2, 10.0e-100);
	}

	/**
	 * Test of median method, of class MathTools.
	 */
	@Test
	public void testMedian_doubleArr() {
		//uneven number of elements -> select center value
		double[] l1 = new double[]{4, 6, 7, 5, 1, 2, 3, 8, 9};
		double median1 = MathTools.median(l1);
		Assert.assertEquals(5.0d, median1, 10.0e-100);

		//even number of elements -> average of two values next to center
		double[] l2 = new double[]{4, 6, 7, 5, 1, 2, 3, 8};
		double median2 = MathTools.median(l2);
		Assert.assertEquals(4.5d, median2, 10.0e-100);
	}

	/**
	 * Test of median method, of class MathTools.
	 */
	@Test
	public void testMedian_3args() {
		//uneven number of elements -> select center value
		double[] l1 = new double[]{4, 6, 7, 5, 1, 2, 3, 8, 9};
		double median1 = MathTools.median(l1, 0, l1.length - 1);
		Assert.assertEquals(5.0d, median1, 10.0e-100);

		double median2 = MathTools.median(l1, 1, l1.length - 2);
		Assert.assertEquals(5.0d, median2, 10.0e-100);

		//even number of elements -> average of two values next to center
		double[] l2 = new double[]{4, 6, 7, 5, 1, 2, 3, 8};
		double median3 = MathTools.median(l2, 0, l2.length - 1);
		Assert.assertEquals(4.5d, median3, 10.0e-100);
		double median4 = MathTools.median(l2, 2, l2.length - 1);
		Assert.assertEquals(4.0d, median4, 10.0e-100);
	}

	/**
	 * Test of median method, of class MathTools.
	 */
	@Test
	public void testMedian_doubleArrArr() {
		double[][] l1 = new double[3][];
		l1[0] = new double[]{4, 6, 7};
		l1[1] = new double[]{5, 1, 2};
		l1[2] = new double[]{3, 8, 9};
		double median1 = MathTools.median(l1);
		Assert.assertEquals(5.0d, median1, 10.0e-100);
	}

	/**
	 * Test of median method, of class MathTools.
	 */
	@Test
	public void testMedian_intArr() {
		int[] l1 = new int[]{4, 6, 7, 5, 1, 2, 3, 8, 9};
		double median1 = MathTools.median(l1);
		Assert.assertEquals(5.0d, median1, 10.0e-100);
	}

	/**
	 * Test of medianOnSorted method, of class MathTools.
	 */
	@Test
	public void testMedianOnSorted() {
		double[] l1 = new double[]{4, 6, 7, 5, 1, 2, 3, 8, 9};
		Arrays.sort(l1);
		double median1 = MathTools.medianOnSorted(l1);
		Assert.assertEquals(5.0d, median1, 10.0e-100);
	}

	/**
	 * Test of min method, of class MathTools.
	 */
	@Test
	public void testMin_doubleArr() {
		double[] values = new double[]{1234.123, 8098.314, 897.624};
		Assert.assertEquals(897.624, MathTools.min(values), 10.0e-100);
	}

	/**
	 * Test of min method, of class MathTools.
	 */
	@Test
	public void testMin_3args_1() {
		double[] values = new double[]{1234.123, 8098.314, 897.624, 87123.2235};
		Assert.assertEquals(897.624, MathTools.min(values, 0, values.length - 1), 10.0e-100);
		Assert.assertEquals(897.624, MathTools.min(values, 0, values.length - 2), 10.0e-100);
		Assert.assertEquals(897.624, MathTools.min(values, 2, values.length - 1), 10.0e-100);
	}

	/**
	 * Test of min method, of class MathTools.
	 */
	@Test
	public void testMin_intArr() {
		int[] values = new int[]{1234, 8098, 897};
		Assert.assertEquals(897, MathTools.min(values));
	}

	/**
	 * Test of min method, of class MathTools.
	 */
	@Test
	public void testMin_3args_2() {
		int[] values = new int[]{1234, 8098, 897, 87123};
		Assert.assertEquals(897, MathTools.min(values, 0, values.length - 1));
		Assert.assertEquals(897, MathTools.min(values, 0, values.length - 2), 10.0e-100);
		Assert.assertEquals(897, MathTools.min(values, 2, values.length - 1), 10.0e-100);
	}

	/**
	 * Test of relativeBinCoeff method, of class MathTools.
	 */
	@Test
	public void testRelativeBinCoeff() {
	}

	/**
	 * Test of sum method, of class MathTools.
	 */
	@Test
	public void testSum() {
		double sum = MathTools.sum(new double[]{4, 6, 7, 5, 1, 2, 3, 8, 9});
		Assert.assertEquals(45, sum, 10.0e-100);
	}

	/**
	 * Test of weightedAverage method, of class MathTools.
	 */
	@Test
	public void testWeightedAverage_int_doubleArr() {
	}

	/**
	 * Test of weightedAverage method, of class MathTools.
	 */
	@Test
	public void testWeightedAverage_3args() {
	}

	/**
	 * Test of dilate method, of class MathTools.
	 */
	@Test
	public void testDilate_int_doubleArr() {
	}

	/**
	 * Test of erode method, of class MathTools.
	 */
	@Test
	public void testErode_int_doubleArr() {
	}

	/**
	 * Test of dilate method, of class MathTools.
	 */
	@Test
	public void testDilate_3args() {
	}

	/**
	 * Test of erode method, of class MathTools.
	 */
	@Test
	public void testErode_3args() {
	}

	/**
	 * Test of opening method, of class MathTools.
	 */
	@Test
	public void testOpening() {
	}

	/**
	 * Test of closing method, of class MathTools.
	 */
	@Test
	public void testClosing() {
	}

	/**
	 * Test of topHat method, of class MathTools.
	 */
	@Test
	public void testTopHat() {
	}

	/**
	 * Test of bottomHat method, of class MathTools.
	 */
	@Test
	public void testBottomHat() {
	}

	/**
	 * Tesf of seq method, of class MathTools.
	 */
	@Test
	public void testSeqInt() {
		int[] referenceSequence = new int[]{3, 5, 7, 9};
		int[] testSequence = MathTools.seq(3, 10, 2);
		Assert.assertArrayEquals(referenceSequence, testSequence);
		referenceSequence = new int[]{-3, -5, -7, -9};
		testSequence = MathTools.seq(-3, -9, -2);
		Assert.assertArrayEquals(referenceSequence, testSequence);
	}

	/**
	 * Tesf of seq method, of class MathTools.
	 */
	@Test
	public void testSeqDouble() {
		double[] referenceSequence = new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5};
		double[] testSequence = MathTools.seq(0.0, 0.5, 0.1);
		Assert.assertArrayEquals(referenceSequence, testSequence, 1.0e-10d);

		referenceSequence = new double[]{-2.1, -2.05, -2.0, -1.95, -1.9};
		testSequence = MathTools.seq(-2.1, -1.9, 0.05);
		Assert.assertArrayEquals(referenceSequence, testSequence, 1.0e-10d);
	}
}
