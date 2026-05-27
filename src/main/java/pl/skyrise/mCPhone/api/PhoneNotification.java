package pl.skyrise.mCPhone.api;

import java.util.UUID;

/**
 * Reprezentuje powiadomienie w telefonie.
 */
public class PhoneNotification {

    private final String id;
    private final String appId;
    private final String title;
    private final String message;
    private final long timestamp;
    private boolean read;

    public PhoneNotification(String appId, String title, String message) {
        this.id = UUID.randomUUID().toString();
        this.appId = appId;
        this.title = title;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
        this.read = false;
    }

    public PhoneNotification(String id, String appId, String title, String message, long timestamp, boolean read) {
        this.id = id;
        this.appId = appId;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.read = read;
    }

    public String getId() {
        return id;
    }

    public String getAppId() {
        return appId;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public void markAsRead() {
        this.read = true;
    }
}
