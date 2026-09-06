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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Before;

public class BugFreeNoOpExecutorService {

    private NoOpExecutorService executor;

    @Before
    public void before_each() {
        executor = new NoOpExecutorService();
    }

    @Test
    public void execute_does_not_run_command() {
        AtomicBoolean executed = new AtomicBoolean(false);

        executor.execute(() -> executed.set(true));

        then(executed.get()).isFalse();
    }

    @Test
    public void shutdown_now_returns_empty_list() {
        List<Runnable> pendingTasks = executor.shutdownNow();

        then(pendingTasks).isEmpty();
    }

    @Test
    public void lifecycle_queries_return_terminated_status() throws InterruptedException {
        executor.shutdown();

        then(executor.isShutdown()).isTrue();
        then(executor.isTerminated()).isTrue();
        then(executor.awaitTermination(1, TimeUnit.SECONDS)).isTrue();
    }
}