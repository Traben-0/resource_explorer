package traben.resource_explorer.mixin;


import net.minecraft.resource.NamespaceResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import traben.resource_explorer.ResourceExplorerClient;
import traben.resource_explorer.explorer.ExplorerUtils;


@Mixin(NamespaceResourceManager.class)
public abstract class NamespaceResourceManagerMixin {


    @Redirect(method = "findResources", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourcePack;findResources(Lnet/minecraft/resource/ResourceType;Ljava/lang/String;Ljava/lang/String;Lnet/minecraft/resource/ResourcePack$ResultConsumer;)V"))
    private void re$tryWrappingWhenSearching(final ResourcePack instance, final ResourceType resourceType, final String s, final String startingPath, final ResourcePack.ResultConsumer resultConsumer) {
        if (ExplorerUtils.isSearching()) {
            //if the search key is in the path, we want to search the resource pack and not crash
            try {
                instance.findResources(resourceType, s, startingPath, resultConsumer);
            } catch (Exception e) {
                String baseMessage = "Failed to find resources in resource-pack of class:\n " + instance.getClass() + "\n because: " + e.getMessage() + ".";
                ResourceExplorerClient.log(baseMessage + "\nThis is probably fine and just some mod adding resources differently to vanilla, but if you really wanted to explore this mods resources then contact @Traben.");
                ExplorerUtils.addSearchException(baseMessage);
            }
        } else {
            //vanilla behavior
            instance.findResources(resourceType, s, startingPath, resultConsumer);
        }
    }
}
