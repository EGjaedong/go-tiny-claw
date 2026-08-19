package com.egjaedong.tinyclaw.schema;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * 上下文中的单条消息。对照 {@code go/internal/schema/message.go}。
 */
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public final class Message {

    Role role;
    String content;
    // 如果模型决定调用工具，此字段将被填充，（支持并行调用多个工具）
    List<ToolCall> toolCalls;
    // 如果这是对某个工具调用的响应，此字段必须填写，以告知模型上下文的关联性
    String toolCallId;
}
