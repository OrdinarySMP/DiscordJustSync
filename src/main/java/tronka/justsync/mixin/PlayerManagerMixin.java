package tronka.justsync.mixin;

import java.net.SocketAddress;
import java.util.UUID;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tronka.justsync.JustSyncApplication;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Inject(method = "checkCanJoin", at = @At("HEAD"), cancellable = true)
    private void canJoin(
            SocketAddress address, 
            /*$ profile_class {*/ net.minecraft.server.PlayerConfigEntry/*$}*/ profile,
            CallbackInfoReturnable<Text> cir) {
        JustSyncApplication integration = JustSyncApplication.getInstance();
        if (!integration.isReady()) {
            cir.setReturnValue(Text.of("DiscordJS not ready, please try again in a few seconds."));
            return;
        }
        UUID uuid = profile./*? if >= 1.21.9 {*/ id() /*?} else {*/ /*getId() *//*?}*/;
        if (!integration.getFloodgateIntegration().canJoinMixedAccountType(uuid)) {
            cir.setReturnValue(Text.of(
                integration.getConfig().integrations.floodgate.joiningMixedAccountTypesKickMessage));
            return;
        }

        if (integration.getLinkManager().canJoin(uuid)) {
            return;
        }


        cir.setReturnValue(Text.of(integration.getLinkManager().getJoinError(profile)));
    }

    @Inject(method = "onPlayerConnect", at = @At("HEAD"))
    private void onPlayerJoin(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData,
        CallbackInfo ci) {
        JustSyncApplication.getInstance().getLinkManager().onPlayerJoin(player);
        if (!JustSyncApplication.getInstance().getVanishIntegration().isVanished(player)) {
            JustSyncApplication.getInstance().getChatBridge().onPlayerJoin(player);
        }
    }

}
