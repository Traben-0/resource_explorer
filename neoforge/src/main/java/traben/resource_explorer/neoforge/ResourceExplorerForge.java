package traben.resource_explorer.neoforge;


import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import traben.resource_explorer.REConfig;
import traben.resource_explorer.ResourceExplorerClient;

@Mod(ResourceExplorerClient.MOD_ID)
public class ResourceExplorerForge {
    public ResourceExplorerForge() {

        if (FMLEnvironment.dist == Dist.CLIENT) {
            try {
                ModLoadingContext.get().registerExtensionPoint(
                        IConfigScreenFactory.class,
                        () -> (minecraftClient, screen) -> new REConfig.REConfigScreen(screen));
            } catch (NoClassDefFoundError e) {
                ResourceExplorerClient.logError(" Mod config screen broken, download latest forge version");
            }
            ResourceExplorerClient.init();
        } else {

            throw new UnsupportedOperationException("Attempting to load a clientside only mod [resource_explorer] on the server, refusing");
        }
    }
}