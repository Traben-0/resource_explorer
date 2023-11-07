package traben.resource_explorer;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import traben.resource_explorer.gui.REResourceEntry;
import traben.resource_explorer.gui.REResourceFileEntry;
import traben.resource_explorer.gui.REResourceFolderEntry;
import traben.resource_explorer.mixin.TextureManagerAccessor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

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
			fabricApiFolder.contentIcon = new Identifier("fabricloader","icon.png");
			fabricApiFolders.forEach(fabricApiFolder::addSubFolder);
			namesSpaceFoldersRoot.addFirst(fabricApiFolder);
		}
		//get filter
		final REConfig.REFileFilter filter = REConfig.getInstance().filterMode;

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

	public static final Identifier ICON_FILE_BUILT = new Identifier("resource_explorer:file_built.png");
	public static final Identifier ICON_FOLDER_BUILT = new Identifier("resource_explorer:folder_built.png");
	public static final Identifier ICON_FOLDER = new Identifier("resource_explorer:folder.png");
	public static final Identifier ICON_FOLDER_OPEN = new Identifier("resource_explorer:folder_open.png");
	public static final Identifier ICON_FOLDER_BACK = new Identifier("resource_explorer:folder_back.png");
	public static final Identifier ICON_FILE_PNG = new Identifier("resource_explorer:file_png.png");
	public static final Identifier ICON_FILE_TEXT = new Identifier("resource_explorer:file_text.png");
	public static final Identifier ICON_FILE_PROPERTY = new Identifier("resource_explorer:file_property.png");
	public static final Identifier ICON_FILE_OGG = new Identifier("resource_explorer:file_ogg.png");
	public static final Identifier ICON_FILE_UNKNOWN = new Identifier("resource_explorer:file_unknown.png");
//	public static final Identifier ICON_FILE_MOJANG = new Identifier("resource_explorer:file_mojang.png");
	public static final Identifier ICON_FOLDER_MOJANG = new Identifier("resource_explorer:folder_mojang.png");
	public static final Identifier ICON_FOLDER_OPTIFINE = new Identifier("resource_explorer:folder_optifine.png");
//	public static final Identifier ICON_FILE_SMILE = new Identifier("resource_explorer:file_smile.png");
	public static final Identifier ICON_FOLDER_CORNER = new Identifier("resource_explorer:folder_corner.png");
	public static final Identifier ICON_FILE_BLANK = new Identifier("resource_explorer:file_blank.png");
	public static final Identifier ICON_FILE_JSON = new Identifier("resource_explorer:file_json.png");
	public static final Identifier ICON_FOLDER_UP = new Identifier("resource_explorer:folder_up.png");
	public static final Identifier ICON_FOLDER_UP_SELECTED = new Identifier("resource_explorer:folder_up_selected.png");
	public static final Identifier ICON_FILE_ZIP = new Identifier("resource_explorer:file_zip.png");
	public static final Identifier ICON_FILE_JEM = new Identifier("resource_explorer:file_jem.png");
	public static final Identifier ICON_HAS_META = new Identifier("resource_explorer:has_meta.png");
	public static final Identifier ICON_FOLDER_FABRIC = new Identifier("resource_explorer:folder_fabric.png");



}
