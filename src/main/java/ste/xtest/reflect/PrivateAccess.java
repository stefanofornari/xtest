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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 *
 * @author ste
 */
public class PrivateAccess {
    public static void setStaticValue(final Class c, final String field, final Object value)
    throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        getStaticField(c, field).set(null, value);
    }

    public static Object getStaticValue(final Class c, final String field)
    throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        return getStaticField(c, field).get(null);
    }

    public static void setInstanceValue(final Object o, final String field, final Object value)
    throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        getInstanceField(o, field).set(o, value);
    }

    public static Object getInstanceValue(final Object o, final String field)
    throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        return getInstanceField(o, field).get(o);
    }

    // --------------------------------------------------------- Private methods

    private static Field getStaticField(final Class c, final String field)
    throws NoSuchFieldException {
        if (c == null) {
            throw new IllegalArgumentException("the class can not be null");
        }

        Field f = c.getDeclaredField(field);
        if (!Modifier.isStatic(f.getModifiers())) {
            throw new NoSuchFieldException(field + " is not a static field");
        }
        f.setAccessible(true);

        return f;
    }

    private static Field getInstanceField(final Object instance, final String field)
    throws NoSuchFieldException {
        if (instance == null) {
            throw new IllegalArgumentException("the instance can not be null");
        }

        Class c = instance.getClass();

        Field f = null;
        try {
            f = c.getDeclaredField(field);
        } catch (NoSuchFieldException x) {
            Class superClass = c.getSuperclass();
            if (superClass == null) {
                throw x;
            }
            f = superClass.getDeclaredField(field);
        }

        if (Modifier.isStatic(f.getModifiers())) {
            throw new NoSuchFieldException(field + " is a static field");
        }
        f.setAccessible(true);

        return f;
    }



}
