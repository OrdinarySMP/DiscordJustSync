package tronka.justsync.config;

public class MessageFormat {

    public SendType type;
    public String format;
    public Integer embedColor;
    public String webhookName;

    public MessageFormat(SendType type, String format, Integer embedColor, String webhookName) {
        this.type = type;
        this.format = format;
        this.embedColor = embedColor;
        this.webhookName = webhookName;
    }

    public enum SendType {
        WEBHOOK,
        DEFAULT,
        EMBED
    }
}
