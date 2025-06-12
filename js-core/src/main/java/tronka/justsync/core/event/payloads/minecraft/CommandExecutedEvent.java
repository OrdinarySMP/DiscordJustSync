package tronka.justsync.core.event.payloads.minecraft;

import tronka.justsync.core.view.minecraft.CommandSource;

public record CommandExecutedEvent(CommandSource source, String command) {}
