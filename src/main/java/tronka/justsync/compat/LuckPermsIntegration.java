package tronka.justsync.compat;

import java.util.Set;
import java.util.UUID;
import net.fabricmc.loader.api.FabricLoader;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import tronka.justsync.JustSyncApplication;

public class LuckPermsIntegration {

    private final JustSyncApplication integration;
    private boolean loaded = false;

    public LuckPermsIntegration(JustSyncApplication integration) {
        this.integration = integration;
        if (!integration.getConfig().integrations.enableLuckPermsIntegration) {
            return;
        }
        if (!FabricLoader.getInstance().isModLoaded("luckperms")) {
            return;
        }
        this.loaded = true;
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
        final LuckPerms luckPerms = getLuckPerms();
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
        final LuckPerms luckPerms = getLuckPerms();
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
