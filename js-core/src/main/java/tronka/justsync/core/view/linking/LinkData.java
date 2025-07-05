package tronka.justsync.core.view.linking;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LinkData {
    Optional<PlayerLink> getPlayerLink(UUID playerId);

    Optional<PlayerLink> getPlayerLink(long discordId);

    void addPlayerLink(PlayerLink playerLink);

    void removePlayerLink(PlayerLink playerLink);

    void updatePlayerLink(PlayerLink playerLink);

    List<PlayerLink> getPlayerLinks();
}
