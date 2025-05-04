package traben.resource_explorer.mixin;


import com.mojang.serialization.DataResult;
import net.minecraft.util.path.PathUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import traben.resource_explorer.explorer.ExplorerUtils;

import java.util.List;


@Mixin(PathUtil.class)
public abstract class PathUtilMixin {


    @Inject(method = "split", at = @At("HEAD"), cancellable = true)
    private static void re$injected(String path, CallbackInfoReturnable<DataResult<List<String>>> cir) {
        if (ExplorerUtils.isSearching()) {
            cir.setReturnValue(DataResult.success(List.of()));
        }
    }
}