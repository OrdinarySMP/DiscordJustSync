package tronka.justsync.compat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.fabricmc.loader.api.FabricLoader;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tronka.justsync.JustSyncApplication;
import tronka.justsync.Utils;
import tronka.justsync.config.Config;
import tronka.justsync.linking.PlayerLink;

public class LuckPermsIntegration {

    private static final Logger log = LoggerFactory.getLogger(LuckPermsIntegration.class);
    private final JustSyncApplication integration;
    private boolean loaded = false;
    private Map<Role, List<String>> syncedRoles = Map.of();

    public LuckPermsIntegration(JustSyncApplication integration) {
        this.integration = integration;
        if (!FabricLoader.getInstance().isModLoaded("luckperms")) {
            return;
        }
        integration.registerConfigReloadHandler(this::onConfigLoaded);
    }

    private void onConfigLoaded(Config config) {
        this.loaded = config.integrations.enableLuckPermsIntegration;
        this.syncedRoles = new HashMap<>();
        for (Entry<String, List<String>> sync : config.integrations.luckPerms.syncedRoles.entrySet()) {
            Optional<Role> role = Utils.parseRole(this.integration.getGuild(), sync.getKey());
            role.ifPresent(value -> this.syncedRoles.put(value, sync.getValue()));
        }
    }

    public void evaluateRolesFor(Member member) {
        final LuckPerms luckPerms = this.getLuckPerms();
        if (luckPerms == null) {
            return;
        }
        Optional<PlayerLink> link = this.integration.getLinkManager().getDataOf(member.getIdLong());
        if (link.isEmpty()) {
            return;
        }

        List<UUID> uuids =
                this.integration.getConfig().integrations.luckPerms.assignSyncedRolesToAlts
                        ? link.get().getAllUuids()
                        : List.of(link.get().getPlayerId());

        uuids.forEach(uuid -> {
            luckPerms.getUserManager().loadUser(uuid).thenAccept(user -> {
                Map<String, Boolean> groups = this.getGroups(member);
                applyGroups(user, groups);
            });
        });
    }

    public void removeAllSyncedRoles(UUID altUuid) {
        this.removeAllSyncedRoles(List.of(altUuid));
    }

    public void removeAllSyncedRoles(List<UUID> uuids) {
        final LuckPerms luckPerms = this.getLuckPerms();
        if (luckPerms == null) {
            return;
        }
        uuids.forEach(uuid -> {
            luckPerms.getUserManager().loadUser(uuid).thenAccept(user -> {
                Map<String, Boolean> groups = new HashMap<>();
                this.syncedRoles.values().forEach(groupList -> 
                    groupList.forEach(group -> groups.put(group, false))
                );
                applyGroups(user, groups);
            });
        });
    }

    private void applyGroups(User user, Map<String, Boolean> groups) {
        if (groups.isEmpty()) {
            return;
        }
        for (Entry<String, Boolean> group : groups.entrySet()) {
            if (group.getValue()) {
                user.data().add(LuckPermsHelper.getNode(group.getKey()));
            } else {
                user.data().remove(LuckPermsHelper.getNode(group.getKey()));
            }
        }
        this.getLuckPerms().getUserManager().saveUser(user);
    }

    private Map<String, Boolean> getGroups(Member member) {
        Map<String, Boolean> groups = new HashMap<>();
        for (Entry<Role, List<String>> sync : this.syncedRoles.entrySet()) {
            if (member.getRoles().contains(sync.getKey())) {
                sync.getValue().forEach(v -> groups.put(v, true));
            } else {
                sync.getValue().forEach(v -> groups.putIfAbsent(v, false));
            }
        }
        return groups;
    }

    private LuckPerms getLuckPerms() {
        if (!this.loaded) {
            return null;
        }
        try {
            return LuckPermsProvider.get();
        } catch (Exception ignored) {
            return null;
        }
    }

    public void setAlt(UUID uuid) {
        final LuckPerms luckPerms = this.getLuckPerms();
        if (luckPerms == null) {
            return;
        }
        luckPerms.getUserManager().loadUser(uuid).thenAccept(user -> {
            for (String group : this.integration.getConfig().integrations.luckPerms.altGroups) {
                user.data().add(LuckPermsHelper.getNode(group));
            }
            luckPerms.getUserManager().saveUser(user);
        });
    }

    public void unsetAlt(UUID uuid) {
        final LuckPerms luckPerms = this.getLuckPerms();
        if (luckPerms == null) {
            return;
        }
        luckPerms.getUserManager().loadUser(uuid).thenAccept(user -> {
            Set<String> groups = Set.copyOf(
                this.integration.getConfig().integrations.luckPerms.altGroups
                    .stream().map(group -> "group." + group).toList());
            user.data().clear(node -> groups.contains(node.getKey()));
            luckPerms.getUserManager().saveUser(user);
        });
    }
}
