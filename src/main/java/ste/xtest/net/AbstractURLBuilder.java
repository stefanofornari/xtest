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

/**
 *
 * @author ste
 * 
 * @TODO - not blank check for set()
 */
public abstract class AbstractURLBuilder {
    
    protected URL url;
    
    /**
     * Sets the url to be invoked
     * 
     * @param url the url to be set - NOT BLANK
     * 
     * @return this object
     * 
     * @throws java.net.MalformedURLException
     */
    public AbstractURLBuilder set(final URL url) {
        this.url = url;
        
        return this;
    }
    
    public URL getUrl() {
        return url;
    }
    
    public abstract URL build() throws MalformedURLException;
    
}
