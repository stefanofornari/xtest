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

import java.io.IOException;
import java.net.URL;
import org.apache.commons.lang3.ObjectUtils;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.Assert.fail;
import org.junit.Test;
import ste.xtest.reflect.PrivateAccess;

/**
 *
 * @TODO: sanity check for the following methods: header, status
 */
public class BugFreeStubURLConnectionCloneable {
    
    private static final String TEST_URL_DUMMY = "http://url.com";
    
    @Test
    public void all_cloned_values() throws Exception {
        StubURLConnection C = new StubURLConnection(new URL(TEST_URL_DUMMY));
        StubURLConnection c;
        
        c = ObjectUtils.cloneIfPossible(C);
        then(c).isInstanceOf(C.getClass()).isNotSameAs(C);
        then(c.isConnected()).isFalse();
        then(c.getURL()).isNotSameAs(C.getURL()).isEqualTo(C.getURL());
        then(c.getMessage()).isNull();
        then(c.getStatus()).isEqualTo(C.getStatus());
        then(c.getContent()).isNull();
        then(c.getOutputStream()).isNotNull().isNotSameAs(C.getOutputStream());
        then(PrivateAccess.getInstanceValue(c, "exec")).isNull();
        
        then(c.getHeaders()).isInstanceOf(C.getHeaders().getClass()).isNotSameAs(C.getHeaders());
        
        C.message("a message"); c = ObjectUtils.cloneIfPossible(C);      
        then(c.getMessage()).isNotSameAs(C.getMessage()).isEqualTo(C.getMessage());
        
        C.status(401); c = ObjectUtils.cloneIfPossible(C);
        then(c.getMessage()).isNotSameAs(C.getMessage()).isEqualTo(C.getMessage());
        then(c.getStatus()).isEqualTo(C.getStatus());
        
        C.content("some content".getBytes()); c = ObjectUtils.cloneIfPossible(C);
        then(c.getMessage()).isNotSameAs(C.getMessage()).isEqualTo(C.getMessage());
        then(c.getStatus()).isEqualTo(C.getStatus());
        then(c.getContent()).isNotSameAs(C.getContent()).isEqualTo(C.getContent());
        
        C.text("some text"); c = ObjectUtils.cloneIfPossible(C);
        then(c.getMessage()).isNotSameAs(C.getMessage()).isEqualTo(C.getMessage());
        then(c.getStatus()).isEqualTo(C.getStatus());
        then(c.getContent()).isNotSameAs(C.getContent()).isEqualTo(C.getContent());
        
        C.json("{}"); c = ObjectUtils.cloneIfPossible(C);
        then(c.getMessage()).isNotSameAs(C.getMessage()).isEqualTo(C.getMessage());
        then(c.getStatus()).isEqualTo(C.getStatus());
        then(c.getContent()).isNotSameAs(C.getContent()).isEqualTo(C.getContent());
        
        C.file("src/test/somefile.txt"); c = ObjectUtils.cloneIfPossible(C);
        then(c.getMessage()).isNotSameAs(C.getMessage()).isEqualTo(C.getMessage());
        then(c.getStatus()).isEqualTo(C.getStatus());
        then(c.getContent()).isNotSameAs(C.getContent()).isEqualTo(C.getContent());
        
        C.html("<html></html>"); c = ObjectUtils.cloneIfPossible(C);
        then(c.getMessage()).isNotSameAs(C.getMessage()).isEqualTo(C.getMessage());
        then(c.getStatus()).isEqualTo(C.getStatus());
        then(c.getContent()).isNotSameAs(C.getContent()).isEqualTo(C.getContent());
        
        C.type("some/type"); c = ObjectUtils.cloneIfPossible(C);
        then(c.getMessage()).isNotSameAs(C.getMessage()).isEqualTo(C.getMessage());
        then(c.getStatus()).isEqualTo(C.getStatus());
        then(c.getContent()).isNotSameAs(C.getContent()).isEqualTo(C.getContent());
        then(c.getContentType()).isNotSameAs(C.getContentType()).isEqualTo(C.getContentType());
    }
    
    @Test
    public void all_not_cloned_values() throws Exception {
        StubURLConnection C = new StubURLConnection(new URL(TEST_URL_DUMMY));
        StubURLConnection c;
        
        StubConnectionCall call = new MyStubConnectionCall();
        C.exec(call); c = ObjectUtils.cloneIfPossible(C);
        StubConnectionCall clonedCall = (StubConnectionCall)PrivateAccess.getInstanceValue(c, "exec");
        then(clonedCall).isSameAs(call);
        clonedCall.call(c);
        then(((MyStubConnectionCall)clonedCall).value).isZero();
        
        C.exec(null);
        C.error(new IOException("an IO error")); c = ObjectUtils.cloneIfPossible(C);
        call = (StubConnectionCall)PrivateAccess.getInstanceValue(C, "exec");
        clonedCall = (StubConnectionCall)PrivateAccess.getInstanceValue(c, "exec");
        then(clonedCall).isSameAs(call);
    }
    
}
