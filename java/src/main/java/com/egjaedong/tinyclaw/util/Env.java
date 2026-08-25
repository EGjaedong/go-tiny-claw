package com.egjaedong.tinyclaw.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 从当前目录或仓库根目录加载 {@code java/.env}。
 * 已存在的环境变量不会被覆盖。对照 {@code go/internal/util/env.go}。
 */
public final class Env {

    private static final Map<String, String> FROM_FILE = new HashMap<>();

    private Env() {
    }

    public static void loadDotEnv() {
        for (String candidate : List.of(".env", "java/.env")) {
            Path path = Path.of(candidate);
            if (!Files.isRegularFile(path)) {
                continue;
            }
            try {
                for (String raw : Files.readAllLines(path)) {
                    String line = raw.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    int eq = line.indexOf('=');
                    if (eq <= 0) {
                        continue;
                    }
                    String key = line.substring(0, eq).trim();
                    String value = stripQuotes(line.substring(eq + 1).trim());
                    FROM_FILE.putIfAbsent(key, value);
                }
            } catch (IOException ignored) {
                return;
            }
            return;
        }
    }

    public static String get(String key) {
        String fromEnv = System.getenv(key);
        if (fromEnv != null && !fromEnv.isEmpty()) {
            return fromEnv;
        }
        return FROM_FILE.getOrDefault(key, "");
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}
