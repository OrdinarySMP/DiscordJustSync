package tronka.justsync.core.event.payloads.linking;

import java.util.UUID;

public record PlayerUnlinkEvent(UUID uuid, long discordId) {}
