import { existsSync } from "node:fs";
import { resolve } from "node:path";
import { config } from "dotenv";

/**
 * 从当前目录或仓库根加载 .env；已存在的环境变量不会被覆盖。
 * 对照 go/internal/util/env.go。也可复用 java/.env / go/.env。
 */
const CANDIDATES = [
  ".env",
  "ts/.env",
  "java/.env",
  "go/.env",
  "../.env",
  "../java/.env",
  "../go/.env",
];

export function loadDotEnv(): void {
  for (const rel of CANDIDATES) {
    const path = resolve(process.cwd(), rel);
    if (existsSync(path)) {
      config({ path, override: false, quiet: true });
      return;
    }
  }
}

export function getEnv(key: string): string {
  return process.env[key] ?? "";
}
