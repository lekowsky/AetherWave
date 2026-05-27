package pl.skyrise.mCPhone.models;

import java.util.UUID;

/**
 * Model karty SIM w ekwipunku gracza
 */
public class SimCard {

    private final String id;
    private final String typeId;
    private final String phoneNumber;
    private final long createdAt;
    private boolean activated;

    public SimCard(String typeId, String phoneNumber) {
        this.id = UUID.randomUUID().toString();
        this.typeId = typeId;
        this.phoneNumber = phoneNumber;
        this.createdAt = System.currentTimeMillis();
        this.activated = false;
    }

    public SimCard(String id, String typeId, String phoneNumber, long createdAt, boolean activated) {
        this.id = id;
        this.typeId = typeId;
        this.phoneNumber = phoneNumber;
        this.createdAt = createdAt;
        this.activated = activated;
    }

    public String getId() {
        return id;
    }

    public String getTypeId() {
        return typeId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    @Override
    public String toString() {
        return "SimCard{typeId='" + typeId + "', phoneNumber='" + phoneNumber + "', activated=" + activated + "}";
    }
}
