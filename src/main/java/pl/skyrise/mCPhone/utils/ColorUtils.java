package pl.skyrise.mCPhone.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Narzędzia do obsługi kolorów w Minecraft
 */
public class ColorUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    private ColorUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Koloruje tekst z obsługą kodów &x i hex &#RRGGBB
     */
    public static String colorize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // Obsługa hex kolorów &#RRGGBB
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hexCode = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hexCode.toCharArray()) {
                replacement.append("§").append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);
        text = buffer.toString();

        // Zamiana & na § dla standardowych kodów kolorów
        return text.replace("&", "§");
    }

    /**
     * Koloruje listę tekstów
     */
    public static List<String> colorize(List<String> texts) {
        return texts.stream()
            .map(ColorUtils::colorize)
            .collect(Collectors.toList());
    }

    /**
     * Konwertuje tekst na Adventure Component
     */
    public static Component toComponent(String text) {
        return LEGACY_SERIALIZER.deserialize(colorize(text));
    }

    /**
     * Usuwa kody kolorów z tekstu
     */
    public static String stripColor(String text) {
        if (text == null) {
            return null;
        }
        return text.replaceAll("§[0-9a-fk-orA-FK-OR]", "")
                   .replaceAll("&[0-9a-fk-orA-FK-OR]", "");
    }

    /**
     * Sprawdza czy tekst zawiera kody kolorów
     */
    public static boolean hasColor(String text) {
        if (text == null) {
            return false;
        }
        return text.contains("§") || text.contains("&");
    }
}
