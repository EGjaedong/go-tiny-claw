package com.egjaedong.tinyclaw.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.egjaedong.tinyclaw.schema.Message;
import com.egjaedong.tinyclaw.schema.Role;
import com.egjaedong.tinyclaw.schema.ToolCall;
import com.egjaedong.tinyclaw.schema.ToolDefinition;
import com.egjaedong.tinyclaw.util.Env;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.JsonValue;
import static com.openai.core.ObjectMappers.jsonMapper;
import com.openai.models.FunctionDefinition;
import com.openai.models.FunctionParameters;
import com.openai.models.chat.completions.ChatCompletionAssistantMessageParam;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionFunctionTool;
import com.openai.models.chat.completions.ChatCompletionMessageFunctionToolCall;
import com.openai.models.chat.completions.ChatCompletionToolMessageParam;

public class OpenaiProvider implements LlmProvider {

    private OpenAIClient client;
    private String model;

    public OpenaiProvider(String model) {
        Env.loadDotEnv();

        String apiKey = Env.get("DASHSCOPE_API_KEY");
        if (apiKey.isEmpty()) {
            throw new IllegalStateException("请设置 DASHSCOPE_API_KEY 环境变量");
        }

        // 核心：将官方 SDK 的地址替换为 DashScope 兼容 HOST
        String apiHost = Env.get("DASHSCOPE_OPENAI_HOST");

        this.client = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .baseUrl(apiHost)
                .build();
        this.model = model;
    }

    @Override
    public Message generate(List<Message> messages, List<ToolDefinition> availableTools) {
        ChatCompletionCreateParams.Builder params = ChatCompletionCreateParams.builder()
                .model(model);

        // 1. 翻译上下文信息
        for (Message message : messages) {
            switch (message.getRole()) {
                case Role.SYSTEM ->
                    params.addSystemMessage(message.getContent());
                case Role.USER -> {
                    if (message.getToolCallId() != null) {
                        // 如果是工具调用的响应，则在用户消息中附加 toolCallId
                        params.addMessage(ChatCompletionToolMessageParam.builder()
                                .toolCallId(message.getToolCallId()).content(message.getContent())
                                .build()
                        );
                    } else {
                        params.addUserMessage(message.getContent());
                    }
                }
                case Role.ASSISTANT -> {
                    ChatCompletionAssistantMessageParam.Builder assistantParams
                            = ChatCompletionAssistantMessageParam.builder();
                    if (message.getContent() != null && !message.getContent().isEmpty()) {
                        assistantParams.content(message.getContent());
                    }

                    // 历史里的 ToolCalls 必须原样回传，否则模型接不上上一轮工具调用
                    if (message.getToolCalls() != null && !message.getToolCalls().isEmpty()) {
                        for (ToolCall tc : message.getToolCalls()) {
                            assistantParams.addToolCall(
                                    ChatCompletionMessageFunctionToolCall.builder()
                                            .id(tc.getId())
                                            .function(
                                                    ChatCompletionMessageFunctionToolCall.Function.builder()
                                                            .name(tc.getName())
                                                            .arguments(tc.getArguments())
                                                            .build())
                                            .build());
                        }
                    }

                    params.addMessage(assistantParams.build());
                }
            }
        }

        // 2. 翻译工具定义 （V3 新 API 特性适配）
        if (availableTools != null && !availableTools.isEmpty()) {
            for (ToolDefinition toolDef : availableTools) {
                var schema = FunctionParameters.builder();
                if (toolDef.getInputSchema() != null && !toolDef.getInputSchema().isEmpty()) {
                    Map<String, Object> raw;
                    try {
                        raw = jsonMapper().readValue(
                                toolDef.getInputSchema(),
                                new TypeReference<Map<String, Object>>() {
                        });
                    } catch (JsonProcessingException e) {
                        throw new IllegalArgumentException("工具 schema 不是合法 JSON: " + toolDef.getName(), e);
                    }
                    raw.forEach((key, value)
                            -> schema.putAdditionalProperty(key, JsonValue.from(value)));
                }

                var function = FunctionDefinition.builder()
                        .name(toolDef.getName())
                        .parameters(schema.build());
                if (toolDef.getDescription() != null) {
                    function.description(toolDef.getDescription());
                }

                params.addTool(ChatCompletionFunctionTool.builder()
                        .function(function.build()).build());
            }
        }

        // 3. 构建并发送请求 (model / messages / tools 都已经在 params 上)
        var response = client.chat().completions().create(params.build());
        if (response.choices().isEmpty()) {
            throw new IllegalStateException("API 返回了空的 Choices");
        }

        // 4. 反向翻译为内部 shcema.Message
        var choice = response.choices().get(0).message();
        var result = new Message();
        result.setRole(Role.ASSISTANT);
        result.setContent(choice.content().orElse(""));

        List<ToolCall> toolCalls = new ArrayList<>();
        choice.toolCalls().orElse(List.of()).forEach(toolCall -> {
            if (toolCall.isFunction()) {
                var function = toolCall.asFunction();
                toolCalls.add(new ToolCall(
                    function.id(),
                    function.function().name(),
                    function.function().arguments()
                ));
            }
        });
        result.setToolCalls(toolCalls);
        return result;
    }
}
