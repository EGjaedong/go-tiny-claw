package com.egjaedong.tinyclaw.tools;

import com.egjaedong.tinyclaw.schema.ToolDefinition;
import org.apache.commons.lang3.tuple.Pair;

public interface BaseTool {

    String getName();

    ToolDefinition getDefinition();

    Pair<String, Boolean> execute(String arguments);
}
