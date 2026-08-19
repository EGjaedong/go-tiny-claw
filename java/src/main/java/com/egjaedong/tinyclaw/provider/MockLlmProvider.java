package com.egjaedong.tinyclaw.provider;

import java.util.ArrayList;
import java.util.List;

import com.egjaedong.tinyclaw.schema.Message;
import com.egjaedong.tinyclaw.schema.Role;
import com.egjaedong.tinyclaw.schema.ToolCall;
import com.egjaedong.tinyclaw.schema.ToolDefinition;

public class MockLlmProvider implements LlmProvider {

    private int turn;

    public MockLlmProvider() {
        this.turn = 0;
    }

    @Override
    public Message generate(List<Message> messages, List<ToolDefinition> availableTools) {
        // 如果工具列表为空，说明这是引擎发起的 Phase 1: Thinking 阶段
        if (availableTools == null || availableTools.isEmpty()) {
            return new Message(Role.ASSISTANT, "【推理中】目标是检查文件。我不能直接盲猜，我需要先调用 bash 工具执行 ls 命令，看看当前目录下有什么，然后再做定夺。", null, null);
        }

        turn++;
        if (turn == 1) {
            // 第一轮 Action: 顺着刚才的 Thinking，精准调用工具
            List<ToolCall> toolCalls = new ArrayList<>();
            toolCalls.add(new ToolCall("call_123", "bash", "ls -la"));
            return new Message(Role.ASSISTANT, "我要执行我刚才计划的步骤了。", toolCalls, null);
        }

        // 第二轮 Action：直接总结退出
        return new Message(Role.ASSISTANT, "根据工具返回的结果，我看到了 main.go，任务圆满完成！", null, null);
    }
}
