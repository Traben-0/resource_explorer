package traben.resource_explorer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.Resource;
import net.minecraft.resource.metadata.ResourceMetadata;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class REResourceFileEntry extends REResourceEntry {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        REResourceFileEntry that = (REResourceFileEntry) o;
        return identifier.equals(that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }

    public final Identifier identifier;
    private final String displayName;
    private final OrderedText displayText;

    @Nullable
    public final Resource resource;
    public final Type type;

    public final LinkedList<String> folderStructureList;

    public REResourceFileEntry(Identifier identifier,@Nullable Resource resource){
        this.identifier = identifier;
        this.resource = resource;
        this.type = Type.getType(this.identifier);

        //split out folder hierarchy
        String[] splitDirectories = this.identifier.getPath().split("/");
        LinkedList<String> directories = new LinkedList<>(List.of(splitDirectories));

        //final entry is display file name
        displayName = directories.getLast();
        directories.removeLast();

        //remainder is folder hierarchy, which can be empty
        folderStructureList = directories;

        this.displayText = trimmedTextToWidth(displayName).asOrderedText();
    }


    @Override
    public String toString() {
        return toString(0);
    }

    @Override
    public String toString(int indent) {

        return " ".repeat(Math.max(0, indent)) + "\\ "+
                displayName + "\n";
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
    List<Text> getExtraText() {
        ArrayList<Text> text = new ArrayList<>();
        text.add(trimmedTextToWidth(" type: " + type.toString() +(hasMetaData ? " + metadata" : "")) );
        if(resource != null) {
            text.add(trimmedTextToWidth(" pack: " + resource.getResourcePackName().replace("file/","")));
        }else{
            text.add(trimmedTextToWidth("§8§o ~generated by minecraft~" ));
        }
        return text;
    }

    boolean imageDone = false;
    @Override
    public Identifier getIcon(boolean hovered) {

        if(type == Type.PNG && hovered) {
            if(imageDone || resource == null)
                return identifier;

            imageDone = true;

            NativeImage img;
            try {
                InputStream in = resource.getInputStream();
                try {
                    img = NativeImage.read(in);
                    in.close();
                    NativeImageBackedTexture imageBackedTexture = new NativeImageBackedTexture(img);
                    MinecraftClient.getInstance().getTextureManager().registerTexture(identifier, imageBackedTexture);

                    return identifier;
                } catch (Exception e) {
                    //resource.close();
                    in.close();
                }
            } catch (Exception ignored) {}
        }
        return resource == null? ResourceExplorer.ICON_FILE_BUILT : type.getDefaultIcon();
    }


    Boolean hasMetaData = null;
    @Override
    @Nullable Identifier getIcon2OrNull(boolean hovered) {
        if(hasMetaData == null){
            if (resource != null) {
                try {
                    ResourceMetadata meta = resource.getMetadata();
                    hasMetaData = meta != null && meta != ResourceMetadata.NONE;
                } catch (IOException e) {
                    hasMetaData = false;
                }
            }else{
                hasMetaData = false;
            }
        }
        return hasMetaData ? ResourceExplorer.ICON_HAS_META : null;
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        System.out.println("clicked: " + getDisplayName());
        return false;
    }

    public enum Type{
        PNG(ResourceExplorer.ICON_FILE_PNG),
        TXT(ResourceExplorer.ICON_FILE_TEXT),
        JSON(ResourceExplorer.ICON_FILE_JSON),
        PROPERTIES(ResourceExplorer.ICON_FILE_PROPERTY),
        OGG(ResourceExplorer.ICON_FILE_OGG),
        ZIP(ResourceExplorer.ICON_FILE_ZIP),
        JEM(ResourceExplorer.ICON_FILE_JEM),
        JPM(ResourceExplorer.ICON_FILE_JEM),
        OTHER(ResourceExplorer.ICON_FILE_UNKNOWN),
        BLANK(ResourceExplorer.ICON_FILE_BLANK);

        public Identifier getDefaultIcon() {
            return defaultIcon;
        }

        @Override
        public String toString() {
            return switch (this){
                case PNG -> "texture";
                case TXT -> "text";
                case PROPERTIES -> "properties";
                case OGG -> "sound";
                case ZIP -> "zip folder";
                case JEM -> "CEM model";
                case JPM -> "CEM model part";
                case OTHER -> "unknown";
                case BLANK -> "";
                case JSON -> "json";
            };
        }

        private final Identifier defaultIcon;
        Type(Identifier defaultIcon){
            this.defaultIcon = defaultIcon;
        }

        public static Type getType(Identifier identifier){
            String path = identifier.getPath();
            if(path.endsWith(".png")){
                return PNG;
            }
            if(path.endsWith(".json")|| path.endsWith(".json5")){
                return JSON;
            }
            if( path.endsWith(".properties") || path.endsWith(".toml")){
                return PROPERTIES;
            }
            if(path.endsWith(".jem")){
                return JEM;
            }
            if(path.endsWith(".jpm")){
                return JPM;
            }
            if(path.endsWith(".txt")){
                return TXT;
            }
            if(path.endsWith(".ogg") || path.endsWith(".mp3")){
                return OGG;
            }
            if(path.endsWith(".zip")){
                return ZIP;
            }
            return OTHER;
        }


    }
}
