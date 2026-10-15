package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Đọc cấu hình SePay từ file .env theo cùng cách ChatBotConfig.
 * Không in giá trị key/secret ra log.
 */
public class SepayConfig {

    private static final Map<String, String> envProperties = loadEnv();

    public static final String API_KEY              = getEnv("SEPAY_API_KEY",            "");
    public static final String BANK_CODE            = getEnv("SEPAY_BANK_CODE",          "BIDV");
    public static final String ACCOUNT_NUMBER       = getEnv("SEPAY_ACCOUNT_NUMBER",     "");
    public static final String ACCOUNT_NAME         = getEnv("SEPAY_ACCOUNT_NAME",       "");
    public static final int    PAYMENT_EXPIRE_MINUTES;

    static {
        int minutes = 15;
        try {
            String raw = getEnv("PAYMENT_EXPIRE_MINUTES", "15");
            minutes = Integer.parseInt(raw);
        } catch (NumberFormatException ignored) {
            // giữ giá trị mặc định 15
        }
        PAYMENT_EXPIRE_MINUTES = minutes;
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Helpers — theo pattern ChatBotConfig
    // ────────────────────────────────────────────────────────────────────────
    private static Map<String, String> loadEnv() {
        Map<String, String> props = new HashMap<>();
        try {
            File envFile = new File(".env");
            if (!envFile.exists()) envFile = new File("../.env");
            if (envFile.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(envFile))) {
                    readToMap(br, props);
                }
            } else {
                var resource = SepayConfig.class.getClassLoader().getResourceAsStream(".env");
                if (resource != null) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(resource))) {
                        readToMap(br, props);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[SepayConfig] Error loading .env: " + e.getMessage());
        }
        return props;
    }

    private static void readToMap(BufferedReader br, Map<String, String> props) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#") || line.startsWith(";")) continue;
            int eq = line.indexOf('=');
            if (eq > 0) {
                props.put(line.substring(0, eq).trim(), line.substring(eq + 1).trim());
            }
        }
    }

    private static String getEnv(String key, String defaultValue) {
        String value = envProperties.get(key);
        return (value == null || value.isEmpty()) ? defaultValue : value;
    }

    /** Trả true nếu mọi tham số bắt buộc đã được khai báo trong .env. */
    public static boolean isConfigured() {
        return !API_KEY.isEmpty() && !ACCOUNT_NUMBER.isEmpty() && !BANK_CODE.isEmpty();
    }
}
