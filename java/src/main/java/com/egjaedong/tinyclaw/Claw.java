package com.egjaedong.tinyclaw;

import java.nio.file.Path;

import com.egjaedong.tinyclaw.engine.AgentEngine;
import com.egjaedong.tinyclaw.provider.LlmProvider;
import com.egjaedong.tinyclaw.provider.MockLlmProvider;
import com.egjaedong.tinyclaw.tools.MockRegistry;
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
        LlmProvider llmProvider = new MockLlmProvider();
        Registry registry = new MockRegistry();
        AgentEngine agentEngine = new AgentEngine(llmProvider, registry, workDir, true);
        agentEngine.run("帮我检查当前目录的文件");
        System.out.println("任务完成！");
    }
}
