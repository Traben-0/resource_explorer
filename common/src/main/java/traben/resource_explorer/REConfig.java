package traben.resource_explorer;

import traben.resource_explorer.gui.REResourceFileEntry;

import java.util.function.Predicate;

public class REConfig {
    private static REConfig instance;

    public static REConfig getInstance() {
        if(instance == null){
            instance = new REConfig();
        }
        return instance;
    }
    private REConfig(){}

    public boolean showResourcePackButton = true;
    public boolean logFullFileTree = false;

    public REFileFilter filterMode = REFileFilter.ALL_RESOURCES;



    public enum REFileFilter{
        ALL_RESOURCES("Filter: all",
                (fileEntry)->true),
        ALL_RESOURCES_NO_GENERATED("Filter: files only",
                (fileEntry)->fileEntry.resource!= null),
        ONLY_FROM_PACKS_NO_GENERATED("Filter: packs only",
                (fileEntry)->fileEntry.resource!= null && !"vanilla".equals(fileEntry.resource.getResourcePackName())),
        ONLY_TEXTURES("Filter: textures only",
                (fileEntry)->fileEntry.fileType == REResourceFileEntry.FileType.PNG),
        ONLY_TEXTURE_NO_GENERATED("Filter: texture files only",
                (fileEntry)->fileEntry.resource!= null && fileEntry.fileType == REResourceFileEntry.FileType.PNG),
        ONLY_TEXTURE_FROM_PACKS_NO_GENERATED("Filter: pack texture files only",
                (fileEntry)->fileEntry.resource!= null && fileEntry.fileType == REResourceFileEntry.FileType.PNG && !"vanilla".equals(fileEntry.resource.getResourcePackName()));

        public String getKey() {
            return key;
        }

        private final String key;

        private final Predicate<REResourceFileEntry> test;

        REFileFilter(String key, Predicate<REResourceFileEntry> test){
            this.key = key;
            this.test = test;
        }

        public boolean allows(REResourceFileEntry fileEntry){
            return test.test(fileEntry);
        }

        public REFileFilter next(){
            return switch (this){
                case ALL_RESOURCES
                        -> ALL_RESOURCES_NO_GENERATED;
                case ALL_RESOURCES_NO_GENERATED
                        -> ONLY_FROM_PACKS_NO_GENERATED;
                case ONLY_FROM_PACKS_NO_GENERATED
                        -> ONLY_TEXTURES;
                case ONLY_TEXTURES
                        -> ONLY_TEXTURE_NO_GENERATED;
                case ONLY_TEXTURE_NO_GENERATED
                        -> ONLY_TEXTURE_FROM_PACKS_NO_GENERATED;
                case ONLY_TEXTURE_FROM_PACKS_NO_GENERATED
                        -> ALL_RESOURCES;
            };
        }
    }
}
