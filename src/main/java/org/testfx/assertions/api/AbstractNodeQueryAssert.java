/*
 * xTest
 * Copyright (C) 2014 Stefano Fornari
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
package org.testfx.assertions.api;

import java.util.Set;
import org.assertj.core.api.AbstractAssert;
import org.testfx.service.query.NodeQuery;

/**
 * This assertion set is meant to find "widgets" within the nodes. <i>Widget</i>
 * is here intended as a Control object in javafx.
 *
 * @param <SELF>
 */
public class AbstractNodeQueryAssert<SELF extends AbstractNodeQueryAssert<SELF>>
        extends AbstractAssert<SELF, NodeQuery> {

    protected AbstractNodeQueryAssert(NodeQuery actual, Class<?> selfType) {
        super(actual, selfType);
    }

    /**
     * Does the node contain one and only one child?
     * @return
     */
    public SELF hasOneWidget() {
        return hasNWidgets(1);
    }

    /**
    * Does the node contain exactly <i>n<i> children?
    */
    public SELF hasNWidgets(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n can not be negative");
        }
        Set widgets = actual.queryAll();
        if (widgets.size() == n) {
            return myself;
        }

        throw new AssertionError(
            (widgets.size() == 0) ?
            String.format(
                "Expected %d result%s from query %s but found nothing",
                n, (n == 1)? "" : "s", String.valueOf(actual)
            )
            :
            String.format(
                "Expected %d result%s from query %s but found %d %s",
                n, (n == 1)? "" : "s",
                String.valueOf(actual), widgets.size(), String.valueOf(widgets)
            )
        );
    }

    /**
    * Does the node contain at least one children?
    */
    public SELF hasWidgets() {
        Set widgets = actual.queryAll();

        if (!widgets.isEmpty()) {
            return myself;
        }

        throw new AssertionError(
            String.format(
                "Expected something from query %s but found nothing",
                String.valueOf(actual)
            )
        );
    }

    /**
    * Does the node contain mp children?
    */
    public SELF hasNoWidgets() {
        Set widgets = actual.queryAll();
        if (widgets.isEmpty()) {
            return myself;
        }

        throw new AssertionError(
            String.format(
                "Expected nothing from query %s but found %d %s",
                String.valueOf(actual), widgets.size(), String.valueOf(widgets)
            )
        );
    }
}