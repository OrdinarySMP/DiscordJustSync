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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tronka.justsync.JustSyncApplication;
import tronka.justsync.Utils;
import tronka.justsync.config.Config;
import tronka.justsync.core.view.linking.PlayerLink;

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
        for (Entry<String, List<String>> sync :
            config.integrations.luckPerms.syncedRoles.entrySet()) {
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
        luckPerms.getUserManager().loadUser(link.get().getPlayerId()).thenAccept(user -> {
            Set<String> applyGroups = new HashSet<>();
            Set<String> removeGroups = new HashSet<>();
            for (Entry<Role, List<String>> sync : this.syncedRoles.entrySet()) {
                if (member.getRoles().contains(sync.getKey())) {
                    applyGroups.addAll(sync.getValue());
                } else {
                    removeGroups.addAll(sync.getValue());
                }
            }
            removeGroups.removeAll(applyGroups);
            if (applyGroups.isEmpty() && removeGroups.isEmpty()) {
                return;
            }
            for (String group : applyGroups) {
                user.data().add(LuckPermsHelper.getNode(group));
            }
            for (String group : removeGroups) {
                user.data().remove(LuckPermsHelper.getNode(group));
            }
            luckPerms.getUserManager().saveUser(user);
        });
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
            Set<String> groups = Set.copyOf(this.integration.getConfig()
                    .integrations.luckPerms.altGroups.stream()
                    .map(group -> "group." + group)
                    .toList());
            user.data().clear(node -> groups.contains(node.getKey()));
            luckPerms.getUserManager().saveUser(user);
        });
    }
}
