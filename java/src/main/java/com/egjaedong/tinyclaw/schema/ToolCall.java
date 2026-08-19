package com.egjaedong.tinyclaw.schema;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 模型请求的一次工具调用。对照 {@code go/internal/schema/message.go}。
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public final class ToolCall {

    String id;
    String name;
    String arguments;
}
