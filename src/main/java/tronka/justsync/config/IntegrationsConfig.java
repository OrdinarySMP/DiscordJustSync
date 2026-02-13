package tronka.justsync.config;

import com.moandjiezana.toml.comments.TomlComment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IntegrationsConfig {
    @TomlComment("Vanish https://modrinth.com/mod/vanish")
    public boolean enableVanishIntegration = true;
    @TomlComment("Luck Perms https://modrinth.com/plugin/luckperms")
    public boolean enableLuckPermsIntegration = true;
    public Config.ExternalIntegrations.LuckPermsIntegration luckPerms = new Config.ExternalIntegrations.LuckPermsIntegration();
    public Config.ExternalIntegrations.FloodGateIntegration floodgate = new Config.ExternalIntegrations.FloodGateIntegration();

    public static class FloodGateIntegration {
        @TomlComment({"Floodgate https://modrinth.com/mod/floodgate",
                "set to false to disable java players linking bedrock as alts and vice versa"})
        public boolean allowLinkingMixedAccountTypes = true;
        @TomlComment("Discord roles to bypass above restriction if set to false")
        public List<String> allowLinkingMixedAccountTypesBypass = new ArrayList<>();
        @TomlComment("Deny message for wrong account type (response of /link command)")
        public String linkingMixedAccountTypesDenyMessage = "You are not allowed to mix account types on this server";

        @TomlComment("set false to disallow mixed account types to join at the same time")
        public boolean allowJoiningMixedAccountTypes = true;
        @TomlComment("Discord roles to bypass above restrictions if set to false")
        public List<String> allowJoiningMixedAccountTypesBypass = new ArrayList<>();
        @TomlComment("Kick message when trying to join with mixed account types")
        public String joiningMixedAccountTypesKickMessage = "You are not allowed to join with mixed account types at the same time";
    }

    public static class LuckPermsIntegration {

        @TomlComment("Groups to assign to minecraft accounts that are alts")
        public List<String> altGroups = new ArrayList<>();
        @TomlComment("If a discord member has the specified role they will be given the specified groups")
        public Map<String, List<String>> syncedRoles = Map.of("0123456789", List.of("some.group"));
        @TomlComment("Assign synced roles to alternate accounts")
        public boolean assignSyncedRolesToAlts = true;
    }
}
