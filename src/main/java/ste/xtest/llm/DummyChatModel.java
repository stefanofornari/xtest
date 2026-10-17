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

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import java.util.Set;

public class DummyChatModel extends DummyChatModelBase implements ChatModel {

    public DummyChatModel() {
        super();
    }

    @Override
    public ChatResponse doChat(final ChatRequest chatRequest) {
        return computeResponse(chatRequest);
    }

    @Override
    public ChatResponse chat(final ChatRequest chatRequest) {
        final ChatResponse chatResponse = ChatModel.super.chat(chatRequest);

        return chatResponse;
    }

    @Override
    public String chat(final String userMessage) {
        final String chatResponse = ChatModel.super.chat(userMessage);

        return chatResponse;
    }

    @Override
    public ChatResponse chat(final ChatMessage[] messages) {
        final ChatResponse chatResponse = ChatModel.super.chat(messages);

        return chatResponse;
    }

    @Override
    public Set<Capability> supportedCapabilities() {
        final Set capabilities = ChatModel.super.supportedCapabilities();

        return capabilities;
    }

    @Override
    public ChatRequestParameters defaultRequestParameters() {
        final ChatRequestParameters params = ChatModel.super.defaultRequestParameters();

        return params;
    }

    @Override
    public ModelProvider provider() {
        final ModelProvider provider = ChatModel.super.provider();

        return provider;
    }
}
