package traben.resource_explorer.explorer;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import traben.resource_explorer.REConfig;
import traben.resource_explorer.ResourceExplorerClient;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class REResourceFolder extends REResourceEntry {


    private final LinkedList<REResourceFile> fileContent = new LinkedList<>();

    private final Object2ObjectLinkedOpenHashMap<String, REResourceFolder> subFolders = new Object2ObjectLinkedOpenHashMap<>();

    private final String displayName;
    private final OrderedText displayText;
    public Identifier contentIcon = null;
    private Identifier folderIcon = null;
    private REResourceFile.FileType contentFileType = null;
    private boolean containsExportableFiles = false;
    private Identifier hoverIcon = null;

    public REResourceFolder(String folderName) {
        this.displayName = folderName;
        this.displayText = trimmedTextToWidth(folderName).asOrderedText();
        topLevelDirectory = false;
    }

    private boolean topLevelDirectory;

    public REResourceFolder(String folderName, List<REResourceEntry> entries) {
        topLevelDirectory = true;
        this.displayName = folderName;
        this.displayText = trimmedTextToWidth(folderName).asOrderedText();
        for (REResourceEntry entry : entries) {
            if (entry instanceof REResourceFolder folder) {
                addSubFolder(folder);
            } else {
                ResourceExplorerClient.logError("non folder in folder only init");
            }
        }
    }

    @Override
    boolean canExport() {
        return containsExportableFiles;
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

        int sizeFolders = countOfFolderMatchingFilterAndSearch(REExplorerScreen.searchTerm);
        if (sizeFolders > 0) {
            text.add(trimmedTextToWidth(" " + sizeFolders + " " + translated(sizeFolders > 1 ?
                    "resource_explorer.detail.folders" : "resource_explorer.detail.folder")));
        }
        int sizeFiles = countOfFilesMatchingFilterAndSearch(REExplorerScreen.searchTerm);
        if (sizeFiles > 0) {
            boolean multiple = sizeFiles > 1;
            String fileWord;

            if (folderIcon == REExplorer.ICON_FOLDER_BUILT) {
                fileWord = translated(multiple ? "resource_explorer.detail.built_files" : "resource_explorer.detail.built_file");
            } else {
                fileWord = translated(multiple ? "resource_explorer.detail.files" : "resource_explorer.detail.file");
            }
            text.add(trimmedTextToWidth(" " + sizeFiles + " " + fileWord));
        }

        if (smallMode && text.size() >= 2) return text;

        if (REExplorer.ICON_FOLDER_BUILT.equals(folderIcon))
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
        for (REResourceEntry content :
                subFolders.values()) {
            builder.append(content.toString(indent + 1));
        }
        for (REResourceEntry content :
                fileContent) {
            builder.append(content.toString(indent + 1));
        }
        return builder.toString();
    }

    public void addSubFolder(REResourceFolder resourceFolder) {
        subFolders.put(resourceFolder.displayName, resourceFolder);
    }


    @Override
    public void exportToOutputPack(REExplorer.REExportContext context) {
        //if(canExport()){
        fileContent.forEach(file -> file.exportToOutputPack(context));
        subFolders.values().forEach(file -> file.exportToOutputPack(context));
        //}
    }

    public void addResourceFile(REResourceFile resourceFile, REStats stats) {
        if (resourceFile.canExport())
            containsExportableFiles = true;

        //tracks whether the folder contains only 1 file type
        if (contentFileType == null) {
            contentFileType = resourceFile.fileType;
        } else if (contentFileType != REResourceFile.FileType.BLANK) {
            if (contentFileType != resourceFile.fileType) {
                contentFileType = REResourceFile.FileType.BLANK;
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
                subFolders.put(subFolderName, new REResourceFolder(subFolderName));
                if (stats != null) stats.folderCount++;
            }

            //iterate placing file into this sub folder
            REResourceFolder subFolder = subFolders.get(subFolderName);
            subFolder.addResourceFile(resourceFile, stats);

        }

    }


    int countOfContentMatchingFilter() {
        return countOfFolderMatchingFilter() + countOfFilesMatchingFilter();
    }

    int countOfFilesMatchingFilter() {
        int count = 0;
        for (REResourceFile reResourceFile : fileContent) {
            if (REConfig.getInstance().filterMode.allows(reResourceFile)) count++;
        }
        return count;
    }

    int countOfFolderMatchingFilter() {
        int count = 0;
        for (REResourceFolder value : subFolders.values()) {
            if (value.countOfContentMatchingFilter() > 0) count++;
        }
        return count;
    }

    int countOfContentMatchingFilterAndSearch(String search) {
        return countOfFolderMatchingFilterAndSearch(search) + countOfFilesMatchingFilterAndSearch(search);
    }

    int countOfFilesMatchingFilterAndSearch(String search) {
        int count = 0;
        for (REResourceFile reResourceFile : fileContent) {
            if (reResourceFile.matchesSearch(search) && REConfig.getInstance().filterMode.allows(reResourceFile))
                count++;
        }
        return count;
    }

    int countOfFolderMatchingFilterAndSearch(String search) {
        int count = 0;
        for (REResourceFolder value : subFolders.values()) {
            if (value.displayName.contains(search) || value.countOfContentMatchingFilterAndSearch(search) > 0) count++;
        }
        return count;
    }

    public LinkedList<REResourceEntry> getContentFiltered() {
        LinkedList<REResourceEntry> allContent = new LinkedList<>();
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

    public LinkedList<REResourceEntry> getContentSearched(final String search) {
        LinkedList<REResourceEntry> allContent = new LinkedList<>();
        for (REResourceEntry entry : getContentFiltered()) {
            if (entry.matchesSearch(search)) {
                allContent.add(entry);
            }
        }
        return allContent;
    }

    public LinkedList<REResourceEntry> getContentViaSearch(final String search) {
        final LinkedList<REResourceEntry> allContent;
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
            UpOneDirFolder upFolder = new UpOneDirFolder("...");
            upFolder.setWidget(this.widget);
            allContent.addFirst(upFolder);
        }

        return allContent;
    }


    private Identifier getRegularIcon() {
        if (folderIcon == null) {

            folderIcon = switch (displayName) {
                case "optifine" -> REExplorer.ICON_FOLDER_OPTIFINE;
                case "minecraft", "realms" -> REExplorer.ICON_FOLDER_MOJANG;
//                case "sound", "sounds", "music" -> REExplorer.ICON_FOLDER_OGG;
//                case "textures", "texture", "images" -> REExplorer.ICON_FOLDER_PNG;
                case "resource_explorer" -> REExplorer.ICON_MOD;
                case "etf", "entity_texture_features" -> REExplorer.ICON_FOLDER_ETF;
                case "emf", "entity_model_features" -> REExplorer.ICON_FOLDER_EMF;
                default -> {
                    if (displayName.startsWith("fabric"))
                        yield REExplorer.ICON_FOLDER_FABRIC;
                    if (!containsExportableFiles && contentFileType != null)
                        yield REExplorer.ICON_FOLDER_BUILT;
                    if (contentFileType == REResourceFile.FileType.PNG)
                        yield REExplorer.ICON_FOLDER_PNG;
                    if (contentFileType == REResourceFile.FileType.OGG)
                        yield REExplorer.ICON_FOLDER_OGG;

                    yield REExplorer.ICON_FOLDER;
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
                    hoverIcon = REExplorer.ICON_FOLDER_BACK;
                } else {
                    hoverIcon = REExplorer.ICON_FOLDER_OPEN;
                }
            } else {
                hoverIcon = REExplorer.ICON_FOLDER_BACK;
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
        return getIcon2OrNull(hovered) == null ? null : REExplorer.ICON_FOLDER_CORNER;
    }

    @Override
    boolean matchesSearch(final String search) {
        if (displayName.matches(search)) return true;

        for (REResourceFolder value : subFolders.values()) {
            if (value.matchesSearch(search)) return true;
        }
        for (REResourceFile reResourceFile : fileContent) {
            if (reResourceFile.matchesSearch(search)) return true;
        }

        return false;
    }

    @Override
    public Identifier getIcon(boolean hovered) {
        return hovered ? getHoverIcon() : getRegularIcon();
    }

    @Override
    public boolean mouseClickExplorer() {
        REExplorerScreen parent = this.widget.screen;
        String path = "fabric-api".equals(getDisplayName()) ? parent.cumulativePath : parent.cumulativePath + getDisplayName() + "/";

//        LinkedList<REResourceEntry> content = getContent();
        MinecraftClient.getInstance().setScreen(new REExplorerScreen(parent, this, path));
        return false;
    }


    public static class UpOneDirFolder extends REResourceFolder {


        public UpOneDirFolder(String folderName) {
            super(folderName);
        }

        @Override
        public boolean mouseClickExplorer() {
            Screen parent = this.widget.screen.reParent;
            MinecraftClient.getInstance().setScreen(parent);
            // this.widget.screen.close();
            return false;
        }

        @Override
        public Identifier getIcon(boolean hovered) {
            return hovered ? REExplorer.ICON_FOLDER_UP_SELECTED : REExplorer.ICON_FOLDER_UP;
        }

        @Override
        List<Text> getExtraText(boolean ignored) {
            return List.of(Text.of("§8§o move up directory"));
        }

        @Override
        boolean canExport() {
            return false;
        }

    }


}
