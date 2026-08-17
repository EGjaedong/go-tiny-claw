package com.egjaedong.tinyclaw.engine;

/**
 * Agent 核心循环。对照 {@code go/internal/engine/loop.go}。
 *
 * <p>职责：锁定工作区，维护上下文历史，跑 ReAct（Reason + Action）循环。
 */
public final class AgentEngine {

    // TODO: 注入 LlmProvider、Registry、WorkDir

    public void run(String userPrompt) {
        throw new UnsupportedOperationException("TODO: 实现 ReAct 主循环");
    }
}
