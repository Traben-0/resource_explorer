package traben.resource_explorer.fabric;

import net.fabricmc.api.ClientModInitializer;
import traben.resource_explorer.ResourceExplorer;

public class ResourceExplorerFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ResourceExplorer.init();
    }
}