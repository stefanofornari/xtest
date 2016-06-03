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
package ste.xtest.net;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ste
 */
public class URLStubMap {
    
    private static final Map<String, StubURL> map = new HashMap<String, StubURL>();
    
    public static Map<String, StubURL> getMapping() {
        return map;
    }
    
    /**
     * 
     * @param url the url to be used to select a mock - NOT NULL
     * 
     * @return the selected mock 
     * 
     * @throws IllegalArgumentException if url is malformed or null
     * 
     */
    public static StubURL get(String url) {
        if (url == null) {
            throw new IllegalArgumentException("url can not be null");
        }
        
        StubURL selectedUrl = map.get(url);
        if (selectedUrl != null) {
            return selectedUrl;
        }
        
        return new StubURL();
    }
    
    public static void put(String url, StubURL stub) {
        map.put(url, stub);
    }
}
