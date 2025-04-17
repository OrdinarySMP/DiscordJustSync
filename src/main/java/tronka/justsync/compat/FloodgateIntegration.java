package tronka.justsync.compat;

import com.mojang.authlib.GameProfile;
import net.fabricmc.loader.api.FabricLoader;
import org.geysermc.floodgate.api.FloodgateApi;
import tronka.justsync.JustSyncApplication;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class FloodgateIntegration {
    private FloodgateApi floodgateApi;

    public FloodgateIntegration(JustSyncApplication integration) {
        if (FabricLoader.getInstance().isModLoaded("floodgate")) {
            this.floodgateApi = FloodgateApi.getInstance();
        }
    }

    public boolean isBedrock(UUID uuid) {
        return this.floodgateApi != null && this.floodgateApi.isFloodgateId(uuid);
    }

    public String getUsername(UUID uuid) {
        if (this.floodgateApi != null) {
            try {
                return this.floodgateApi.getPlayerPrefix() + this.floodgateApi.getGamertagFor(uuid.getLeastSignificantBits()).get();
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

}

