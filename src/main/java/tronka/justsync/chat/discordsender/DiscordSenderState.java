package tronka.justsync.chat.discordsender;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import tronka.justsync.config.Config;
import tronka.justsync.events.payload.MinecraftToDiscordMessagePayload;

import java.util.concurrent.CompletableFuture;

public class DiscordSenderState {
    private Config config;
    private MinecraftToDiscordMessagePayload payload;
    private TextChannel channel;
    private long messageId;
    private long lastMessageEdit;
    private int count;
    private int sentCount;
    private CompletableFuture<Void> future;
    private boolean isEditPending;

    public DiscordSenderState(MinecraftToDiscordMessagePayload payload, TextChannel channel) {
        this.payload = payload;
        this.channel = channel;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public TextChannel getChannel() {
        return channel;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public long getLastMessageEdit() {
        return lastMessageEdit;
    }

    public void setLastMessageEdit(long lastMessageEdit) {
        this.lastMessageEdit = lastMessageEdit;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getSentCount() {
        return sentCount;
    }

    public void setSentCount(int sentCount) {
        this.sentCount = sentCount;
    }

    public CompletableFuture<Void> getFuture() {
        return future;
    }

    public void setFuture(CompletableFuture<Void> future) {
        this.future = future;
    }

    public MinecraftToDiscordMessagePayload getPayload() {
        return payload;
    }

    public boolean isEditPending() {
        return isEditPending;
    }

    public void setEditPending(boolean isEditPending) {
        this.isEditPending = isEditPending;
    }
}
