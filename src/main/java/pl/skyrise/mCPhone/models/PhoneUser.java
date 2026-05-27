package pl.skyrise.mCPhone.models;

import java.util.*;

/**
 * Model użytkownika telefonu
 */
public class PhoneUser {

    private final UUID uuid;
    private String phoneNumber;
    private String simType;
    private boolean simActive;
    private int dailySmsCount;
    private long lastSmsReset;
    private final Set<String> installedApps;
    private final List<Contact> contacts;

    public PhoneUser(UUID uuid) {
        this.uuid = uuid;
        this.phoneNumber = null;
        this.simType = null;
        this.simActive = false;
        this.dailySmsCount = 0;
        this.lastSmsReset = System.currentTimeMillis();
        this.installedApps = new HashSet<>();
        this.contacts = new ArrayList<>();
    }

    public PhoneUser(UUID uuid, String phoneNumber, String simType, boolean simActive,
                     int dailySmsCount, long lastSmsReset, Set<String> installedApps, List<Contact> contacts) {
        this.uuid = uuid;
        this.phoneNumber = phoneNumber;
        this.simType = simType;
        this.simActive = simActive;
        this.dailySmsCount = dailySmsCount;
        this.lastSmsReset = lastSmsReset;
        this.installedApps = installedApps != null ? new HashSet<>(installedApps) : new HashSet<>();
        this.contacts = contacts != null ? new ArrayList<>(contacts) : new ArrayList<>();
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSimType() {
        return simType;
    }

    public void setSimType(String simType) {
        this.simType = simType;
    }

    public boolean hasActiveSim() {
        return simActive && simType != null;
    }

    public boolean isSimActive() {
        return simActive;
    }

    public void setSimActive(boolean simActive) {
        this.simActive = simActive;
    }

    public int getDailySmsCount() {
        return dailySmsCount;
    }

    public void incrementSmsCount() {
        this.dailySmsCount++;
    }

    public void resetSmsCount() {
        this.dailySmsCount = 0;
        this.lastSmsReset = System.currentTimeMillis();
    }

    public long getLastSmsReset() {
        return lastSmsReset;
    }

    public void setLastSmsReset(long lastSmsReset) {
        this.lastSmsReset = lastSmsReset;
    }

    public void setDailySmsCount(int dailySmsCount) {
        this.dailySmsCount = dailySmsCount;
    }

    /**
     * Sprawdza czy minął dzień od ostatniego resetu licznika SMS
     */
    public boolean shouldResetSmsCount() {
        long dayInMillis = 24 * 60 * 60 * 1000L;
        return System.currentTimeMillis() - lastSmsReset > dayInMillis;
    }

    // Zainstalowane aplikacje
    public Set<String> getInstalledApps() {
        return new HashSet<>(installedApps);
    }

    public boolean hasAppInstalled(String appId) {
        return installedApps.contains(appId);
    }

    public void installApp(String appId) {
        installedApps.add(appId);
    }

    public void uninstallApp(String appId) {
        installedApps.remove(appId);
    }

    // Kontakty
    public List<Contact> getContacts() {
        return new ArrayList<>(contacts);
    }

    public Contact getContact(String phoneNumber) {
        return contacts.stream()
            .filter(c -> c.getPhoneNumber().equals(phoneNumber))
            .findFirst()
            .orElse(null);
    }

    public Contact getContactByName(String name) {
        return contacts.stream()
            .filter(c -> c.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    }

    public void addContact(Contact contact) {
        contacts.add(contact);
    }

    public void removeContact(String phoneNumber) {
        contacts.removeIf(c -> c.getPhoneNumber().equals(phoneNumber));
    }

    public void removeContact(Contact contact) {
        contacts.remove(contact);
    }

    public boolean hasContact(String phoneNumber) {
        return contacts.stream().anyMatch(c -> c.getPhoneNumber().equals(phoneNumber));
    }

    /**
     * Pobiera nazwę kontaktu dla danego numeru lub zwraca numer jeśli nie ma kontaktu
     */
    public String getContactNameOrNumber(String phoneNumber) {
        Contact contact = getContact(phoneNumber);
        return contact != null ? contact.getName() : phoneNumber;
    }
}
