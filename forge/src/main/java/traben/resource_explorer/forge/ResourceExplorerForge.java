package traben.resource_explorer.forge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import traben.resource_explorer.REConfig;
import traben.resource_explorer.ResourceExplorer;

@Mod(ResourceExplorer.MOD_ID)
public class ResourceExplorerForge {
    public ResourceExplorerForge() {

        if (FMLEnvironment.dist == Dist.CLIENT) {
            try {
                ModLoadingContext.get().registerExtensionPoint(
                        ConfigScreenHandler.ConfigScreenFactory.class,
                        () -> new ConfigScreenHandler.ConfigScreenFactory((minecraftClient, screen) -> new REConfig.REConfigScreen(screen)));
            } catch (NoClassDefFoundError e) {
                ResourceExplorer.logError(" Mod config screen broken, download latest forge version");
            }
            ResourceExplorer.init();
        } else {

            throw new UnsupportedOperationException("Attempting to load a clientside only mod [resource_explorer] on the server, refusing");
        }
    }
}