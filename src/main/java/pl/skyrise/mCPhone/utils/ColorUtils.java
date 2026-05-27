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
 * NAPRAWIONE - pełna obsługa MiniMessage <shift:N> jak w DeluxeMenus
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
        if (text == null || text.isEmpty()) return "";

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
        return buffer.toString().replace("&", "§");
    }

    public static List<String> colorize(List<String> texts) {
        return texts.stream().map(ColorUtils::colorize).collect(Collectors.toList());
    }

    public static Component toComponent(String text) {
        return LEGACY_SERIALIZER.deserialize(colorize(text));
    }

    // =========================================================
    // NAPRAWIONA METODA - obsługa <shift:N> jak DeluxeMenus
    // =========================================================

    /**
     * Konwertuje tytuł GUI na Component z obsługą MiniMessage.
     *
     * Tryby (title-format w gui.yml):
     *   - "minimessage" → parsuje <shift:N>, <color:...>, <bold> itp.
     *                     Dokładnie tak samo jak DeluxeMenus!
     *   - "legacy"      → parsuje &a, &b, &#RRGGBB
     *   - "raw"         → zwraca tekst bez żadnego parsowania
     *
     * Przykład w gui.yml:
     *   title: "<shift:-80><white>Mój Telefon</white>"
     *   title-format: "minimessage"
     *
     * @param title  Tekst tytułu z opcjonalnym <shift:N>
     * @param offset Dodatkowe przesunięcie w pikselach (0 = brak)
     *               UWAGA: użyj raczej <shift:N> inline w title!
     * @param format "minimessage", "legacy" lub "raw"
     * @return Component gotowy do Bukkit.createInventory()
     */
    public static Component toTitleComponent(String title, int offset, String format) {
        if (title == null || title.isEmpty()) return Component.empty();

        Component titleComponent;

        switch (format.toLowerCase()) {
            case "minimessage" -> {
                // ✅ POPRAWNE - MiniMessage obsługuje <shift:N> natywnie
                // Działa tak samo jak DeluxeMenus!
                String parsed = title;

                // Jeśli podano dodatkowy offset (title-offset), dodaj <shift:N> na początku
                // ALE tylko jeśli title NIE zawiera już <shift:...>
                if (offset != 0 && !title.contains("<shift:")) {
                    parsed = "<shift:" + offset + ">" + title;
                }

                titleComponent = MINI_MESSAGE.deserialize(parsed);
            }
            case "raw" -> {
                // RAW - przekaż bezpośrednio (dla własnych fontów)
                Component base = Component.text(title);
                if (offset != 0) {
                    // Używamy MiniMessage tylko dla shift prefix
                    Component shiftPrefix = MINI_MESSAGE.deserialize("<shift:" + offset + ">");
                    titleComponent = shiftPrefix.append(base);
                } else {
                    titleComponent = base;
                }
            }
            default -> {
                // LEGACY - &a, &b, &#RRGGBB
                Component base = LEGACY_SERIALIZER.deserialize(colorize(title));
                if (offset != 0) {
                    Component shiftPrefix = MINI_MESSAGE.deserialize("<shift:" + offset + ">");
                    titleComponent = shiftPrefix.append(base);
                } else {
                    titleComponent = base;
                }
            }
        }

        return titleComponent;
    }

    /**
     * Usuwa kody kolorów z tekstu
     */
    public static String stripColor(String text) {
        if (text == null) return null;
        return text.replaceAll("§[0-9a-fk-orA-FK-OR]", "")
                .replaceAll("&[0-9a-fk-orA-FK-OR]", "");
    }

    public static boolean hasColor(String text) {
        if (text == null) return false;
        return text.contains("§") || text.contains("&");
    }
}