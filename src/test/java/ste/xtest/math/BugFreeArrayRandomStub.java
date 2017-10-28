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
package ste.xtest.math;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 * @TODO: invalid arguments check for constructor
 * 
 * @author ste
 */
public class BugFreeArrayRandomStub {
    @Test
    public void constructor_accepts_a_byte_array() {
        int[] numbers = new int[10];
        
        ArrayRandomStub bar = new ArrayRandomStub(numbers);
        then(bar.getNumbers()).isEqualTo(numbers);
        
        numbers = new int[10]; // same content different reference
        bar = new ArrayRandomStub(numbers);
        then(bar.getNumbers()).isEqualTo(numbers);
        
        numbers = new int[] {10, 54, 590};
        bar = new ArrayRandomStub(numbers);
        then(bar.getNumbers()).isEqualTo(numbers);
    }
    
    @Test
    public void nextInt_returns_the_given_sequence() {
        int[] numbers = new int[] {10, 54, 590};
        
        ArrayRandomStub bar = new ArrayRandomStub(numbers);
        
        for(int i=0; i<numbers.length; ++i) {
            then(bar.nextInt()).isEqualTo(numbers[i]);
        }
    }
    
    @Test
    public void nextBytes_returns_the_given_sequence_with_overflow() {
        int[] numbers = new int[] {10, 54, 123};
        
        ArrayRandomStub bar = new ArrayRandomStub(numbers);
        
        byte[] randoms = new byte[2];
        bar.nextBytes(randoms);
        then(randoms).isEqualTo(new byte[] {(byte)numbers[0], (byte)numbers[1]});
        
        randoms = new byte[5];
        bar.nextBytes(randoms);
        then(randoms).isEqualTo(
            new byte[] {
                (byte)numbers[2], (byte)numbers[0],
                (byte)numbers[1], (byte)numbers[2],
                (byte)numbers[0]
            }
        );
    }
    
    @Test 
    public void next_starts_from_zero_on_overflow() {
        int[] numbers = new int[] {10, 54, 590};
        
        ArrayRandomStub bar = new ArrayRandomStub(numbers);
        
        for(int i=0; i<numbers.length; ++i) {
            then(bar.nextInt()).isEqualTo(numbers[i]);
        }
        for(int i=0; i<numbers.length; ++i) {
            then(bar.nextInt()).isEqualTo(numbers[i]);
        }
    }
    
    @Test
    public void byte_array_can_not_be_null_or_zero_length() {
        try {
            new ArrayRandomStub(null);
            fail("missing arguments validity check");
        } catch (IllegalArgumentException x) {
            then(x).hasMessage("numbers can not be null");
        }
        
        try {
            new ArrayRandomStub(new int[0]);
            fail("missing arguments validity check");
        } catch (IllegalArgumentException x) {
            then(x).hasMessage("numbers can not be of length zero");
        }
    }
    
    @Test
    public void reset_restarts_the_sequence() {
        ArrayRandomStub bar = new ArrayRandomStub(new int[] {10, 54, 123, 39, 209, 0});
        bar.nextBytes(new byte[3]); bar.reset();
        byte[] numbers = new byte[3]; bar.nextBytes(numbers);
        then(numbers).isEqualTo(new byte[] {10, 54, 123});
        
        bar.nextInt(); bar.reset();
        then(numbers).isEqualTo(new byte[] {10, 54, 123});
    }
}
