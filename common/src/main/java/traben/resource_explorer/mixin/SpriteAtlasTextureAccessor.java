package traben.resource_explorer.mixin;

import net.minecraft.client.texture.SpriteAtlasTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SpriteAtlasTexture.class)
public interface SpriteAtlasTextureAccessor {
    @Accessor
    int getWidth();

    @Accessor
    int getHeight();
}
