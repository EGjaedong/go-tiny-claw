package com.egjaedong.tinyclaw.tools;

import java.util.List;

import com.egjaedong.tinyclaw.schema.ToolCall;
import com.egjaedong.tinyclaw.schema.ToolDefinition;
import com.egjaedong.tinyclaw.schema.ToolResult;

public class MockRegistry implements Registry {

    public MockRegistry() {}

    @Override
    public List<ToolDefinition> getAvailableTools() {
        // 对照 go/cmd/claw/main.go：挂一份 JSON Schema 给模型看
        return List.of(new ToolDefinition(
                "get_weather",
                "获取制定城市的当前天气情况。",
                """
                {
                  "type": "object",
                  "properties": {
                    "city": { "type": "string" }
                  },
                  "required": ["city"]
                }
                """
        ));
    }

    @Override
    public ToolResult execute(ToolCall toolCall) {
        return new ToolResult(toolCall.getId(), "API 返回：今天是晴天，气温 25 度。", false);
    }
}
