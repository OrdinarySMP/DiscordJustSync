package tronka.justsync.chat.discordsender;

import club.minnced.discord.webhook.external.JDAWebhookClient;
import club.minnced.discord.webhook.receive.ReadonlyMessage;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;

import tronka.justsync.Utils;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class WebhookDiscordSender implements DiscordSender {

    private JDAWebhookClient webhookClient;
    private DiscordSenderState state;
    private String avatarUrl;

    public WebhookDiscordSender(DiscordSenderState state, JDAWebhookClient webhookClient) {
        this.state = state;
        this.webhookClient = Objects.requireNonNull(webhookClient);
        this.avatarUrl =
                Utils.getAvatarUrl(this.state.getPayload().player(), this.state.getConfig());
    }

    @Override
    public void send() {
        this.state.setCount(1);
        this.state.setSentCount(1);
        WebhookMessage msg =
                new WebhookMessageBuilder()
                        .setUsername(
                                this.state.getPayload().player().getName().tryCollapseToString())
                        .setAvatarUrl(this.avatarUrl)
                        .setContent(this.state.getPayload().message())
                        .build();
        this.state.setFuture(
                this.webhookClient
                        .send(msg)
                        .thenApply(ReadonlyMessage::getId)
                        .thenAccept(this::setId));
    }

    private void setId(long messageId) {
        this.state.setMessageId(messageId);
    }

    @Override
    public void incrementCountAndEdit() {
        this.state.setCount(this.state.getCount() + 1);
        if (this.state.isEditPending()) {
            return;
        }
        this.state.setEditPending(true);

        tryEditMessage();
    }

    private void tryEditMessage() {
        if (this.shouldEdit()) {
            editMessage();
            return;
        }

        CompletableFuture.runAsync(
                this::tryEditMessage,
                CompletableFuture.delayedExecutor(
                        Math.max(
                                100,
                                1000
                                        - (System.currentTimeMillis()
                                                - this.state.getLastMessageEdit())),
                        TimeUnit.MILLISECONDS));
    }

    private boolean shouldEdit() {
        return this.state.getFuture() != null
                && this.state.getFuture().isDone()
                && this.state.getLastMessageEdit() + 1000 <= System.currentTimeMillis();
    }

    private void editMessage() {
        String message = this.state.getPayload().message() + "*(" + this.state.getCount() + ")*";
        this.state.setSentCount(this.state.getCount());
        this.state.setEditPending(false);
        this.state.setFuture(
                this.webhookClient.edit(this.state.getMessageId(), message).thenAccept(m -> {}));
    }

    @Override
    public boolean hasChanged(DiscordSender sender) {
        return sender.getState().getPayload().equals(this.state.getPayload());
    }

    @Override
    public DiscordSenderState getState() {
        return this.state;
    }
}
