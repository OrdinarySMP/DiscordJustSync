package tronka.justsync.mixin;

import java.util.Optional;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tronka.justsync.JustSyncApplication;

@Mixin(PlayerAdvancements.class)
public class AdvancementMixin {

    @Shadow
    private ServerPlayer player;

    @Inject(method = "award", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerAdvancements;markForVisibilityUpdate(Lnet/minecraft/advancements/AdvancementHolder;)V"))
    private void receiveAdvancement(AdvancementHolder advancementEntry, String criterionName,
        CallbackInfoReturnable<Boolean> cir) {
        if (!JustSyncApplication.getInstance().isReady()) {
            return;
        }
        Advancement advancement = advancementEntry.value();

        if (advancement == null) {
            return;
        }
        Optional<DisplayInfo> advancementDisplay = advancement.display();
        if (advancementDisplay.isPresent() && advancementDisplay.get().shouldAnnounceChat()) {
            JustSyncApplication.getInstance()
                    .getChatBridge()
                    .onReceiveAdvancement(this.player, advancementDisplay.get());
        }
    }
}
