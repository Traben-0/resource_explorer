package traben.resource_explorer.explorer;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.resource.Resource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import traben.resource_explorer.REConfig;
import traben.resource_explorer.ResourceExplorerClient;
import traben.resource_explorer.mixin.TextureManagerAccessor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

public class REExplorer {
    public static final Identifier ICON_FILE_BUILT = new Identifier("resource_explorer:textures/file_built.png");
    public static final Identifier ICON_FOLDER_BUILT = new Identifier("resource_explorer:textures/folder_built.png");
    public static final Identifier ICON_FOLDER = new Identifier("resource_explorer:textures/folder.png");
    public static final Identifier ICON_FOLDER_OPEN = new Identifier("resource_explorer:textures/folder_open.png");
    public static final Identifier ICON_FOLDER_BACK = new Identifier("resource_explorer:textures/folder_back.png");
    public static final Identifier ICON_FILE_PNG = new Identifier("resource_explorer:textures/file_png.png");
    public static final Identifier ICON_FILE_TEXT = new Identifier("resource_explorer:textures/file_text.png");
    public static final Identifier ICON_FILE_PROPERTY = new Identifier("resource_explorer:textures/file_property.png");
    public static final Identifier ICON_FILE_OGG = new Identifier("resource_explorer:textures/file_ogg.png");
    public static final Identifier ICON_FILE_UNKNOWN = new Identifier("resource_explorer:textures/file_unknown.png");
    //	public static final Identifier ICON_FILE_MOJANG = new Identifier("resource_explorer:file_mojang.png");
    public static final Identifier ICON_FOLDER_MOJANG = new Identifier("resource_explorer:textures/folder_mojang.png");
    public static final Identifier ICON_FOLDER_OPTIFINE = new Identifier("resource_explorer:textures/folder_optifine.png");
    public static final Identifier ICON_FOLDER_ETF = new Identifier("resource_explorer:textures/folder_etf.png");
    public static final Identifier ICON_FOLDER_EMF = new Identifier("resource_explorer:textures/folder_emf.png");
    //	public static final Identifier ICON_FILE_SMILE = new Identifier("resource_explorer:file_smile.png");
    public static final Identifier ICON_FOLDER_CORNER = new Identifier("resource_explorer:textures/folder_corner.png");
    public static final Identifier ICON_FILE_BLANK = new Identifier("resource_explorer:textures/file_blank.png");
    public static final Identifier ICON_FILE_JSON = new Identifier("resource_explorer:textures/file_json.png");
    public static final Identifier ICON_FOLDER_UP = new Identifier("resource_explorer:textures/folder_up.png");
    public static final Identifier ICON_FOLDER_UP_SELECTED = new Identifier("resource_explorer:textures/folder_up_selected.png");
    public static final Identifier ICON_FILE_ZIP = new Identifier("resource_explorer:textures/file_zip.png");
    public static final Identifier ICON_FILE_JEM = new Identifier("resource_explorer:textures/file_jem.png");
    public static final Identifier ICON_HAS_META = new Identifier("resource_explorer:textures/has_meta.png");
    public static final Identifier ICON_FOLDER_FABRIC = new Identifier("resource_explorer:textures/folder_fabric.png");
    public static final Identifier ICON_MOD = new Identifier("resource_explorer:textures/icon.png");


    public static LinkedList<REResourceEntry> getResourceFolderRoot() {
        try {

            ObjectLinkedOpenHashSet<REResourceFileEntry> allFilesList = new ObjectLinkedOpenHashSet<>();

            boolean print = REConfig.getInstance().logFullFileTree;

            if (print) {
                ResourceExplorerClient.log("/START/");
                ResourceExplorerClient.log("/START/READ/");
            }

            //perform vanilla search with placeholder string that will trigger a blanket resource search
            try {
                Map<Identifier, Resource> resourceMap = MinecraftClient.getInstance().getResourceManager().findResources("resource_explorer$search", (id) -> true);
                resourceMap.forEach((k, v) -> {
                    allFilesList.add(new REResourceFileEntry(k, v));
                });

            } catch (Exception ignored) {
                //the method I use to explore all resources will cause an exception once at the end of the resource list as I need to search for blank file names
            }

            //fabric resources allow direct blank searches so catch those too
            Map<Identifier, Resource> resourceMap2 = MinecraftClient.getInstance().getResourceManager().findResources("", (id) -> true);
            resourceMap2.forEach((k, v) -> {
                allFilesList.add(new REResourceFileEntry(k, v));
            });

            //search for generated texture assets
            Map<Identifier, AbstractTexture> textures = ((TextureManagerAccessor) MinecraftClient.getInstance().getTextureManager()).getTextures();
            textures.forEach((k, v) -> {
                allFilesList.add(new REResourceFileEntry(k, v));
            });


            if (print) {
                ResourceExplorerClient.log("/END/READ/");
                ResourceExplorerClient.log("/START/FOLDER_SORT/");
            }
            Set<String> namespaces = MinecraftClient.getInstance().getResourceManager().getAllNamespaces();

            LinkedList<REResourceEntry> namesSpaceFoldersRoot = new LinkedList<>();
            Map<String, REResourceFolderEntry> namespaceFolderMap = new HashMap<>();

            LinkedList<REResourceFolderEntry> fabricApiFolders = new LinkedList<>();

            REResourceFolderEntry minecraftFolder = new REResourceFolderEntry("minecraft");
            namespaceFolderMap.put("minecraft", minecraftFolder);
            namespaces.remove("minecraft");

            for (String nameSpace :
                    namespaces) {
                REResourceFolderEntry namespaceFolder = new REResourceFolderEntry(nameSpace);

                if ("fabric".equals(nameSpace) || nameSpace.matches("(fabric-.*|renderer-registries)(renderer|api|-v\\d).*")) {
                    fabricApiFolders.add(namespaceFolder);
                } else {
                    namesSpaceFoldersRoot.addLast(namespaceFolder);
                }
                namespaceFolderMap.put(nameSpace, namespaceFolder);

            }
            //fabric api all in 1
            if (!fabricApiFolders.isEmpty()) {
                REResourceFolderEntry fabricApiFolder = new REResourceFolderEntry("fabric-api");
                fabricApiFolder.contentIcon = new Identifier("fabricloader", "icon.png");
                fabricApiFolders.forEach(fabricApiFolder::addSubFolder);
                namesSpaceFoldersRoot.addFirst(fabricApiFolder);
            }
            //get filter
            REConfig.REFileFilter filter = REConfig.getInstance().filterMode;

            //minecraft at the top
            if (filter != REConfig.REFileFilter.ONLY_FROM_PACKS_NO_GENERATED)
                namesSpaceFoldersRoot.addFirst(minecraftFolder);

            //here allFilesAndFoldersRoot is only empty namespace directories

            REStats statistics = new REStats();

            //iterate over all files and give them folder structure
            for (REResourceFileEntry resourceFile :
                    allFilesList) {
                if (filter.allows(resourceFile)) {
                    String namespace = resourceFile.identifier.getNamespace();
                    REResourceFolderEntry namespaceFolder = namespaceFolderMap.get(namespace);
                    if (namespaceFolder != null) {
                        namespaceFolder.addResourceFile(resourceFile, statistics);
                    }
                    statistics.addEntryStatistic(resourceFile, true);
                } else {
                    statistics.addEntryStatistic(resourceFile, false);
                }
            }

            if (print) {
                namesSpaceFoldersRoot.forEach(System.out::println);
                ResourceExplorerClient.log("/END/FOLDER_SORT/");
                ResourceExplorerClient.log("/END/");
            }

            REExplorerScreen.currentStats = statistics;

            return namesSpaceFoldersRoot;
        }catch (Exception e){
            LinkedList<REResourceEntry> fail = new LinkedList<>();
            fail.add(REResourceFileEntry.FAILED_FILE);
            return fail;
        }
    }




    @SuppressWarnings("ResultOfMethodCallIgnored")
    static boolean outputResourceToPackInternal(REResourceFileEntry reResourceFileEntry){
        //only save existing file resources
        if(reResourceFileEntry.resource == null)
            return false;

        Path resourcePackFolder = MinecraftClient.getInstance().getResourcePackDir();
        File thisPackFolder = new File(resourcePackFolder.toFile(),"resource_explorer/");

        if(validateOutputResourcePack(thisPackFolder)) {
            if (thisPackFolder.exists()) {
                File assets = new File(thisPackFolder, "assets");
                if (!assets.exists()) {
                    assets.mkdir();
                }
                File namespace = new File(assets, reResourceFileEntry.identifier.getNamespace());
                if (!namespace.exists()) {
                    namespace.mkdir();
                }
                String[] pathList = reResourceFileEntry.identifier.getPath().split("/");
                String file = pathList[pathList.length - 1];
                String directories = reResourceFileEntry.identifier.getPath().replace(file, "");
                File directoryFolder = new File(namespace, directories);
                if (!directoryFolder.exists()) {
                    directoryFolder.mkdirs();
                }
                if (directoryFolder.exists()) {
                    if (reResourceFileEntry.fileType.isRawTextType()) {
                        File txtFile = new File(directoryFolder, file);
                        try {
                            FileWriter fileWriter = new FileWriter(txtFile);
                            fileWriter.write(new String(reResourceFileEntry.resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
                            fileWriter.close();
                            ResourceExplorerClient.log(" output resource file created.");
                            return true;
                        } catch (IOException e) {
                            ResourceExplorerClient.log(" output resource file not created.");
                        }
                    } else if (reResourceFileEntry.fileType == REResourceFileEntry.FileType.PNG) {
                        File thisImgFile = new File(directoryFolder, file + (file.endsWith(".png") ? "" : ".png"));
                        try {
                            InputStream stream = reResourceFileEntry.resource.getInputStream();
                            NativeImage.read(stream).writeTo(thisImgFile);
                            ResourceExplorerClient.log(" output resource image created.");
                            return true;
                        } catch (IOException e) {
                            ResourceExplorerClient.log(" output resource image not created.");
                        }
                    }
                }
            }
        }
        return false;
    }



    private static boolean validateOutputResourcePack(File packFolder){
        if(!packFolder.exists()){
            if(packFolder.mkdir()) {
                return validateOutputResourcePackMeta(packFolder);
            }
        }else {
            return validateOutputResourcePackMeta(packFolder);
        }
        return false;
    }

    private static boolean validateOutputResourcePackMeta(File packFolder){
        File thisMetaFile = new File(packFolder,"pack.mcmeta");
        if (thisMetaFile.exists()) {
            return true;
        }
        String mcmeta = """
                {
                \t"pack": {
                \t\t"pack_format": 15,
                \t\t"supported_formats":[0,99],
                \t\t"description": "Output file for the Resource Explorer mod"
                \t}
                }""";
        try {
            FileWriter fileWriter = new FileWriter(thisMetaFile);
            fileWriter.write(mcmeta);
            fileWriter.close();
            ResourceExplorerClient.log(" output resource-pack created.");
        } catch (IOException e) {
            ResourceExplorerClient.log(" output resource-pack not created.");
        }
        File thisIconFile = new File(packFolder, "pack.png");
        Optional<Resource> image = MinecraftClient.getInstance().getResourceManager().getResource(ICON_FOLDER_BUILT);
        if (image.isPresent()) {
            try {
                InputStream stream = image.get().getInputStream();
                NativeImage.read(stream).writeTo(thisIconFile);
                ResourceExplorerClient.log(" output resource-pack icon created.");
            } catch (IOException e) {
                ResourceExplorerClient.log(" output resource-pack icon not created.");
            }
        }
        return thisMetaFile.exists();
    }

    static class REExportContext{

        int vanillaCount = 0;
        int packCount = 0;
        int moddedCount = 0;

        int totalAttempted = 0;

        REExportContext(){}

        public int getTotatExported(){
            return vanillaCount + packCount +moddedCount;
        }

        public int getTotatAttempted(){
            return totalAttempted;
        }

        public void tried(REResourceFileEntry file, boolean exported){
            totalAttempted++;
            if(exported && file.resource != null && file.fileType.isExportableType()){
                if("minecraft".equals(file.identifier.getNamespace())){
                    if( "vanilla".equals(file.resource.getResourcePackName())) {
                        vanillaCount++;
                    }else {
                        packCount++;
                    }
                }else{
                    if("vanilla".equals(file.resource.getResourcePackName()) ||
                       "fabric".equals(file.resource.getResourcePackName()) ||
                       "forge".equals(file.resource.getResourcePackName())//todo check this is what forge does
                    ) {
                        moddedCount++;
                    }else {
                        packCount++;
                    }
                }
            }
        }

        public void showExportToast() {
            ToastManager toastManager = MinecraftClient.getInstance().getToastManager();
            boolean partially = getTotatAttempted() != getTotatExported() && totalAttempted != 1 && getTotatExported() != 0;
            Text title = partially ?
                    Text.of(Text.translatable("resource_explorer.export_warn.partial")+" "+getTotatExported()+"/"+getTotatAttempted()):
                    Text.translatable(getTotatAttempted() == getTotatExported() ?
                        ResourceExplorerClient.MOD_ID+".export_warn":
                        ResourceExplorerClient.MOD_ID+".export_warn.fail");

            SystemToast.show(toastManager, SystemToast.Type.PERIODIC_NOTIFICATION, title, getMessage());
        }

        private Text getMessage(){
            if(getTotatExported() == 0){
                return Text.translatable("resource_explorer.export_warn.none");
            }
            if(getTotatAttempted() == 1){
               return Text.translatable(
                       packCount >0 ?
                               "resource_explorer.export_warn.pack" :
                               moddedCount > 0 ?
                                       "resource_explorer.export_warn.mod":
                                       "resource_explorer.export_warn.vanilla"
               );
            }else{
                return Text.translatable("resource_explorer.export_warn.all");
            }
        }

    }
}