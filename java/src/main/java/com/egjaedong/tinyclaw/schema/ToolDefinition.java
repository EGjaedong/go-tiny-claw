package com.egjaedong.tinyclaw.schema;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 可供模型理解的工具元信息。对照 {@code go/internal/schema/message.go}。
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public final class ToolDefinition {

    String name;
    String description;
    String inputSchema;
}
