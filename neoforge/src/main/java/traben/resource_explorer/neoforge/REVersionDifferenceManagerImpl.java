package traben.resource_explorer.neoforge;



import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class REVersionDifferenceManagerImpl {

    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

}
