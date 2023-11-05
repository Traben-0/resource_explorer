package traben.resource_explorer;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import traben.resource_explorer.mixin.TextureManagerAccessor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class ResourceExplorer
{
	public static final String MOD_ID = "resource_explorer";

	public static void init() {
		
	}

	public static LinkedList<REResourceEntry> getResourceFolderRoot() {
		ObjectLinkedOpenHashSet<REResourceFileEntry> allFilesList = new ObjectLinkedOpenHashSet<>();
		System.out.println("/START/");
		System.out.println("/START/READ/");
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

		System.out.println("/END/READ/");
		System.out.println("/START/FOLDER_SORT/");

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
			fabricApiFolders.forEach(fabricApiFolder::addSubFolder);
			namesSpaceFoldersRoot.addFirst(fabricApiFolder);
		}
		//minecraft at the top
		namesSpaceFoldersRoot.addFirst(minecraftFolder);

		//here allFilesAndFoldersRoot is only empty namespace directories

		//iterate over all files and give them folder structure
		for (REResourceFileEntry resourceFile:
			 allFilesList) {
			String namespace = resourceFile.identifier.getNamespace();
			REResourceFolderEntry namespaceFolder = namespaceFolderMap.get(namespace);
			if(namespaceFolder != null){
				namespaceFolder.addResourceFile(resourceFile);
			}
		}

		namesSpaceFoldersRoot.forEach(System.out::println);

		System.out.println("/END/FOLDER_SORT/");
		System.out.println("/END/");

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
	public static final Identifier ICON_FILE_MOJANG = new Identifier("resource_explorer:file_mojang.png");
	public static final Identifier ICON_FOLDER_MOJANG = new Identifier("resource_explorer:folder_mojang.png");
	public static final Identifier ICON_FOLDER_OPTIFINE = new Identifier("resource_explorer:folder_optifine.png");
	public static final Identifier ICON_FILE_SMILE = new Identifier("resource_explorer:file_smile.png");



}
