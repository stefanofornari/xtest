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
public class URLMockSelector {
    
    private Map<String, URL> map;
    
    public URLMockSelector(final HashMap<String, URL> map) {
        if (map == null) {
            throw new IllegalArgumentException("map can not be null");
        }
        
        this.map = map;
    }
    
    public Map<String, URL> getMapping() {
        return map;
    }
    
    /**
     * 
     * @param url the url to be used to select a mock - NOT NULL
     * 
     * @return the selected mock 
     * 
     * @throws IllegalArgumentExceptino if url is malformed or null
     */
    public URL select(final String url) {
        if (url == null) {
            throw new IllegalArgumentException("url can not be null");
        }
        int pos = url.indexOf('#');
        String urlKey = (pos > 0) ? url.substring(0, pos) : url;
        URL selectedUrl = map.get(urlKey);
        
        if (selectedUrl != null) {
            return selectedUrl;
        }
        
        try {
            return new URL(url);
        } catch (MalformedURLException x) {
            throw new IllegalArgumentException(
                String.format("'%s' is not a valid url: %s", url, x.getMessage())
            );
        }
    }
}
