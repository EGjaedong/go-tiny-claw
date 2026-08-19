package com.egjaedong.tinyclaw.provider;

import java.util.List;

import com.egjaedong.tinyclaw.schema.Message;
import com.egjaedong.tinyclaw.schema.ToolDefinition;

/**
 * 与大模型通信的统一契约。对照 {@code go/internal/provider/interface.go}。
 */
public interface LlmProvider {

    Message generate(List<Message> messages, List<ToolDefinition> availableTools);
}
