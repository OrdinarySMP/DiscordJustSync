package tronka.justsync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import com.mojang.brigadier.CommandDispatcher;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.dv8tion.jda.api.entities.Member;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import tronka.justsync.linking.PlayerData;
import tronka.justsync.linking.PlayerLink;

public class InGameCommand {

    private final JustSyncApplication integration;

    public InGameCommand(JustSyncApplication integration) {
        this.integration = integration;
        CommandRegistrationCallback.EVENT.register(this::register);
    }

    private void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registry,
        Commands.CommandSelection environment) {
        dispatcher.register(
            Commands.literal("discord")
                .then(this.unlinkSubcommand())
                .then(this.reloadSubcommand())
                .then(this.getInfoSubcommand())
        );
    }

    private LiteralArgumentBuilder<CommandSourceStack> getInfoSubcommand() {
        return Commands.literal("get")
                .executes(this::getSelfLinkInfo)
                .then(
                        Commands.argument("player", GameProfileArgument.gameProfile())
                                .requires(
                                        Permissions.require(
                                                "justsync.get",
                                                CompatUtil.getPermissionLevel(
                                                        CompatUtil.PermissionLevel.ADMINS)))
                                .requires(
                                        Permissions.require(
                                                "justsync.get",
                                                CompatUtil.getPermissionLevel(
                                                        CompatUtil.PermissionLevel.ADMINS)))
                                .executes(this::getLinkInfo));
    }

    private int getLinkInfo(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection</*$ profile_class {*/net.minecraft.server.players.NameAndId/*$}*/> profiles = GameProfileArgument.getGameProfiles(context, "player");
        Collection<String> lines = new ArrayList<>();
        for (/*$ profile_class {*/net.minecraft.server.players.NameAndId/*$}*/ profile : profiles) {
            UUID uuid = profile./*? if >= 1.21.9 {*/ id() /*?} else {*/ /*getId() *//*?}*/;
            String profileName = profile./*? if >= 1.21.9 {*/ name() /*?} else {*/ /*getName() *//*?}*/;
            Optional<PlayerLink> optionalLink = this.integration.getLinkManager().getDataOf(uuid);
            if (optionalLink.isEmpty()) {
                lines.add("No records for " + profileName);
            } else {
                Optional<Member> member = this.integration.getLinkManager().getDiscordOf(optionalLink.get());
                if (member.isPresent()) {
                    lines.add(formatPlayerInfo(optionalLink.get(), member.get()));
                } else {
                    lines.add("Unable to load discord member for " + profileName);

                }
            }

        }
        context.getSource().sendSuccess(() -> Component.literal(String.join("\n", lines)), false);
        return 1;
    }

    private int getSelfLinkInfo(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendSuccess(() -> Component.literal("Player only!"),
                false);
            return 1;
        }
        Optional<PlayerLink> playerLinkOptional = this.integration.getLinkManager()
            .getDataOf(player.getUUID());
        if (playerLinkOptional.isEmpty()) {
            context.getSource().sendSuccess(
                () -> Component.literal("Player is not linked"),
                false);
            return 1;
        }
        PlayerLink playerLink = playerLinkOptional.get();

        Optional<Member> member = this.integration.getLinkManager()
            .getDiscordOf(playerLink.getPlayerId());

        if (member.isEmpty()) {
            context.getSource().sendSuccess(
                () -> Component.literal("Discord member not found"),
                false);
            return 1;
        }

        String message = formatPlayerInfo(playerLink, member.get());
        context.getSource()
            .sendSuccess(() -> Component.literal(message), false);
        return 1;
    }

    private static String formatPlayerInfo(PlayerLink playerLink, Member member) {
        StringBuilder message = new StringBuilder()
            .append(playerLink.getPlayerName())
            .append(" (@")
            .append(member.getEffectiveName())
            .append(")");

        if (playerLink.altCount() > 0) {
            message.append(" Alts: ").append(String.join(", ", playerLink.getAlts()
                .stream().map(PlayerData::getName).toList()));
        }
        return message.toString();
    }

    private LiteralArgumentBuilder<CommandSourceStack> reloadSubcommand() {
        return Commands.literal("reload")
                .requires(
                        Permissions.require(
                                "justsync.reload",
                                CompatUtil.getPermissionLevel(CompatUtil.PermissionLevel.OWNERS)))
                .executes(this::reloadConfigs);
    }

    private int reloadConfigs(CommandContext<CommandSourceStack> context) {
        String result = this.integration.tryReloadConfig();
        final String feedback = result.isEmpty() ? "Successfully reloaded config!" : result;
        context.getSource().sendSuccess(() -> Component.literal(feedback), result.isEmpty());
        return 1;
    }

    private LiteralArgumentBuilder<CommandSourceStack> unlinkSubcommand() {
        return Commands.literal("unlink")
                .requires(Permissions.require("justsync.unlink", true))
                .executes(this::unlinkSelf)
                .then(
                        Commands.argument("player", GameProfileArgument.gameProfile())
                                .requires(
                                        Permissions.require(
                                                "justsync.unlink.other",
                                                CompatUtil.getPermissionLevel(
                                                        CompatUtil.PermissionLevel.ADMINS)))
                                .executes(this::unlinkSpecifiedPlayer));
    }

    private int unlinkSpecifiedPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection</*$ profile_class {*/net.minecraft.server.players.NameAndId/*$}*/> profiles = GameProfileArgument.getGameProfiles(context, "player");
        int count = 0;
        for (/*$ profile_class {*/net.minecraft.server.players.NameAndId/*$}*/ profile : profiles) {
            UUID uuid = /*? if >= 1.21.9 {*/ profile.id() /*?} else {*/ /*profile.getId() *//*?}*/;
            if (this.integration.getLinkManager().unlinkPlayer(uuid)) {
                count++;
            }
        }
        if (count > 0) {
            context.getSource().sendSuccess(() -> Component.literal(
                    "Successfully unlinked %d player(s)".formatted(profiles.size())),
                false);
            return count;
        }
        context.getSource().sendSuccess(() -> Component.literal("Found no linked players!"), false);
        return 0;
    }

    private int unlinkSelf(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player != null) {
            this.integration.getLinkManager().unlinkPlayer(player.getUUID());
            context.getSource().sendSuccess(() -> Component.literal("Unlinked!"), false);
        } else {
            context.getSource().sendSuccess(() -> Component.literal("Player Only!"), false);
        }
        return 1;
    }
}
