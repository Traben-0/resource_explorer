package traben.resource_explorer.explorer.stats;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.client.gui.screen.Screen;
import traben.resource_explorer.explorer.display.resources.entries.ResourceFileEntry;

public class ExplorerStats {


    final Object2IntArrayMap<ResourceFileEntry.FileType> totalPerFileType = new Object2IntArrayMap<>() {{
        defRetValue = 0;
    }};
    final Object2IntArrayMap<String> totalPerNameSpace = new Object2IntArrayMap<>() {{
        defRetValue = 0;
    }};
    final Object2IntArrayMap<String> totalTexturesPerNameSpace = new Object2IntArrayMap<>() {{
        defRetValue = 0;
    }};
    final Object2IntArrayMap<String> totalPerResourcepack = new Object2IntArrayMap<>() {{
        defRetValue = 0;
    }};
    final Object2IntArrayMap<String> totalTexturesPerResourcepack = new Object2IntArrayMap<>() {{
        defRetValue = 0;
    }};
    int totalResources = 0;
    int totalFileResources = 0;
    int totalTextureResources = 0;
    int totalTextureFileResources = 0;
    int folderCount = 0;

    public ExplorerStats() {
    }

    public void incFolderCount() {
        folderCount++;
    }

    void incrementMap(Object2IntArrayMap<String> map, String key) {
        map.put(key, map.getInt(key) + 1);
    }


    public void addEntryStatistic(ResourceFileEntry entry) {
        boolean isFile = entry.resource != null;
        boolean isTexture = entry.fileType == ResourceFileEntry.FileType.PNG;

        //top level
        totalResources++;
        if (isFile) totalFileResources++;


        //per file type
        totalPerFileType.put(entry.fileType, totalPerFileType.getInt(entry.fileType) + 1);

        //textures only
        if (isTexture) {
            totalTextureResources++;
            if (isFile) {
                totalTextureFileResources++;
            }
        }

        //by namespace
        incrementMap(totalPerNameSpace, entry.identifier.getNamespace());
        if (isTexture)
            incrementMap(totalTexturesPerNameSpace, entry.identifier.getNamespace());

        //by resourcepack
        if (isFile) {
            incrementMap(totalPerResourcepack, entry.resource.getPackId());
            if (isTexture)
                incrementMap(totalTexturesPerResourcepack, entry.resource.getPackId());
        }

    }

    public ExplorerStatsScreen getAsScreen(Screen parent) {
        return new ExplorerStatsScreen(parent, this);
    }

}
