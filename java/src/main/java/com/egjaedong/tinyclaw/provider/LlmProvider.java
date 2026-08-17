package com.egjaedong.tinyclaw.provider;

/**
 * 与大模型通信的统一契约。对照 {@code go/internal/provider/interface.go}。
 */
public interface LlmProvider {

    // TODO: 定义 Generate(messages, availableTools) -> Message
}
