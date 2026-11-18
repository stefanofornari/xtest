# `ste.xtest.llm.DummyChatModel`

`DummyChatModel` is a test-double implementation of LangChain4j's `ChatModel`
interface. It lives in the `ste.xtest.llm` package of the `xtest` testing
utility library. Its purpose is to let tests exercise code that depends on a
`ChatModel` without calling a real LLM: responses are loaded from text mock
files on the classpath, tool calls can be simulated, errors and thinking can be
injected, and streaming can be exercised.

`DummyChatModel` extends `DummyChatModelBase` and only wires the non-streaming
`ChatModel` contract. The companion `DummyStreamingChatModel` (same base)
implements `StreamingChatModel`. The base class holds all shared behaviour and
state.

---

## 1. Coordinates

- **Package:** `ste.xtest.llm`
- **Class:** `ste.xtest.llm.DummyChatModel`
- **Implements:** `dev.langchain4j.model.chat.ChatModel`
- **Extends:** `ste.xtest.llm.DummyChatModelBase`
- **Constructor:** `public DummyChatModel()` (no arguments)

Maven dependency: `ste.xtest:xtest` (see project `pom.xml`).

---

## 2. Quick start

```java
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import ste.xtest.llm.DummyChatModel;

DummyChatModel chat = new DummyChatModel();

ChatRequest request = ChatRequest.builder()
    .messages(UserMessage.from("use mock 'hello world.txt'"))
    .build();

ChatResponse response = chat.chat(request);
System.out.println(response.aiMessage().text()); // "hello world"
```

The mock file `hello world.txt` must be on the classpath under
`/llm/mocks/` (in `xtest` itself these live in
`src/main/resources/llm/mocks/`).

---

## 3. How a response is chosen

`DummyChatModel` never calls a network. On every call to `chat(...)` /
`doChat(...)` it runs `computeResponse(ChatRequest)`:

1. If `error != null`, throw that `RuntimeException` immediately.
2. Find an **instruction** in the conversation messages.
3. If tool simulation is enabled and the instruction asks to execute a tool,
   return an `AiMessage` containing a `ToolExecutionRequest` on the first turn.
4. Otherwise load a mock text file and return its contents as the `AiMessage`.

### 3.1 Instruction detection

An instruction is a message whose text contains (case-insensitive)
`use mock` or `execute tool`. `messageWithInstruction` looks in this order:

1. The **last** `UserMessage` (single text, or first text content).
2. If that has no instruction, the `AiMessage` immediately before it
   (when the conversation has more than 2 messages).
3. Otherwise the first `SystemMessage`.

Only one instruction string is used. If none is found, the empty string is
used and the default mock file is loaded.

### 3.2 Mock file selection

Pattern: `use mock <file>` where `<file>` is either:

- a single-quoted name: `use mock 'my file.txt'`
- an unquoted token: `use mock my_file.txt`

The last match in the instruction wins. The name is resolved against the
classpath root `/llm/mocks/` (i.e. resource path `/llm/mocks/<file>`).

- If no `use mock` instruction is present, `/llm/mocks/default.txt` is used.
- If the requested file does not exist, `/llm/mocks/error.txt` is used and
  the `{error}` placeholder in it is replaced with
  `Mock file '<path>' not found.`.

Mock file content is returned verbatim (after `{error}` substitution) as the
text of the `AiMessage`.

### 3.3 Supported instruction examples

```
use mock 'hello world.txt'
use mock error.txt
execute tool dummyTool
execute tool dummyTool with arguments: hello world
execute tool dummyToolWithArgs with arguments: {"arg1": "hello", "arg2": ["a", "b"]}
```

---

## 4. Public mutable state

`DummyChatModelBase` exposes these public fields. Tests set them directly.
Listeners are kept in a private list and registered with `addListener`.

| Field | Type | Default | Meaning |
|---|---|---|---|
| `toolChoice` | `ToolChoice` | `ToolChoice.AUTO` | `AUTO`/`REQUIRED` enable tool simulation; `NONE` disables it. |
| `error` | `RuntimeException` | `null` | If set, the next call throws it. |
| `thinking` | `String` | `null` | Injected reasoning text. |
| `tokenUsage` | `TokenUsage` | `null` | Stubbed token usage put into response metadata. |
| `toolExecuted` | `boolean` | `false` | Internal toggle for the two-turn tool flow. |
| `lastToolExecutionResult` | `String` | `null` | Scratch field for callers (not read by the model). |
| `streamingHandle` | `DummyStreamingHandle` | new | Cancellation handle used during streaming. |
| listeners | private `List<ChatModelListener>` | empty | Not a public field; use `addListener` and `listeners()`. |

Accessors:

```java
void addListener(ChatModelListener listener)
List<ChatModelListener> listeners()
```

---

## 5. Tool execution simulation

Enabled only when `toolChoice` is `AUTO` or `REQUIRED`.

On a call whose instruction contains `execute tool <name>` (word boundary
after the name) and where `<name>` matches one of the `toolSpecifications` in
the `ChatRequest`:

- First turn (`toolExecuted == false`): sets `toolExecuted = true` and returns
  an `AiMessage` with one `ToolExecutionRequest`:
  - `id` is the fixed string `"XKSdkL2PU"`,
  - `name` is the matched tool name,
  - `arguments` is `"{}"` by default. When the instruction contains
    `with arguments: <text>` (text up to the next newline) it is:
    - `<text>` itself, if `<text>` is a JSON object (e.g.
      `{"city": "London", "unit": "CELSIUS"}`), so that tools with several
      named parameters can be driven;
    - `{"prompt": "<text>"}` (JSON-escaped) for any other text.
- Second turn (`toolExecuted == true`): resets `toolExecuted = false` and
  returns the normal mock-file response for the (possibly updated) instruction.

If `toolChoice == NONE`, or no tool spec matches, no tool request is produced
and the mock file path is used.

Arguments are extracted like this:

```java
int argsIndex = mockInstruction.toLowerCase().indexOf("with arguments:");
// text after the colon, trimmed, cut at the first newline
```

The text is passed through as it is only if it parses as a single JSON object
(Jackson, trailing text not allowed). Anything else - plain text, `{not json}`,
a JSON array, an object followed by other text - is wrapped as the `prompt`
argument. A JSON object must fit on one line, because the text is cut at the
first newline. (JSON object arguments need xtest 3.14.1 or later.)

Only the first matching tool specification (in iteration order) is used.

The tool-name match is case-insensitive. The implementation requires that the
first character after the configured name is not an ASCII letter; digits and
underscores therefore do not produce a match boundary.

### Example: two-turn tool flow

```java
DummyChatModel chat = new DummyChatModel();
chat.toolChoice = ToolChoice.REQUIRED;

// Turn 1: model asks to execute the tool
ChatRequest req1 = ChatRequest.builder()
    .messages(UserMessage.from("execute tool dummyTool"))
    .toolSpecifications(ToolSpecifications.toolSpecificationsFrom(new DummyTool()))
    .build();
ChatResponse r1 = chat.chat(req1);
assert r1.aiMessage().hasToolExecutionRequests();

// Turn 2: feed tool result back; model returns the mock response
ChatRequest req2 = ChatRequest.builder()
    .messages(List.of(
        UserMessage.from("execute tool dummyTool"),
        AiMessage.from(r1.aiMessage().toolExecutionRequests().get(0)), // echo
        UserMessage.from("use mock 'hello world.txt'")
    ))
    .toolSpecifications(ToolSpecifications.toolSpecificationsFrom(new DummyTool()))
    .build();
ChatResponse r2 = chat.chat(req2);
System.out.println(r2.aiMessage().text()); // mock content
```

---

## 6. Error injection

```java
DummyChatModel chat = new DummyChatModel();
chat.error = new RuntimeException("boom");
// any chat(...) call throws "boom"
```

The exception is thrown before any mock loading or tool logic. Streaming
variants do not currently propagate this through `onError` (see limitations).

---

## 7. Thinking / reasoning injection

```java
DummyChatModel chat = new DummyChatModel();
chat.thinking = "step one\nstep two";
```

- Non-streaming: the resulting `AiMessage` has `.thinking(thinking)` and the
  mock text as `.text(...)` on mock-response turns. A tool-execution
  `AiMessage` has no text and is not given the thinking value.
- Streaming (`doChat(request, handler)`): emits one
  `onPartialThinking(PartialThinking, ...)` per non-empty trimmed line of
  `thinking`, then the response lines. This partial-thinking behavior belongs
  to the base `DummyChatModelBase.doChat` method; `DummyStreamingChatModel`
  does not emit partial thinking.

---

## 8. Token usage

```java
chat.tokenUsage = new TokenUsage(10, 20, 30); // input, output, total (as available)
ChatResponse r = chat.chat(request);
r.metadata().tokenUsage(); // same instance
```

---

## 9. Streaming

### 9.1 `DummyChatModel` (base streaming method)

`DummyChatModelBase.doChat(ChatRequest, StreamingChatResponseHandler)` computes
the response, then:

1. Emits thinking lines (if `thinking` set).
2. For each non-empty trimmed line of the answer, checks
   `streamingHandle.isCancelled()`; if cancelled throws
   `RuntimeException("the chat has been canceled!")`.
   Otherwise calls `handler.onPartialResponse(PartialResponse, ...)`.
3. Calls `handler.onCompleteResponse(response)`.

### 9.2 `DummyStreamingChatModel`

A separate class implementing `StreamingChatModel` whose `doChat` overrides the
base and streams every line produced by splitting the answer on `"\n"` (empty
lines become `"\n"`), checks cancellation, then completes. It does not emit
partial thinking. Use this when the API under test requires a
`StreamingChatModel`.

```java
DummyStreamingChatModel chat = new DummyStreamingChatModel();
DummyStreamingChatResponseHandler handler = new DummyStreamingChatResponseHandler();
chat.chat("use mock 'hello world.txt'", handler);
// handler.messages contains streamed chunks + final trimmed text
```

### 9.3 Cancellation

```java
DummyStreamingChatModel chat = new DummyStreamingChatModel();
chat.streamingHandle.cancel(); // DummyStreamingHandle
// next chat(...) throws RuntimeException("the chat has been canceled!")
```

`DummyStreamingHandle` implements `StreamingHandle` with `cancel()` and
`isCancelled()`.

Cancellation is a permanent state on the handle; `cancel()` does not reset
until a new `DummyStreamingHandle`/model instance is created.

---

## 10. Listeners

`DummyChatModel` inherits the LangChain4j `ChatModel` default behaviour for
`chat(...)` overloads, which notifies registered listeners. Tests register a
`DummyChatModelListener` to capture the last request/response contexts:

```java
DummyChatModel chat = new DummyChatModel();
DummyChatModelListener listener = new DummyChatModelListener();
chat.addListener(listener);

chat.chat("use mock 'hello world.txt'");

assert listener.lastRequestContext.isPresent();
assert listener.lastResponseContext.isPresent();
```

`DummyChatModelListener` stores
`Optional<ChatModelRequestContext> lastRequestContext` and
`Optional<ChatModelResponseContext> lastResponseContext`.

---

## 11. ChatModel interface methods

`DummyChatModel` overrides and delegates to `ChatModel.super`:

- `ChatResponse doChat(ChatRequest)`
- `ChatResponse chat(ChatRequest)`
- `String chat(String userMessage)`
- `ChatResponse chat(ChatMessage... messages)`
- `Set<Capability> supportedCapabilities()`
- `ChatRequestParameters defaultRequestParameters()`
- `ModelProvider provider()`

So callers may use any convenience overload.

---

## 12. Related classes in the same package

| Class | Role |
|---|---|
| `DummyChatModelBase` | Shared mock logic and state. |
| `DummyStreamingChatModel` | `StreamingChatModel` variant. |
| `DummyStreamingChatResponseHandler` | Test handler collecting partial responses. |
| `DummyChatModelListener` | Listener capturing request/response contexts. |
| `DummyStreamingHandle` (nested) | Cancellation handle. |
| `DummyTool` | Example `@Tool`-annotated tool for tool tests. |
| `DummyAgent` | Example AI service interface. |
| `DummyTokenStream` | Dummy `TokenStream` for agent tests. |
| `AIMockServer` | HTTP mock server (separate concern). |

---

## 13. Limitations and caveats

- **Stateful tool toggle:** `toolExecuted` is an instance field. Reuse the same
  instance across tests without resetting can cause off-by-one tool behaviour.
  Create a fresh `DummyChatModel` per test.
- **Tool toggle and `ToolChoice.NONE`:** once `toolExecuted` is set by a tool
  turn, switching `toolChoice` to `NONE` skips the tool-toggle branch and does
  not reset the flag until a later call with `AUTO` or `REQUIRED`.
- **Single tool per turn:** only the first matching tool specification is used.
- **Argument format:** tool arguments are either a JSON object given in the
  instruction (passed as it is), or `{"prompt": "<text>"}` for any other text.
  A JSON object cannot span more than one line.
- **JSON escaping:** when text is wrapped as `prompt`, only `\`, `"`, newline,
  carriage return and tab are escaped. A JSON object is not touched.
- **Empty-line handling differs:** the base streaming method skips empty lines;
  `DummyStreamingChatModel` turns them into `"\n"`.
- **No thread safety:** fields are plain mutable fields; not safe for
  concurrent use.
- **Resource location:** mocks must be on the classpath at `/llm/mocks/`.
  The default `default.txt` message mentions `src/test/resources/mocks`, but
  the implementation resolves from the classpath root.
- **`error` in streaming:** `computeResponse` throws, but streaming handlers'
  `onError` is not invoked by the base implementation; callers see the
  exception directly.

---

## 14. Minimal contract summary (for LLM tooling)

```
class DummyChatModel extends DummyChatModelBase implements ChatModel {
  DummyChatModel()
  ChatResponse doChat(ChatRequest)
  ChatResponse chat(ChatRequest)
  String chat(String)
  ChatResponse chat(ChatMessage...)
  Set<Capability> supportedCapabilities()
  ChatRequestParameters defaultRequestParameters()
  ModelProvider provider()
  // inherited state & helpers:
  ToolChoice toolChoice = AUTO
  RuntimeException error = null
  String thinking = null
  TokenUsage tokenUsage = null
  boolean toolExecuted = false
  DummyStreamingHandle streamingHandle
  void addListener(ChatModelListener)
  List<ChatModelListener> listeners()
}
```

Response algorithm (pseudocode):

```
if error != null: throw error
instruction = messageWithInstruction(messages)
if toolChoice in {AUTO, REQUIRED}:
    if toolExecuted:
        toolExecuted = false
        // fall through to mock loading
    else:
        for spec in chatRequest.toolSpecifications():
            if instruction matches "execute tool <spec.name>":
                toolExecuted = true
                args = parse "with arguments:" (optional)
                       -> the text itself if it is a JSON object,
                          else {"prompt": <text>}, or {} if there is no text
                return AiMessage.from(ToolExecutionRequest(...))
        // no match -> fall through
mockFile = parse "use mock" or default.txt
response = AiMessage.from(loadMock(mockFile))
if thinking != null and response.text != null:
    response = AiMessage.withThinking(thinking, response.text)
return response
```

Place this article next to your tests; the mock files it references must be
available on the test classpath.
