/** 对照 go/internal/schema/message.go */

export const Role = {
  System: "system",
  User: "user",
  Assistant: "assistant",
} as const;

export type Role = (typeof Role)[keyof typeof Role];

export interface Message {
  role: Role;
  content: string;
  /** 模型决定调用工具时填充，支持并行多个 */
  toolCalls?: ToolCall[];
  /** 对某次工具调用的 Observation，必须带回以维持推理链 */
  toolCallId?: string;
}

export interface ToolCall {
  id: string;
  name: string;
  /** JSON 字符串，延迟解析，交给具体工具 */
  arguments: string;
}

export interface ToolResult {
  toolCallId: string;
  output: string;
  isError: boolean;
}

export interface ToolDefinition {
  name: string;
  description: string;
  /** 对应 JSON Schema，TS 里直接用对象即可 */
  inputSchema: unknown;
}
