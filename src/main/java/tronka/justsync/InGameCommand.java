package tronka.justsync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.dv8tion.jda.api.entities.Member;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import tronka.justsync.linking.PlayerData;
import tronka.justsync.linking.PlayerLink;

public class InGameCommand {

    private final JustSyncApplication integration;

    public InGameCommand(JustSyncApplication integration) {
        this.integration = integration;
        CommandRegistrationCallback.EVENT.register(this::register);
    }

    private void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registry,
        CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(
            CommandManager.literal("discord")
                .then(this.unlinkSubcommand())
                .then(this.reloadSubcommand())
                .then(this.getInfoSubcommand())
        );
    }

    private LiteralArgumentBuilder<ServerCommandSource> getInfoSubcommand() {
        return CommandManager.literal("get").executes(this::getSelfLinkInfo)
            .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile()).requires(Permissions.require("justsync.get", 4))
                .requires(Permissions.require("justsync.get", 4)).executes(this::getLinkInfo));
    }

    private int getLinkInfo(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<GameProfile> profiles = GameProfileArgumentType.getProfileArgument(context, "player");
        Collection<String> lines = new ArrayList<>();
        for (GameProfile profile : profiles) {
            Optional<PlayerLink> optionalLink = this.integration.getLinkManager().getDataOf(profile.getId());
            if (optionalLink.isEmpty()) {
                lines.add("No records for " + profile.getName());
            } else {
                Optional<Member> member = this.integration.getLinkManager().getDiscordOf(optionalLink.get());
                if (member.isPresent()) {
                    lines.add(formatPlayerInfo(optionalLink.get(), member.get()));
                } else {
                    lines.add("Unable to load discord member for " + profile.getName());

                }
            }

        }
        context.getSource().sendFeedback(() -> Text.literal(String.join("\n", lines)), false);
        return 1;
    }

    private int getSelfLinkInfo(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFeedback(() -> Text.literal("Player only!"),
                false);
            return 1;
        }
        Optional<PlayerLink> playerLinkOptional = this.integration.getLinkManager()
            .getDataOf(player.getUuid());
        if (playerLinkOptional.isEmpty()) {
            context.getSource().sendFeedback(
                () -> Text.literal("Player is not linked"),
                false);
            return 1;
        }
        PlayerLink playerLink = playerLinkOptional.get();

        Optional<Member> member = this.integration.getLinkManager()
            .getDiscordOf(playerLink.getPlayerId());

        if (member.isEmpty()) {
            context.getSource().sendFeedback(
                () -> Text.literal("Discord member not found"),
                false);
            return 1;
        }

        String message = formatPlayerInfo(playerLink, member.get());
        context.getSource()
            .sendFeedback(() -> Text.literal(message), false);
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

    private LiteralArgumentBuilder<ServerCommandSource> reloadSubcommand() {
        return CommandManager.literal("reload")
            .requires(Permissions.require("justsync.reload", 4))
            .executes(this::reloadConfigs);
    }

    private int reloadConfigs(CommandContext<ServerCommandSource> context) {
        String result = this.integration.tryReloadConfig();
        final String feedback = result.isEmpty() ? "Successfully reloaded config!" : result;
        context.getSource().sendFeedback(() -> Text.literal(feedback), result.isEmpty());
        return 1;
    }

    private LiteralArgumentBuilder<ServerCommandSource> unlinkSubcommand() {
        return CommandManager.literal("unlink")
            .requires(Permissions.require("justsync.unlink", true))
            .executes(this::unlinkSelf)
            .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                .requires(Permissions.require("justsync.unlink.other", 4))
                .executes(this::unlinkSpecifiedPlayer)
            );
    }

    private int unlinkSpecifiedPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<GameProfile> profiles = GameProfileArgumentType.getProfileArgument(context, "player");
        int count = 0;
        for (GameProfile profile : profiles) {
            if (this.integration.getLinkManager().unlinkPlayer(profile.getId())) {
                count++;
            }
        }
        if (count > 0) {
            context.getSource().sendFeedback(() -> Text.literal(
                    "Successfully unlinked %d player(s)".formatted(profiles.size())),
                false);
            return count;
        }
        context.getSource().sendFeedback(() -> Text.literal("Found no linked players!"), false);
        return 0;
    }

    private int unlinkSelf(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player != null) {
            this.integration.getLinkManager().unlinkPlayer(player.getUuid());
            context.getSource().sendFeedback(() -> Text.literal("Unlinked!"), false);
        } else {
            context.getSource().sendFeedback(() -> Text.literal("Player Only!"), false);
        }
        return 1;
    }
}
