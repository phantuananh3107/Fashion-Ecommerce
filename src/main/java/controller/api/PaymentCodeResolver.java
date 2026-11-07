package controller.api;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PaymentCodeResolver {
    private static final Pattern PAYMENT_CODE_PATTERN =
            Pattern.compile("(?<![\\w\\d])(DH\\d+)(?![\\d])", Pattern.CASE_INSENSITIVE);

    private PaymentCodeResolver() {
    }

    public static String resolve(String code, String content) {
        if (code != null && !code.trim().isEmpty()) {
            return code.trim().toUpperCase();
        }
        if (content == null || content.trim().isEmpty()) {
            return null;
        }

        Matcher matcher = PAYMENT_CODE_PATTERN.matcher(content);
        List<String> found = new ArrayList<>();
        while (matcher.find()) {
            String candidate = matcher.group(1).toUpperCase();
            if (!found.contains(candidate)) {
                found.add(candidate);
            }
        }
        return found.size() == 1 ? found.get(0) : null;
    }
}
