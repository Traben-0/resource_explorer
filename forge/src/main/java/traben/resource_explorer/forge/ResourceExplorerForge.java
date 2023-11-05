package traben.resource_explorer.forge;

import traben.resource_explorer.ResourceExplorer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ResourceExplorer.MOD_ID)
public class ResourceExplorerForge {
    public ResourceExplorerForge() {
        ResourceExplorer.init();
    }
}