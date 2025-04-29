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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.AbstractAssert;
import static org.assertj.core.api.Assertions.entry;
import org.assertj.core.data.MapEntry;
import org.assertj.core.error.BasicErrorMessageFactory;
import static org.assertj.core.error.ShouldHaveSize.shouldHaveSize;
import org.assertj.core.internal.Failures;
import org.assertj.core.internal.Maps;
import static org.assertj.core.util.Arrays.array;
import org.json.JSONException;
import org.json.JSONObject;
import static ste.xtest.json.error.ShouldContain.shouldContain;

/**
 *
 * @author ste
 */
public class JSONObjectAssert extends AbstractAssert<JSONObjectAssert, JSONObject> {

    Maps maps = Maps.instance();

    protected JSONObjectAssert(final JSONObject o) {
        super(o, JSONObjectAssert.class);
    }

    /**
     * Verifies that the actual {@link JSONObject} is equal to the given one.
     * <p>
     * Example :
     *
     * <pre>
     * JSONObject o1 = new JSONObject();
     * JSONObject o2 = new JSONObject();
     *
     * o1.set("k1", "v1");
     * o2.set("k1", "v1");
     *
     * then(o1(.isEqualTo(o2);
     *
     * </pre>
     *
     * @param expected the given value to compare the actual value to.
     *
     * @return {@code this} assertion object.
     *
     * @throws AssertionError if the actual {@code JSONObject} is not equal to
     *         the {@link JSONObject}.
     */
    public JSONObjectAssert isEqualTo(JSONObject expected) {
        if (expected == null) {
            super.isEqualTo(expected);
        } else if (actual == null) {
            super.isEqualTo(expected);
        } else {
            try {
                org.skyscreamer.jsonassert.JSONAssert.assertEquals(expected, actual, false);
            } catch (JSONException x) {
                throw Failures.instance().failure(
                    info,
                    new BasicErrorMessageFactory("Exception in comparison: %s", x.getMessage())
                );
            }
        }

        return this;
    }

    /**
     * Verifies that the actual {@link JSONObject} contains the given property
     * <p>
     * Example :
     *
     * <pre>
     * JSONObject o1 = new JSONObject("{'p1':'value1'}");
     *
     * then(o1).contains("p1");
     *
     * @param propertyName the property to check
     *
     * @return {@code this} assertion object.
     *
     * @throws AssertionError if the given property is not in the current JSONObject
     */
    public JSONObjectAssert contains(final String propertyName) {
        parameterSanityCheckBlank(propertyName);

        if (!actual.has(propertyName)) {
            throw Failures.instance().failure(
                info,
                shouldContain(actual, propertyName)
            );
        }
        return this;
    }

    /**
     * Verifies that the actual {@link JSONObject} does not contain the given property
     * <p>
     * Example :
     *
     * <pre>
     * JSONObject o1 = new JSONObject("{'p1':'value1'}");
     *
     * then(o1).doesNotContain("a1");
     *
     * @param propertyName the property to check
     *
     * @return {@code this} assertion object.
     *
     * @throws AssertionError if the given property is not in the current JSONObject
     */
    public JSONObjectAssert doesNotContain(final String propertyName) {
        parameterSanityCheckBlank(propertyName);

        if (actual.has(propertyName)) {
            throw Failures.instance().failure(
                info,
                new BasicErrorMessageFactory("property %s expected to be missing", propertyName)
            );
        }
        return this;
    }

    /**
     * Verifies that the actual {@link JSONObject} has the given size
     * <p>
     * Example :
     *
     * <pre>
     * JSONObject a1 = new JSONObject();
     *
     * then(a1).hasSize(0);
     *
     * @param size the expected size
     *
     * @return {@code this} assertion object.
     *
     * @throws AssertionError if the given size is not in the current JSONObject length
     */
    public JSONObjectAssert hasSize(final int size) {
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

    /**
    * Verifies that the actual map contains only the given entries and nothing else, in any order.
    *
    * <p>
    * Examples :
    *
    * <pre><code class='java'>
    * JSONObject ringBearers = ... // init with a JSON object
    *
    * // assertion will pass
    * then(ringBearers).containsOnly(entry(oneRing, frodo), entry(nenya, galadriel), entry(narya, gandalf), entry(vilya, elrond));
    *
    * // assertion will fail
    * then(ringBearers).containsOnly(entry(oneRing, frodo), entry(nenya, galadriel));
    *  </code></pre>
    *
    * @param entries the entries that should be in the actual map.
    *
    * @return the assert object
    *
    * @throws AssertionError if the actual map is {@code null}.
    * @throws NullPointerException if the given argument is {@code null}.
    * @throws AssertionError if the actual map does not contain the given entries, i.e. the actual map contains some or
    *           none of the given entries, or the actual map contains more entries than the given ones.
    */
    public JSONObjectAssert containsOnly(@SuppressWarnings("unchecked") MapEntry... entries) {
        try {
            HashMap map = new ObjectMapper().readValue(actual.toString(), HashMap.class);
            maps.assertContainsOnly(info, map, entries);
            return myself;
        } catch (IOException x) {
                throw Failures.instance().failure(
                    info,
                    new BasicErrorMessageFactory("Exception in contains: %s", x.getMessage())
                );
            }
    }


    /**
     * Verifies that the actual map contains the given entry
     *
     *<p>
     * Examples :
     *
     * <pre><code class='java'>
     * JSONObject ringBearers = ... // init with a JSON object
     *
     * // assertion will pass
     *  then(ringBearers).containsEntry(entry(oneRing, frodo));
     *
     * // assertion will fail
     * then(ringBearers).containsEntry(entry(oneRing, galadriel));
     *  </code></pre>
     *
     * @param entry the entry that should be in the actual map.
     *
     * @return the assert object
     *
     * @throws AssertionError if the actual map is {@code null}.
     * @throws NullPointerException if the given argument is {@code null}.
     * @throws AssertionError if the actual map does not contain the given entries, i.e. the actual map contains some or
     *           none of the given entries, or the actual map contains more entries than the given ones.
     */
    public JSONObjectAssert containsEntry(MapEntry entry) {
        try {
            HashMap map = new ObjectMapper().readValue(actual.toString(), HashMap.class);
            maps.assertContains(info, map, array(entry));
            return myself;
        } catch (IOException x) {
            throw Failures.instance().failure(
                info,
                new BasicErrorMessageFactory("Exception in contains: %s", x.getMessage())
            );
        }
    }

    /**
     * Verifies that the actual map contains the given key-value pair
     *
     * <p>
     * Examples :
     *
     * <pre><code class='java'>
     * JSONObject ringBearers = ... // init with a JSON object
     *
     * // assertion will pass
     * then(ringBearers).containsEntry(oneRing, frodo);
     *
     * // assertion will fail
     * then(ringBearers).containsEntry(oneRing, gadriel);
     *  </code></pre>
     *
     * @param key
     * @param value
     *
     * @return the assert object
     *
     * @throws AssertionError if the actual map is {@code null}.
     * @throws NullPointerException if the given argument is {@code null}.
     * @throws AssertionError if the actual map does not contain the given entries, i.e. the actual map contains some or
     *           none of the given entries, or the actual map contains more entries than the given ones.
     */
    public JSONObjectAssert containsEntry(String key, Object value) {
        return containsEntry(entry(key, value));
    }


    // --------------------------------------------------------- private methods

    private void parameterSanityCheckBlank(final String propertyName) throws IllegalArgumentException {
        if (StringUtils.isBlank(propertyName)) {
            throw new IllegalArgumentException("propertyName can not be blank");
        }
    }

}
