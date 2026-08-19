package com.egjaedong.tinyclaw.schema;

/**
 * 消息角色。对照 {@code go/internal/schema/message.go} 中的 {@code Role}。
 */
public enum Role {
    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
