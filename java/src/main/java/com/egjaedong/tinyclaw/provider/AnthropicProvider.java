package com.egjaedong.tinyclaw.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.JsonValue;
import com.anthropic.models.beta.messages.BetaContentBlockParam;
import com.anthropic.models.beta.messages.BetaTool;
import com.anthropic.models.beta.messages.BetaToolResultBlockParam;
import com.anthropic.models.beta.messages.BetaToolUseBlockParam;
import com.anthropic.models.beta.messages.MessageCreateParams;
import com.egjaedong.tinyclaw.schema.Message;
import com.egjaedong.tinyclaw.schema.Role;
import com.egjaedong.tinyclaw.schema.ToolCall;
import com.egjaedong.tinyclaw.schema.ToolDefinition;
import com.egjaedong.tinyclaw.util.Env;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        for (Message message : messages) {
            switch (message.getRole()) {
                case Role.SYSTEM ->
                    params.addSystemMessage(message.getContent());
                case Role.USER -> {
                    if (message.getToolCallId() != null) {
                        // 如果是工具调用的响应，则在用户消息中附加 toolCallId
                        var toolResult = BetaToolResultBlockParam.builder()
                                .toolUseId(message.getToolCallId())
                                .content(message.getContent())
                                .isError(false)
                                .build();
                        params.addUserMessageOfBetaContentBlockParams(
                                List.of(BetaContentBlockParam.ofToolResult(toolResult)));
                    } else {
                        params.addUserMessage(message.getContent());
                    }
                }
                case Role.ASSISTANT -> {
                    var blocks = new ArrayList<BetaContentBlockParam>();
                    if (message.getContent() != null && !message.getContent().isBlank()) {
                        blocks.add(BetaContentBlockParam.ofText(message.getContent()));
                    }

                    if (message.getToolCalls() != null) {
                        for (ToolCall toolCall : message.getToolCalls()) {
                            Map<String, Object> raw = Map.of();
                            if (toolCall.getArguments() != null && !toolCall.getArguments().isBlank()) {
                                try {
                                    raw = new ObjectMapper().readValue(
                                            toolCall.getArguments(),
                                            new TypeReference<Map<String, Object>>() {
                                            });
                                } catch (JsonProcessingException ex) {
                                    System.getLogger(AnthropicProvider.class.getName()).log(System.Logger.Level.ERROR,
                                            (String) null, ex);
                                }
                            }

                            var input = BetaToolUseBlockParam.Input.builder();
                            raw.forEach((key, value) -> input.putAdditionalProperty(key, JsonValue.from(value)));

                            blocks.add(BetaContentBlockParam.ofToolUse(
                                    BetaToolUseBlockParam.builder()
                                            .id(toolCall.getId())
                                            .name(toolCall.getName())
                                            .input(input.build())
                                            .build()));
                        }
                    }

                    if (!blocks.isEmpty()) {
                        params.addAssistantMessageOfBetaContentBlockParams(blocks);
                    }
                }
            }
        }

        // 2. 翻译工具定义
        if (availableTools != null && !availableTools.isEmpty()) {
            for (ToolDefinition toolDef : availableTools) {
                try {
                    Map<String, Object> raw = new ObjectMapper().readValue(
                            toolDef.getInputSchema(),
                            new TypeReference<Map<String, Object>>() {
                            });
                    Map<String, Object> props = (Map<String, Object>) raw.getOrDefault("properties", Map.of());
                    List<String> required = (List<String>) raw.getOrDefault("required", List.of());

                    var properties = BetaTool.InputSchema.Properties.builder();
                    props.forEach((key, value) -> properties.putAdditionalProperty(key, JsonValue.from(value)));

                    params.addTool(
                            BetaTool.builder()
                                    .name(toolDef.getName())
                                    .description(toolDef.getDescription())
                                    .inputSchema(
                                            BetaTool.InputSchema.builder()
                                                    .properties(properties.build()).required(required).build())
                                    .build());
                } catch (JsonMappingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (JsonProcessingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        // 3. 构建并发送请求
        params.maxTokens(4096);
        var response = client.beta().messages().create(params.build());

        // 4. 反向翻译为内部 schema.Message
        var result = new Message();
        result.setRole(Role.ASSISTANT);
        var content = new StringBuilder();
        var toolCalls = new ArrayList<ToolCall>();
        var mapper = new ObjectMapper();

        for (var block : response.content()) {
            if (block.isText()) {
                content.append(block.asText().text());
            } else if (block.isToolUse()) {
                var toolUse = block.asToolUse();
                String arguments;
                try {
                    arguments = mapper.writeValueAsString(toolUse._input());
                } catch (JsonProcessingException e) {
                    throw new IllegalStateException("无法序列化 tool_use.input", e);
                }
                toolCalls.add(new ToolCall(toolUse.id(), toolUse.name(), arguments));
            }
        }

        result.setContent(content.toString());
        result.setToolCalls(toolCalls);
        return result;
    }

}
