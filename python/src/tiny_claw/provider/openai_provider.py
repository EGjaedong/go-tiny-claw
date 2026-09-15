from tiny_claw.provider.base import LlmProvider
from tiny_claw.schema import Message, ToolDefinition


class OpenaiProvider(LlmProvider):
    """对照 go/internal/provider/openai.go"""

    def __init__(self, model: str) -> None:
        self.model = model

    def generate(
        self,
        messages: list[Message],
        available_tools: list[ToolDefinition] | None,
    ) -> Message:
        raise NotImplementedError("TODO: 对照 go/internal/provider/openai.go 实现")
