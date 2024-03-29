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

package ste.xtest;

import java.time.LocalDateTime;
import java.util.Date;

/**
 *
 * @author ste
 */
public class Constants {
    
    public static final String[] BLANKS = new String[] {
        null, "", " ", "   ", "\n\t " 
    };
    
    public static final String[] BLANKS_WITHOUT_NULL = new String[] {
        "", " ", "   ", "\n\t " 
    };
    
    public static final int[] POSITIVES_1_25_389_4567 = new int[] {
        1, 25, 389, 4567
    };
    
    public static final int[] NOT_NEGATIVES_0_1_25_389_4567 = new int[] {
        0, 1, 25, 389, 4567
    };
    
    public static final int[] NEGATIVES_1_25_389_4567 = new int[] {
        -1, -25, -389, -4567
    };
    
    public static final int[] NOT_POSITIVES_0_1_25_389_4567 = new int[] {
        0, -1, -25, -389, -4567
    };
    
    public static final Date DATETIME_197110290000 = new Date(57538800000l);
    public static final Date DATETIME_197110301200 = new Date(57668400000l);
}
