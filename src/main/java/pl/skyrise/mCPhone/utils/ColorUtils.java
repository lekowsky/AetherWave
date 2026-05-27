package pl.skyrise.mCPhone.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Narzędzia do obsługi kolorów w Minecraft
 * NAPRAWIONE - dodano obsługę MiniMessage i raw Unicode dla tytułów GUI
 */
public class ColorUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final LegacyComponentSerializer LEGACY_SERIALIZER =
            LegacyComponentSerializer.legacyAmpersand();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

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
     * Konwertuje tekst na Adventure Component (legacy - dla zwykłych tekstów)
     * Zachowana dla kompatybilności z resztą pluginu
     */
    public static Component toComponent(String text) {
        return LEGACY_SERIALIZER.deserialize(colorize(text));
    }

    // =====================================================
    // NOWE METODY - dla tytułów GUI z obsługą offsetu
    // =====================================================

    /**
     * NOWA METODA: Konwertuje tytuł GUI na Component z obsługą negative space.
     *
     * @param title       Tekst tytułu (może zawierać Unicode, MiniMessage, lub legacy)
     * @param offset      Przesunięcie w pikselach (ujemne = lewo, dodatnie = prawo)
     * @param format      Format tytułu: "legacy", "minimessage", lub "raw"
     * @return            Component gotowy do użycia w Bukkit.createInventory()
     */
    public static Component toTitleComponent(String title, int offset, String format) {
        if (title == null || title.isEmpty()) {
            return Component.empty();
        }

        // Buduj Component tytułu w zależności od formatu
        Component titleComponent;
        switch (format.toLowerCase()) {
            case "minimessage" -> {
                titleComponent = MINI_MESSAGE.deserialize(title);
            }
            case "raw" -> {
                // RAW: tytuł jest przekazywany BEZ parsowania
                // Znaki Unicode (w tym negative space) przechodzą bez zmian
                titleComponent = Component.text(title);
            }
            default -> {
                // LEGACY: standardowe parsowanie &a, &b, &#RRGGBB
                titleComponent = LEGACY_SERIALIZER.deserialize(colorize(title));
            }
        }

        // Jeśli offset == 0, zwróć sam tytuł
        if (offset == 0) {
            return titleComponent;
        }

        // Dodaj negative space character PRZED tytułem
        Component shiftComponent = Component.text(getNegativeSpaceChar(offset));

        // Złóż: [shift] + [tytuł]
        return shiftComponent.append(titleComponent);
    }

    /**
     * NOWA METODA: Generuje znak negative space z NegativeSpaceFont.
     *
     * Formuła z NegativeSpaceFont:
     *   Unicode code point = 0xD0000 + offset
     *   Dla offset od -8192 do 8192
     *
     * W Javie wartości > 0xFFFF muszą być jako surrogate pairs.
     *
     * @param pixels Przesunięcie w pikselach (np. -8 = 8px w lewo)
     * @return       String z jednym znakiem negative space (surrogate pair)
     */
    public static String getNegativeSpaceChar(int pixels) {
        // Clamp do zakresu NegativeSpaceFont
        pixels = Math.max(-8192, Math.min(8192, pixels));

        // Code point = 0xD0000 + pixels
        int codePoint = 0xD0000 + pixels;

        // Konwersja na surrogate pair (Java UTF-16)
        return new String(Character.toChars(codePoint));
    }

    /**
     * Tworzy Component z samym negative space (do testowania)
     */
    public static Component createShiftComponent(int pixels) {
        return Component.text(getNegativeSpaceChar(pixels));
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