package traben.resource_explorer.mixin;


import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import traben.resource_explorer.REConfig;


@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {


    @Unique
    private Throwable re$exception = null;

    @ModifyArg(
            method = "onResourceReloadFailure",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resource/ResourceReloadLogger;recover(Ljava/lang/Throwable;)V"),
            index = 0
    )
    private Throwable re$captureThrowable(Throwable throwable) {
        this.re$exception = REConfig.getInstance().addCauseToReloadFailureToast ? throwable : null;
        return throwable;
    }

    @ModifyArg(
            method = "showResourceReloadFailureToast",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/toast/SystemToast;show(Lnet/minecraft/client/toast/ToastManager;Lnet/minecraft/client/toast/SystemToast$Type;Lnet/minecraft/text/Text;Lnet/minecraft/text/Text;)V"),
            index = 3
    )
    private Text re$mixin(Text title) {
        if (re$exception != null && title == null && re$exception.getMessage() != null) {
            return Text.of(re$exception.getMessage());
        }
        return title;
    }
}