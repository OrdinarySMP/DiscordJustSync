package tronka.justsync.mixin;

import java.net.SocketAddress;
import java.util.UUID;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tronka.justsync.JustSyncApplication;

@Mixin(PlayerList.class)
public class PlayerManagerMixin {

    @Inject(method = "canPlayerLogin", at = @At("HEAD"), cancellable = true)
    private void canJoin(
            SocketAddress address, 
            /*$ profile_class {*/ net.minecraft.server.players.NameAndId/*$}*/ profile,
            CallbackInfoReturnable<Component> cir) {
        JustSyncApplication integration = JustSyncApplication.getInstance();
        if (!integration.isReady()) {
            cir.setReturnValue(Component.nullToEmpty("DiscordJS not ready, please try again in a few seconds."));
            return;
        }
        UUID uuid = profile./*? if >= 1.21.9 {*/ id() /*?} else {*/ /*getId() *//*?}*/;
        if (!integration.getFloodgateIntegration().canJoinMixedAccountType(uuid)) {
            cir.setReturnValue(Component.nullToEmpty(
                integration.getConfig().integrations.floodgate.joiningMixedAccountTypesKickMessage));
            return;
        }

        if (integration.getLinkManager().canJoin(uuid)) {
            return;
        }


        cir.setReturnValue(Component.nullToEmpty(integration.getLinkManager().getJoinError(profile)));
    }

    @Inject(method = "placeNewPlayer", at = @At("HEAD"))
    private void onPlayerJoin(Connection connection, ServerPlayer player, CommonListenerCookie clientData,
        CallbackInfo ci) {
        JustSyncApplication.getInstance().getLinkManager().onPlayerJoin(player);
        if (!JustSyncApplication.getInstance().getVanishIntegration().isVanished(player)) {
            JustSyncApplication.getInstance().getChatBridge().onPlayerJoin(player);
        }
    }

}
