/*
 * xTest
 * Copyright (C) 2023 Stefano Fornari
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

import org.junit.Test;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import org.junit.Before;

public class BugFreeSingleTaskExecutorService {

    @Before
    public void before_each() {

    }

    @Test
    public void execute_runs_the_provided_task() {
        AtomicInteger executed = new AtomicInteger(0);

        SingleTaskExecutorService executor = new SingleTaskExecutorService(
            () -> executed.set(1)
        );
        executor.execute(() -> executed.set(100));

        then(executed.get()).isEqualTo(1);
    }

    @Test
    public void shutdown_now_returns_empty_list() {
        SingleTaskExecutorService executor = new SingleTaskExecutorService(null);
        List<Runnable> pendingTasks = executor.shutdownNow();

        then(pendingTasks).isEmpty();
    }

    @Test
    public void lifecycle_queries_return_terminated_status() throws InterruptedException {
        SingleTaskExecutorService executor = new SingleTaskExecutorService(null);
        executor.shutdown();

        then(executor.isShutdown()).isTrue();
        then(executor.isTerminated()).isTrue();
        then(executor.awaitTermination(1, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    public void submission_fails_if_shut_down() {
        SingleTaskExecutorService executor = new SingleTaskExecutorService(() -> {});

        executor.shutdown();

        thenThrownBy(() -> executor.execute(() -> {}))
        .isInstanceOf(RejectedExecutionException.class);
    }
}