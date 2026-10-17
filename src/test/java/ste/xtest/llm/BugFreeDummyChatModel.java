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

import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ToolChoice;
import dev.langchain4j.model.chat.response.ChatResponse;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import org.junit.Test;

/**
 *
 */
public class BugFreeDummyChatModel {

    @Test
    public void doChat_returns_provided_message() {
        final DummyChatModel chat = new DummyChatModel();

        ChatRequest chatRequest = ChatRequest.builder().messages(
            UserMessage.from("use mock 'hello world.txt'")
        ).build();

        then(chat.doChat(chatRequest).aiMessage().text().trim()).isEqualTo("hello world");
    }

    @Test
    public void doChat_returns_the_error_page_if_the_mock_does_not_exist() {
        final DummyChatModel chat = new DummyChatModel();

        ChatRequest chatRequest = ChatRequest.builder().messages(
            UserMessage.from("use mock none.txt")
        ).build();

        then(chat.doChat(chatRequest).aiMessage().text().trim())
            .startsWith(
                "Oops! Mock file '" +
                Paths.get("src/test/resources/mocks/none.txt").toAbsolutePath().toString().replace('\\', '/') +
                "' not found."
            );
    }

    @Test
    public void doChat_returns_the_default_page_if_no_mock_is_given() {
        final DummyChatModel chat = new DummyChatModel();

        ChatRequest chatRequest = ChatRequest.builder().messages(
            UserMessage.from("Hello!")
        ).build();

        then(chat.doChat(chatRequest).aiMessage().text().trim())
            .startsWith("To use a mock, send a prompt containing the following instruction:");
    }

    @Test
    public void doChat_picks_the_mock_from_system_message() {
        final DummyChatModel chat = new DummyChatModel();

        ChatRequest chatRequest = ChatRequest.builder().messages(
            SystemMessage.from("use mock 'hello world.txt'"),
            UserMessage.from("Hello!")
        ).build();

        then(chat.doChat(chatRequest).aiMessage().text().trim())
            .isEqualToIgnoringNewLines("hello world");
    }

    @Test
    public void doChat_picks_the_mock_from_user_message() {
        final DummyChatModel chat = new DummyChatModel();

        ChatRequest chatRequest = ChatRequest.builder().messages(
            SystemMessage.from("System message"),
            UserMessage.from("use mock 'hello world.txt'")
        ).build();

        then(chat.doChat(chatRequest).aiMessage().text().trim())
            .isEqualToIgnoringNewLines("hello world");
    }

    @Test
    public void doChat_picks_the_mock_from_response_message() {
        final DummyChatModel chat = new DummyChatModel();

        ChatRequest chatRequest = ChatRequest.builder().messages(
            UserMessage.from("this is the first message"),
            AiMessage.from("this is the response: use mock 'hello world.txt'"),
            UserMessage.from("this is the last prompt")
        ).build();

        then(chat.doChat(chatRequest).aiMessage().text().trim())
            .isEqualToIgnoringNewLines("hello world");
    }

    @Test
    public void doChat_picks_the_mock_from_priority() {
        final DummyChatModel chat = new DummyChatModel();

        //
        // If in both system and user messages, the latter wins
        //
        ChatRequest chatRequest = ChatRequest.builder().messages(
            SystemMessage.from("use mock 'hello world.txt'"),
            UserMessage.from("use mock error.txt")
        ).build();

        then(chat.doChat(chatRequest).aiMessage().text().trim())
            .startsWith("Oops!");

        //
        // Always pick only the last user message
        //
        chatRequest = ChatRequest.builder().messages(
            UserMessage.from("use mock error.txt!"),
            UserMessage.from("Hello!"),
            UserMessage.from("use mock 'hello world.txt'")
        ).build();

        then(chat.doChat(chatRequest).aiMessage().text().trim())
            .isEqualToIgnoringNewLines("hello world");

        //
        // Always pick only the last response if not in last user message
        //
        chatRequest = ChatRequest.builder().messages(
            UserMessage.from("use 'multi message.1.txt'"),
            UserMessage.from("Hello!"),
            AiMessage.from("this is the response: use mock 'multi message.2.txt'"),
            UserMessage.from("use mock 'hello world.txt'"),
            AiMessage.from("this is the response: use mock 'multi message.3.txt'"),
            UserMessage.from("last message")
        ).build();

        then(chat.doChat(chatRequest).aiMessage().text().trim())
            .isEqualToIgnoringNewLines("This is the third message");
    }

    @Test
    public void listeners_registers_the_provided_listeners() {
        // Given
        final DummyChatModel chat = new DummyChatModel();
        final ChatModelListener listener1 = new ChatModelListener() {}; // Concrete listener 1
        final ChatModelListener listener2 = new ChatModelListener() {}; // Concrete listener 2

        // When 1: Add the first listener
        chat.addListener(listener1);

        // Then 1: Check if the first listener is registered
        then(chat.listeners()).containsExactly(listener1);

        // When 2: Add the second listener
        chat.addListener(listener2);

        // Then 2: Check if both listeners are registered
        then(chat.listeners()).containsExactly(listener1, listener2);
    }

    @Test
    public void chat_string_invokes_listeners() {
        // Given
        final DummyChatModel chat = new DummyChatModel();

        DummyChatModelListener testListener = new DummyChatModelListener();
        chat.addListener(testListener);

        String userMessageString = "use mock 'hello world.txt'";

        // When
        chat.chat(userMessageString);

        // Then
        thenReceivedMessageIs(testListener, new UserMessage(userMessageString));
    }

    @Test
    public void chat_array_invokes_listeners() {
        // Given
        final DummyChatModel chat = new DummyChatModel();

        DummyChatModelListener testListener = new DummyChatModelListener();
        chat.addListener(testListener);

        ChatMessage[] messagesArray = new ChatMessage[]{new UserMessage("use mock 'hello world.txt'")};

        // When
        chat.chat(messagesArray);

        // Then
        thenReceivedMessageIs(testListener, (UserMessage) messagesArray[0]);
    }

    @Test
    public void tools_support() throws IOException {
        DummyChatModel chat = new DummyChatModel();

        //
        // With known tool and ToolChoice.REQUIRED
        //
        // Given
        chat.toolChoice = ToolChoice.REQUIRED;

        ChatRequest request = ChatRequest.builder()
            .messages(List.of(
                UserMessage.from("execute tool dummyTool")
            ))
            .toolSpecifications(
                ToolSpecifications.toolSpecificationsFrom(new DummyTool())
            )
            .build();

        // When
        ChatResponse response = chat.chat(request);

        // Then
        then(response.aiMessage().hasToolExecutionRequests()).isTrue();

        //
        // With known tool and ToolChoice.AUTO
        //
        chat = new DummyChatModel();

        // Given
        chat.toolChoice = ToolChoice.AUTO;

        request = ChatRequest.builder()
            .messages(List.of(
                UserMessage.from("execute tool dummyTool")
            ))
            .toolSpecifications(
                ToolSpecifications.toolSpecificationsFrom(new DummyTool())
            )
            .build();

        // When
        response = chat.chat(request);

        // Then
        then(response.aiMessage().hasToolExecutionRequests()).isTrue();

        //
        // With unknown tool
        //
        chat = new DummyChatModel();
        // Given
        request = ChatRequest.builder()
            .messages(List.of(
                UserMessage.from("execute tool fileRead")
            ))
            .toolSpecifications(
                ToolSpecifications.toolSpecificationsFrom(new DummyTool())
            )
            .build();

        // When
        response = chat.chat(request);

        // Then
        then(response.aiMessage().hasToolExecutionRequests()).isFalse();
    }

    @Test
    public void no_tools_support() throws IOException {
        final DummyChatModel chat = new DummyChatModel();

        // Given
        chat.toolChoice = ToolChoice.NONE;

        ChatRequest request = ChatRequest.builder()
            .messages(List.of(
                UserMessage.from("execute tool dummyTool")
            ))
            .toolSpecifications(
                ToolSpecifications.toolSpecificationsFrom(new DummyTool())
            )
            .build();

        // When
        ChatResponse response = chat.chat(request);

        // Then
        then(response.aiMessage().hasToolExecutionRequests()).isFalse();
    }

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

    // --------------------------------------------------------- private methods

    private void thenReceivedMessageIs(DummyChatModelListener testListener, UserMessage expectedUserMessage) {
        then(testListener.lastRequestContext).isPresent();
        then(testListener.lastRequestContext.get().chatRequest().messages()).containsExactly(expectedUserMessage);

        then(testListener.lastResponseContext).isPresent();
        then(testListener.lastResponseContext.get().chatResponse().aiMessage()).isInstanceOf(AiMessage.class);
        then(testListener.lastResponseContext.get().chatResponse().aiMessage().text().trim()).isEqualTo("hello world"); // Default mock response
    }

}