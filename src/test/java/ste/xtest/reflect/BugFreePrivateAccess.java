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
    public void set_and_get_static_field_ok() throws Exception {
        PrivateAccess.setStaticValue(PrivateAccessHelper.class, "staticPrivateObject", null);
        then(PrivateAccess.getStaticValue(PrivateAccessHelper.class, "staticPrivateObject")).isNull();

        Object o = new Object();
        PrivateAccess.setStaticValue(PrivateAccessHelper.class, "staticPrivateObject", o);
        then(PrivateAccess.getStaticValue(PrivateAccessHelper.class, "staticPrivateObject")).isSameAs(o);

        String s = "hello";
        PrivateAccess.setStaticValue(PrivateAccessHelper.class, "staticPrivateString", s);
        then(PrivateAccess.getStaticValue(PrivateAccessHelper.class, "staticPrivateString")).isSameAs(s);
    }

    @Test
    public void set_and_get_static_field_ko_invalid_name() throws Exception {
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
    public void set_static_field_ko_non_static_field() throws Exception {
        try {
            PrivateAccess.setStaticValue(PrivateAccessHelper.class, "instancePrivateObject", null);
            fail("not static fields shall throw an exception");
        } catch (NoSuchFieldException x) {
            then(x.getMessage()).contains("instancePrivateObject").contains("not a static field");
        }
    }

    @Test
    public void get_static_field_ko_non_static_field() throws Exception {
        try {
            PrivateAccess.getStaticValue(PrivateAccessHelper.class, "instancePrivateObject");
            fail("not static fields shall throw an exception");
        } catch (NoSuchFieldException x) {
            then(x.getMessage()).contains("instancePrivateObject").contains("not a static field");
        }
    }

    @Test
    public void get_instance_field_ko_null_class() throws Exception {
        try {
            PrivateAccess.getStaticValue(null, "staticPrivateObject");
            fail("null values shall be checked");
        } catch (IllegalArgumentException x) {
            then(x.getMessage()).contains("the class can not be null");
        }
    }

    @Test
    public void get_set_instance_field_ok() throws Exception {
        PrivateAccessHelper h = new PrivateAccessHelper();
        PrivateAccess.setInstanceValue(h, "instancePrivateObject", null);
        then(PrivateAccess.getInstanceValue(h, "instancePrivateObject")).isNull();

        Object o = new Object();
        PrivateAccess.setInstanceValue(h, "instancePrivateObject", o);
        then(PrivateAccess.getInstanceValue(h, "instancePrivateObject")).isSameAs(o);

        String s = "hello";
        PrivateAccess.setInstanceValue(h, "instancePrivateObject", s);
        then(PrivateAccess.getInstanceValue(h, "instancePrivateObject")).isSameAs(s);
    }

    @Test
    public void get_instance_field_ko_static_field() throws Exception {
        try {
            PrivateAccess.getInstanceValue(new PrivateAccessHelper(), "staticPrivateObject");
            fail("static fields shall throw an exception");
        } catch (NoSuchFieldException x) {
            then(x.getMessage()).contains("staticPrivateObject").contains("a static field");
        }
    }

    @Test
    public void get_instance_field_ko_null_instance() throws Exception {
        try {
            PrivateAccess.getInstanceValue(null, "instancePrivateObject");
            fail("null values shall be checked");
        } catch (IllegalArgumentException x) {
            then(x.getMessage()).contains("the instance can not be null");
        }
    }

    @Test
    public void get_instance_field_in_parent_class() throws Exception {
        PrivateAccessHelperSubclass h = new PrivateAccessHelperSubclass();
        then(PrivateAccess.getInstanceValue(h, "instancePrivateObject")).isNull();

        String s = "hello world";
        PrivateAccess.setInstanceValue(h, "instancePrivateObject", s);
        then(PrivateAccess.getInstanceValue(h, "instancePrivateObject"))
            .isSameAs(s).isEqualTo("hello world");

        PrivateAccess.setInstanceValue(h, "instancePrivateObjectSubclass", s);
        then(PrivateAccess.getInstanceValue(h, "instancePrivateObjectSubclass"))
            .isEqualTo("hello world");
    }
}
