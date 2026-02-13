package tronka.justsync.config;

import com.moandjiezana.toml.comments.TomlComment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LinkingOptions {
    @TomlComment("Require players to link their Minecraft account to a Discord account before joining")
    public boolean enableLinking = true;
    @TomlComment("Automatically unlink players when they leave the Discord server")
    public boolean unlinkOnLeave = true;
    @TomlComment("Log account linking and unlinking events to a Discord channel")
    public boolean logLinking = false;
    @TomlComment("Discord channel ID for logging linking events (only used if logLinking is true)")
    public String linkingLogChannel = "";
    @TomlComment("Discord roles that players must have to join the Minecraft server (list of role IDs)")
    public List<String> requiredRoles = new ArrayList<>();
    @TomlComment({"Minimum number of roles from requiredRoles list that players must have",
            "-1 means all roles are required, 1 means at least one role is required"})
    public int requiredRolesCount = -1;
    @TomlComment("Discord roles to automatically assign when players join Minecraft")
    public List<String> joinRoles = new ArrayList<>();
    @TomlComment("Automatically set Discord nickname to match Minecraft username when players join")
    public boolean renameOnJoin = true;
    @TomlComment("Prevent Discord users with active timeouts from joining Minecraft")
    public boolean disallowTimeoutMembersToJoin = true;
    @TomlComment("How long (in minutes) a linking code remains valid before expiring")
    public long linkCodeExpireMinutes = 10;
    @TomlComment({"Maximum alternate accounts that can be linked to one Discord account",
            "Example: maxAlts=1 allows 1 main account + 1 alt account per Discord user"})
    public int maxAlts = 1;
    @TomlComment("Per-role overrides for maximum alt accounts (Discord role ID -> max alts)")
    public Map<String, Integer> maxAltsForRoles = Map.of("1234567890", 2);
}
