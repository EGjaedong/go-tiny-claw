package provider

import (
	"context"
	"encoding/json"
	"fmt"
	"os"

	"github.com/EGjaedong/go-tiny-claw/internal/schema"
	"github.com/EGjaedong/go-tiny-claw/internal/util"
	"github.com/openai/openai-go/v3"
	"github.com/openai/openai-go/v3/option"
	"github.com/openai/openai-go/v3/shared"
)

type OpenAIProvider struct {
	client openai.Client // 值类型，非指针
	model  string
}

// NewDashscopeAIProvider 构造函数：基于 OpenAI V3 SDK，指向 DeepSeek 底座
func NewDashscopeOpenAIProvider(model string) *OpenAIProvider {
	util.LoadDotEnv()

	apiKey := os.Getenv("DASHSCOPE_API_KEY")
	if apiKey == "" {
		panic("请设置 DASHSCOPE_API_KEY 环境变量")
	}

	// 核心：将官方 SDK 的地址替换为目标的兼容HOST
	apiHost := os.Getenv("DASHSCOPE_API_HOST")

	return &OpenAIProvider{
		client: openai.NewClient(option.WithAPIKey(apiKey), option.WithBaseURL(apiHost)),
		model:  model,
	}
}

func (provider *OpenAIProvider) Generate(
	ctx context.Context,
	messages []schema.Message,
	availableTools []schema.ToolDefinition) (*schema.Message, error) {
	var openaiMessages []openai.ChatCompletionMessageParamUnion

	// 1. 翻译上下文信息
	for _, msg := range messages {
		switch msg.Role {
		case schema.RoleSystem:
			openaiMessages = append(openaiMessages, openai.SystemMessage(msg.Content))

		case schema.RoleUser:
			if msg.ToolCallID != "" {
				// 注意： V3 参数顺序是 (content, toolCallID)
				openaiMessages = append(openaiMessages, openai.ToolMessage(msg.Content, msg.ToolCallID))
			} else {
				openaiMessages = append(openaiMessages, openai.UserMessage(msg.Content))
			}

		case schema.RoleAssistant:
			astParam := openai.ChatCompletionAssistantMessageParam{}
			if msg.Content != "" {
				astParam.Content = openai.ChatCompletionAssistantMessageParamContentUnion{
					OfString: openai.String(msg.Content),
				}
			}

			// 【重要】如果历史包含 ToolCalls，必须原样返回，以维系大模型的逻辑链
			if len(msg.ToolCalls) > 0 {
				var toolCalls []openai.ChatCompletionMessageToolCallUnionParam
				for _, tc := range msg.ToolCalls {
					// OfFunction 对应 GetFunction()，字段类型严格要求为指针
					toolCalls = append(toolCalls, openai.ChatCompletionMessageToolCallUnionParam{
						OfFunction: &openai.ChatCompletionMessageFunctionToolCallParam{
							ID:   tc.ID,
							Type: "function",
							Function: openai.ChatCompletionMessageFunctionToolCallFunctionParam{
								Name:      tc.Name,
								Arguments: string(tc.Arguments),
							},
						},
					})
				}
				astParam.ToolCalls = toolCalls
			}

			openaiMessages = append(openaiMessages, openai.ChatCompletionMessageParamUnion{
				OfAssistant: &astParam,
			})
		}
	}

	// 2. 翻译工具定义 （v3 新 API 特性适配）
	var openaiTools []openai.ChatCompletionToolUnionParam
	for _, toolDef := range availableTools {
		var params shared.FunctionParameters

		// 尝试直接断言，如果不成功则通过 JSON 往返序列化来保证类型匹配
		if m, ok := toolDef.InputSchema.(map[string]interface{}); ok {
			params = shared.FunctionParameters(m)
		} else {
			// fallback: JSON 往返序列化
			b, _ := json.Marshal(toolDef.InputSchema)
			_ = json.Unmarshal(b, &params)
		}

		openaiTools = append(openaiTools, openai.ChatCompletionFunctionTool(
			shared.FunctionDefinitionParam{
				Name:        toolDef.Name,
				Description: openai.String(toolDef.Description),
				Parameters:  params,
			},
		))
	}

	// 3. 构建请求并发送
	params := openai.ChatCompletionNewParams{
		Model:    provider.model,
		Messages: openaiMessages,
	}

	// 【慢思考机制支撑】仅当 availableTools 存在时才挂载 Tools
	if len(openaiTools) > 0 {
		params.Tools = openaiTools
	}

	response, err := provider.client.Chat.Completions.New(ctx, params)
	if err != nil {
		return nil, fmt.Errorf("OpenAI Provider API 请求失败: %w", err)
	}
	if len(response.Choices) == 0 {
		return nil, fmt.Errorf("API 返回了空的 Choices")
	}

	// 4. 将 API Response 反向翻译为内部 schema.Message
	choice := response.Choices[0].Message
	resultMessage := &schema.Message{
		Role:    schema.RoleAssistant,
		Content: choice.Content,
	}

	for _, toolCall := range choice.ToolCalls {
		if toolCall.Type == "function" {
			resultMessage.ToolCalls = append(resultMessage.ToolCalls, schema.ToolCall{
				ID:        toolCall.ID,
				Name:      toolCall.Function.Name,
				Arguments: []byte(toolCall.Function.Arguments), // 提取 JSON 字符串字节
			})
		}
	}

	return resultMessage, nil
}
