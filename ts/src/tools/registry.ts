import type { ToolCall, ToolDefinition, ToolResult } from "../schema/message";
import type { BaseTool } from "./base-tool";

/** 对照 go/internal/tools/registry.go */
export interface Registry {
  register(tool: BaseTool): void;
  getAvailableTools(): ToolDefinition[];
  execute(call: ToolCall): Promise<ToolResult>;
}

export class RegistryImpl implements Registry {
  register(_tool: BaseTool): void {
    throw new Error("TODO: 对照 go/internal/tools/registry.go 实现");
  }

  getAvailableTools(): ToolDefinition[] {
    throw new Error("TODO: 对照 go/internal/tools/registry.go 实现");
  }

  execute(_call: ToolCall): Promise<ToolResult> {
    throw new Error("TODO: 对照 go/internal/tools/registry.go 实现");
  }
}
