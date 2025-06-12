package tronka.justsync.core.view.minecraft;

import java.util.Collection;

public interface MinecraftAccess {
    boolean isModLoaded(String name);
    Collection<Player> getPlayers();
}
