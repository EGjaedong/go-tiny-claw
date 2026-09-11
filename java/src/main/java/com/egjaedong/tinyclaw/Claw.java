package com.egjaedong.tinyclaw;

import com.egjaedong.tinyclaw.provider.OpenaiProvider;
import com.egjaedong.tinyclaw.tools.ReadFileTool;
import com.egjaedong.tinyclaw.tools.RegistryImpl;
import java.nio.file.Path;

import com.egjaedong.tinyclaw.engine.AgentEngine;
import com.egjaedong.tinyclaw.provider.LlmProvider;
import com.egjaedong.tinyclaw.tools.Registry;

/**
 * 入口。对照 {@code go/cmd/claw/main.go}。
 *
 * <p>建议在这里组装 Provider、Registry、AgentEngine，然后发起一次任务。
 */
public final class Claw {

    public static void main(String[] args) {
        // 对照 os.Getwd()：进程 cwd，不是 jar 所在目录，也不是 git 根
        String workDir = Path.of("").toAbsolutePath().normalize().toString();
        LlmProvider llmProvider = new OpenaiProvider("deepseek-flash");
        Registry registry = new RegistryImpl();
        registry.register(new ReadFileTool(workDir));
        AgentEngine agentEngine = new AgentEngine(llmProvider, registry, workDir, false);
        String prompt = "请调用工具读取一下当前工作区目录下 hello.txt 文件的内容，并用一句话向我总结它说了什么。";
        agentEngine.run(prompt);
        System.out.println("任务完成！");
    }
}
