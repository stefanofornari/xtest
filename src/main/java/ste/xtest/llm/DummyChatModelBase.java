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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ContentType;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ToolChoice;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialResponse;
import dev.langchain4j.model.chat.response.PartialResponseContext;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.PartialThinkingContext;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.chat.response.StreamingHandle;
import dev.langchain4j.model.output.TokenUsage;

import ste.lazyjava.safe.Safe;

import static ste.lloop.Loop._break_;
import static ste.lloop.Loop.on;

/**
 * {@code DummyChatModelBase} is an abstract base class for mock chat models used in testing
 * Langchain4j-based integrations. It simulates realistic chat model behaviors—including response
 * generation, tool execution requests, thinking processes, token usage tracking, error injection,
 * and streaming—controlled via instructions embedded in messages or explicit property settings.
 *
 * <h2>Key Features and Behavior:</h2>
 * <ul>
 *   <li><strong>Mock Response Loading:</strong> Reads response text from classpath resources located
 *       under {@code /llm/mocks/}. The mock file can be explicitly requested in a message using the
 *       phrase {@code use mock <filename>} (supports both quoted and unquoted filenames). If no file
 *       is specified, it falls back to {@code default.txt}. If the requested file is missing, it
 *       automatically loads {@code error.txt} and replaces any {@code {error}} placeholder with a descriptive
 *       not-found message.
 *   <li><strong>Instruction Detection:</strong> Automatically scans conversation messages for instructions
 *       ({@code "use mock"} or {@code "execute tool"}). It inspects the latest user message, falls back
 *       to the preceding AI message if applicable, or checks the initial system message.
 *   <li><strong>Tool Execution Simulation:</strong> When {@link #toolChoice} is set to {@code AUTO} or
 *       {@code REQUIRED}, the model checks if any provided tool specification matches an {@code execute tool <toolname>}
 *       instruction.
 *       <ul>
 *          <li>You can pass arguments to the tool by appending <strong>{@code with arguments: <text>}</strong>
 *              after the tool name (e.g., {@code execute tool myTool with arguments: hello world}).
 *              The text up to the next newline is extracted and packaged as JSON: {@code {"prompt": "<text>"}}.
 *       </ul>
 *       On the first turn, it returns an {@link AiMessage} containing a {@link ToolExecutionRequest}
 *       and toggles the internal {@link #toolExecuted} flag. On the subsequent turn, it resets the flag and returns
 *       the regular mock response.
 *   <li><strong>Error Injection:</strong> Setting the {@link #error} field to a {@link RuntimeException}
 *       causes {@link #computeResponse(ChatRequest)} to immediately throw that exception.
 *   <li><strong>Thinking/Reasoning Simulation:</strong> Setting the {@link #thinking} field causes the model
 *       to simulate reasoning steps, including emitting partial thinking events during streaming or embedding
 *       thinking content in the resulting {@link AiMessage}.
 *   <li><strong>Token Usage:</strong> Supports stubbing response token usage via the {@link #tokenUsage} field,
 *       which is populated into the resulting {@link ChatResponse} metadata.
 *   <li><strong>Streaming Support:</strong> Implements {@link #doChat(ChatRequest, StreamingChatResponseHandler)}
 *       to stream response and thinking content line-by-line, supporting cancellation checks via
 *       {@link DummyStreamingHandle}.
 * </ul>
 */
public abstract class DummyChatModelBase implements Safe {

    protected final Logger LOG = Logger.getLogger(getClass().getCanonicalName());

    private static final String DEFAULT_MOCK_FILE = "/llm/mocks/default.txt";
    private static final String ERROR_MOCK_FILE = "/llm/mocks/error.txt";
    private static final Pattern MOCK_INSTRUCTION_PATTERN =
        Pattern.compile("use mock\\s+(?:'([^']+)'|(\\S+))", Pattern.CASE_INSENSITIVE);

    private final List<ChatModelListener> listeners;

    public final DummyStreamingHandle streamingHandle = new DummyStreamingHandle();

    public ToolChoice toolChoice = ToolChoice.AUTO;

    public RuntimeException error = null;

    public String lastToolExecutionResult = null;

    public boolean toolExecuted = false;

    public String thinking = null;

    public TokenUsage tokenUsage = null;

    protected DummyChatModelBase() {
        this.listeners = new ArrayList<>();
    }

    public void addListener(final ChatModelListener listener) {
        this.listeners.add(listener);
    }

    public List<ChatModelListener> listeners() {
        return listeners;
    }

    protected ChatResponse computeResponse(final ChatRequest chatRequest) {
        LOG.info(() -> "> " + String.valueOf(chatRequest));

        if (error != null) {
            LOG.info(() -> getClass().getSimpleName() + " instructed to raise " + error);

            throw error;
        }

        final String mockInstruction = messageWithInstruction(chatRequest.messages());

        AiMessage responseMessage = null;

        if ((toolChoice == ToolChoice.REQUIRED) || (toolChoice == ToolChoice.AUTO)) {
            if (!toolExecuted) {
                final String tool = on(chatRequest.toolSpecifications()).loop((specification) -> {
                    final String name = specification.name();
                    final String regex = "(?i)execute tool " + Pattern.quote(name) + "(?![a-zA-Z])";

                    if (Pattern.compile(regex).matcher(mockInstruction).find()) {
                        _break_(name);
                    }
                });

                if (tool != null) {
                    LOG.info(() -> "Requesting to execute tool %s".formatted(tool));

                    toolExecuted = true;
                    String arguments = "{}";

                    String argsText = "";
                    int argsIndex = mockInstruction.toLowerCase().indexOf("with arguments:");
                    if (argsIndex >= 0) {
                        argsText = mockInstruction.substring(argsIndex + "with arguments:".length()).trim();
                        int newlineIndex = argsText.indexOf('\n');
                        if (newlineIndex >= 0) {
                            argsText = argsText.substring(0, newlineIndex).trim();
                        }
                    }
                    if (!argsText.isEmpty()) {
                        arguments = "{\"prompt\": \"" + escapeForJson(argsText) + "\"}";
                    }

                    ToolExecutionRequest toolRequest = ToolExecutionRequest.builder()
                        .id("XKSdkL2PU")
                        .name(tool)
                        .arguments(arguments)
                        .build();

                    responseMessage = AiMessage.from(toolRequest);
                }
            } else {
                toolExecuted = false;

                final String mockInstructionAfterTool = messageWithInstruction(chatRequest.messages());
                 final Matcher matcher = MOCK_INSTRUCTION_PATTERN.matcher(mockInstructionAfterTool);
                 String mockFile = null;

                 while(matcher.find()) {
                     mockFile = matcher.group(1);
                     if (mockFile == null) {
                         mockFile = matcher.group(2);
                     }
                 }

                 String resourcePath;
                 if (mockFile != null) {
                     resourcePath = "/llm/mocks/" + mockFile;
                 } else {
                     resourcePath = DEFAULT_MOCK_FILE;
                 }
                 String errorMessage = "";
                 InputStream input = getClass().getResourceAsStream(resourcePath);
                 if (input == null) {
                     errorMessage = "Mock file '%s' not found.".formatted(resourcePath);
                     input = getClass().getResourceAsStream(ERROR_MOCK_FILE);
                 }
                 String mockContent;
                 if (input == null) {
                     mockContent = "Error reading mock file: Resource not found: " + resourcePath;
                 } else {
                     try {
                         mockContent = new String(input.readAllBytes(), StandardCharsets.UTF_8);
                     } catch (IOException x) {
                         mockContent = "Error reading mock file: " + x.getMessage();
                     }
                 }
                 mockContent = mockContent.replaceAll("\\{error}", errorMessage);

                responseMessage = AiMessage.from(mockContent);
            }
        }

        if (responseMessage == null) {
             final Matcher matcher = MOCK_INSTRUCTION_PATTERN.matcher(mockInstruction);
             String mockFile = null;

             while(matcher.find()) {
                 mockFile = matcher.group(1);
                 if (mockFile == null) {
                     mockFile = matcher.group(2);
                 }
             }

             String resourcePath;
             if (mockFile != null) {
                 resourcePath = "/llm/mocks/" + mockFile;
             } else {
                 resourcePath = DEFAULT_MOCK_FILE;
             }
             String errorMessage = "";
             InputStream input = getClass().getResourceAsStream(resourcePath);
             if (input == null) {
                 errorMessage = "Mock file '%s' not found.".formatted(resourcePath);
                 input = getClass().getResourceAsStream(ERROR_MOCK_FILE);
             }
             String mockContent;
             if (input == null) {
                 mockContent = "Error reading mock file: Resource not found: " + resourcePath;
             } else {
                 try {
                     mockContent = new String(input.readAllBytes(), StandardCharsets.UTF_8);
                 } catch (IOException x) {
                     mockContent = "Error reading mock file: " + x.getMessage();
                 }
             }
             mockContent = mockContent.replaceAll("\\{error}", errorMessage);

            responseMessage = AiMessage.from(mockContent);
        }

        final AiMessage[] finalResponseMessage = new AiMessage[] { responseMessage };

        ifNotNull(thinking,
            () -> {
                final String[] captured = new String[] { finalResponseMessage[0].text() };
                ifNotNull(captured[0],
                    () -> finalResponseMessage[0] = AiMessage.builder()
                            .thinking(thinking)
                            .text(captured[0])
                            .build()
                );
            }
        );

        responseMessage = finalResponseMessage[0];

        ChatResponse chatResponse =
            ChatResponse.builder()
                .aiMessage(responseMessage)
                .metadata(dev.langchain4j.model.chat.response.ChatResponseMetadata.builder()
                    .tokenUsage(tokenUsage)
                    .build())
                .build();

        LOG.info(() -> "< " + String.valueOf(chatResponse));

        return chatResponse;
    }

    public void doChat(final ChatRequest chatRequest, final StreamingChatResponseHandler handler) {
        LOG.info(() -> "> " + chatRequest + ", " + handler);

        final ChatResponse response = computeResponse(chatRequest);
        final String answer = response.aiMessage().text();

        ifNull(thinking,
            () -> {},
            () -> {
                for (String t : thinking.trim().split("\n")) {
                    String trimmed = t.trim();
                    if (!trimmed.isEmpty()) {
                        handler.onPartialThinking(
                            new PartialThinking(trimmed),
                            new PartialThinkingContext(streamingHandle)
                        );
                    }
                }
            }
        );

        if (answer != null) {
            for(String m: answer.trim().split("\n")) {
                String trimmed = m.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                if (streamingHandle.isCancelled()) {
                    final String msg = "the chat has been canceled!";
                    LOG.info(msg);
                    throw new RuntimeException(msg);
                }
                handler.onPartialResponse(
                    new PartialResponse(trimmed),
                    new PartialResponseContext(streamingHandle)
                );
            }
        }

        handler.onCompleteResponse(response);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    // --------------------------------------------------------- private methods

    private String messageWithInstruction(final List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return "";
        }

        final int size = messages.size();
        if (messages.get(size-1) instanceof UserMessage msg) {
            if (msg.hasSingleText() && doesContainIstruction(msg.singleText())) {
                return msg.singleText();
            } else {
                final String contentText = on(msg.contents()).loop((content) -> {
                   if (content.type() == ContentType.TEXT) {
                       final String text = content.toString();
                       if (doesContainIstruction(text)) {
                            _break_(text);
                       }
                   }
                });
                if (contentText != null) {
                    return contentText;
                }
            }
        }

        if ((size > 2) && (messages.get(size-2) instanceof AiMessage msg)) {
            final String text = msg.text();
            if (doesContainIstruction(text)) {
                return text;
            }
        }

        if (messages.get(0) instanceof SystemMessage msg) {
            final String text = msg.text();
            if (doesContainIstruction(text)) {
                return text;
            }
        }

        return "";
    }

    private boolean doesContainIstruction(final String text) {
        if (text == null) {
            return false;
        }
        final String lowerText = text.toLowerCase();
        return lowerText.toLowerCase().contains("use mock")
            || lowerText.toLowerCase().contains("execute tool");
    }

    private static String escapeForJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // ---------------------------------------------------- DummyStreamingHandle

    public static class DummyStreamingHandle implements StreamingHandle {
        boolean canceled = false;

        @Override
        public void cancel() {
            canceled = true;
        }

        @Override
        public boolean isCancelled() {
            return canceled;
        }
    }
}