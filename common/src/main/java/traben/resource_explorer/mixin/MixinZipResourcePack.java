package traben.resource_explorer.mixin;


import net.minecraft.resource.ZipResourcePack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;


@Mixin(ZipResourcePack.class)
public abstract class MixinZipResourcePack {


    @ModifyArg(method = "findResources", at = @At(value = "INVOKE", target = "Ljava/lang/String;startsWith(Ljava/lang/String;)Z"), index = 0)
    private String injected(String prefix) {
        if(prefix.endsWith("resource_explorer$search/"))
            return prefix.replace("resource_explorer$search/","");
        return prefix;
    }
}