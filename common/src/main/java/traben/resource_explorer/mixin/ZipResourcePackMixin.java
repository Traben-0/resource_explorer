package traben.resource_explorer.mixin;


import net.minecraft.resource.ZipResourcePack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import traben.resource_explorer.explorer.ExplorerUtils;


@Mixin(ZipResourcePack.class)
public abstract class ZipResourcePackMixin {


    @ModifyArg(method = "findResources", at = @At(value = "INVOKE", target = "Ljava/lang/String;startsWith(Ljava/lang/String;)Z"), index = 0)
    private String re$injected(String prefix) {
        // zip search fails if the search term is empty
        // use a dedicated search key to bypass the emptiness checking and then make it empty where it counts
        if (ExplorerUtils.isSearching())
            return prefix.replace(ExplorerUtils.SEARCH_KEY + "/", "");
//            return prefix.replaceAll("//$", "/");
        return prefix;
    }
}