package tronka.justsync.mixin;


import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tronka.justsync.JustSyncApplication;

@Mixin(Commands.class)
public class CommandManagerMixin {

    @Inject(method = "performCommand", at = @At("HEAD"))
    private void onExecuteCommand(ParseResults<CommandSourceStack> parseResults, String command, CallbackInfo ci) {
        if (!JustSyncApplication.getInstance().isReady()) {
            return;
        }
        JustSyncApplication.getInstance().getConsoleBridge()
            .onCommandExecute(parseResults.getContext().getSource(), command);
        JustSyncApplication.getInstance().getChatBridge()
            .onCommandExecute(parseResults.getContext().getSource(), command);
    }
}
