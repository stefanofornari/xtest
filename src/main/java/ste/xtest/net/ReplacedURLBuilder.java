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
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author ste
 */
public class ReplacedURLBuilder extends AbstractURLBuilder {
    
    private String match, replace;

    public ReplacedURLBuilder() {
        super();
        match = replace = null;
    }
    
    public ReplacedURLBuilder set(final String url) throws MalformedURLException {
        super.set(new URL(url));
        
        return this;
    }
    
    public ReplacedURLBuilder match(final String match) {
        if (StringUtils.isBlank(match)) {
            throw new IllegalArgumentException("match can not be blank");
        }
        this.match = match;
        
        return this;
    }
    
    public ReplacedURLBuilder replace(final String replace) {
        this.replace = replace;
        
        return this;
    }
    

    @Override
    public URL build() throws MalformedURLException {
        String original = url.toExternalForm();
        String replaced = original;
        if ((match != null) && (replace != null)) {
            replaced = original.replace(match, replace);
        }

        return new URL(replaced);
    }
    
}
