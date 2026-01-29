package tronka.justsync.config;

import com.moandjiezana.toml.comments.TomlNullComment;

public class MessageFormat {

    public SendType type;
    public String format;
    @TomlNullComment("\"FF0000\"")
    public String embedColor;
    @TomlNullComment("\"%user%\"")
    public String webhookName;

    public MessageFormat(SendType type, String format, String embedColor, String webhookName) {
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
