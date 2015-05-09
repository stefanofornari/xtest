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

import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.Fail.fail;
import org.junit.Test;

/**
 *
 * @author ste
 */
public class BugFreeJSONObjectAssertContains extends BugFreeJSONAssertBase {
    
    @Test
    public void contains_with_property_name() {
        JSONObjectAssert a = new JSONObjectAssert(TEST_O1);
        
        a.contains("p1");
        a.contains("p2");
        a.contains("p3");
        
        try {
            a.contains("a1");
            fail("assertion not rised");
        } catch (AssertionError e) {
            then(e.getMessage()).contains("property:<\"a1\">");
        }
    }
    
    @Test
    public void contains_with_blank() {
        JSONObjectAssert a = new JSONObjectAssert(TEST_O1);
        
        for (String BLANK: new String[] { null, "", "  ", "\t\n"}) {
            try {
                a.contains(BLANK);
                fail("nissing parameter sanity check");
            } catch (IllegalArgumentException x) {
                then(x).hasMessageContaining("propertyName")
                       .hasMessageContaining("can not be blank");
            } catch (Throwable t) {
                fail("nissing sanity check");
            }
        }
    }
    
    @Test
    public void does_not_contain_with_property_name() {
        JSONObjectAssert a = new JSONObjectAssert(TEST_O1);
        
        a.doesNotContain("a1");
        a.doesNotContain("a2");
        
        try {
            a.doesNotContain("p1");
            fail("assertion not rised");
        } catch (AssertionError e) {
            then(e.getMessage()).isEqualTo("property \"p1\" expected to be missing");
        }
    }
    
    @Test
    public void does_not_contain_with_blanks() {
        JSONObjectAssert a = new JSONObjectAssert(TEST_O1);
        
        for (String BLANK: new String[] { null, "", "  ", "\t\n"}) {
            try {
                a.doesNotContain(BLANK);
                fail("nissing parameter sanity check");
            } catch (IllegalArgumentException x) {
                then(x).hasMessageContaining("propertyName")
                       .hasMessageContaining("can not be blank");
            } catch (Throwable t) {
                fail("nissing sanity check");
            }
        }
    }
    
    @Test
    public void contains_only_ok() throws Exception {
        JSONObjectAssert a = new JSONObjectAssert(TEST_O1);
        
        a.containsOnly(
            entry("p1", "value1"), entry("p2", "value2"), entry("p3", "value3")
        );
        
        a = new JSONObjectAssert(TEST_O2);
        
        a.containsOnly(
            entry("q1", "valueA"), entry("q2", "valueB")
        );
    }
    
    @Test
    public void contains_only_ko() throws Exception {
        JSONObjectAssert a = new JSONObjectAssert(TEST_O1);
        
        try {
            a.containsOnly(
                entry("p1", "value1"), entry("p2", "value2"), entry("p4", "value5")
            );
            fail("spec not met");
        } catch (AssertionError x) {}
        
        a = new JSONObjectAssert(TEST_O2);
        
        try {
            a.containsOnly(entry("p1", "value1"));
            fail("spec not met");
        } catch (AssertionError x) {}
    }
    
    @Test
    public void contains_entry_ok() throws Exception {
        JSONObjectAssert a = new JSONObjectAssert(TEST_O1);
        
        a.containsEntry(
            entry("p1", "value1")
        );
        a.containsEntry(
            entry("p2", "value2")
        );
        
        a = new JSONObjectAssert(TEST_O2);
        
        a.containsEntry(
            entry("q1", "valueA")
        );
    }
    
    @Test
    public void contains_entry_ko() throws Exception {
        JSONObjectAssert a = new JSONObjectAssert(TEST_O1);
        
        try {
            a.containsEntry(
                entry("p2", "value1")
            );
            fail("spec not met");
        } catch (AssertionError x) {}
        
        try{
            a.containsEntry(
                entry("p1", "value2")
            );
            fail("spec not met");
        } catch (AssertionError x) {}
        
        a = new JSONObjectAssert(TEST_O2);
        
        try {
            a.containsEntry(
                entry("q1", "value1")
            );
            fail("spec not met");
        } catch (AssertionError x) {}
    }
    
    @Test
    public void contains_key_and_value_ok() throws Exception {
        JSONObjectAssert a = new JSONObjectAssert(TEST_O1);
        
        a.containsEntry("p1", "value1");
        a.containsEntry("p2", "value2");
        
        a = new JSONObjectAssert(TEST_O2);
        
        a.containsEntry("q1", "valueA");
    }
    
    @Test
    public void contains_key_and_value_ko() throws Exception {
        JSONObjectAssert a = new JSONObjectAssert(TEST_O1);
        
        try {
            a.containsEntry("p2", "value1");
            fail("spec not met");
        } catch (AssertionError x) {}
        
        try{
            a.containsEntry("p1", "value2");
            fail("spec not met");
        } catch (AssertionError x) {}
        
        a = new JSONObjectAssert(TEST_O2);
        
        try {
            a.containsEntry("q1", "value1");
            fail("spec not met");
        } catch (AssertionError x) {}
    }
}
