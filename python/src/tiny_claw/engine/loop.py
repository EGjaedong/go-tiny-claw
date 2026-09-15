from tiny_claw.provider.base import LlmProvider
from tiny_claw.tools.registry import Registry


class AgentEngine:
    """对照 go/internal/engine/loop.go"""

    def __init__(
        self,
        provider: LlmProvider,
        registry: Registry,
        work_dir: str,
        enable_thinking: bool,
    ) -> None:
        self.provider = provider
        self.registry = registry
        self.work_dir = work_dir
        self.enable_thinking = enable_thinking

    def run(self, user_prompt: str) -> None:
        raise NotImplementedError("TODO: 对照 go/internal/engine/loop.go 实现 ReAct 循环")
