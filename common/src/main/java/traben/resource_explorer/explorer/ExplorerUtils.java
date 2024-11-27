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
import traben.resource_explorer.explorer.display.ExplorerScreen;
import traben.resource_explorer.explorer.display.resources.entries.ExplorerDetailsEntry;
import traben.resource_explorer.explorer.display.resources.entries.ResourceEntry;
import traben.resource_explorer.explorer.display.resources.entries.ResourceFileEntry;
import traben.resource_explorer.explorer.display.resources.entries.ResourceFolderEntry;
import traben.resource_explorer.explorer.stats.ExplorerStats;
import traben.resource_explorer.mixin.accessors.TextureManagerAccessor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

public abstract class ExplorerUtils {
    public static final Identifier ICON_FILE_BUILT = Identifier.of("resource_explorer:textures/file_built.png");
    public static final Identifier ICON_FOLDER_BUILT = Identifier.of("resource_explorer:textures/folder_built.png");
    public static final Identifier ICON_FOLDER = Identifier.of("resource_explorer:textures/folder.png");
    public static final Identifier ICON_FOLDER_OPEN = Identifier.of("resource_explorer:textures/folder_open.png");
    public static final Identifier ICON_FOLDER_BACK = Identifier.of("resource_explorer:textures/folder_back.png");
    public static final Identifier ICON_FILE_PNG = Identifier.of("resource_explorer:textures/file_png.png");
    public static final Identifier ICON_FILE_TEXT = Identifier.of("resource_explorer:textures/file_text.png");
    public static final Identifier ICON_FILE_PROPERTY = Identifier.of("resource_explorer:textures/file_property.png");
    public static final Identifier ICON_FILE_OGG = Identifier.of("resource_explorer:textures/file_ogg.png");
    public static final Identifier ICON_FILE_UNKNOWN = Identifier.of("resource_explorer:textures/file_unknown.png");
    public static final Identifier ICON_FOLDER_MOJANG = Identifier.of("resource_explorer:textures/folder_mojang.png");
    public static final Identifier ICON_FOLDER_OPTIFINE = Identifier.of("resource_explorer:textures/folder_optifine.png");
    public static final Identifier ICON_FOLDER_ETF = Identifier.of("resource_explorer:textures/folder_etf.png");
    public static final Identifier ICON_FOLDER_EMF = Identifier.of("resource_explorer:textures/folder_emf.png");
    public static final Identifier ICON_FOLDER_CORNER = Identifier.of("resource_explorer:textures/folder_corner.png");
    public static final Identifier ICON_FILE_BLANK = Identifier.of("resource_explorer:textures/file_blank.png");
    public static final Identifier ICON_FILE_JSON = Identifier.of("resource_explorer:textures/file_json.png");
    public static final Identifier ICON_FOLDER_UP = Identifier.of("resource_explorer:textures/folder_up.png");
    public static final Identifier ICON_FOLDER_UP_SELECTED = Identifier.of("resource_explorer:textures/folder_up_selected.png");
    public static final Identifier ICON_FILE_ZIP = Identifier.of("resource_explorer:textures/file_zip.png");
    public static final Identifier ICON_FILE_JEM = Identifier.of("resource_explorer:textures/file_jem.png");
    public static final Identifier ICON_HAS_META = Identifier.of("resource_explorer:textures/has_meta.png");
    public static final Identifier ICON_FOLDER_FABRIC = Identifier.of("resource_explorer:textures/folder_fabric.png");
    public static final Identifier ICON_FOLDER_PNG = Identifier.of("resource_explorer:textures/folder_png.png");
    public static final Identifier ICON_FOLDER_OGG = Identifier.of("resource_explorer:textures/folder_ogg.png");
    public static final Identifier ICON_MOD = Identifier.of("resource_explorer:textures/icon.png");

    public static final String SEARCH_KEY = "resource_explorer$search";
    private static final List<String> searchedExceptions = new ArrayList<>();
    private static boolean isSearching = false;

    public static boolean isSearching() {
        return isSearching;
    }

    /**
     * performs a search of all loaded packs and returns the root namespace level directory for the explorer to navigate from
     * This search 'breaks' the minecraft resource system (explained below) and all screens and tasks that utilise this should trigger a
     * full resource reload upon completion, As all Resource Explorer screens do, as well as handle their texture usage accordingly.
     * <p>
     * It seems this searching method specifically 'breaks' the resource system in a way that makes it so all textures not
     * already registered to the TextureManager will no longer automatically register upon attempts to use the texture.
     * I.E if "cobblestone.png" is not already registered prior to this search, it will resolve to an error texture if used.
     * While in this 'broken' state this issue can be rectified where required by manually registering the texture to the TextureManager before usage.
     * If a resource reload is not triggered upon closing the screen, the resource system will remain in this 'broken' state.
     *
     * @return a list of all namespace folders or a singleton list containing the failure context
     */
    public static LinkedList<ResourceEntry> getResourceFolderRoot() {
        try {
            isSearching = true;
            searchedExceptions.clear();

            ObjectLinkedOpenHashSet<ResourceFileEntry> allFilesList = new ObjectLinkedOpenHashSet<>();

            boolean print = REConfig.getInstance().logFullFileTree;

            if (print) {
                ResourceExplorerClient.log("/START/");
                ResourceExplorerClient.log("/START/READ/");
            }

            //perform vanilla search with placeholder string that will trigger a blanket resource search
            // zip resource-packs specifically do not allow empty search strings
            try {
                Map<Identifier, Resource> resourceMap = MinecraftClient.getInstance().getResourceManager().findResources(SEARCH_KEY, (id) -> true);
                resourceMap.forEach((k, v) -> allFilesList.add(new ResourceFileEntry(k, v)));

            } catch (Exception ignored) {
                //the method I use to explore all resources will cause an exception once at the end of the resource list as I need to search for blank file names
            }

            //fabric mod resources allow direct blank searches so catch those too
            try {
                Map<Identifier, Resource> resourceMap2 = MinecraftClient.getInstance().getResourceManager().findResources("", (id) -> true);
                resourceMap2.forEach((k, v) -> allFilesList.add(new ResourceFileEntry(k, v)));
            } catch (Exception ignored) {
            }


            //search for generated texture assets
            Map<Identifier, AbstractTexture> textures = ((TextureManagerAccessor) MinecraftClient.getInstance().getTextureManager()).getTextures();
            textures.forEach((k, v) -> allFilesList.add(new ResourceFileEntry(k, v)));


            if (print) {
                ResourceExplorerClient.log("/END/READ/");
                ResourceExplorerClient.log("/START/FOLDER_SORT/");
            }
            Set<String> namespaces = MinecraftClient.getInstance().getResourceManager().getAllNamespaces();

            LinkedList<ResourceEntry> namesSpaceFoldersRoot = new LinkedList<>();
            Map<String, ResourceFolderEntry> namespaceFolderMap = new HashMap<>();

            LinkedList<ResourceFolderEntry> fabricApiFolders = new LinkedList<>();

            ResourceFolderEntry minecraftFolder = new ResourceFolderEntry("minecraft");
            namespaceFolderMap.put("minecraft", minecraftFolder);
            namespaces.remove("minecraft");

            for (String nameSpace :
                    namespaces) {
                ResourceFolderEntry namespaceFolder = new ResourceFolderEntry(nameSpace);

                if ("fabric".equals(nameSpace) || nameSpace.matches("(fabric-.*|renderer-registries)(renderer|api|-v\\d).*")) {
                    fabricApiFolders.add(namespaceFolder);
                } else {
                    namesSpaceFoldersRoot.addLast(namespaceFolder);
                }
                namespaceFolderMap.put(nameSpace, namespaceFolder);

            }
            //fabric api all in 1
            if (!fabricApiFolders.isEmpty()) {
                ResourceFolderEntry fabricApiFolder = new ResourceFolderEntry("fabric-api");
                fabricApiFolder.contentIcon = Identifier.of("fabricloader", "icon.png");
                fabricApiFolders.forEach(fabricApiFolder::addSubFolder);
                namesSpaceFoldersRoot.addFirst(fabricApiFolder);
            }
            //get filter
//            REConfig.REFileFilter filter = REConfig.getInstance().filterMode;

            //minecraft at the top
//            if (filter != REConfig.REFileFilter.ONLY_FROM_PACKS_NO_GENERATED)
            namesSpaceFoldersRoot.addFirst(minecraftFolder);

            //here allFilesAndFoldersRoot is only empty namespace directories

            ExplorerStats statistics = new ExplorerStats();

            //iterate over all files and give them folder structure
            for (ResourceFileEntry resourceFile :
                    allFilesList) {
//                if (filter.allows(resourceFile)) {
                String namespace = resourceFile.identifier.getNamespace();
                ResourceFolderEntry namespaceFolder = namespaceFolderMap.get(namespace);
                if (namespaceFolder == null) {
                    namespaceFolder = new ResourceFolderEntry(namespace);
                    namespaceFolderMap.put(namespace, namespaceFolder);
                    namesSpaceFoldersRoot.addLast(namespaceFolder);
                }
                namespaceFolder.addResourceFile(resourceFile, statistics);
                statistics.addEntryStatistic(resourceFile);//, true);
//                } else {
//                    statistics.addEntryStatistic(resourceFile, false);
//                }
            }

            if (print) {
                namesSpaceFoldersRoot.forEach(System.out::println);
                ResourceExplorerClient.log("/END/FOLDER_SORT/");
                ResourceExplorerClient.log("/END/");
            }

            ExplorerScreen.currentStats = statistics;

            insertFeedbackIfRequired(namesSpaceFoldersRoot, print);

            return namesSpaceFoldersRoot;
        } catch (Exception e) {
            e.printStackTrace();
            LinkedList<ResourceEntry> fail = new LinkedList<>();

            StringBuilder error = new StringBuilder(e.getMessage()).append("\n");
            for (StackTraceElement line : e.getStackTrace()) {
                error.append(line.toString()).append("\n");
            }
            addSearchException(error.toString());
            insertFeedbackIfRequired(fail, true);
            return fail;
        } finally {
            searchedExceptions.clear();
            isSearching = false;
        }
    }

    private static void insertFeedbackIfRequired(LinkedList<ResourceEntry> namesSpaceFoldersRoot, boolean print) {
        //insert feedback if there were any exceptions
        //or if the search returned no results
        boolean resultOnlyContainsEmptyMCFolder = namesSpaceFoldersRoot.size() == 1 && namesSpaceFoldersRoot.getFirst().isEmpty();
        if (resultOnlyContainsEmptyMCFolder || !searchedExceptions.isEmpty()) {
            StringBuilder feedbackMessage = new StringBuilder(Text.translatable("resource_explorer.explorer.feedback.info.header").getString());
            if (resultOnlyContainsEmptyMCFolder) {
                feedbackMessage.append(Text.translatable("resource_explorer.explorer.feedback.info.empty").getString());
                namesSpaceFoldersRoot.clear();
            } else {
                feedbackMessage.append(Text.translatable("resource_explorer.explorer.feedback.info.exceptions").getString());
            }
            feedbackMessage.append(Text.translatable("resource_explorer.explorer.feedback.info.exceptions_list").getString());
            searchedExceptions.forEach(s -> feedbackMessage.append(s).append("\n"));

            namesSpaceFoldersRoot.add(new ExplorerDetailsEntry(feedbackMessage.toString()));
            if (print) ResourceExplorerClient.log(feedbackMessage);
        }
    }

    public static void addSearchException(String exception) {
        String[] split = exception.split("\n");
        searchedExceptions.addAll(Arrays.asList(split));
        searchedExceptions.add("");
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean outputResourceToPackInternal(Identifier identifier, Function<File, Boolean> saveResources/*REResourceFile reResourceFile*/) {
        //only save existing file resources
        if (saveResources == null)
            return false;

        Path resourcePackFolder = MinecraftClient.getInstance().getResourcePackDir();
        File thisPackFolder = new File(resourcePackFolder.toFile(), "resource_explorer/");

        if (validateOutputResourcePack(thisPackFolder)) {
            if (thisPackFolder.exists()) {
                File assets = new File(thisPackFolder, "assets");
                if (!assets.exists()) {
                    assets.mkdir();
                }
                File namespace = new File(assets, identifier.getNamespace());
                if (!namespace.exists()) {
                    namespace.mkdir();
                }
                String[] pathList = identifier.getPath().split("/");
                String file = pathList[pathList.length - 1];
                String directories = identifier.getPath().replace(file, "");
                File directoryFolder = new File(namespace, directories);
                if (!directoryFolder.exists()) {
                    directoryFolder.mkdirs();
                }
                if (directoryFolder.exists()) {

                    File outputFile = new File(directoryFolder, file);
                    if (outputFile.exists()) {
                        ResourceExplorerClient.log("Overwriting existing resource: " + identifier);
                    }

                    boolean saved = saveResources.apply(outputFile);
                    if (!saved) {
                        ResourceExplorerClient.log("Exporting resource file failed for: " + identifier);
                    }
                    return saved;
                }
            }
        }
        return false;
    }


    private static boolean validateOutputResourcePack(File packFolder) {
        if (!packFolder.exists()) {
            if (packFolder.mkdir()) {
                return validateOutputResourcePackMeta(packFolder);
            }
        } else {
            return validateOutputResourcePackMeta(packFolder);
        }
        return false;
    }

    private static boolean validateOutputResourcePackMeta(File packFolder) {
        File thisMetaFile = new File(packFolder, "pack.mcmeta");
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

    public static class REExportContext {


        final Set<ResourceFileEntry.FileType> types = new HashSet<>();
        int vanillaCount = 0;
        int packCount = 0;
        int moddedCount = 0;
        int totalAttempted = 0;


        public REExportContext() {
        }

        public void sendLargeFolderWarning() {
            ToastManager toastManager = MinecraftClient.getInstance().getToastManager();
            toastManager.clear();
            SystemToast.show(toastManager, SystemToast.Type.PERIODIC_NOTIFICATION,
                    Text.translatable("resource_explorer.export_start.1"), Text.translatable("resource_explorer.export_start.2"));

        }

        public int getTotalExported() {
            return vanillaCount + packCount + moddedCount;
        }

        public int getTotalAttempted() {
            return totalAttempted;
        }

        public void tried(ResourceFileEntry file, boolean exported) {
            if (file.resource != null) {
                totalAttempted++;
                if (exported) {
                    types.add(file.fileType);
                    String packName = file.resource.getPackId();
                    if ("fabric".equals(packName) || "mod_resources".equals(packName)) {
                        moddedCount++;
                    } else if ("vanilla".equals(packName) &&
                            ("minecraft".equals(file.identifier.getNamespace()) || "realms".equals(file.identifier.getNamespace()))) {
                        vanillaCount++;
                    } else {
                        //mod a mod default resource or vanilla
                        packCount++;
                    }
                }
            }
        }

        public void showExportToast() {
            ToastManager toastManager = MinecraftClient.getInstance().getToastManager();
            toastManager.clear();
            boolean partially = getTotalAttempted() != getTotalExported() && totalAttempted != 1 && getTotalExported() != 0;
            Text title = partially ?
                    Text.of(Text.translatable("resource_explorer.export_warn.partial").getString()
                            .replace("#", String.valueOf(getTotalExported())).replace("$", String.valueOf(getTotalAttempted()))) :
                    Text.of(getTotalAttempted() == getTotalExported() ?
                            Text.translatable(ResourceExplorerClient.MOD_ID + ".export_warn").getString()
                                    .replace("#", String.valueOf(getTotalExported())) :
                            Text.translatable(ResourceExplorerClient.MOD_ID + ".export_warn.fail").getString()
                                    .replace("#", String.valueOf(getTotalExported())));

            SystemToast.show(toastManager, SystemToast.Type.PERIODIC_NOTIFICATION, title, getMessage());
        }

        private Text getMessage() {
            if (getTotalExported() == 0) {
                return Text.translatable("resource_explorer.export_warn.none");
            }
            if (getTotalAttempted() == 1) {
                return Text.translatable(
                        packCount > 0 ?
                                "resource_explorer.export_warn.pack" :
                                moddedCount > 0 ?
                                        "resource_explorer.export_warn.mod" :
                                        "resource_explorer.export_warn.vanilla"
                );
            } else {
                return Text.translatable("resource_explorer.export_warn.all");
            }
        }

    }
}
