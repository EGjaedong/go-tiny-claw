# tiny-claw

同一套 Agent 教程的四个实现：Go 跟教程走，其余对照 `go/` 自己写。

```
.
├── go/       # 教程实现
├── java/     # Java，对照 go/ 自行填写
├── ts/       # TypeScript + Node，对照 go/ 自行填写
└── python/   # Python，对照 go/ 自行填写
```

## Go

```bash
cd go
go run ./cmd/claw
```

| 路径 | 职责 |
| --- | --- |
| `go/cmd/claw` | 入口，组装 Provider / Registry |
| `go/internal/schema` | Message、ToolCall 等上下文协议 |
| `go/internal/provider` | 大模型调用契约 |
| `go/internal/tools` | 工具注册与执行 |
| `go/internal/engine` | ReAct 主循环 |

## Java

包名：`com.egjaedong.tinyclaw`，目录与 Go 一一对应。

需要 JDK 21+。

```bash
cd java
gradle run
# 或 ./gradlew run
```

| 路径 | 对照 Go |
| --- | --- |
| `.../tinyclaw/Claw.java` | `go/cmd/claw/main.go` |
| `.../schema/` | `go/internal/schema` |
| `.../provider/` | `go/internal/provider` |
| `.../tools/` | `go/internal/tools` |
| `.../engine/` | `go/internal/engine` |

## TypeScript

需要 Node 20+。首次：

```bash
cd ts
npm install
```

```bash
cd ts
npm start
npm run typecheck
```

| 路径 | 对照 Go |
| --- | --- |
| `ts/src/claw.ts` | `go/cmd/claw/main.go` |
| `ts/src/schema/` | `go/internal/schema` |
| `ts/src/provider/` | `go/internal/provider` |
| `ts/src/tools/` | `go/internal/tools` |
| `ts/src/engine/` | `go/internal/engine` |

## Python

需要 Python 3.12+ 和 [uv](https://docs.astral.sh/uv/)。首次：

```bash
cd python
uv sync
```

```bash
cd python
uv run python -m tiny_claw
# 或
uv run claw
```

| 路径 | 对照 Go |
| --- | --- |
| `python/src/tiny_claw/claw.py` | `go/cmd/claw/main.go` |
| `python/src/tiny_claw/schema/` | `go/internal/schema` |
| `python/src/tiny_claw/provider/` | `go/internal/provider` |
| `python/src/tiny_claw/tools/` | `go/internal/tools` |
| `python/src/tiny_claw/engine/` | `go/internal/engine` |

密钥：各包可放自己的 `.env`（见 `.env.example`），也可复用 `java/.env` / `go/.env`。
