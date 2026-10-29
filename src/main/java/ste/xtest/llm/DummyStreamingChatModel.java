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
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 */

package ste.xtest.llm;

import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialResponse;
import dev.langchain4j.model.chat.response.PartialResponseContext;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import java.util.Optional;

public class DummyStreamingChatModel extends DummyChatModelBase
implements dev.langchain4j.model.chat.StreamingChatModel {

    @Override
    public void doChat(final ChatRequest chatRequest, final StreamingChatResponseHandler handler) {
        LOG.info(() -> "> " + chatRequest + ", " + handler);

        final ChatResponse response = super.computeResponse(chatRequest);
        final Optional<String> answer = Optional.ofNullable(response.aiMessage().text());

        answer.ifPresent((a) -> {
            for (String m : a.split("\n")) {
                if (m.isEmpty()) {
                    m = "\n";
                }
                if (streamingHandle.isCancelled()) {
                    final String msg = "the chat has been canceled!";
                    LOG.info(msg);
                    throw new RuntimeException(msg);
                }
                handler.onPartialResponse(
                    new PartialResponse(m),
                    new PartialResponseContext(streamingHandle)
                );
            }
        });
        handler.onCompleteResponse(response);
    }
}