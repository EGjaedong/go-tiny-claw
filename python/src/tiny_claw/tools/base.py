from abc import ABC, abstractmethod

from tiny_claw.schema import ToolDefinition


class BaseTool(ABC):
    """对照 go/internal/tools/registry.go 的 BaseTool"""

    @abstractmethod
    def name(self) -> str:
        raise NotImplementedError

    @abstractmethod
    def definition(self) -> ToolDefinition:
        raise NotImplementedError

    @abstractmethod
    def execute(self, arguments: str) -> str:
        raise NotImplementedError
