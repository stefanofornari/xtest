/*
 * xTest
 * Copyright (C) 2018 Stefano Fornari
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

package ste.xtest.aws;

import com.amazonaws.client.AwsSyncClientParams;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.AuthorizationErrorException;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

/**
 *
 */
public class AmazonSNSClientStub extends AmazonSNSClient {
    
    private boolean active = false;
    private boolean wrongCredentials = false;
    private String[] ids = null;
    private int sentMessageCounter = 0;
    
    
    public AmazonSNSClientStub(AwsSyncClientParams params) {
        super();
    }
    
    @Override
    public PublishResult publish(PublishRequest request) {
        if (!active) {
            PublishResult result = super.publish(request);
            System.out.println(result.toString());
            return result;
        }
        
        checkCredentials();
        
        PublishResult result = new PublishResult();
        if (ids != null) {
            result.setMessageId(ids[sentMessageCounter++]);
        }
        
        return result;
    }
    
    public AmazonSNSClientStub withWrongCredentials() {
        active(); wrongCredentials = true; return this;
    }
    
    public AmazonSNSClientStub active() {
        active = true; return this;
    }
    
    public AmazonSNSClientStub withMessageIds(String... ids) {
        active(); this.ids = ids; sentMessageCounter = 0; return this;
    }
    
    // --------------------------------------------------------- private methods
    
    private void checkCredentials() throws AuthorizationErrorException {
        if (wrongCredentials) {
            AuthorizationErrorException x = new AuthorizationErrorException("The security token included in the request is invalid.");
            x.setStatusCode(403);
            x.setErrorCode("AuthorizationError");

            throw x;
        }
    }
}
