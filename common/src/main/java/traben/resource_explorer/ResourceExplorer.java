package traben.resource_explorer;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import traben.resource_explorer.gui.REResourceEntry;
import traben.resource_explorer.gui.REResourceFileEntry;
import traben.resource_explorer.gui.REResourceFolderEntry;
import traben.resource_explorer.mixin.TextureManagerAccessor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

public class ResourceExplorer
{
	public static final String MOD_ID = "resource_explorer";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static void init() {

	}

	public static void log(Object message){
		LOGGER.info("[resource_explorer]: " + message.toString());
	}
	public static void logWarn(Object message){
		LOGGER.warn("[resource_explorer]: " + message.toString());
	}
	public static void logError(Object message){
		LOGGER.error("[resource_explorer]: " + message.toString());
	}

	public static LinkedList<REResourceEntry> getResourceFolderRoot() {
		ObjectLinkedOpenHashSet<REResourceFileEntry> allFilesList = new ObjectLinkedOpenHashSet<>();

		boolean print = REConfig.getInstance().logFullFileTree;

		if(print) {
			log("/START/");
			log("/START/READ/");
		}
		try {
			Map<Identifier, Resource> resourceMap = MinecraftClient.getInstance().getResourceManager().findResources("resource_explorer$search", (id) -> true);
			resourceMap.forEach((k, v) -> {
				allFilesList.add(new REResourceFileEntry(k,v));
			});

		}catch (Exception ignored){
			//the method I use to explore all resources will cause an exception once at the end of the resource list as i need to search for blank file names
		}

		//modded
		Map<Identifier, Resource> resourceMap2 = MinecraftClient.getInstance().getResourceManager().findResources("",(id)->true);
		resourceMap2.forEach((k,v)->{
			allFilesList.add(new REResourceFileEntry(k,v));
		});


		Map<Identifier, AbstractTexture> textures = ((TextureManagerAccessor)MinecraftClient.getInstance().getTextureManager()).getTextures();
		textures.forEach((k,v)->{
			allFilesList.add(new REResourceFileEntry(k,null));
		});

		if(print) {
			log("/END/READ/");
			log("/START/FOLDER_SORT/");
		}
		Set<String> namespaces = MinecraftClient.getInstance().getResourceManager().getAllNamespaces();


		LinkedList<REResourceEntry> namesSpaceFoldersRoot = new LinkedList<>();
		Map<String, REResourceFolderEntry> namespaceFolderMap = new HashMap<>();

		LinkedList<REResourceFolderEntry> fabricApiFolders = new LinkedList<>();

		REResourceFolderEntry minecraftFolder = new REResourceFolderEntry("minecraft");
		namespaceFolderMap.put("minecraft",minecraftFolder);
		namespaces.remove("minecraft");

		for (String nameSpace:
			 namespaces) {
			REResourceFolderEntry namespaceFolder = new REResourceFolderEntry(nameSpace);

			if("fabric".equals(nameSpace) || nameSpace.matches("(fabric-.*|renderer-registries)(renderer|api|-v\\d).*")){
				fabricApiFolders.add(namespaceFolder);
			}else{
				namesSpaceFoldersRoot.addLast(namespaceFolder);
			}
			namespaceFolderMap.put(nameSpace,namespaceFolder);

		}
		//fabric api all in 1
		if(!fabricApiFolders.isEmpty()) {
			REResourceFolderEntry fabricApiFolder =  new REResourceFolderEntry("fabric-api");
			fabricApiFolder.contentIcon = new Identifier("fabricloader", "icon.png");
			fabricApiFolders.forEach(fabricApiFolder::addSubFolder);
			namesSpaceFoldersRoot.addFirst(fabricApiFolder);
		}
		//get filter
		REConfig.REFileFilter filter = REConfig.getInstance().filterMode;

		//minecraft at the top
		if(filter != REConfig.REFileFilter.ONLY_FROM_PACKS_NO_GENERATED)
			namesSpaceFoldersRoot.addFirst(minecraftFolder);

		//here allFilesAndFoldersRoot is only empty namespace directories

		//iterate over all files and give them folder structure
		for (REResourceFileEntry resourceFile:
			 allFilesList) {
			if(filter.allows(resourceFile)) {
				String namespace = resourceFile.identifier.getNamespace();
				REResourceFolderEntry namespaceFolder = namespaceFolderMap.get(namespace);
				if (namespaceFolder != null) {
					namespaceFolder.addResourceFile(resourceFile);
				}
			}
		}

		if(print) {
			namesSpaceFoldersRoot.forEach(System.out::println);
			log("/END/FOLDER_SORT/");
			log("/END/");
		}

		return namesSpaceFoldersRoot;
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	public static boolean outputResourceToPack(REResourceFileEntry reResourceFileEntry){

		//only save png resources
		if(reResourceFileEntry.resource == null)
			return false;

		Path resourcePackFolder = MinecraftClient.getInstance().getResourcePackDir();
		File thisPackFolder = new File(resourcePackFolder.toFile(),"resource_explorer/");
		if(!thisPackFolder.exists()){
			String mcmeta = """
					{
					\t"pack": {
					\t\t"pack_format": 15,
					\t\t"supported_formats":[0,99],
					\t\t"description": "Output file for the Resource Explorer mod"
					\t}
					}""";
			if(thisPackFolder.mkdir()) {
				File thisMetaFolder = new File(thisPackFolder,"pack.mcmeta");
				try {
					FileWriter fileWriter = new FileWriter(thisMetaFolder);
					fileWriter.write(mcmeta);
					fileWriter.close();
					log(" output resource-pack created.");
				} catch (IOException e) {
					log(" output resource-pack not created.");
				}
				File thisIconFile = new File(thisPackFolder,"pack.png");
				Optional<Resource> image = MinecraftClient.getInstance().getResourceManager().getResource(ICON_FOLDER_BUILT);
				if(image.isPresent()) {
					try {
						InputStream stream = image.get().getInputStream();
						NativeImage.read(stream).writeTo(thisIconFile);
						log(" output resource-pack icon created.");
					} catch (IOException e) {
						log(" output resource-pack icon not created.");
					}
				}
			}
		}
		if(thisPackFolder.exists()){
			File assets = new File(thisPackFolder,"assets");
			if(!assets.exists()) {
				assets.mkdir();
			}
			File namespace = new File(assets,reResourceFileEntry.identifier.getNamespace());
			if(!namespace.exists()) {
				namespace.mkdir();
			}
			String[] pathList = reResourceFileEntry.identifier.getPath().split("/");
			String file = pathList[pathList.length-1];
			String directories = reResourceFileEntry.identifier.getPath().replace(file,"");
			File directoryFolder = new File(namespace,directories);
			if(!directoryFolder.exists()){
				directoryFolder.mkdirs();
			}
			if(directoryFolder.exists()){
				if(reResourceFileEntry.fileType.isRawTextType()){
					File txtFile = new File(directoryFolder,file);
					try {
						FileWriter fileWriter = new FileWriter(txtFile);
						fileWriter.write(new String(reResourceFileEntry.resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
						fileWriter.close();
						log(" output resource file created.");
						return true;
					} catch (IOException e) {
						log(" output resource file not created.");
					}
				}else if(reResourceFileEntry.fileType == REResourceFileEntry.FileType.PNG){
					File thisImgFile = new File(directoryFolder,file + (file.endsWith(".png") ? "" : ".png"));
					try {
						InputStream stream = reResourceFileEntry.resource.getInputStream();
						NativeImage.read(stream).writeTo(thisImgFile);
						log(" output resource image created.");
						return true;
					} catch (IOException e) {
						log(" output resource image not created.");
					}
				}
			}
		}
		return false;
	}


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



}
