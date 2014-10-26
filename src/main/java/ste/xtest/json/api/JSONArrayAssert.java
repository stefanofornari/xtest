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
package ste.xtest.json.api;

import org.assertj.core.api.AbstractAssert;
import static org.assertj.core.error.ShouldHaveSize.shouldHaveSize;
import org.assertj.core.internal.Failures;
import org.json.JSONArray;

/**
 *
 * @author ste
 */
public class JSONArrayAssert extends AbstractAssert<JSONArrayAssert, JSONArray> {

    protected JSONArrayAssert(final JSONArray a) {
        super(a, JSONArrayAssert.class);
    }
    
    /**
     * Verifies that the actual {@link JSONArray} has the given size
     * <p>
     * Example :
     *     
     * <pre>
     * JSONArray a1 = new JSONArray();
     * 
     * then(a1).hasSize(0);
     *     
     * @param size the expected size
     * 
     * @return {@code this} assertion object.
     * 
     * @throws AssertionError if the given size is not in the current JSONArray length
     */
    public JSONArrayAssert hasSize(final int size) {
        if (size < 0) {
            throw new IllegalArgumentException("size can not be negative");
        }
        
        if (actual.length() != size) {
            throw Failures.instance().failure(
                info, 
                shouldHaveSize(actual, actual.length(), size)
            );
        }
        return this;
    }

    // --------------------------------------------------------- private methods
    
}
