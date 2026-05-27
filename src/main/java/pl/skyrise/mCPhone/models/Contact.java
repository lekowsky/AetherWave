package pl.skyrise.mCPhone.models;

import java.util.UUID;

/**
 * Model kontaktu telefonicznego
 */
public class Contact {

    private String name;
    private String phoneNumber;
    private UUID playerUUID; // UUID gracza powiązanego z kontaktem (może być null)

    public Contact(String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.playerUUID = null;
    }

    public Contact(String name, String phoneNumber, UUID playerUUID) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.playerUUID = playerUUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public boolean hasPlayerUUID() {
        return playerUUID != null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Contact contact = (Contact) obj;
        return phoneNumber.equals(contact.phoneNumber);
    }

    @Override
    public int hashCode() {
        return phoneNumber.hashCode();
    }

    @Override
    public String toString() {
        return "Contact{name='" + name + "', phoneNumber='" + phoneNumber + "'}";
    }
}
