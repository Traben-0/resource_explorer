package traben.resource_explorer.fabric;

import net.fabricmc.api.ModInitializer;
import traben.resource_explorer.ResourceExplorer;

public class ResourceExplorerFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ResourceExplorer.init();


    }
}