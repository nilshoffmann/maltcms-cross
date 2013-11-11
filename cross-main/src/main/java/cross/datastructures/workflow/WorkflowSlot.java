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
package cross.datastructures.workflow;

import java.io.Serializable;

/**
 * Categories for workflow commands and workflow results.
 *
 * @author Nils Hoffmann
 *
 */
public enum WorkflowSlot implements Serializable {

	/**
	 * General unspecific File IO.
	 */
	FILEIO, 
	/**
	 * Statistics generation.
	 */
	STATISTICS, 
	/**
	 * Specific File IO for format conversion.
	 */
	FILECONVERSION, 
	/**
	 * Temporal readjustment of discrete signals.
	 */
	WARPING, 
	/**
	 * Signal filtering with the target of increasing signal-to-noise ratio.
	 */
	NOISEREDUCTION, 
	/**
	 * Unspecified preprocessing.
	 */
	GENERAL_PREPROCESSING, 
	/**
	 * Detection and integration of signal peaks.
	 */
	PEAKFINDING, 
	/**
	 * Synonym for ALIGNMENT on peaks
	 */
	PEAKMATCHING, 
	/**
	 * Unspecified identification, e.g. of peaks, mass spectra, ion traces.
	 */
	IDENTIFICATION, 
	/**
	 * Validation of data formats.
	 */
	VALIDATION, 
	/**
	 * Unspecified visualization results.
	 */
	VISUALIZATION, 
	/**
	 * General alignment of signals.
	 */
	ALIGNMENT, 
	/**
	 * Clustering as a means of statistics, meaning grouping of similar signals.
	 */
	CLUSTERING;
}
