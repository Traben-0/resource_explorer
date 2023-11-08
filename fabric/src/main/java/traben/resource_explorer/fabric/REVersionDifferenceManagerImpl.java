package traben.resource_explorer.fabric;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class REVersionDifferenceManagerImpl {
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static boolean isThisModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @SuppressWarnings("SameReturnValue")
    public static boolean isForge() {
        return false;
    }
}
