package tronka.justsync.impl;

import java.util.UUID;
import net.minecraft.server.network.ServerPlayerEntity;
import tronka.justsync.core.view.minecraft.Player;
public class FabricPlayer extends Player {
    private final ServerPlayerEntity entity;
    public FabricPlayer(ServerPlayerEntity entity) {
        this.entity = entity;
    }
    @Override
    public UUID getID() {
        return null;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public boolean equals(Player player) {
        return player instanceof FabricPlayer fabricPlayer && fabricPlayer.entity.equals(this.entity);
    }
}
