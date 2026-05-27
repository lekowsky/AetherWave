package pl.skyrise.mCPhone.utils;

import pl.skyrise.mCPhone.MCPhone;

import java.util.Random;

/**
 * Generator numerów telefonów
 */
public class NumberGenerator {

    private static final Random RANDOM = new Random();

    private NumberGenerator() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Generuje losowy numer telefonu w formacie z konfiguracji
     */
    public static String generate() {
        MCPhone plugin = MCPhone.getInstance();
        String format = plugin.getConfigManager().getPhoneNumberFormat();
        int length = plugin.getConfigManager().getPhoneNumberLength();
        
        StringBuilder number = new StringBuilder();
        int digitsAdded = 0;
        
        for (char c : format.toCharArray()) {
            if (c == '#' && digitsAdded < length) {
                number.append(RANDOM.nextInt(10));
                digitsAdded++;
            } else if (c != '#') {
                number.append(c);
            }
        }
        
        // Dopełnij brakujące cyfry
        while (digitsAdded < length) {
            number.append(RANDOM.nextInt(10));
            digitsAdded++;
        }
        
        return number.toString();
    }

    /**
     * Generuje unikalny numer telefonu (sprawdza czy nie jest już zajęty)
     */
    public static String generateUnique() {
        MCPhone plugin = MCPhone.getInstance();
        String number;
        int attempts = 0;
        int maxAttempts = 1000;
        
        do {
            number = generate();
            attempts++;
            if (attempts > maxAttempts) {
                throw new RuntimeException("Nie można wygenerować unikalnego numeru telefonu po " + maxAttempts + " próbach!");
            }
        } while (plugin.getPhoneManager().isNumberTaken(number));
        
        return number;
    }

    /**
     * Formatuje numer do standardowego formatu
     */
    public static String format(String number) {
        if (number == null) return null;
        
        // Usuń wszystkie znaki inne niż cyfry
        String digits = number.replaceAll("[^0-9]", "");
        
        MCPhone plugin = MCPhone.getInstance();
        String format = plugin.getConfigManager().getPhoneNumberFormat();
        
        StringBuilder formatted = new StringBuilder();
        int digitIndex = 0;
        
        for (char c : format.toCharArray()) {
            if (c == '#') {
                if (digitIndex < digits.length()) {
                    formatted.append(digits.charAt(digitIndex));
                    digitIndex++;
                }
            } else {
                formatted.append(c);
            }
        }
        
        return formatted.toString();
    }

    /**
     * Sprawdza czy numer jest w poprawnym formacie
     */
    public static boolean isValid(String number) {
        if (number == null || number.isEmpty()) return false;
        
        MCPhone plugin = MCPhone.getInstance();
        String format = plugin.getConfigManager().getPhoneNumberFormat();
        
        // Usuń wszystkie znaki inne niż cyfry z obu
        String numberDigits = number.replaceAll("[^0-9]", "");
        String formatDigits = format.replaceAll("[^#]", "");
        
        return numberDigits.length() == formatDigits.length();
    }

    /**
     * Normalizuje numer (usuwa formatowanie, pozostawia tylko cyfry)
     */
    public static String normalize(String number) {
        if (number == null) return null;
        return number.replaceAll("[^0-9]", "");
    }

    /**
     * Porównuje dwa numery (ignorując formatowanie)
     */
    public static boolean equals(String number1, String number2) {
        if (number1 == null || number2 == null) return false;
        return normalize(number1).equals(normalize(number2));
    }
}
