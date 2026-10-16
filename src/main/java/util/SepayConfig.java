package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SepayConfig {
    private static final List<String> CONFIG_KEYS = Arrays.asList(
            "SEPAY_API_KEY", "SEPAY_BANK_CODE", "SEPAY_ACCOUNT_NUMBER",
            "SEPAY_ACCOUNT_NAME", "PAYMENT_EXPIRE_MINUTES");
    private static final ConfigValues CONFIG = loadConfig();

    public static final String API_KEY = CONFIG.value("SEPAY_API_KEY", "");
    public static final String BANK_CODE = CONFIG.value("SEPAY_BANK_CODE", "");
    public static final String ACCOUNT_NUMBER = CONFIG.value("SEPAY_ACCOUNT_NUMBER", "");
    public static final String ACCOUNT_NAME = CONFIG.value("SEPAY_ACCOUNT_NAME", "");
    public static final int PAYMENT_EXPIRE_MINUTES = parseExpireMinutes(
            CONFIG.value("PAYMENT_EXPIRE_MINUTES", "15"));

    private static int parseExpireMinutes(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 15;
        }
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }

    public static boolean isConfigured() {
        return isWebhookConfigured() && isQrConfigured();
    }

    public static boolean isWebhookConfigured() {
        return !API_KEY.isEmpty();
    }

    public static boolean isQrConfigured() {
        return !ACCOUNT_NUMBER.isEmpty() && !BANK_CODE.isEmpty();
    }

    private static ConfigValues loadConfig() {
        Path envPath = findEnvPath();
        Map<String, String> fileValues = envPath == null
                ? Collections.emptyMap() : readEnvFile(envPath);
        Map<String, String> resolved = new LinkedHashMap<>();
        for (String key : CONFIG_KEYS) {
            String value = firstNonBlank(System.getenv(key),
                    System.getProperty(key), fileValues.get(key));
            if (value != null) {
                resolved.put(key, value);
            }
        }

        StringBuilder present = new StringBuilder();
        StringBuilder missing = new StringBuilder();
        for (String key : CONFIG_KEYS) {
            if (resolved.containsKey(key) && !resolved.get(key).trim().isEmpty()) {
                appendName(present, key);
            } else {
                appendName(missing, key);
            }
        }
        System.err.println("[SepayConfig] .env path: "
                + (envPath == null ? "<not found>" : envPath.toAbsolutePath().normalize()));
        System.err.println("[SepayConfig] variables present: " + present);
        System.err.println("[SepayConfig] variables missing: " + missing);
        return new ConfigValues(resolved);
    }

    private static void appendName(StringBuilder target, String name) {
        if (target.length() > 0) {
            target.append(", ");
        }
        target.append(name);
    }

    private static Map<String, String> readEnvFile(Path envPath) {
        try (BufferedReader reader = Files.newBufferedReader(envPath, StandardCharsets.UTF_8)) {
            return EnvFileParser.parse(reader);
        } catch (IOException e) {
            System.err.println("[SepayConfig] Cannot read .env file: "
                    + envPath.toAbsolutePath().normalize());
            return Collections.emptyMap();
        }
    }

    private static Path findEnvPath() {
        Path userDir = pathOrNull(System.getProperty("user.dir"));
        if (userDir != null) {
            Path userEnv = userDir.resolve(".env");
            if (Files.isRegularFile(userEnv)) {
                return userEnv;
            }
        }

        try {
            CodeSource codeSource = SepayConfig.class.getProtectionDomain().getCodeSource();
            if (codeSource != null && codeSource.getLocation() != null) {
                Path classLocation = Paths.get(codeSource.getLocation().toURI());
                if (Files.isRegularFile(classLocation)) {
                    classLocation = classLocation.getParent();
                }
                Path fromClasses = findInAncestors(classLocation);
                if (fromClasses != null) {
                    return fromClasses;
                }
            }
        } catch (Exception e) {
            System.err.println("[SepayConfig] Cannot inspect class location");
        }
        return null;
    }

    private static Path findInAncestors(Path start) {
        Path current = start;
        while (current != null) {
            Path candidate = current.resolve(".env");
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }
        return null;
    }

    private static Path pathOrNull(String value) {
        return value == null || value.trim().isEmpty() ? null : Paths.get(value);
    }

    private static final class ConfigValues {
        private final Map<String, String> values;

        private ConfigValues(Map<String, String> values) {
            this.values = values;
        }

        private String value(String key, String defaultValue) {
            String value = values.get(key);
            return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
        }
    }
}
