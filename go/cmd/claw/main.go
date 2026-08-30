// cmd/claw/main.go
package main

import (
	"context"
	"log"
	"os"

	"github.com/EGjaedong/go-tiny-claw/internal/engine"
	"github.com/EGjaedong/go-tiny-claw/internal/provider"
	"github.com/EGjaedong/go-tiny-claw/internal/schema"
	"github.com/EGjaedong/go-tiny-claw/internal/tools"
	"github.com/EGjaedong/go-tiny-claw/internal/util"
)

// 伪造的工具注册表 (用于测试 Provider 的工具提取能力)
type mockRegistry struct{}

func (registry *mockRegistry) GetAvailableTools() []schema.ToolDefinition {
	return []schema.ToolDefinition{
		{
			Name: "get_weather",
			Description: "获取制定城市的当前天气情况。",
			InputSchema: map[string]interface{}{
				"type": "object",
				"properties": map[string]interface{}{
					"city": map[string]interface{}{
						"type": "string",
					},
				},
				"required": []string{"city"},
			},
		},
	}
}

func (registry *mockRegistry) Execute(ctx context.Context, call schema.ToolCall) schema.ToolResult {
	log.Printf("  -> [Mock 工具执行] 获取 %s 的天气中...\n", call.Name)
	return schema.ToolResult{
		ToolCallID: call.ID,
		Output: "API 返回：今天是晴天，气温 25 度。",
		IsError: false,
	}
}

// 3. 组装运行
func main() {
	// 确保能读取到 ENV
	util.LoadDotEnv()

	if os.Getenv("DASHSCOPE_API_KEY") == "" {
		log.Fatal("环境变量加载失败")
	}

	// 获取当前执行目录作为 WorkDir 物理边界
	workDir, _ := os.Getwd()

	// 1.初始化真实的 Provider
	// 可以切换不同的 Provider 试试，当然当前只有 openai 和 A畜 的两种
	llmProvider := provider.NewDashscopeOpenAIProvider("qwen3.8-max")
	// llmProvider := provider.NewDashscopeClaudeProvider("qwen3.8-max")

	// 2. 注入伪造的工具列表
	registry := tools.NewRegistry()

	// 3. 将真实的 ReadFile 工具挂载到注册表中
	readFileTool := tools.NewReadFileTool(workDir)
	registry.Register(readFileTool)

	// 4. 实例化核心引擎，任务简单，关闭 thinking
	eng := engine.NewAgentEngine(llmProvider, registry, workDir, false)

	// 设定测试任务
	prompt := "请调用工具读取一下当前工作区目录下 hello.txt 文件的内容，并用一句话向我总结它说了什么。"

	// 发起任务指令
	err := eng.Run(context.Background(), prompt)
	if err != nil {
		log.Fatalf("引擎运行崩溃: %v", err)
	}
}
