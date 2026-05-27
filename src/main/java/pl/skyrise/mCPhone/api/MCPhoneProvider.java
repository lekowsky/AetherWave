package pl.skyrise.mCPhone.api;

/**
 * Provider dla MCPhoneAPI.
 * Używany do rejestracji i pobierania instancji API.
 */
public class MCPhoneProvider {

    private static MCPhoneAPI api;

    private MCPhoneProvider() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    /**
     * Rejestruje instancję API.
     * Wywoływane przez plugin MCPhone przy starcie.
     *
     * @param apiInstance Instancja API
     */
    public static void register(MCPhoneAPI apiInstance) {
        if (api != null) {
            throw new IllegalStateException("MCPhoneAPI is already registered!");
        }
        api = apiInstance;
    }

    /**
     * Wyrejestrowuje API.
     * Wywoływane przez plugin MCPhone przy wyłączaniu.
     */
    public static void unregister() {
        api = null;
    }

    /**
     * Pobiera instancję API.
     *
     * @return Instancja MCPhoneAPI lub null jeśli plugin nie jest włączony
     */
    public static MCPhoneAPI getAPI() {
        return api;
    }

    /**
     * Sprawdza czy API jest dostępne.
     *
     * @return true jeśli API jest zarejestrowane
     */
    public static boolean isAvailable() {
        return api != null;
    }
}
