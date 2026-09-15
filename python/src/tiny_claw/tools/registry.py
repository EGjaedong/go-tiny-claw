from abc import ABC, abstractmethod

from tiny_claw.schema import ToolCall, ToolDefinition, ToolResult
from tiny_claw.tools.base import BaseTool


class Registry(ABC):
    """对照 go/internal/tools/registry.go"""

    @abstractmethod
    def register(self, tool: BaseTool) -> None:
        raise NotImplementedError

    @abstractmethod
    def get_available_tools(self) -> list[ToolDefinition]:
        raise NotImplementedError

    @abstractmethod
    def execute(self, call: ToolCall) -> ToolResult:
        raise NotImplementedError


class RegistryImpl(Registry):
    def register(self, tool: BaseTool) -> None:
        raise NotImplementedError("TODO: 对照 go/internal/tools/registry.go 实现")

    def get_available_tools(self) -> list[ToolDefinition]:
        raise NotImplementedError("TODO: 对照 go/internal/tools/registry.go 实现")

    def execute(self, call: ToolCall) -> ToolResult:
        raise NotImplementedError("TODO: 对照 go/internal/tools/registry.go 实现")
