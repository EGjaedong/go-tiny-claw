package tools

import (
	"context"
	"github.com/EGjaedong/go-tiny-claw/internal/schema"
)

// Registry 定义了工具的注册与分发执行接口
type Registry interface {
	// GetAvaiablableTools 返回当前系统挂在的所有可用工具的 schema
	GetAvailableTools() []schema.ToolDefinition

	// Execute 实际执行模型请求的工具，并返回执行结果
	Execute(ctx context.Context, call schema.ToolCall) schema.ToolResult
}
