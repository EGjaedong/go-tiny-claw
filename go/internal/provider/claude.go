package provider

import (
	"context"
	"encoding/json"
	"fmt"
	"os"

	"github.com/EGjaedong/go-tiny-claw/internal/schema"
	"github.com/EGjaedong/go-tiny-claw/internal/util"
	"github.com/anthropics/anthropic-sdk-go"
	"github.com/anthropics/anthropic-sdk-go/option"
)

type ClaudeProvider struct {
	client anthropic.Client
	model string
}

func NewDashscopeCluadeProvider(model string) *ClaudeProvider {
	util.LoadDotEnv()

	apiKey := os.Getenv("DASHSCOPE_API_KEY")
	if apiKey == "" {
		panic("请设置 DASHSCOPE_API_KEY 环境变量")
	}

	// 核心：将官方 SDK 的地址替换为目标的兼容HOST
	apiHost := os.Getenv("DASHSCOPE_ANTHROPIC_HOST")
	return &ClaudeProvider{
		client: anthropic.NewClient(option.WithAPIKey(apiKey), option.WithBaseURL(apiHost)),
		model: model,
	}
}

func (provider *ClaudeProvider) Generate(ctx context.Context, messages []schema.Message, avaiableTooles []schema.ToolDefinition) (*schema.Message, error) {
	var anthropicMessages []anthropic.MessageParam
	var systemPrompt string

	// 1. 消息翻译
	for _, msg := range messages {
		switch msg.Role {
		case schema.RoleSystem:
			systemPrompt = msg.Content
		case schema.RoleUser:
			if msg.ToolCallID != "" {
				anthropicMessages = append(anthropicMessages, anthropic.NewUserMessage(
					anthropic.NewToolResultBlock(msg.ToolCallID, msg.Content, false),
				))
			} else {
				anthropicMessages = append(anthropicMessages, anthropic.NewUserMessage(
					anthropic.NewTextBlock(msg.Content),
				))
			}
		case schema.RoleAssistant:
			var blocks []anthropic.ContentBlockParamUnion
			if msg.Content != "" {
				blocks = append(blocks, anthropic.NewTextBlock(msg.Content))
			}

			// 将历史工具调用传回 Claude 特有的 ToolUseBlockParam
			for _, toolCall := range msg.ToolCalls {
				var inputMap map[string]interface{}
				_ = json.Unmarshal(toolCall.Arguments, &inputMap)
				blocks = append(blocks, anthropic.ContentBlockParamUnion{
					OfToolUse: &anthropic.ToolUseBlockParam{
						ID: toolCall.ID,
						Name: toolCall.Name,
						Input: inputMap,
					},
				})
			}
			if len(blocks) > 0 {
				anthropicMessages = append(anthropicMessages, anthropic.NewAssistantMessage(blocks...))
			}
		}
	}

	// 2. 工具 Schema 翻译
	var anthropicTools []anthropic.ToolUnionParam
	for _, toolDef := range avaiableTooles {
		// ToolInputSchemaParam 是结构体，需要通过 Properties 字段精准填充
		var properties map[string]any
		var required []string

		if m, ok := toolDef.InputSchema.(map[string]interface{}); ok {
			if p, ok := m["properties"].(map[string]interface{}); ok {
				properties = p
			}
			if r, ok := m["required"].([]string); ok {
				required = r
			}
		}

		toolParam := anthropic.ToolParam {
			Name: toolDef.Name,
			Description: anthropic.String(toolDef.Description),
			InputSchema: anthropic.ToolInputSchemaParam{
				Properties: properties,
				Required: required,
			},
		}
		anthropicTools = append(anthropicTools, anthropic.ToolUnionParam{OfTool: &toolParam})
	}

	// 3. 构建请求并发送
	params := anthropic.MessageNewParams{
		Model: anthropic.Model(provider.model),
		MaxTokens: 4096,
		Messages: anthropicMessages,
	}

	if systemPrompt != "" {
		params.System = []anthropic.TextBlockParam{
			{Text: systemPrompt},
		}
	}

	if len(anthropicTools) > 0 {
		params.Tools = anthropicTools
	}

	responses, err := provider.client.Messages.New(ctx, params)
	if err != nil {
		return nil, fmt.Errorf("Claude provider API 请求失败: %w", err)
	}

	// 反向解析
	resultMessage := &schema.Message{
		Role: schema.RoleAssistant,
	}

	for _, block := range responses.Content {
		switch block.Type {
		case "text":
			resultMessage.Content += block.Text
		case "tool_use":
			argsBytes, _ := json.Marshal(block.Input)
			resultMessage.ToolCalls = append(resultMessage.ToolCalls, schema.ToolCall{
				ID:        block.ID,
				Name:      block.Name,
				Arguments: argsBytes,
			})
		}
	}

	return resultMessage, nil
}