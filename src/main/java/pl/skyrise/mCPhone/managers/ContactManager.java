package pl.skyrise.mCPhone.managers;

import pl.skyrise.mCPhone.MCPhone;
import pl.skyrise.mCPhone.models.Contact;
import pl.skyrise.mCPhone.models.PhoneUser;

import java.util.List;
import java.util.UUID;

/**
 * Manager kontaktów
 */
public class ContactManager {

    private final MCPhone plugin;

    public ContactManager(MCPhone plugin) {
        this.plugin = plugin;
    }

    /**
     * Dodaje kontakt do listy użytkownika
     */
    public boolean addContact(UUID ownerUUID, String name, String phoneNumber) {
        PhoneUser user = plugin.getPhoneManager().getOrCreateUser(ownerUUID);
        
        // Sprawdź czy kontakt już istnieje
        if (user.hasContact(phoneNumber)) {
            return false;
        }
        
        // Znajdź UUID gracza powiązanego z numerem
        UUID playerUUID = plugin.getPhoneManager().getPlayerByNumber(phoneNumber);
        
        Contact contact = new Contact(name, phoneNumber, playerUUID);
        user.addContact(contact);
        plugin.getPhoneManager().saveUser(user);
        
        return true;
    }

    /**
     * Usuwa kontakt
     */
    public boolean removeContact(UUID ownerUUID, String phoneNumber) {
        PhoneUser user = plugin.getPhoneManager().getUser(ownerUUID);
        if (user == null) return false;
        
        if (!user.hasContact(phoneNumber)) {
            return false;
        }
        
        user.removeContact(phoneNumber);
        plugin.getPhoneManager().saveUser(user);
        
        return true;
    }

    /**
     * Edytuje nazwę kontaktu
     */
    public boolean editContact(UUID ownerUUID, String phoneNumber, String newName) {
        PhoneUser user = plugin.getPhoneManager().getUser(ownerUUID);
        if (user == null) return false;
        
        Contact contact = user.getContact(phoneNumber);
        if (contact == null) {
            return false;
        }
        
        contact.setName(newName);
        plugin.getPhoneManager().saveUser(user);
        
        return true;
    }

    /**
     * Pobiera listę kontaktów użytkownika
     */
    public List<Contact> getContacts(UUID ownerUUID) {
        PhoneUser user = plugin.getPhoneManager().getUser(ownerUUID);
        if (user == null) return List.of();
        return user.getContacts();
    }

    /**
     * Pobiera kontakt po numerze telefonu
     */
    public Contact getContact(UUID ownerUUID, String phoneNumber) {
        PhoneUser user = plugin.getPhoneManager().getUser(ownerUUID);
        if (user == null) return null;
        return user.getContact(phoneNumber);
    }

    /**
     * Pobiera kontakt po nazwie
     */
    public Contact getContactByName(UUID ownerUUID, String name) {
        PhoneUser user = plugin.getPhoneManager().getUser(ownerUUID);
        if (user == null) return null;
        return user.getContactByName(name);
    }

    /**
     * Sprawdza czy użytkownik ma kontakt z danym numerem
     */
    public boolean hasContact(UUID ownerUUID, String phoneNumber) {
        PhoneUser user = plugin.getPhoneManager().getUser(ownerUUID);
        if (user == null) return false;
        return user.hasContact(phoneNumber);
    }

    /**
     * Pobiera wyświetlaną nazwę dla numeru (kontakt lub sam numer)
     */
    public String getDisplayName(UUID ownerUUID, String phoneNumber) {
        PhoneUser user = plugin.getPhoneManager().getUser(ownerUUID);
        if (user == null) return phoneNumber;
        return user.getContactNameOrNumber(phoneNumber);
    }
}
