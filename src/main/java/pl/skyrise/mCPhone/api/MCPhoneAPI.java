package pl.skyrise.mCPhone.api;

import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * Główne API pluginu MCPhone
 * Umożliwia rejestrację aplikacji, wysyłanie SMS,
 * zarządzanie powiadomieniami i więcej.
 */
public interface MCPhoneAPI {

    // ═══════════════════════════════════════════
    // REJESTRACJA APLIKACJI
    // ═══════════════════════════════════════════

    /**
     * Rejestruje nową aplikację w systemie telefonu.
     * Aplikacja pojawi się w AppStore lub bezpośrednio na pulpicie.
     *
     * @param app Obiekt aplikacji implementujący PhoneApp
     * @return true jeśli rejestracja się powiodła
     */
    boolean registerApp(PhoneApp app);

    /**
     * Wyrejestrowuje aplikację z systemu.
     *
     * @param appId Identyfikator aplikacji
     * @return true jeśli wyrejestrowanie się powiodło
     */
    boolean unregisterApp(String appId);

    /**
     * Pobiera listę wszystkich zarejestrowanych aplikacji.
     *
     * @return Lista aplikacji
     */
    List<PhoneApp> getRegisteredApps();

    /**
     * Sprawdza czy aplikacja o danym ID jest zarejestrowana.
     *
     * @param appId Identyfikator aplikacji
     * @return true jeśli aplikacja jest zarejestrowana
     */
    boolean isAppRegistered(String appId);

    // ═══════════════════════════════════════════
    // SYSTEM WIADOMOŚCI SMS
    // ═══════════════════════════════════════════

    /**
     * Wysyła wiadomość SMS od jednego gracza do drugiego.
     *
     * @param sender UUID nadawcy
     * @param receiver UUID odbiorcy
     * @param message Treść wiadomości
     * @return true jeśli wiadomość została wysłana
     */
    boolean sendSMS(UUID sender, UUID receiver, String message);

    /**
     * Wysyła wiadomość SMS na konkretny numer telefonu.
     *
     * @param sender UUID nadawcy
     * @param phoneNumber Numer telefonu odbiorcy
     * @param message Treść wiadomości
     * @return true jeśli wiadomość została wysłana
     */
    boolean sendSMS(UUID sender, String phoneNumber, String message);

    /**
     * Pobiera historię wiadomości między dwoma graczami.
     *
     * @param player1 UUID pierwszego gracza
     * @param player2 UUID drugiego gracza
     * @param limit Maksymalna ilość wiadomości
     * @return Lista wiadomości SMS
     */
    List<SMSMessage> getMessageHistory(UUID player1, UUID player2, int limit);

    // ═══════════════════════════════════════════
    // SYSTEM NUMERÓW TELEFONÓW
    // ═══════════════════════════════════════════

    /**
     * Pobiera numer telefonu gracza.
     *
     * @param playerUUID UUID gracza
     * @return Numer telefonu lub null jeśli nie ma
     */
    String getPhoneNumber(UUID playerUUID);

    /**
     * Ustawia numer telefonu graczowi.
     *
     * @param playerUUID UUID gracza
     * @param phoneNumber Nowy numer telefonu
     * @return true jeśli numer został ustawiony
     */
    boolean setPhoneNumber(UUID playerUUID, String phoneNumber);

    /**
     * Sprawdza czy gracz ma aktywną kartę SIM.
     *
     * @param playerUUID UUID gracza
     * @return true jeśli gracz ma aktywną kartę SIM
     */
    boolean hasActiveSIM(UUID playerUUID);

    /**
     * Pobiera UUID gracza po numerze telefonu.
     *
     * @param phoneNumber Numer telefonu
     * @return UUID gracza lub null
     */
    UUID getPlayerByNumber(String phoneNumber);

    // ═══════════════════════════════════════════
    // SYSTEM POWIADOMIEŃ
    // ═══════════════════════════════════════════

    /**
     * Wysyła powiadomienie do gracza na telefon.
     *
     * @param playerUUID UUID gracza
     * @param notification Obiekt powiadomienia
     */
    void sendNotification(UUID playerUUID, PhoneNotification notification);

    /**
     * Pobiera listę nieprzeczytanych powiadomień gracza.
     *
     * @param playerUUID UUID gracza
     * @return Lista powiadomień
     */
    List<PhoneNotification> getUnreadNotifications(UUID playerUUID);

    /**
     * Czyści wszystkie powiadomienia gracza.
     *
     * @param playerUUID UUID gracza
     */
    void clearNotifications(UUID playerUUID);

    // ═══════════════════════════════════════════
    // SYSTEM TELEFONÓW
    // ═══════════════════════════════════════════

    /**
     * Sprawdza czy gracz ma telefon w ekwipunku.
     *
     * @param player Gracz
     * @return true jeśli gracz ma telefon
     */
    boolean hasPhone(Player player);

    /**
     * Daje graczowi telefon.
     *
     * @param player Gracz
     */
    void givePhone(Player player);

    /**
     * Otwiera GUI telefonu dla gracza.
     *
     * @param player Gracz
     */
    void openPhone(Player player);

    /**
     * Pobiera instancję API.
     *
     * @return Instancja MCPhoneAPI
     */
    static MCPhoneAPI getInstance() {
        return MCPhoneProvider.getAPI();
    }
}
