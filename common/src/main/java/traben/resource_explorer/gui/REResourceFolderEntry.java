package traben.resource_explorer.gui;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import traben.resource_explorer.ResourceExplorer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class REResourceFolderEntry extends REResourceEntry {



    private final LinkedList<REResourceFileEntry> fileContent = new LinkedList<>();

    private final Object2ObjectLinkedOpenHashMap<String, REResourceFolderEntry> subFolders = new Object2ObjectLinkedOpenHashMap<>();

    private final String displayName;
    private final OrderedText displayText;

    private Identifier folderIcon = null;
    public REResourceFolderEntry(String folderName){
        this.displayName = folderName;
        this.displayText = trimmedTextToWidth(folderName).asOrderedText();
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
        if(subFolders.size() > 0)
            text.add(trimmedTextToWidth(" "+subFolders.size() + " folder" + (subFolders.size() > 1 ? "s" : "")));
        if(fileContent.size() > 0)
            text.add(trimmedTextToWidth(" "+fileContent.size() + " file" + (fileContent.size() > 1 ? "s" : "")));

        if(smallMode && text.size() >=2) return text;

        if(ResourceExplorer.ICON_FOLDER_BUILT.equals(folderIcon))
            text.add(trimmedTextToWidth("§8§o generated by code" ));
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
        for (REResourceEntry content:
                subFolders.values()) {
            builder.append(content.toString(indent+1));
        }
        for (REResourceEntry content:
             fileContent) {
            builder.append(content.toString(indent+1));
        }
        return builder.toString();
    }
    public void addSubFolder(REResourceFolderEntry resourceFolder) {
        subFolders.put(resourceFolder.displayName,resourceFolder);
    }

    public Identifier contentIcon = null;
    private REResourceFileEntry.FileType contentFileType = null;



    public void addResourceFile(REResourceFileEntry resourceFile) {
        if(resourceFile.folderStructureList.isEmpty()){
            //file goes here
            fileContent.addLast(resourceFile);
            if("icon.png".equals(resourceFile.getDisplayName())){
                contentIcon = resourceFile.identifier;
            }

            //figure out if folder contains only 1 content type and no folders
            if(contentFileType == null){
                contentFileType = resourceFile.fileType;
            }else if(contentFileType != REResourceFileEntry.FileType.BLANK){
                if(contentFileType != resourceFile.fileType){
                    contentFileType = REResourceFileEntry.FileType.BLANK;
                }
            }
        }else{
            //mark folder to have no extra icon
            contentFileType = REResourceFileEntry.FileType.BLANK;
            //find next sub folder to move into and remove it from the file search list
            String subFolderName = resourceFile.folderStructureList.getFirst();
            resourceFile.folderStructureList.removeFirst();

            //add the folder if absent
            if(!subFolders.containsKey(subFolderName)){
                subFolders.put(subFolderName, new REResourceFolderEntry(subFolderName));
            }

            //iterate placing file into this sub folder
            REResourceFolderEntry subFolder = subFolders.get(subFolderName);
            subFolder.addResourceFile(resourceFile);

        }

    }


    public LinkedList<REResourceEntry> getContent(){
        LinkedList<REResourceEntry> allContent = new LinkedList<>();
        subFolders.keySet().stream().sorted().forEachOrdered(key-> allContent.add(subFolders.get(key)));
        fileContent.stream().sorted().forEachOrdered(allContent::add);


        UpOneDirFolder upFolder = new UpOneDirFolder("...");
        upFolder.setWidget(this.widget);
        allContent.addFirst(upFolder);

        return allContent;
    }


    private Identifier getRegularIcon(){
        if(folderIcon == null){
            folderIcon = switch (displayName){
                case "optifine"-> ResourceExplorer.ICON_FOLDER_OPTIFINE;
                case "minecraft", "realms"-> ResourceExplorer.ICON_FOLDER_MOJANG;
                case "resource_explorer"-> ResourceExplorer.ICON_MOD;
                case "etf"-> ResourceExplorer.ICON_FOLDER_ETF;
                case "emf"-> ResourceExplorer.ICON_FOLDER_EMF;
                default -> {
                    if(displayName.startsWith("fabric")) yield  ResourceExplorer.ICON_FOLDER_FABRIC;

                    if(fileContent.isEmpty()) yield  ResourceExplorer.ICON_FOLDER;

                    boolean allBuiltFiles = true;
                    for (REResourceFileEntry entry:
                         fileContent) {
                        if(entry.resource != null){
                            allBuiltFiles = false;
                            break;
                        }
                    }
                    if(allBuiltFiles)
                        yield ResourceExplorer.ICON_FOLDER_BUILT;
                    yield  ResourceExplorer.ICON_FOLDER;
                }
            };
        }
        return folderIcon;
    }

    private Identifier hoverIcon = null;

    private Identifier getHoverIcon(){
        if(hoverIcon == null){
            if (contentIcon == null) {
                if(contentFileType != null && subFolders.isEmpty()){
                    contentIcon = contentFileType.getDefaultIcon();
                    hoverIcon = ResourceExplorer.ICON_FOLDER_BACK;
                }else {
                    hoverIcon = ResourceExplorer.ICON_FOLDER_OPEN;
                }
            } else {
                hoverIcon = ResourceExplorer.ICON_FOLDER_BACK;
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
        return getIcon2OrNull(hovered) == null ? null : ResourceExplorer.ICON_FOLDER_CORNER;
    }

    @Override
    public Identifier getIcon(boolean hovered) {
        return hovered ? getHoverIcon() : getRegularIcon();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        REDirectoryScreen parent = this.widget.screen;
        String path = "fabric-api".equals(getDisplayName()) ? parent.cumulativePath : parent.cumulativePath+getDisplayName() + "/";

        LinkedList<REResourceEntry> content = getContent();
        MinecraftClient.getInstance().setScreen(new REDirectoryScreen(parent.vanillaParent, parent, content, path));
        return false;
    }

    public static class UpOneDirFolder extends REResourceFolderEntry{

        public UpOneDirFolder(String folderName) {
            super(folderName);
        }
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            Screen parent = this.widget.screen.reParent;
            MinecraftClient.getInstance().setScreen(parent);
           // this.widget.screen.close();
            return false;
        }
        @Override
        public Identifier getIcon(boolean hovered) {
            return hovered ? ResourceExplorer.ICON_FOLDER_UP_SELECTED : ResourceExplorer.ICON_FOLDER_UP;
        }

        @Override
        List<Text> getExtraText(boolean ignored) {
            return List.of(Text.of("§8§o move up directory"));
        }
    }
}
