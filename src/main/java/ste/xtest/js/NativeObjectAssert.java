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
package ste.xtest.js;

import org.assertj.core.api.AbstractAssert;
import org.mozilla.javascript.NativeObject;

/**
 *
 * @author ste
 */
public class NativeObjectAssert extends AbstractAssert<NativeObjectAssert, NativeObject> {
    
    private final String property;
    
    protected NativeObjectAssert(final NativeObject o) {
        super(o, NativeObjectAssert.class);
        property = null;
    }
    
    protected NativeObjectAssert(final NativeObject o, final String p) {
        super(o, NativeObjectAssert.class);
        property = p;
    }

    /*
    public NativeObjectAssert hasLevel(final Level expected) {
        if (actual.getLevel().equals(expected)) {
            return this;
        }
        
        throw Failures.instance().failure(
            info, 
            new BasicErrorMessageFactory(
                "expected to be at level %s but was %s", 
                String.valueOf(expected),
                String.valueOf(actual.getLevel())
            )
        );
    }
    
    public NativeObjectAssert hasMessage(final String expected) {
        if (actual.getMessage().equals(expected)) {
            return this;
        }
        
        throw Failures.instance().failure(
            info, 
            new BasicErrorMessageFactory(
                "expecting message <%s> but was <%s>", 
                expected,
                String.valueOf(actual.getMessage())
            )
        );
    }
    */

    /**
     * @return the selected property (null if no property selected)
     */
    public String getSelectedProperty() {
        return property;
    }
}
