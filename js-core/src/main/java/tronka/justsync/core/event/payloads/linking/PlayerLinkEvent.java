package tronka.justsync.core.event.payloads.linking;

import java.util.UUID;

public record PlayerLinkEvent(UUID uuid, long discordId) {}
