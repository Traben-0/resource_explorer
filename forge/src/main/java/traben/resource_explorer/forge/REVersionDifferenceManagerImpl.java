package traben.resource_explorer.forge;

import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class REVersionDifferenceManagerImpl {

    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    public static boolean isForge() {
        return true;
    }

}
