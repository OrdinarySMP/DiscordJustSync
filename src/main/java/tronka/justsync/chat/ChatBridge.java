package tronka.justsync.chat;

import club.minnced.discord.webhook.external.JDAWebhookClient;
import eu.pb4.placeholders.api.node.TextNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import tronka.justsync.JustSyncApplication;
import tronka.justsync.Utils;
import tronka.justsync.config.Config;

public class ChatBridge extends ListenerAdapter {

    private static final String webhookId = "justsync-hook";
    private final JustSyncApplication integration;
    private TextChannel channel;
    private boolean stopped = false;
    private DiscordChatMessageSender messageSender;
    private JDAWebhookClient webhookClient;

    public ChatBridge(JustSyncApplication integration) {
        this.integration = integration;
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);
        integration.registerConfigReloadHandler(this::onConfigLoaded);
        this.sendMessageToDiscord(integration.getConfig().messages.startMessage, null, null);
    }

    private void onConfigLoaded(Config config) {
        this.channel = Utils.getTextChannel(this.integration.getJda(), config.serverChatChannel, "serverChatChannel");
        this.messageSender = null;
        this.setWebhook(null);
        if (this.integration.getConfig().useWebHooks) {
            this.channel.retrieveWebhooks().onSuccess((webhooks -> {
                Optional<Webhook> hook = webhooks.stream()
                    .filter(w -> w.getOwner() == this.integration.getGuild().getSelfMember()).findFirst();
                if (hook.isPresent()) {
                    this.setWebhook(hook.get());
                } else {
                    this.channel.createWebhook(webhookId).onSuccess(this::setWebhook).queue();
                }
            })).queue();
        }
        this.updateRichPresence(0);
    }

    private void setWebhook(Webhook webhook) {
        if (this.webhookClient != null) {
            this.webhookClient.close();
            this.webhookClient = null;
        }
        if (webhook != null) {
            this.webhookClient = JDAWebhookClient.from(webhook);
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // discord message
        if (event.getChannel() != this.channel) {
            return;
        }
        if (event.getMember() == null || event.getAuthor().isBot()) {
            return;
        }

        Message repliedMessage = event.getMessage().getReferencedMessage();

        String baseText = repliedMessage == null ? this.integration.getConfig().messages.chatMessageFormat
            : this.integration.getConfig().messages.chatMessageFormatReply;

        TextNode attachmentInfo;
        if (!event.getMessage().getAttachments().isEmpty()) {
            List<TextNode> attachments = new ArrayList<>(List.of(TextNode.of("\nAttachments:")));
            for (Message.Attachment attachment : event.getMessage().getAttachments()) {
                attachments.add(
                    TextReplacer.create().replace("link", attachment.getUrl()).replace("name", attachment.getFileName())
                        .applyNode(this.integration.getConfig().messages.attachmentFormat));
            }
            attachmentInfo = TextNode.wrap(attachments);
        } else {
            attachmentInfo = TextNode.empty();
        }

        String messageText = event.getMessage().getContentDisplay();
        if (messageText.contains("§") && this.integration.getConfig().restrictFormattingCodes &&
                event.getMember().getRoles().stream().noneMatch(
                    Utils.parseRoleList(this.integration.getGuild(),
                        this.integration.getConfig()
                        .formattingCodeRestrictionOverrideRoles)::contains)) {

            if (this.integration.getConfig().formattingCodeReplacement.isEmpty()) {
                messageText = Utils.removeFormattingCode(messageText);
            } else {
                messageText = Utils.replaceFormattingCode(
                    messageText, this.integration.getConfig().formattingCodeReplacement);
            }
        }

        String replyUser = repliedMessage == null ? "%userRepliedTo%"
            : (repliedMessage.getMember() == null ? repliedMessage.getAuthor().getEffectiveName()
                : repliedMessage.getMember().getEffectiveName());
        this.sendMcChatMessage(TextReplacer.create()
            .replace("msg", Utils.parseUrls(messageText, this.integration.getConfig()))
            .replace("user", event.getMember().getEffectiveName()).replace("userRepliedTo", replyUser)
            .replace("attachments", attachmentInfo).apply(baseText));
    }

    public void onPlayerJoin(ServerPlayerEntity player) {
        this.onPlayerJoin(player, false);
    }

    public void onPlayerJoin(ServerPlayerEntity player, boolean vanish) {
        this.sendMessageToDiscord(this.integration.getConfig().messages.playerJoinMessage.replace("%user%",
            Utils.escapeUnderscores(player.getName().getString())), null, player);
        this.updateRichPresence(vanish ? 0 : 1);
    }

    public void onPlayerTimeOut(ServerPlayerEntity player) {
        this.sendMessageToDiscord(this.integration.getConfig().messages.playerTimeOutMessage.replace("%user%",
                Utils.escapeUnderscores(player.getName().getString())), null, player);
        this.updateRichPresence(-1);
    }

    public void onPlayerLeave(ServerPlayerEntity player) {
        this.onPlayerLeave(player, false);
    }

    public void onPlayerLeave(ServerPlayerEntity player, boolean vanish) {
        if (this.stopped) {
            return;
        }
        String message = this.integration.getConfig().messages.playerLeaveMessage.replace("%user%",
                Utils.escapeUnderscores(player.getName().getString()));
        if (vanish) {
            this.sendMessageToDiscordUnchecked(message, null);
        } else {
            this.sendMessageToDiscord(message, null, player);
        }
        this.updateRichPresence(vanish ? 0 : -1);
    }

    private void updateRichPresence(int modifier) {
        if (!this.integration.getConfig().showPlayerCountStatus) {
            return;
        }
        long playerCount;
        if (this.integration.getServer() == null || this.integration.getServer().getPlayerManager() == null) {
            playerCount = 0;
        } else {
            playerCount = this.integration.getServer().getPlayerManager()
                    .getPlayerList().stream()
                    .filter(p -> !this.integration.getVanishIntegration().isVanished(p))
                    .count() + modifier;
        }

        this.integration.getJda().getPresence().setPresence(Activity.playing(switch ((int) playerCount) {
            case 0 -> this.integration.getConfig().messages.onlineCountZero;
            case 1 -> this.integration.getConfig().messages.onlineCountSingular;
            default -> this.integration.getConfig().messages.onlineCountPlural.formatted(playerCount);
        }), false);
    }

    public void onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        if (this.integration.getConfig().broadCastDeathMessages) {
            String message = source.getDeathMessage(player).getString();
            if (message.equals("death.attack.badRespawnPoint")) {
                message = "%s was killed by [Intentional Mod Design]".formatted(player.getName().getString());
            }
            this.sendMessageToDiscord(Utils.escapeUnderscores(message), null, player);
        }
    }

    public void onReceiveAdvancement(ServerPlayerEntity player, AdvancementDisplay advancement) {
        if (this.integration.getConfig().announceAdvancements && advancement.shouldAnnounceToChat()) {
            this.sendMessageToDiscord(this.integration.getConfig().messages.advancementMessage.replace("%user%",
                    Utils.escapeUnderscores(player.getName().getString()))
                .replace("%title%", advancement.getTitle().getString())
                .replace("%description%", advancement.getDescription().getString()), null, player);
        }
    }

    public void sendMcChatMessage(Text message) {
        this.integration.getServer().getPlayerManager().broadcast(message, false);
    }

    private void onServerStopping(MinecraftServer minecraftServer) {
        this.sendMessageToDiscord(this.integration.getConfig().messages.stopMessage, null, null);
        this.stopped = true;
    }


    public void onMcChatMessage(String message, ServerPlayerEntity player) {
        if (this.integration.getConfig().waypoints.formatWaypoints) {
            message = Utils.formatXaero(message, this.integration.getConfig());
            message = Utils.formatVoxel(message, this.integration.getConfig(), player);
        }
        this.sendMessageToDiscord(message, player, player);
    }

    private void sendMessageToDiscord(String message, ServerPlayerEntity sender, ServerPlayerEntity connectedPlayer) {
        if (connectedPlayer != null && this.integration.getVanishIntegration().isVanished(connectedPlayer)) {
            return;
        }
        this.sendMessageToDiscordUnchecked(message, sender);
    }

    private void sendMessageToDiscordUnchecked(String message, ServerPlayerEntity sender) {
        if (message.trim().isEmpty()) {
            return;
        }

        message = Utils.escapeMentions(message);
        message = Utils.formatMentions(message, this.integration, sender);
        if (this.messageSender == null || this.messageSender.hasChanged(message, sender)) {
            this.messageSender = new DiscordChatMessageSender(this.webhookClient, this.channel,
                this.integration.getConfig(), message, sender);
        }
            this.messageSender.sendMessage();
    }

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        if (this.messageSender != null) {
            this.messageSender.onMessageDelete(event.getMessageIdLong());
        }
    }

    public void onCommandExecute(ServerCommandSource source, String command) {
        if (!command.startsWith("me") && !command.startsWith("say")) {
            return;
        }
        ServerPlayerEntity sender;
        String prefix;
        if (source.getEntity() instanceof ServerPlayerEntity player) {
            sender = player;
            prefix = "";
        } else {
            sender = null;
            prefix = Utils.escapeUnderscores(source.getName()) + ": ";
        }
        String data = command.split(" ", 2)[1];
        String message;
        if (command.startsWith("me")) {
            message = prefix + "*" + data + "*";
        } else {
            message = prefix + data;
        }
        this.sendMessageToDiscord(message, sender, sender);
    }
}
