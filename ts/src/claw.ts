import { getEnv, loadDotEnv } from "./util/env";

/**
 * 入口。对照 go/cmd/claw/main.go。
 * 在这里组装 Provider、Registry、AgentEngine，然后发起一次任务。
 */
function main(): void {
  loadDotEnv();

  const workDir = process.cwd();
  console.log("[Claw] TypeScript 脚手架就绪");
  console.log(`[Claw] 工作区: ${workDir}`);

  if (!getEnv("DASHSCOPE_API_KEY")) {
    console.log("[Claw] 未读到 DASHSCOPE_API_KEY，可复制 ts/.env.example 为 ts/.env，或复用 java/.env / go/.env");
  }

  console.log("[Claw] 下一步：对照 go/ 填写 provider / tools / engine，再在本文件组装运行");
}

main();
