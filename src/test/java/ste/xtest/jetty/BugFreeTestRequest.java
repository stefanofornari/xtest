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
package ste.xtest.jetty;

import org.apache.commons.io.IOUtils;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;

/**
 *
 * @author ste
 */
public class BugFreeTestRequest {
    
    @Test
    public void setNullContentAsString() throws Exception {
        TestRequest r = new TestRequest();
        
        r.setContent(null);
        then(r.getContentLength()).isEqualTo(-1);
        then(r.getInputStream()).isNull();
    }

    @Test
    public void setContentAsString() throws Exception {
        final String TEST_CONTENT = "in quel ramo del lago di como che volge a mezzogiorno";
        
        TestRequest r = new TestRequest();
        
        r.setContent(TEST_CONTENT);
        
        then(r.getContentLength()).isEqualTo(TEST_CONTENT.length());
        then(r.getInputStream()).isNotNull();     
        then(IOUtils.toString(r.getInputStream())).isEqualTo(TEST_CONTENT);
    }
    
    @Test
    public void setEmptyContentAsString() throws Exception {
        TestRequest r = new TestRequest();
        
        r.setContent("");
        then(r.getContentLength()).isEqualTo(0);
        then(r.getInputStream()).isNotNull();     
        then(IOUtils.toString(r.getInputStream())).isEmpty();
    }

}
