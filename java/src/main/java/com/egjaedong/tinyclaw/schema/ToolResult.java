package com.egjaedong.tinyclaw.schema;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 工具本地执行后的结果。对照 {@code go/internal/schema/message.go}。
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public final class ToolResult {

    String toolCallId;
    String output;
    boolean isError;
}
