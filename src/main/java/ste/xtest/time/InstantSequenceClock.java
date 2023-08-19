/*
 * xTest
 * Copyright (C) 2015 Stefano Fornari
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

/**
 *
 * @author ste
 */
public class InstantSequenceClock extends Clock {

    public final ZoneId zone;

    public final Instant[] instants;

    private int index = 0;

    public InstantSequenceClock() {
        this(Instant.ofEpochMilli(System.currentTimeMillis()));
    }

    public InstantSequenceClock(Instant... instants) {
        this(ZoneId.systemDefault(), instants);
    }

    public InstantSequenceClock(ZoneId zone, Instant... instants) {
        if (instants == null) {
            throw new IllegalArgumentException("instants can not be null");
        }
        if (zone == null) {
            throw new IllegalArgumentException("zone can not be null");
        }
        this.instants = instants;
        this.zone = zone;
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new InstantSequenceClock(zone);
    }

    @Override
    public Instant instant() {
        if (index < (instants.length-1)) {
            return instants[index++];
        }
        return instants[instants.length-1];
    }

}
