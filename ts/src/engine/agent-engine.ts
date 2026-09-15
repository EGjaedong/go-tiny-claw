import type { LlmProvider } from "../provider/llm-provider";
import type { Registry } from "../tools/registry";

/** 对照 go/internal/engine/loop.go */
export class AgentEngine {
  constructor(
    readonly provider: LlmProvider,
    readonly registry: Registry,
    readonly workDir: string,
    readonly enableThinking: boolean,
  ) {}

  run(_userPrompt: string): Promise<void> {
    throw new Error("TODO: 对照 go/internal/engine/loop.go 实现 ReAct 循环");
  }
}
