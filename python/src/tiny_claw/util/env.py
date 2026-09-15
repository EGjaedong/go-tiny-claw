"""从当前目录或仓库根加载 .env；已存在的环境变量不会被覆盖。

对照 go/internal/util/env.go。也可复用 java/.env / go/.env。
"""

import os
from pathlib import Path

from dotenv import load_dotenv as _load_dotenv

_CANDIDATES = (
    ".env",
    "python/.env",
    "java/.env",
    "go/.env",
    "../.env",
    "../java/.env",
    "../go/.env",
)


def load_dotenv() -> None:
    cwd = Path.cwd()
    for rel in _CANDIDATES:
        path = cwd / rel
        if path.is_file():
            _load_dotenv(path, override=False)
            return


def get_env(key: str) -> str:
    return os.environ.get(key, "")
