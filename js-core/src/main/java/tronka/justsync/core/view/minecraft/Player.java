package tronka.justsync.core.view.minecraft;

import java.util.UUID;

/**
 * Abstracted view of a player, should implement equals
 */
public interface Player {
    UUID getID();
    String getName();
}
