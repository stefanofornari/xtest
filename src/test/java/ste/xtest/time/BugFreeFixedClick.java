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

package ste.xtest.time;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;

/**
 *
 */
public class BugFreeFixedClick {
    @Test
    public void initialization() throws Exception {
        //
        // By default, initializing with the current time in millis...
        //
        final long NOW = System.currentTimeMillis();
        Clock clock = new InstantSequenceClock(); Thread.sleep(100);
        then(clock.getZone()).isEqualTo(ZoneId.systemDefault());
        then(clock.instant())
            .isBetween(Instant.ofEpochMilli(NOW), Instant.ofEpochMilli(NOW+100))
            .isEqualTo(clock.instant());

        final Instant NOW_PLUS_ONE_HOUR = Instant.ofEpochMilli(NOW+60*60*1000);
        clock = new InstantSequenceClock(NOW_PLUS_ONE_HOUR);
        then(clock.instant()).isEqualTo(NOW_PLUS_ONE_HOUR);
        Thread.sleep(100);
        then(clock.instant()).isEqualTo(NOW_PLUS_ONE_HOUR);

        try {
            new InstantSequenceClock(null);
            fail("missing arguments check");
        } catch (IllegalArgumentException x) {
            then(x).hasMessage("instants can not be null");
        }

        try {
            new InstantSequenceClock(null, new Instant[0]);
            fail("missing arguments check");
        } catch (IllegalArgumentException x) {
            then(x).hasMessage("zone can not be null");
        }
    }

    @Test
    public void with_many_instants() {
        final Instant[] TIMES = new Instant[] {
            Instant.ofEpochMilli(-5694924134l), Instant.ofEpochMilli(-5694924034l),
            Instant.ofEpochMilli(-569492394l), Instant.ofEpochMilli(-569492294l)
        };

        final Clock C = new InstantSequenceClock(TIMES);
        then(C.instant()).isEqualTo(TIMES[0]);
        then(C.instant()).isEqualTo(TIMES[1]);
        then(C.instant()).isEqualTo(TIMES[2]);
        then(C.instant()).isEqualTo(TIMES[3]);
        then(C.instant()).isEqualTo(TIMES[3]);
        then(C.instant()).isEqualTo(TIMES[3]);
    }
}
