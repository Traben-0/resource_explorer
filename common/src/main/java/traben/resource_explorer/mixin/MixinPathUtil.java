package traben.resource_explorer.mixin;


import com.mojang.serialization.DataResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourcePack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.PathUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


@Mixin(PathUtil.class)
public abstract class MixinPathUtil {


    @Inject(method = "split", at = @At("HEAD"), cancellable = true)
    private static void injected(String path, CallbackInfoReturnable<DataResult<List<String>>> cir) {
        if("resource_explorer$search".equals(path)){
            cir.setReturnValue( DataResult.success(List.of()));
        }
    }
}