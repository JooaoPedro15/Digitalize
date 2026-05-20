package com.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EnvConfig {
    private static final Map<String, String> DOTENV = loadDotenv();

    private EnvConfig() {
    }

    public static String get(String key) {
        String value = System.getenv(key);
        if (hasText(value)) {
            return value;
        }
        return DOTENV.get(key);
    }

    public static String getOrDefault(String key, String defaultValue) {
        String value = get(key);
        return hasText(value) ? value : defaultValue;
    }

    private static Map<String, String> loadDotenv() {
        Path dotenv = findDotenv();
        if (dotenv == null) {
            return Collections.emptyMap();
        }

        Map<String, String> values = new HashMap<>();
        try {
            List<String> lines = Files.readAllLines(dotenv);
            for (String line : lines) {
                parseLine(line, values);
            }
        } catch (IOException e) {
            System.err.println("Aviso: nao foi possivel ler " + dotenv + ": " + e.getMessage());
        }
        return values;
    }

    private static Path findDotenv() {
        Path current = Path.of("").toAbsolutePath();
        Path[] candidates = new Path[] {
            current.resolve(".env"),
            current.resolve("../.env").normalize(),
            current.resolve("../../.env").normalize()
        };

        for (Path candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static void parseLine(String line, Map<String, String> values) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return;
        }
        if (trimmed.startsWith("export ")) {
            trimmed = trimmed.substring("export ".length()).trim();
        }

        int separator = trimmed.indexOf('=');
        if (separator <= 0) {
            return;
        }

        String key = trimmed.substring(0, separator).trim();
        String value = trimmed.substring(separator + 1).trim();
        values.put(key, unquote(value));
    }

    private static String unquote(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
