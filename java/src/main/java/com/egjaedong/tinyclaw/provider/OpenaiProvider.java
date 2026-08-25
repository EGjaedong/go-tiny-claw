package com.egjaedong.tinyclaw.provider;

import java.util.List;

import com.egjaedong.tinyclaw.schema.Message;
import com.egjaedong.tinyclaw.schema.Role;
import com.egjaedong.tinyclaw.schema.ToolCall;
import com.egjaedong.tinyclaw.schema.ToolDefinition;
import com.egjaedong.tinyclaw.util.Env;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletionAssistantMessageParam;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
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
                case Role.SYSTEM -> params.addSystemMessage(message.getContent());
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
                    ChatCompletionAssistantMessageParam.Builder assistantParams =
                            ChatCompletionAssistantMessageParam.builder();
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

        return null;
    }
}
