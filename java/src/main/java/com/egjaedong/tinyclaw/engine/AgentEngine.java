package com.egjaedong.tinyclaw.engine;

import java.util.ArrayList;
import java.util.List;

import com.egjaedong.tinyclaw.provider.LlmProvider;
import com.egjaedong.tinyclaw.schema.Message;
import com.egjaedong.tinyclaw.schema.Role;
import com.egjaedong.tinyclaw.schema.ToolCall;
import com.egjaedong.tinyclaw.schema.ToolDefinition;
import com.egjaedong.tinyclaw.schema.ToolResult;
import com.egjaedong.tinyclaw.tools.Registry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Agent 核心循环。对照 {@code go/internal/engine/loop.go}。
 *
 * <p>
 * 职责：锁定工作区，维护上下文历史，跑 ReAct（Reason + Action）循环。
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public final class AgentEngine {

    private LlmProvider llmProvider;
    private Registry registry;

    private String workDir;
    private boolean enableThinking;

    // 启动 Agent 的生命周期
    public void run(String userPrompt) {
        System.out.println("[Engine] 引擎启动，锁定工作区: " + workDir);
        System.out.println("[Engine] 思考模式 (Thinking Phase): " + enableThinking);

        // 1. 初始化会话的 Context
        // 在真实的场景中，这里会由动态 Prompt 组装器加载 AGENTS.md。目前先硬编码
        List<Message> contextHistory = new ArrayList<>();
        contextHistory.add(new Message(Role.SYSTEM, "You are go-tiny-claw, an export coding assistant. You have full access to tools in the workspace.", null, null));
        contextHistory.add(new Message(Role.USER, userPrompt, null, null));

        int turnCount = 0;

        // 2. The Main Loop: 心跳开始 （标准的 ReAct 循环） (ReAct = Reason + Action)
        do {
            turnCount++;
            System.out.println("======= [Turn " + turnCount + "] 开始 =======");

            // 获取当前挂载的所有工具定义
            List<ToolDefinition> availableTools = registry.getAvailableTools();

            // Phase 1: 思考阶段 (Thinking Phase) - 剥夺工具，强制规划
            if (enableThinking) {
                System.out.println("[Engine][Phase 1] 剥夺工具访问权，强制进入思考与规划阶段...");

                // 核心机制：传入的 availableTools 为 null!
                // 大模型看不到任何 JSON Schema，被迫只能输出纯文本的思考过程
                Message thinkResponse = llmProvider.generate(contextHistory, null);
                if (thinkResponse.getContent() != null) {
                    System.out.println("🧠 [内部思考 Trace]: " + thinkResponse.getContent());
                    contextHistory.add(thinkResponse);
                }
            }

            // Phase 2: 行动阶段 (Action Phase) - 恢复工具访问权，执行行动
            System.out.println("[Engine][Phase 2] 恢复工具挂载，等待模型采取行动...");

            // 此时的 contextHistory 中已经包含了上一阶段模型自己的 Thinking Trace.
            // 模型会顺着自己的逻辑，结合恢复的 availableTools 发起精准的工具调用
            Message actionResponse = llmProvider.generate(contextHistory, availableTools);
            if (actionResponse.getContent() != null) {
                System.out.println("🤖 [对外回复]: " + actionResponse.getContent());
            }

            contextHistory.add(actionResponse);

            // 3. 退出条件判断
            // 如果模型么有请求任何工具调用，说明它认为任务已经完成，跳出循环
            if (actionResponse.getToolCalls() == null || actionResponse.getToolCalls().isEmpty()) {
                System.out.println("[Engine] 模型未请求调用工具，任务宣告完成。");
                break;
            }

            // 4. 执行行动 （Action） 与 获取观察结果 （Observation）
            System.out.println(String.format("[Engine] 模型请求调用 %d 个工具...", actionResponse.getToolCalls().size()));

            for (ToolCall toolCall : actionResponse.getToolCalls()) {
                System.out.println(String.format("-> 🛠️ 执行工具: %s, 参数: %s", toolCall.getName(), toolCall.getArguments()));

                ToolResult result = registry.execute(toolCall);
                if (result.isError()) {
                    System.out.println(String.format(" -> ❌ 工具执行报错: %s", result.getOutput()));
                } else {
                    System.out.println(String.format(" -> ✅ 工具执行成功（返回 %d 字节）", result.getOutput().length()));
                }

                // 将工具执行的观察结果（Observation）封装为 User Message 追加到上下文中
                // 注意： ToolCallID 必须携带！这是维系大模型推理链条的关键
                Message observationMsg = new Message(Role.USER, result.getOutput(), null, toolCall.getId());
                contextHistory.add(observationMsg);
            }
        } while (true);

        // 循环回到开头，模型将带着新加入的 Observation 继续它的下一轮思考...
    }
}
