package traben.resource_explorer.explorer.display.resources.entries;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import traben.resource_explorer.REConfig;
import traben.resource_explorer.ResourceExplorerClient;
import traben.resource_explorer.explorer.ExplorerUtils;
import traben.resource_explorer.explorer.display.ExplorerScreen;
import traben.resource_explorer.explorer.display.detail.entries.DisplayEntry;
import traben.resource_explorer.explorer.stats.ExplorerStats;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ResourceFolderEntry extends ResourceEntry {


    private final LinkedList<ResourceFileEntry> fileContent = new LinkedList<>();

    private final Object2ObjectLinkedOpenHashMap<String, ResourceFolderEntry> subFolders = new Object2ObjectLinkedOpenHashMap<>();

    private final String displayName;
    private final OrderedText displayText;
    private final boolean topLevelDirectory;
    public Identifier contentIcon = null;
    private Identifier folderIcon = null;
    private ResourceFileEntry.FileType contentFileType = null;
    private boolean containsExportableFiles = false;
    private Identifier hoverIcon = null;

    public ResourceFolderEntry(String folderName) {
        this.displayName = folderName;
        this.displayText = trimmedTextToWidth(folderName).asOrderedText();
        topLevelDirectory = false;
    }

    public ResourceFolderEntry(String folderName, List<ResourceEntry> entries) {
        topLevelDirectory = true;
        this.displayName = folderName;
        this.displayText = trimmedTextToWidth(folderName).asOrderedText();
        for (ResourceEntry entry : entries) {
            if (entry instanceof ResourceFolderEntry folder) {
                addSubFolder(folder);
            } else if (entry instanceof ExplorerDetailsEntry feedbackEntry) {
                fileContent.add(feedbackEntry);
            } else {
                ResourceExplorerClient.logError("non resource in init");
            }
        }
    }

    public DisplayEntry getDetailEntryIfRoot() {
        // returns only if this is the root directory, and it only contains a single file with a hash of -1 which
        // can only be the explorer feedback entry. hash faster than instance check.
        if (topLevelDirectory && fileContent.size() == 1 && fileContent.getFirst().hashCode() == -1) {
            return fileContent.getFirst().wrapEntryAsDetailed();
        }
        return null;
    }


    @Override
    boolean canExport() {
        return containsExportableFiles;
    }

    @Override
    public boolean isEmpty() {
        boolean folders = subFolders.isEmpty() || subFolders.values().stream().allMatch(ResourceFolderEntry::isEmpty);
        boolean files = fileContent.isEmpty() || fileContent.stream().allMatch(ResourceFileEntry::isEmpty);
        return folders && files;
    }

    @Override
    boolean isFolder() {
        return true;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    OrderedText getDisplayText() {
        return displayText;
    }

    @Override
    List<Text> getExtraText(boolean smallMode) {
        ArrayList<Text> text = new ArrayList<>();

        int sizeFolders = countOfFolderMatchingFilterAndSearch(ExplorerScreen.getSearchTerm());
        if (sizeFolders > 0) {
            text.add(trimmedTextToWidth(" " + sizeFolders + " " + translated(sizeFolders > 1 ?
                    "resource_explorer.detail.folders" : "resource_explorer.detail.folder")));
        }
        int sizeFiles = countOfFilesMatchingFilterAndSearch(ExplorerScreen.getSearchTerm());
        if (sizeFiles > 0) {
            boolean multiple = sizeFiles > 1;
            String fileWord;

            if (folderIcon == ExplorerUtils.ICON_FOLDER_BUILT) {
                fileWord = translated(multiple ? "resource_explorer.detail.built_files" : "resource_explorer.detail.built_file");
            } else {
                fileWord = translated(multiple ? "resource_explorer.detail.files" : "resource_explorer.detail.file");
            }
            text.add(trimmedTextToWidth(" " + sizeFiles + " " + fileWord));
        }

        if (smallMode && text.size() >= 2) return text;

        if (ExplorerUtils.ICON_FOLDER_BUILT.equals(folderIcon))
            text.add(trimmedTextToWidth("§8§o " + translated("resource_explorer.detail.built_msg")));
        return text;
    }

    @Override
    public String toString() {
        return toString(0);
    }

    @Override
    public String toString(int indent) {
        StringBuilder builder = new StringBuilder();
        builder.append(" ".repeat(Math.max(0, indent)));
        builder.append("\\ ");
        builder.append(displayName).append("\n");
        for (ResourceEntry content :
                subFolders.values()) {
            builder.append(content.toString(indent + 1));
        }
        for (ResourceEntry content :
                fileContent) {
            builder.append(content.toString(indent + 1));
        }
        return builder.toString();
    }

    public void addSubFolder(ResourceFolderEntry resourceFolder) {
        subFolders.put(resourceFolder.displayName, resourceFolder);
    }


    @Override
    public void exportToOutputPack(ExplorerUtils.REExportContext context) {
        //if(canExport()){
        fileContent.forEach(file -> file.exportToOutputPack(context));
        subFolders.values().forEach(file -> file.exportToOutputPack(context));
        //}
    }

    public void addResourceFile(ResourceFileEntry resourceFile, ExplorerStats stats) {
        if (resourceFile.canExport())
            containsExportableFiles = true;

        //tracks whether the folder contains only 1 file type
        if (contentFileType == null) {
            contentFileType = resourceFile.fileType;
        } else if (contentFileType != ResourceFileEntry.FileType.BLANK) {
            if (contentFileType != resourceFile.fileType) {
                contentFileType = ResourceFileEntry.FileType.BLANK;
            }
        }

        if (resourceFile.folderStructureList.isEmpty()) {
            //file goes here
            fileContent.addLast(resourceFile);
            if ("icon.png".equals(resourceFile.getDisplayName())) {
                contentIcon = resourceFile.identifier;
            }


        } else {
            //find next sub folder to move into and remove it from the file search list
            String subFolderName = resourceFile.folderStructureList.getFirst();
            resourceFile.folderStructureList.removeFirst();

            //add the folder if absent
            if (!subFolders.containsKey(subFolderName)) {
                subFolders.put(subFolderName, new ResourceFolderEntry(subFolderName));
                if (stats != null) stats.incFolderCount();
            }

            //iterate placing file into this sub folder
            ResourceFolderEntry subFolder = subFolders.get(subFolderName);
            subFolder.addResourceFile(resourceFile, stats);

        }

    }


    int countOfContentMatchingFilter() {
        return countOfFolderMatchingFilter() + countOfFilesMatchingFilter();
    }

    int countOfFilesMatchingFilter() {
        int count = 0;
        for (ResourceFileEntry resourceFileEntry : fileContent) {
            if (REConfig.getInstance().filterMode.allows(resourceFileEntry)) count++;
        }
        return count;
    }

    int countOfFolderMatchingFilter() {
        int count = 0;
        for (ResourceFolderEntry value : subFolders.values()) {
            if (value.countOfContentMatchingFilter() > 0) count++;
        }
        return count;
    }

    int countOfContentMatchingFilterAndSearch(String search) {
        return countOfFolderMatchingFilterAndSearch(search) + countOfFilesMatchingFilterAndSearch(search);
    }

    int countOfFilesMatchingFilterAndSearch(String search) {
        int count = 0;
        for (ResourceFileEntry resourceFileEntry : fileContent) {
            if (resourceFileEntry.matchesSearch(search) && REConfig.getInstance().filterMode.allows(resourceFileEntry))
                count++;
        }
        return count;
    }

    int countOfFolderMatchingFilterAndSearch(String search) {
        int count = 0;
        for (ResourceFolderEntry value : subFolders.values()) {
            if (value.displayName.contains(search) || value.countOfContentMatchingFilterAndSearch(search) > 0) count++;
        }
        return count;
    }

    public LinkedList<ResourceEntry> getContentFiltered() {
        LinkedList<ResourceEntry> allContent = new LinkedList<>();
        subFolders.keySet().stream().sorted().forEachOrdered(key -> {
            var folder = subFolders.get(key);
            if (folder.countOfContentMatchingFilter() > 0) {
                allContent.add(folder);
            }
        });
        fileContent.stream().sorted().forEachOrdered((file) -> {
            if (REConfig.getInstance().filterMode.allows(file)) {
                allContent.add(file);
            }
        });
        return allContent;
    }

    public LinkedList<ResourceEntry> getContentSearched(final String search) {
        LinkedList<ResourceEntry> allContent = new LinkedList<>();
        for (ResourceEntry entry : getContentFiltered()) {
            if (entry.matchesSearch(search)) {
                allContent.add(entry);
            }
        }
        return allContent;
    }

    public LinkedList<ResourceEntry> getContentViaSearch(final String search) {
        final LinkedList<ResourceEntry> allContent;
        if (search == null || search.isBlank()) {
            allContent = getContentFiltered();
        } else {
            allContent = getContentSearched(search);
        }


        if (topLevelDirectory) {
            //move minecraft namespace to top
            final var mc = subFolders.get("minecraft");
            if (mc != null && allContent.remove(mc)) {
                allContent.addFirst(mc);
            }
        } else {
            //append navigation up folder to top
            NavigateUpEntry upFolder = new NavigateUpEntry("...");
            upFolder.setWidget(this.widget);
            allContent.addFirst(upFolder);
        }

        return allContent;
    }


    private Identifier getRegularIcon() {
        if (folderIcon == null) {

            folderIcon = switch (displayName) {
                case "optifine" -> ExplorerUtils.ICON_FOLDER_OPTIFINE;
                case "minecraft", "realms" -> ExplorerUtils.ICON_FOLDER_MOJANG;
//                case "sound", "sounds", "music" -> REExplorer.ICON_FOLDER_OGG;
//                case "textures", "texture", "images" -> REExplorer.ICON_FOLDER_PNG;
                case "resource_explorer" -> ExplorerUtils.ICON_MOD;
                case "etf", "entity_texture_features" -> ExplorerUtils.ICON_FOLDER_ETF;
                case "emf", "entity_model_features" -> ExplorerUtils.ICON_FOLDER_EMF;
                default -> {
                    if (displayName.startsWith("fabric"))
                        yield ExplorerUtils.ICON_FOLDER_FABRIC;
                    if (!containsExportableFiles && contentFileType != null)
                        yield ExplorerUtils.ICON_FOLDER_BUILT;
                    if (contentFileType == ResourceFileEntry.FileType.PNG)
                        yield ExplorerUtils.ICON_FOLDER_PNG;
                    if (contentFileType == ResourceFileEntry.FileType.OGG)
                        yield ExplorerUtils.ICON_FOLDER_OGG;

                    yield ExplorerUtils.ICON_FOLDER;
                }
            };
        }
        return folderIcon;
    }

    private Identifier getHoverIcon() {
        if (hoverIcon == null) {
            if (contentIcon == null) {
                if (contentFileType != null && subFolders.isEmpty()) {
                    contentIcon = contentFileType.getDefaultIcon();
                    hoverIcon = ExplorerUtils.ICON_FOLDER_BACK;
                } else {
                    hoverIcon = ExplorerUtils.ICON_FOLDER_OPEN;
                }
            } else {
                hoverIcon = ExplorerUtils.ICON_FOLDER_BACK;
            }
        }
        return hoverIcon;
    }

    @Override
    @Nullable Identifier getIcon2OrNull(boolean hovered) {
        return hovered ? contentIcon : null;
    }

    @Override
    Identifier getIcon3OrNull(boolean hovered) {
        return getIcon2OrNull(hovered) == null ? null : ExplorerUtils.ICON_FOLDER_CORNER;
    }

    @Override
    boolean matchesSearch(final String search) {
        if (displayName.matches(search)) return true;

        for (ResourceFolderEntry value : subFolders.values()) {
            if (value.matchesSearch(search)) return true;
        }
        for (ResourceFileEntry resourceFileEntry : fileContent) {
            if (resourceFileEntry.matchesSearch(search)) return true;
        }

        return false;
    }

    @Override
    public Identifier getIcon(boolean hovered) {
        return hovered ? getHoverIcon() : getRegularIcon();
    }

    @Override
    public boolean mouseClickExplorer() {
        ExplorerScreen parent = this.widget.explorerScreen;
        String path = "fabric-api".equals(getDisplayName()) ? parent.cumulativePath : parent.cumulativePath + getDisplayName() + "/";

//        LinkedList<REResourceEntry> content = getContent();
        MinecraftClient.getInstance().setScreen(new ExplorerScreen(parent, this, path));
        return false;
    }


}
