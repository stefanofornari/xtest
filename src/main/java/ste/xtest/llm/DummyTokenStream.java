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

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import java.util.List;
import java.util.function.Consumer;
import static ste.lloop.Loop.on;

/**
 * This is a dummy implementation of a TokenStream. Please note it does not try
 * in any way to create tokens like a language models does. It just breaks a
 * string in words.
 */
public class DummyTokenStream implements TokenStream {

    public final String response;
    public final String[] tokens;

    private int index = 0;

    public DummyTokenStream(final String response) {
        this.response = response;
        //
        // This regex uses lookahead (?=\\W) and lookbehind (?<=\\W) to split
        // before and after non-word characters while keeping them.
        //
        this.tokens = response.split("(?<=\\W)|(?=\\W)"); // (...) makes return the separators too
    }

    private Consumer<String> partialResponse = null;
    private Consumer<ChatResponse> completeResponse = null;

    @Override
    public TokenStream onPartialResponse(final Consumer<String> c) {
        this.partialResponse = c; return this;
    }

    @Override
    public TokenStream onToolExecuted(final Consumer<ToolExecution> c) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public TokenStream onCompleteResponse(final Consumer<ChatResponse> c) {
        this.completeResponse = c; return this;
    }

    @Override
    public TokenStream onError(final Consumer<Throwable> c) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public TokenStream ignoreErrors() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void start() {
        index = 0;
        on(tokens).loop((element) ->{
            partialResponse.accept(element);
        });
        if (completeResponse != null) {
            completeResponse.accept(
                ChatResponse.builder().aiMessage(AiMessage.from(response)).build()
            );
        }
    }

    @Override
    public TokenStream onRetrieved(Consumer<List<Content>> cnsmr) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
