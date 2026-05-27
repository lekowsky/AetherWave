package pl.skyrise.mCPhone.api;

import java.util.UUID;

/**
 * Reprezentuje wiadomość SMS.
 */
public class SMSMessage {

    private final String id;
    private final UUID sender;
    private final UUID receiver;
    private final String message;
    private final long timestamp;
    private boolean read;

    public SMSMessage(UUID sender, UUID receiver, String message) {
        this.id = UUID.randomUUID().toString();
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
        this.read = false;
    }

    public SMSMessage(String id, UUID sender, UUID receiver, String message, long timestamp, boolean read) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.timestamp = timestamp;
        this.read = read;
    }

    public String getId() {
        return id;
    }

    public UUID getSender() {
        return sender;
    }

    public UUID getReceiver() {
        return receiver;
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

    /**
     * Sprawdza czy wiadomość należy do konwersacji między dwoma graczami.
     */
    public boolean isInConversation(UUID player1, UUID player2) {
        return (sender.equals(player1) && receiver.equals(player2)) ||
               (sender.equals(player2) && receiver.equals(player1));
    }
}
