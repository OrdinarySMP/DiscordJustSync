package tronka.justsync.core.view.minecraft;

import java.util.Optional;

public interface CommandSource {
    String getName();
    boolean isEntity();
    boolean isServer();
    Optional<Player> getPlayer();
}
