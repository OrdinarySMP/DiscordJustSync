package tronka.justsync.mixin;

import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tronka.justsync.JustSyncApplication;

@Mixin(ServerGamePacketListenerImpl.class)
public class NetworkHandlerMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(method = "onDisconnect", at = @At("HEAD"))
    private void onPlayerLeave(DisconnectionDetails info, CallbackInfo ci) {
        if (!JustSyncApplication.getInstance().isReady()) {
            return;
        }
        if (JustSyncApplication.getInstance().getVanishIntegration().isVanished(this.player)) {
            return;
        }
        if (!info.reason().toString().contains("disconnect.timeout")) {
            JustSyncApplication.getInstance().getChatBridge().onPlayerLeave(this.player);
        } else {
            JustSyncApplication.getInstance().getChatBridge().onPlayerTimeOut(this.player);
        }
    }

    @Inject(method = "getSignedMessage", at = @At("RETURN"))
    private void onMessageValidated(ServerboundChatPacket packet, LastSeenMessages lastSeenMessages,
        CallbackInfoReturnable<PlayerChatMessage> cir) {
        if (!JustSyncApplication.getInstance().isReady()) {
            return;
        }
        JustSyncApplication.getInstance().getChatBridge().onMcChatMessage(packet.message(), this.player);
    }

}
