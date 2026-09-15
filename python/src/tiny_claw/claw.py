from pathlib import Path

from tiny_claw.util.env import get_env, load_dotenv


def main() -> None:
    """入口。对照 go/cmd/claw/main.go。"""
    load_dotenv()

    work_dir = str(Path.cwd().resolve())
    print("[Claw] Python 脚手架就绪")
    print(f"[Claw] 工作区: {work_dir}")

    if not get_env("DASHSCOPE_API_KEY"):
        print("[Claw] 未读到 DASHSCOPE_API_KEY，可复制 python/.env.example 为 python/.env，或复用 java/.env / go/.env")

    print("[Claw] 下一步：对照 go/ 填写 provider / tools / engine，再在本文件组装运行")


if __name__ == "__main__":
    main()
