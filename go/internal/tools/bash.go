package tools

type BashTool struct {
	workDir string
}

func NewBashTool(workDir string) *BashTool{
	return &BashTool{workDir: workDir}
}