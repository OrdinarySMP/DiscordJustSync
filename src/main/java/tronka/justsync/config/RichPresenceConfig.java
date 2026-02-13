package tronka.justsync.config;

import com.moandjiezana.toml.comments.TomlComment;

public class RichPresenceConfig {
    @TomlComment("Display online player count in the Discord bot's activity status")
    public boolean showPlayerCountStatus = true;
    @TomlComment("Show the bot as idle when no player is online")
    public boolean showAsIdle = true;

    @TomlComment({
            "Formatting to use for the online player count status if there is more than 1 player online, related to showPlayerCountStatus",
            "Placeholder: %d: Player count"})
    public String onlineCountPlural = "%d players online";
    @TomlComment("Formatting to use for the online player count status if there is 1 player online, related to showPlayerCountStatus")
    public String onlineCountSingular = "1 player online";
    @TomlComment("Formatting to use for the online player count status if there is no player online, related to showPlayerCountStatus")
    public String onlineCountZero = "Server is lonely :(";
}
