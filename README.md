# tiny-claw

同一套 Agent 教程的两个实现：Go 跟教程走，Java 自己写。

```
.
├── go/      # 教程实现
└── java/    # 空架子，对照 go/ 自行填写
```

## Go

```bash
cd go
go run ./cmd/claw
```

| 路径 | 职责 |
| --- | --- |
| `go/cmd/claw` | 入口，组装 mock Provider / Registry |
| `go/internal/schema` | Message、ToolCall 等上下文协议 |
| `go/internal/provider` | 大模型调用契约 |
| `go/internal/tools` | 工具注册与执行 |
| `go/internal/engine` | ReAct 主循环 |

## Java

空架子包名：`com.egjaedong.tinyclaw`，目录与 Go 一一对应。类里只有 TODO，没有循环实现。

需要 JDK 21+。本机若 `java` 不可用，可先：

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@26
export PATH="$JAVA_HOME/bin:$PATH"
```

```bash
cd java
gradle run          # Homebrew 的 Gradle 9.7.0
# 或 ./gradlew run  # 项目自带 Wrapper，不依赖本机 gradle
```

| 路径 | 对照 Go |
| --- | --- |
| `.../tinyclaw/Claw.java` | `go/cmd/claw/main.go` |
| `.../schema/` | `go/internal/schema` |
| `.../provider/` | `go/internal/provider` |
| `.../tools/` | `go/internal/tools` |
| `.../engine/` | `go/internal/engine` |
