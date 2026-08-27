package com.egjaedong.tinyclaw.provider;

import java.util.List;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.beta.messages.MessageCreateParams;
import com.egjaedong.tinyclaw.schema.Message;
import com.egjaedong.tinyclaw.schema.Role;
import com.egjaedong.tinyclaw.schema.ToolDefinition;
import com.egjaedong.tinyclaw.util.Env;

public class AnthropicProvider implements LlmProvider {

    private AnthropicClient client;
    private String model;

    public AnthropicProvider(String model) {
        Env.loadDotEnv();

        String apiKey = Env.get("DASHSCOPE_API_KEY");
        if (apiKey.isBlank()) {
            throw new IllegalStateException("请设置 DASHSCOPE_API_KEY 环境变量");
        }

        String apiHost = Env.get("DASHSCOPE_ANTHROPIC_HOST");

        this.client = AnthropicOkHttpClient.builder()
                .apiKey(apiKey)
                .baseUrl(apiHost)
                .build();
        this.model = model;
    }

    @Override
    public Message generate(List<Message> messages, List<ToolDefinition> availableTools) {
        var params = MessageCreateParams.builder().model(this.model);

        // 1. 翻译上下文信息
        for (Message msg : messages) {
            switch (msg.getRole()) {
                case Role.SYSTEM ->
                    params.addSystemMessage(msg.getContent());
                case Role.USER -> {
                    if (msg.getToolCallId() != null) {
                        // 如果是工具调用的响应，则在用户消息中附加 toolCallId
                    }
                }
            }
        }
        return null;
    }

}