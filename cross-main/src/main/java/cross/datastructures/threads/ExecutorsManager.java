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
package cross.datastructures.threads;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Proxy for different ExecutorService implementations.
 *
 * @author Nils Hoffmann
 *
 *
 */
public class ExecutorsManager implements ExecutorService {

    /**
     *
     */
    public enum ExecutorType {

        /**
         *
         */
        SINGLETON,

        /**
         *
         */
        CACHED,

        /**
         *
         */
        FIXED
    }
    private ExecutorService es = null;
    private int maxThreads = 1;

    private final ExecutorType type = ExecutorType.FIXED;

    /**
     * Create a new instance given the specified executor type.
     *
     * @param et the executor type
     */
    public ExecutorsManager(final ExecutorType et) {
        if (this.type.equals(ExecutorType.SINGLETON)) {
            this.es = Executors.newSingleThreadExecutor();
        } else if (this.type.equals(ExecutorType.CACHED)) {
            this.es = Executors.newCachedThreadPool();
        } else {
            this.es = Executors.newFixedThreadPool(this.maxThreads);
        }
    }

    /**
     * Create a new instance with a fixed number of threads.
     *
     * @param nthreads the number of threads
     */
    public ExecutorsManager(final int nthreads) {
        this.maxThreads = nthreads;
        this.es = Executors.newFixedThreadPool(this.maxThreads);
    }

    /**
     *
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException
     */
    @Override
    public boolean awaitTermination(final long timeout, final TimeUnit unit)
        throws InterruptedException {
        return this.es.awaitTermination(timeout, unit);
    }

    /**
     *
     * @param command
     */
    @Override
    public void execute(final Runnable command) {
        this.es.execute(command);
    }

    /**
     *
     * @param <T>
     * @param tasks
     * @return
     * @throws InterruptedException
     */
    @Override
    public <T> List<Future<T>> invokeAll(
        final Collection<? extends Callable<T>> tasks)
        throws InterruptedException {
        return this.es.invokeAll(tasks);
    }

    /**
     *
     * @param <T>
     * @param tasks
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException
     */
    @Override
    public <T> List<Future<T>> invokeAll(
        final Collection<? extends Callable<T>> tasks, final long timeout,
        final TimeUnit unit) throws InterruptedException {
        return this.es.invokeAll(tasks, timeout, unit);
    }

    /**
     *
     * @param <T>
     * @param tasks
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Override
    public <T> T invokeAny(final Collection<? extends Callable<T>> tasks)
        throws InterruptedException, ExecutionException {
        return this.es.invokeAny(tasks);
    }

    /**
     *
     * @param <T>
     * @param tasks
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    @Override
    public <T> T invokeAny(final Collection<? extends Callable<T>> tasks,
        final long timeout, final TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        return this.es.invokeAny(tasks, timeout, unit);
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isShutdown() {
        return this.es.isShutdown();
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isTerminated() {
        return this.es.isTerminated();
    }

    /**
     *
     */
    @Override
    public void shutdown() {
        this.es.shutdown();
    }

    /**
     *
     * @return
     */
    @Override
    public List<Runnable> shutdownNow() {
        return this.es.shutdownNow();
    }

    /**
     *
     * @param <T>
     * @param task
     * @return
     */
    @Override
    public <T> Future<T> submit(final Callable<T> task) {
        return this.es.submit(task);
    }

    /**
     *
     * @param task
     * @return
     */
    @Override
    public Future<?> submit(final Runnable task) {
        return this.es.submit(task);
    }

    /**
     *
     * @param <T>
     * @param task
     * @param result
     * @return
     */
    @Override
    public <T> Future<T> submit(final Runnable task, final T result) {
        return this.es.submit(task, result);
    }
}
