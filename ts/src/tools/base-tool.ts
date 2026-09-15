import type { ToolDefinition } from "../schema/message";

/** 对照 go/internal/tools/registry.go 的 BaseTool */
export interface BaseTool {
  name(): string;
  definition(): ToolDefinition;
  execute(args: string): Promise<string>;
}
