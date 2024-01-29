package traben.resource_explorer.mixin.accessors;

import net.minecraft.client.texture.SpriteAtlasTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SpriteAtlasTexture.class)
public interface SpriteAtlasTextureAccessor {
    @Invoker
    int callGetWidth();

    @Invoker
    int callGetHeight();
}
