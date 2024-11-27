package traben.resource_explorer.explorer.display.resources.entries;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.resource.Resource;
import net.minecraft.resource.metadata.ResourceMetadata;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import traben.resource_explorer.explorer.ExplorerUtils;
import traben.resource_explorer.explorer.display.ExplorerScreen;
import traben.resource_explorer.explorer.display.detail.entries.DisplayEntry;
import traben.resource_explorer.explorer.display.detail.entries.FileDisplayEntry;
import traben.resource_explorer.mixin.accessors.SpriteAtlasTextureAccessor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static traben.resource_explorer.explorer.ExplorerUtils.outputResourceToPackInternal;

public class ResourceFileEntry extends ResourceEntry {


    public final Identifier identifier;
    @Nullable
    public final Resource resource;
    public final FileType fileType;
    public final LinkedList<String> folderStructureList;
    final AbstractTexture abstractTexture;
    private final String displayName;
    private final OrderedText displayText;
    public MultilineText readTextByLineBreaks = null;
    public int height = 1;
    public int width = 1;
    boolean imageDone = false;
    Boolean hasMetaData = null;

    ResourceFileEntry() {
        //feedback file
        this.identifier = Identifier.of("resource_explorer:feedback_entry");
        this.resource = null;
        this.abstractTexture = null;
        this.fileType = FileType.OTHER;
        displayName = "Explorer details";
        folderStructureList = new LinkedList<>();
        this.displayText = Text.of("Explorer details").asOrderedText();
    }

    public ResourceFileEntry(Identifier identifier, AbstractTexture texture) {
        this.identifier = identifier;
        this.resource = null;
        this.abstractTexture = texture;

        //try to capture some sizes
        if (abstractTexture instanceof SpriteAtlasTexture atlasTexture) {
            width = ((SpriteAtlasTextureAccessor) atlasTexture).callGetWidth();
            height = ((SpriteAtlasTextureAccessor) atlasTexture).callGetHeight();
        } else if (abstractTexture instanceof NativeImageBackedTexture nativeImageBackedTexture) {
            NativeImage image = nativeImageBackedTexture.getImage();
            if (image != null) {
                width = image.getWidth();
                height = image.getHeight();
            }
        }


        this.fileType = FileType.getType(this.identifier);

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


    public ResourceFileEntry(Identifier identifier, @Nullable Resource resource) {
        this.identifier = identifier;
        this.resource = resource;
        this.abstractTexture = null;
        this.fileType = FileType.getType(this.identifier);

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
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceFileEntry that = (ResourceFileEntry) o;
        return identifier.equals(that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }

    @Override
    protected boolean canExport() {
        return resource != null;
    }

    @Override
    public String toString() {
        return toString(0);
    }

    @Override
    public String toString(int indent) {

        return " ".repeat(Math.max(0, indent)) + "\\ " +
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
    public Text[] getExtraText(boolean smallMode) {
        ArrayList<Text> lines = new ArrayList<>();
        lines.add(trimmedTextToWidth(" " + translated("resource_explorer.detail.type") + ": " + fileType.toString() +
                (hasMetaData ? " + " + translated("resource_explorer.detail.metadata") : "")));
        if (resource != null) {
            lines.add(trimmedTextToWidth(" " + translated("resource_explorer.detail.pack") + ": " + resource.getPackId().replace("file/", "")));
        } else {
            lines.add(trimmedTextToWidth("§8§o " + translated("resource_explorer.detail.built_msg")));
        }
        if (smallMode) return lines.toArray(new Text[0]);
        switch (fileType) {
            case PNG -> {
                lines.add(trimmedTextToWidth(" " + translated("resource_explorer.detail.height") + ": " + height));
                lines.add(trimmedTextToWidth(" " + translated("resource_explorer.detail.width") + ": " + width));
                if (hasMetaData && height > width && height % width == 0) {
                    lines.add(trimmedTextToWidth(" " + translated("resource_explorer.detail.frame_count") + ": " + (height / width)));
                }
            }
            case TXT, PROPERTIES, JSON -> {
                if (readTextByLineBreaks != null) {
                    lines.add(trimmedTextToWidth(" " + translated("resource_explorer.detail.lines") + ": " + height));
                    lines.add(trimmedTextToWidth(" " + translated("resource_explorer.detail.character_count") + ": " + width));
                }
            }
            default -> {
            }//lines.add(trimmedTextToWidth(" //todo "));//todo
        }
        return lines.toArray(new Text[0]);
    }

    public MultilineText getTextLines() {
        if (!fileType.isRawTextType())
            return MultilineText.EMPTY;
        if (readTextByLineBreaks == null) {
            if (resource != null) {
                try {
                    InputStream in = resource.getInputStream();
                    try {
                        ArrayList<Text> text = new ArrayList<>();
                        String readString = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                        in.close();

                        //make tabs smaller
                        String reducedTabs = readString.replaceAll("(\t| {4})", " ");


                        String[] splitByLines = reducedTabs.split("\n");

                        width = readString.length();
                        height = splitByLines.length;

                        //trim lines to width now
                        for (int i = 0; i < splitByLines.length; i++) {
                            splitByLines[i] = trimmedStringToWidth(splitByLines[i], 178);
                        }
                        int lineCount = 0;
                        for (String line :
                                splitByLines) {
                            lineCount++;
                            if (lineCount > 512) {//todo set limit in config
                                text.add(Text.of("§l§4-- TEXT LONGER THAN " + 512 + " LINES --"));
                                text.add(Text.of("§r§o " + (height - lineCount) + " lines skipped."));
                                text.add(Text.of("§l§4-- END --"));
                                break;
                            }
                            text.add(Text.of(line));
                        }
                        readTextByLineBreaks = MultilineText.create(MinecraftClient.getInstance().textRenderer, text.toArray(new Text[0]));

                    } catch (Exception e) {
                        //resource.close();
                        in.close();
                        readTextByLineBreaks = MultilineText.EMPTY;
                    }
                } catch (Exception ignored) {
                    readTextByLineBreaks = MultilineText.EMPTY;
                }
            } else {
                readTextByLineBreaks = MultilineText.create(MinecraftClient.getInstance().textRenderer, Text.of(" ERROR: no file info could be read"));
            }
        }
        return readTextByLineBreaks;
    }

    public void exportToOutputPack(ExplorerUtils.REExportContext context) {
        boolean exported = outputResourceToPackInternal(this.identifier,
                resource == null ? null : (file) -> {
                    try {
                        byte[] buffer = resource.getInputStream().readAllBytes();
                        OutputStream outStream = new FileOutputStream(file);
                        outStream.write(buffer);
                        IOUtils.closeQuietly(outStream);
                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                });
        context.tried(this, exported);
    }

    @Override
    public Identifier getIcon(boolean hovered) {


        if (fileType == FileType.PNG && hovered) {
            if (imageDone || resource == null)
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
                    width = img.getWidth();
                    height = img.getHeight();
                    return identifier;
                } catch (Exception e) {
                    //resource.close();
                    in.close();
                }
            } catch (Exception ignored) {
            }
        }
        return resource == null ? ExplorerUtils.ICON_FILE_BUILT : fileType.getDefaultIcon();
    }

    public DisplayEntry wrapEntryAsDetailed() {
        return new FileDisplayEntry(this);
    }

    @Override
    @Nullable Identifier getIcon2OrNull(boolean hovered) {
        if (hasMetaData == null) {
            if (resource != null) {
                try {
                    ResourceMetadata meta = resource.getMetadata();
                    hasMetaData = meta != null && meta != ResourceMetadata.NONE;
                } catch (IOException e) {
                    hasMetaData = false;
                }
            } else {
                hasMetaData = false;
            }
        }
        return hasMetaData ? ExplorerUtils.ICON_HAS_META : null;
    }

    @Override
    boolean matchesSearch(final String search) {
        return displayName.contains(search);
    }


    @Override
    public boolean mouseClickExplorer() {
        if (ExplorerScreen.currentDisplay != null) {
            ExplorerScreen.currentDisplay.setSelectedEntry(this.wrapEntryAsDetailed());
        }
        return true;
    }

    public enum FileType {
        PNG(ExplorerUtils.ICON_FILE_PNG),
        TXT(ExplorerUtils.ICON_FILE_TEXT),
        JSON(ExplorerUtils.ICON_FILE_JSON),
        PROPERTIES(ExplorerUtils.ICON_FILE_PROPERTY),
        OGG(ExplorerUtils.ICON_FILE_OGG),
        ZIP(ExplorerUtils.ICON_FILE_ZIP),
        JEM(ExplorerUtils.ICON_FILE_JEM),
        JPM(ExplorerUtils.ICON_FILE_JEM),
        OTHER(ExplorerUtils.ICON_FILE_UNKNOWN),
        BLANK(ExplorerUtils.ICON_FILE_BLANK);

        private final Identifier defaultIcon;

        FileType(Identifier defaultIcon) {
            this.defaultIcon = defaultIcon;
        }

        public static FileType getType(Identifier identifier) {
            String path = identifier.getPath();
            if (path.endsWith(".png")) {
                return PNG;
            }
            if (path.endsWith(".json") || path.endsWith(".json5")) {
                return JSON;
            }
            if (path.endsWith(".properties") || path.endsWith(".toml")) {
                return PROPERTIES;
            }
            if (path.endsWith(".jem")) {
                return JEM;
            }
            if (path.endsWith(".jpm")) {
                return JPM;
            }
            if (path.endsWith(".txt")) {
                return TXT;
            }
            if (path.endsWith(".ogg") || path.endsWith(".mp3")) {
                return OGG;
            }
            if (path.endsWith(".zip")) {
                return ZIP;
            }
            return OTHER;
        }

        public Identifier getDefaultIcon() {
            return defaultIcon;
        }

        @Override
        public String toString() {
            return switch (this) {
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

        public boolean isRawTextType() {
            return switch (this) {
                case TXT, JSON, JPM, JEM, PROPERTIES -> true;
                default -> false;
            };
        }
    }

}
