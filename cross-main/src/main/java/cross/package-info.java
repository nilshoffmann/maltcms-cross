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
/**
 * <p>
 * Base package for Cross, the Common Runtime Object Support System.</p>
 * <p>
 * Cross provides a number of interfaces and implementations to set up
 * a modular, configurable, pipeline-based light-weight workflow system.</p>
 * <p>
 * It uses the unintrusive
 * <a href="http://docs.spring.io/spring/docs/3.2.4.RELEASE/spring-framework-reference/html/" target="_blank">
 * Spring application context</a> at its core for configuration and
 * <a href="http://ehcache.org/" target="_blank">Ehcache</a> for object caching. It furthermore
 * supports parallel execution of {@link java.lang.Runnable} and
 * {@link java.util.concurrent.Callable} objects on local or cluster systems via
 * <a href="http://sf.net/p/mpaxs" target="_blank">Mpaxs</a>.</p>
 *
 */
package cross;
