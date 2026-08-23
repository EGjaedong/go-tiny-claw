package util

import (
	"os"

	"github.com/joho/godotenv"
)

// LoadDotEnv 从当前目录或仓库根目录加载 .env；已存在的环境变量不会被覆盖。
func LoadDotEnv() {
	for _, p := range []string{".env", "go/.env"} {
		if _, err := os.Stat(p); err == nil {
			_ = godotenv.Load(p)
			return
		}
	}
}
