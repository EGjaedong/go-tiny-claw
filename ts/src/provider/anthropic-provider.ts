import type { Message, ToolDefinition } from "../schema/message";
import type { LlmProvider } from "./llm-provider";

/** 对照 go/internal/provider/claude.go */
export class AnthropicProvider implements LlmProvider {
  constructor(private readonly model: string) {
    void this.model;
  }

  generate(
    _messages: Message[],
    _availableTools: ToolDefinition[] | null,
  ): Promise<Message> {
    throw new Error("TODO: 对照 go/internal/provider/claude.go 实现");
  }
}
