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

import java.io.IOException;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import org.junit.Test;

/**
 *
 */
public class BugFreeDummyStreamingChatModel {

    @Test
    public void simulate_streaming() throws IOException {
        final DummyStreamingChatModel chat = new DummyStreamingChatModel();
        final DummyStreamingChatResponseHandler handler = new DummyStreamingChatResponseHandler();

        chat.chat("use mock 'hello world.txt'", handler);

        then(handler.messages).containsExactly("hello world", "hello world");
    }

    @Test
    public void simulate_model_error() {
        final DummyChatModel chat = new DummyChatModel();

        chat.error = new RuntimeException("this is an error");

        thenThrownBy(() -> chat.chat("any prompt"))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("this is an error");

        //
        // TODO: add error handling when streaming
        //
    }

    @Test
    public void interrupt_streaming_chat() throws Exception {
        final DummyStreamingChatModel chat = new DummyStreamingChatModel();
         final DummyStreamingChatResponseHandler handler = new DummyStreamingChatResponseHandler();

        chat.streamingHandle.cancel();

        thenThrownBy(() ->
            chat.chat("use mock 'multiline hello world.txt'", handler)
        ).isInstanceOf(RuntimeException.class).hasMessage("the chat has been canceled!");

        then(handler.messages).isEmpty();
    }

    @Test
    public void simulate_streaming_with_empty_lines() throws IOException {
        final DummyStreamingChatModel chat = new DummyStreamingChatModel();
        final DummyStreamingChatResponseHandler handler = new DummyStreamingChatResponseHandler();

        chat.chat("use mock 'empty lines.txt'", handler);

        then(handler.messages).containsExactly("line1", "\n", "line3", "line1\n\nline3");
    }
}
