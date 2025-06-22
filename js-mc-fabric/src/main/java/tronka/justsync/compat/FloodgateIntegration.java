package tronka.justsync.compat;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import org.geysermc.floodgate.api.FloodgateApi;
import tronka.justsync.JustSyncApplication;
import tronka.justsync.core.view.linking.PlayerLink;
import tronka.justsync.linking.LinkManager;

public class FloodgateIntegration {
    private FloodgateApi floodgateApi;
    private JustSyncApplication integration;

    public FloodgateIntegration(JustSyncApplication integration) {
        if (FabricLoader.getInstance().isModLoaded("floodgate")) {
            this.floodgateApi = FloodgateApi.getInstance();
        }
        this.integration = integration;
    }

    public boolean isBedrock(UUID uuid) {
        return this.floodgateApi != null && this.floodgateApi.isFloodgateId(uuid);
    }

    public String getUsername(UUID uuid) {
        if (this.floodgateApi != null) {
            try {
                return this.floodgateApi.getPlayerPrefix()
                    + this.floodgateApi.getGamertagFor(uuid.getLeastSignificantBits()).get();
            } catch (InterruptedException | ExecutionException ignored) {
            }
        }
        return "unknown";
    }

    public boolean isFloodGateName(String name) {
        return this.floodgateApi != null && name.startsWith(this.floodgateApi.getPlayerPrefix());
    }

    public GameProfile getGameProfileFor(String name) {
        String gamerTag = name.replaceFirst(this.floodgateApi.getPlayerPrefix(), "");
        try {
            UUID id = this.floodgateApi.getUuidFor(gamerTag).get();
            String username = this.getUsername(id);
            return new GameProfile(id, username);
        } catch (InterruptedException | ExecutionException ignored) {
        }
        return null;
    }

    public boolean canJoinMixedAccountType(UUID id) {
        if (this.integration.getConfig().integrations.floodgate.allowJoiningMixedAccountTypes
            || this.floodgateApi == null) {
            return true;
        }

        LinkManager linkManager = this.integration.getLinkManager();

        Optional<PlayerLink> linkOptional = linkManager.getDataOf(id);
        if (linkOptional.isEmpty()) {
            return true;
        }

        PlayerLink playerLink = linkOptional.get();

        Optional<Member> member = linkManager.getDiscordOf(playerLink);
        List<Role> roles = new ArrayList<>(member.get().getRoles());

        roles.retainAll(linkManager.getAllowJoiningMixedAccountTypesBypass());

        if (!roles.isEmpty()) {
            return true;
        }

        List<UUID> uuids = playerLink.getAllUuids();
        boolean currentIsBedrock = this.isBedrock(id);

        for (UUID uuid : uuids) {
            ServerPlayerEntity player =
                this.integration.getServer().getPlayerManager().getPlayer(uuid);
            if ((player != null) && (this.isBedrock(uuid) != currentIsBedrock)) {
                return false;
            }
        }

        return true;
    }
}
