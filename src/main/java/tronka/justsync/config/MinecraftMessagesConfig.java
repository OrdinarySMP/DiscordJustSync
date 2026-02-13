package tronka.justsync.config;

import com.moandjiezana.toml.comments.TomlComment;

import java.util.ArrayList;
import java.util.List;

public class MinecraftMessagesConfig {
    @TomlComment({"Prevent Discord users from using Minecraft formatting codes (§ symbols)",
            "Reference: https://minecraft.wiki/w/Formatting_codes"})
    public boolean restrictFormattingCodes = false;

    @TomlComment("Character to replace '§' formatting codes with. Leave empty to remove formatting code completely.")
    public String formattingCodeReplacement = "";

    @TomlComment("Discord role IDs that can bypass formatting code restrictions")
    public List<String> formattingCodeRestrictionOverrideRoles = new ArrayList<>();

    @TomlComment({"How a normal discord chat message sent in the serverChatChannel should be displayed ingame",
            "Use https://placeholders.pb4.eu/user/text-format/ for more information on formatting",
            "Placeholders: ",
            "%user%: User who sent the message",
            "%msg%: the message",
            "%attachments%: optional attachments such as images and files"})
    public String chatMessageFormat = "[<blue>Discord</blue>] <%user%> %msg% %attachments%";
    @TomlComment({"How a reply to a message sent in the serverChatChannel should be displayed ingame",
            "Use https://placeholders.pb4.eu/user/text-format/ for more information on formatting",
            "Placeholders: ",
            "%user%: User who sent the message",
            "%msg%: The message",
            "%userRepliedTo%: The user whose message was replied to",
            "%attachments%: Optional attachments such as images and files"})
    public String chatMessageFormatReply = " [<blue>Discord</blue>] <%user% replied to %userRepliedTo%> %msg% %attachments%";
    @TomlComment({"The formatting to use for links/urls",
            "Placeholder: %link%: The url"})
    public String linkFormat = "<blue><underline><i><url:'%link%'>%link%</url></i></underline></blue>";
    @TomlComment({"The formatting to use for attachments",
            "Placeholders",
            "%link%: The url of the attached file",
            "%name%: THe name of the attachment"})
    public String attachmentFormat = "[<blue><url:'%link%'>%name%</url></blue>]";
}
