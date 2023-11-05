package traben.resource_explorer.fabric;

import net.fabricmc.fabric.impl.client.resource.loader.FabricWrappedVanillaResourcePack;
import net.fabricmc.fabric.impl.resource.loader.FabricLifecycledResourceManager;
import traben.resource_explorer.ResourceExplorer;
import net.fabricmc.api.ModInitializer;

public class ResourceExplorerFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ResourceExplorer.init();



    }
}