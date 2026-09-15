from abc import ABC, abstractmethod

from tiny_claw.schema import Message, ToolDefinition


class LlmProvider(ABC):
    """与大模型通信的统一契约。对照 go/internal/provider/interface.go"""

    @abstractmethod
    def generate(
        self,
        messages: list[Message],
        available_tools: list[ToolDefinition] | None,
    ) -> Message:
        raise NotImplementedError
