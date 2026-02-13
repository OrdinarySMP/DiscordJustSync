package tronka.justsync.config;

import com.moandjiezana.toml.comments.TomlComment;
import com.moandjiezana.toml.comments.TomlMapComment;
import tronka.justsync.events.payload.MessageType;

import java.util.EnumMap;
import java.util.Map;

public class DiscordMessagesConfig {
    @TomlComment({
            "URL template for player profile pictures in webhook messages. Placeholders:",
            "%UUID% - player's uuid",
            "%randomUUID% - random uuid to prevent caching on some apis",
            "%textureId% - texture id instead of uuid (for use with e.g. SkinsRestorer)"
    })
    public String avatarUrl = "https://minotar.net/helm/%UUID%?randomuuid=%randomUUID%";
    @TomlComment("The texture id used when no texture id was found (default is steve)")
    public String defaultAvatarTextureId = "989bf1511ab84c7cbbf3545af36b87dd";


    @TomlComment("Combine multiple identical consecutive messages into one Discord message to reduce spam")
    public boolean stackMessages = false;
    @TomlComment("Time in seconds before sending a new message regardless if it is identical")
    public int stackMessagesTimeoutInSec = 60;

    @TomlComment("Broadcast player death messages to Discord")
    public boolean broadCastDeathMessages = true;
    @TomlComment("Send player advancement/achievement notifications to Discord")
    public boolean announceAdvancements = true;

    @TomlComment({"Format for shared waypoints",
            "Placeholders:",
            "%abbr%: abbreviation of waypoint (voxel defaults to first letter)",
            "%name%: name of waypoint",
            "%x%, %y%, %z%: coordinates",
            "%dimension%: dimension"})
    public String waypointFormat = "Waypoint: (%abbr%)  %name% `%x% %y% %z%` in %dimension%";

    @TomlComment({"Message formats for different message types sent to Discord",
            "Every message type supports the following sending modes:",
            "- DEFAULT: sends a normal message to the channel",
            "- WEBHOOK: sends the message using a webhook with custom username and avatar",
            "- EMBED: sends the message as an embed, with custom title, avatar and color",
            "- DISABLED: does not send the message to Discord"})
    @TomlMapComment(key = "CHAT", value = {"Format for Minecraft chat messages going to Discord",
            "Placeholders:",
            "- %msg%: message",
            "- %user%: the name of the player who sent the message"})
    @TomlMapComment(key = "SERVER_START", value = "Format for the server start message")
    @TomlMapComment(key = "SERVER_STOP", value = "Format for the server stop message")
    @TomlMapComment(key = "JOIN", value = {"Format for player join messages",
            "Placeholder: %user%: the name of the player who joined"})
    @TomlMapComment(key = "LEAVE", value = {"Format for player leave messages",
            "Placeholder: %user%: the name of the player who left"})
    @TomlMapComment(key = "TIMEOUT", value = {"Format for player timeout messages",
            "Placeholders: %user%: the name of the player who timed out"})
    @TomlMapComment(key = "DEATH", value = {"Format for player death messages",
            "Placeholders:",
            "- %msg%: death message",
            "- %user%: the name of the player who died"})
    @TomlMapComment(key = "ADVANCEMENT", value = {"Format for player advancement messages",
            "Placeholders:",
            "- %title%: advancement title",
            "- %description%: advancement description",
            "- %user%: the name of the player who got the advancement",})
    @TomlMapComment(key = "COMMAND_SAY", value = {"Format for player or console executed /say and /me commands",
            "Placeholders:",
            "- %msg%: the message submitted with the command",
            "- %user%: player name or 'Server' for console"})
    public EnumMap<MessageType, MessageFormat> formats = new EnumMap<>(DEFAULT_FORMATS);

    public static final EnumMap<MessageType, MessageFormat> DEFAULT_FORMATS = new EnumMap<>(Map.of(
            MessageType.CHAT, new MessageFormat(MessageFormat.SendType.WEBHOOK, "%msg%", null, "%user%"),
            MessageType.COMMAND_SAY, new MessageFormat(MessageFormat.SendType.DEFAULT, "%user%: %msg%", null, null),
            MessageType.JOIN, new MessageFormat(MessageFormat.SendType.DEFAULT, "%user% joined", null, null),
            MessageType.LEAVE, new MessageFormat(MessageFormat.SendType.DEFAULT, "%user% left", null, null),
            MessageType.TIMEOUT, new MessageFormat(MessageFormat.SendType.DEFAULT, "%user% timed out", null, null),
            MessageType.DEATH, new MessageFormat(MessageFormat.SendType.DEFAULT, "%msg%", null, null),
            MessageType.ADVANCEMENT, new MessageFormat(MessageFormat.SendType.EMBED, "*%description%*", null, "%user% made the advancement %title%"),
            MessageType.SERVER_START, new MessageFormat(MessageFormat.SendType.DEFAULT, "Server started", null, null),
            MessageType.SERVER_STOP, new MessageFormat(MessageFormat.SendType.DEFAULT, "Server stopped", null, null)
    ));
}
