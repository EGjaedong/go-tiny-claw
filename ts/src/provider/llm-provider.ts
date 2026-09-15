import type { Message, ToolDefinition } from "../schema/message";

/** 与大模型通信的统一契约。对照 go/internal/provider/interface.go */
export interface LlmProvider {
  generate(
    messages: Message[],
    availableTools: ToolDefinition[] | null,
  ): Promise<Message>;
}
