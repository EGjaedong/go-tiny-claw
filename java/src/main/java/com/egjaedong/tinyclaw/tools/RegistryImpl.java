package com.egjaedong.tinyclaw.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.egjaedong.tinyclaw.schema.ToolCall;
import com.egjaedong.tinyclaw.schema.ToolDefinition;
import com.egjaedong.tinyclaw.schema.ToolResult;
import org.apache.commons.lang3.tuple.Pair;


public class RegistryImpl implements Registry {

    private final Map<String, BaseTool> toolsMap = new HashMap<>();

    @Override
    public void register(BaseTool tool) {
        toolsMap.put(tool.getName(), tool);
    }

    @Override
    public List<ToolDefinition> getAvailableTools() {
        List<ToolDefinition> tools = new ArrayList<>();
        toolsMap.forEach((key, value) -> {
            tools.add(value.getDefinition());
        });
        return tools;
    }

    @Override
    public ToolResult execute(ToolCall toolCall) {
        // 1. 路由查找，找到tool，分发请求
        BaseTool tool = toolsMap.get(toolCall.getName());
        if (tool == null) {
            return new ToolResult(toolCall.getId(), "系统中不存在工具：" + toolCall.getName(), true);
        }

        // 2. 执行工具
        Pair<String, Boolean> results = tool.execute(toolCall.getArguments());

        // 3. 返回结果
        Boolean isSuccess = results.getRight();
        if (isSuccess) {
            return new ToolResult(toolCall.getId(), results.getLeft(), false);
        } else {
            return new ToolResult(toolCall.getId(), results.getLeft(), true);
        }
    }
}
