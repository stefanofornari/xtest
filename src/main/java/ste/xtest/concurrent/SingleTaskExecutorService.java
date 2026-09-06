/*
 * xTest
 * Copyright (C) 2026 Stefano Fornari
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY Stefano Fornari, Stefano Fornari
 * DISCLAIMS THE WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 */

package ste.xtest.concurrent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * A no-operation implementation of {@link java.util.concurrent.ExecutorService} designed
 * for unit testing environments where asynchronous or background thread execution
 * needs to be suppressed.
 *
 * <p>Any tasks submitted via {@link #execute(Runnable)} or standard {@code submit(...)}
 * methods are silently discarded and never executed. Lifecycle queries immediately report
 * the executor as shut down and terminated.</p>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 * @BeforeEach
 * void before_each() {
 *     vncStub = new VNCServiceStub("127.0.0.1", 5900);
 *     // Suppress background thread execution during UI controller unit tests:
 *     vncStub.executor = new NoOpExecutorService();
 *
 *     controller = new VNCViewerController();
 *     controller.initialize(); // Calls vncStub.start(), but no loop thread is spawned
 * }
 * }</pre>
 */
public class SingleTaskExecutorService extends AbstractExecutorService {

    final private Runnable task;
    private boolean running = true;

    public SingleTaskExecutorService(final Runnable task) {
        this.task = task;
    }

    /**
     * Suppresses execution of the given task.
     *
     * @param command the task to execute (ignored)
     */
    @Override
    public void execute(Runnable command) {
        if (!running) {
            throw new RejectedExecutionException();
        }
        task.run();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        running = false;
    }

    /**
     * {@inheritDoc}
     *
     * @return an empty list since no tasks are ever queued or retained
     */
    @Override
    public List<Runnable> shutdownNow() {
        running = false;
        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true} always
     */
    @Override
    public boolean isShutdown() {
        return !running;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true} always
     */
    @Override
    public boolean isTerminated() {
        return !running;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true} always
     */
    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) {
        return true;
    }
}