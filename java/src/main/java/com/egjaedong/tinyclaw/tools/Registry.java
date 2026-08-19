package com.egjaedong.tinyclaw.tools;

import java.util.List;

import com.egjaedong.tinyclaw.schema.ToolCall;
import com.egjaedong.tinyclaw.schema.ToolDefinition;
import com.egjaedong.tinyclaw.schema.ToolResult;

/**
 * 工具注册与分发。对照 {@code go/internal/tools/registry.go}。
 */
public interface Registry {

    List<ToolDefinition> getAvailableTools();

    ToolResult execute(ToolCall toolCall);
}
