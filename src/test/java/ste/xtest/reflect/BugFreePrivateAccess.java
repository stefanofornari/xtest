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

package ste.xtest.reflect;

import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;

/**
 *
 * @author ste
 */
public class BugFreePrivateAccess {
    
    @Test
    public void setAndGetStaticFieldOK() throws Exception {
        PrivateAccess.setStaticValue(PrivateAccessHelper.class, "StaticPrivateObject", null);
        then(PrivateAccess.getStaticValue(PrivateAccessHelper.class, "StaticPrivateObject")).isNull();
        
        Object o = new Object();
        PrivateAccess.setStaticValue(PrivateAccessHelper.class, "StaticPrivateObject", o);
        then(PrivateAccess.getStaticValue(PrivateAccessHelper.class, "StaticPrivateObject")).isSameAs(o);
        
        String s = "hello";
        PrivateAccess.setStaticValue(PrivateAccessHelper.class, "StaticPrivateString", s);
        then(PrivateAccess.getStaticValue(PrivateAccessHelper.class, "StaticPrivateString")).isSameAs(s);
    }
    
    @Test
    public void setAndGetStaticFieldKOInvalidName() throws Exception {
        try {
            PrivateAccess.setStaticValue(PrivateAccessHelper.class, "NotExisting", null);
            fail("not existing fields shall throw an exception");
        } catch (NoSuchFieldException x) {
            //
            // OK
            //
        }
    }
    
    @Test
    public void setStaticFieldKONonStaticField() throws Exception {
        try {
            PrivateAccess.setStaticValue(PrivateAccessHelper.class, "InstancePrivateObject", null);
            fail("not static fields shall throw an exception");
        } catch (NoSuchFieldException x) {
            then(x.getMessage()).contains("InstancePrivateObject").contains("not a static field");
        }
    }
    
    @Test
    public void getStaticFieldKONonStaticField() throws Exception {
        try {
            PrivateAccess.getStaticValue(PrivateAccessHelper.class, "InstancePrivateObject");
            fail("not static fields shall throw an exception");
        } catch (NoSuchFieldException x) {
            then(x.getMessage()).contains("InstancePrivateObject").contains("not a static field");
        }
    }
    
    @Test
    public void getInstanceFieldKONullClass() throws Exception {
        try {
            PrivateAccess.getStaticValue(null, "StaticPrivateObject");
            fail("null values shall be checked");
        } catch (IllegalArgumentException x) {
            then(x.getMessage()).contains("the class can not be null");
        }
    }
    
    @Test
    public void getSetInstanceFieldOK() throws Exception {
        PrivateAccessHelper h = new PrivateAccessHelper();
        PrivateAccess.setInstanceValue(h, "InstancePrivateObject", null);
        then(PrivateAccess.getInstanceValue(h, "InstancePrivateObject")).isNull();
        
        Object o = new Object();
        PrivateAccess.setInstanceValue(h, "InstancePrivateObject", o);
        then(PrivateAccess.getInstanceValue(h, "InstancePrivateObject")).isSameAs(o);
        
        String s = "hello";
        PrivateAccess.setInstanceValue(h, "InstancePrivateObject", s);
        then(PrivateAccess.getInstanceValue(h, "InstancePrivateObject")).isSameAs(s);
    }
    
    @Test
    public void getInstanceFieldKOStaticField() throws Exception {
        try {
            PrivateAccess.getInstanceValue(new PrivateAccessHelper(), "StaticPrivateObject");
            fail("static fields shall throw an exception");
        } catch (NoSuchFieldException x) {
            then(x.getMessage()).contains("StaticPrivateObject").contains("a static field");
        }
    }
    
    @Test
    public void getInstanceFieldKONullInstance() throws Exception {
        try {
            PrivateAccess.getInstanceValue(null, "InstancePrivateObject");
            fail("null values shall be checked");
        } catch (IllegalArgumentException x) {
            then(x.getMessage()).contains("the instance can not be null");
        }
    }
    
    @Test
    public void getInstanceFieldInParentclass() throws Exception {
        PrivateAccessHelperSubclass h = new PrivateAccessHelperSubclass();
        then(PrivateAccess.getInstanceValue(h, "InstancePrivateObject")).isNull();
        
        String s = "hello world";
        PrivateAccess.setInstanceValue(h, "InstancePrivateObject", s);
        then(PrivateAccess.getInstanceValue(h, "InstancePrivateObject"))
            .isSameAs(s).isEqualTo("hello world");
        
        PrivateAccess.setInstanceValue(h, "InstancePrivateObjectSubclass", s);
        then(PrivateAccess.getInstanceValue(h, "InstancePrivateObjectSubclass"))
            .isEqualTo("hello world");
    }
}
