package pl.skyrise.mCPhone.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Interfejs dla aplikacji telefonu.
 * Zaimplementuj ten interfejs aby stworzyć własną aplikację.
 *
 * Przykład użycia:
 * <pre>
 * public class MyApp implements PhoneApp {
 *     @Override
 *     public String getId() { return "my-app"; }
 *
 *     @Override
 *     public String getName() { return "&fMoja Aplikacja"; }
 *
 *     @Override
 *     public void onOpen(Player player) {
 *         // Otwórz GUI swojej aplikacji
 *     }
 * }
 * </pre>
 */
public interface PhoneApp {

    /**
     * Unikalny identyfikator aplikacji.
     * Musi być unikalny wśród wszystkich zarejestrowanych aplikacji.
     * Używaj formatu: "nazwa-pluginu-nazwa-aplikacji"
     */
    String getId();

    /**
     * Nazwa wyświetlana aplikacji (obsługuje kody kolorów).
     */
    String getName();

    /**
     * Opis aplikacji.
     */
    String getDescription();

    /**
     * Kategoria aplikacji.
     */
    String getCategory();

    /**
     * Ikona aplikacji wyświetlana na pulpicie.
     */
    ItemStack getIcon();

    /**
     * Wywoływane gdy gracz otwiera aplikację (klika na ikonę).
     * Tutaj otwierasz swoje GUI lub wykonujesz logikę.
     *
     * @param player Gracz otwierający aplikację
     */
    void onOpen(Player player);

    /**
     * Wywoływane gdy gracz zamyka aplikację.
     *
     * @param player Gracz zamykający aplikację
     */
    default void onClose(Player player) {}

    /**
     * Wywoływane gdy aplikacja jest instalowana.
     *
     * @param player Gracz instalujący aplikację
     */
    default void onInstall(Player player) {}

    /**
     * Wywoływane gdy aplikacja jest odinstalowywana.
     *
     * @param player Gracz odinstalowujący aplikację
     */
    default void onUninstall(Player player) {}

    /**
     * Cena aplikacji w sklepie (0 = darmowa).
     */
    default double getPrice() { return 0; }

    /**
     * Czy aplikacja ma być widoczna w AppStore.
     * Domyślnie true - aplikacja widoczna w sklepie.
     */
    default boolean isVisibleInStore() { return true; }

    /**
     * Czy aplikacja jest wbudowana (nie można odinstalować).
     * Domyślnie false - można odinstalować.
     */
    default boolean isBuiltIn() { return false; }

    /**
     * Wersja aplikacji.
     */
    default String getVersion() { return "1.0.0"; }

    /**
     * Autor aplikacji.
     */
    default String getAuthor() { return "Unknown"; }

    /**
     * Dodatkowe linie opisu (lore) dla ikony aplikacji.
     */
    default List<String> getLore() { return List.of(); }

    /**
     * Pozycja aplikacji na pulpicie (-1 = automatycznie).
     */
    default int getPosition() { return -1; }

    /**
     * Custom Model Data dla ikony aplikacji.
     */
    default int getCustomModelData() { return 0; }
}
