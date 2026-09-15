"""对照 go/internal/schema/message.go"""

from dataclasses import dataclass
from typing import Any, Literal

Role = Literal["system", "user", "assistant"]

SYSTEM: Role = "system"
USER: Role = "user"
ASSISTANT: Role = "assistant"


@dataclass
class ToolCall:
    id: str
    name: str
    arguments: str  # JSON 字符串，延迟解析，交给具体工具


@dataclass
class ToolResult:
    tool_call_id: str
    output: str
    is_error: bool = False


@dataclass
class ToolDefinition:
    name: str
    description: str
    input_schema: Any  # 对应 JSON Schema


@dataclass
class Message:
    role: Role
    content: str
    tool_calls: list[ToolCall] | None = None
    tool_call_id: str | None = None
