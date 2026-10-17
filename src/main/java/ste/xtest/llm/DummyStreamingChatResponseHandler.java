/*
 * xTest
 * Copyright (C) 2026 Stefano Fornari
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

package ste.xtest.llm;

import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class DummyStreamingChatResponseHandler implements StreamingChatResponseHandler {
    public final List<String> messages = new ArrayList();

    @Override
    public void onPartialResponse(final String partialResponse) {
        messages.add(partialResponse);
    }

    @Override
    public void onCompleteResponse(final ChatResponse res) {
        // .trim() to make it platform independent (i.e. \n vs \r\n)
        messages.add(res.aiMessage().text().trim());
    }

    @Override
    public void onError(Throwable thrwbl) {}
}
