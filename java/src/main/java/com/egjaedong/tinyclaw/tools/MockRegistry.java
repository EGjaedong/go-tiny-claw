package com.egjaedong.tinyclaw.tools;

import java.util.ArrayList;
import java.util.List;

import com.egjaedong.tinyclaw.schema.ToolCall;
import com.egjaedong.tinyclaw.schema.ToolDefinition;
import com.egjaedong.tinyclaw.schema.ToolResult;

public class MockRegistry implements Registry {

    public MockRegistry() {}

    @Override
    public List<ToolDefinition> getAvailableTools() {
        List<ToolDefinition> tools = new ArrayList<>();
        tools.add(new ToolDefinition("bash", null,  null));
        return tools;
    }

    @Override
    public ToolResult execute(ToolCall toolCall) {
        return new ToolResult(toolCall.getId(), "-rw-r--r-- 1 user group 234 Oct 24 10:00 main.go\n", false);
    }
}
