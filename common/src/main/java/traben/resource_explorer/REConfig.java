package traben.resource_explorer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import traben.resource_explorer.explorer.REExplorer;
import traben.resource_explorer.explorer.REExplorerScreen;
import traben.resource_explorer.explorer.REResourceFile;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Predicate;

import static traben.resource_explorer.ResourceExplorerClient.MOD_ID;

public class REConfig {

    private static REConfig instance;
    public boolean showResourcePackButton = true;
    public boolean logFullFileTree = false;
    public boolean addCauseToReloadFailureToast = true;
    public REFileFilter filterMode = REFileFilter.ALL_RESOURCES;

    private REConfig() {
    }

    public static REConfig getInstance() {
        if (instance == null) {
            loadConfig();
        }
        return instance;
    }

    public static void setInstance(REConfig newInstance) {
        instance = newInstance;
        saveConfig();
    }

    public static void loadConfig() {
        try {
            File config = new File(REVersionDifferenceManager.getConfigDirectory().toFile(), "resource_explorer.json");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            if (config.exists()) {
                try {
                    FileReader fileReader = new FileReader(config);
                    instance = gson.fromJson(fileReader, REConfig.class);
                    fileReader.close();
                    saveConfig();
                } catch (IOException e) {
                    ResourceExplorerClient.logError("Config could not be loaded, using defaults");
                }
            } else {
                instance = new REConfig();
                saveConfig();
            }
            if (instance == null) {
                instance = new REConfig();
                saveConfig();
            }
        } catch (Exception e) {
            instance = new REConfig();
        }
    }

    public static void saveConfig() {
        File config = new File(REVersionDifferenceManager.getConfigDirectory().toFile(), "resource_explorer.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        if (!config.getParentFile().exists()) {
            //noinspection ResultOfMethodCallIgnored
            config.getParentFile().mkdirs();
        }
        try {
            FileWriter fileWriter = new FileWriter(config);
            fileWriter.write(gson.toJson(instance));
            fileWriter.close();
        } catch (IOException e) {
            ResourceExplorerClient.logError("Config could not be saved");
        }
    }

    public REConfig copy() {
        REConfig newConfig = new REConfig();
        newConfig.showResourcePackButton = showResourcePackButton;
        newConfig.filterMode = filterMode;
        newConfig.logFullFileTree = logFullFileTree;
        newConfig.addCauseToReloadFailureToast = addCauseToReloadFailureToast;
        return newConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        REConfig reConfig = (REConfig) o;
        return showResourcePackButton == reConfig.showResourcePackButton &&
                logFullFileTree == reConfig.logFullFileTree &&
                filterMode == reConfig.filterMode &&
                addCauseToReloadFailureToast == reConfig.addCauseToReloadFailureToast;
    }

    @Override
    public int hashCode() {
        return Objects.hash(showResourcePackButton, logFullFileTree, filterMode, addCauseToReloadFailureToast);
    }

    public enum REFileFilter {
        ALL_RESOURCES(MOD_ID + ".filter.0",
                (fileEntry) -> true),
        ALL_RESOURCES_NO_GENERATED(MOD_ID + ".filter.1",
                (fileEntry) -> fileEntry.resource != null),
        ONLY_FROM_PACKS_NO_GENERATED(MOD_ID + ".filter.2",
                (fileEntry) -> fileEntry.resource != null && !"vanilla".equals(fileEntry.resource.getResourcePackName())),
        ONLY_TEXTURES(MOD_ID + ".filter.3",
                (fileEntry) -> fileEntry.fileType == REResourceFile.FileType.PNG),
        ONLY_TEXTURE_NO_GENERATED(MOD_ID + ".filter.4",
                (fileEntry) -> fileEntry.resource != null && fileEntry.fileType == REResourceFile.FileType.PNG),
        ONLY_TEXTURE_FROM_PACKS_NO_GENERATED(MOD_ID + ".filter.5",
                (fileEntry) -> fileEntry.resource != null && fileEntry.fileType == REResourceFile.FileType.PNG && !"vanilla".equals(fileEntry.resource.getResourcePackName())),
        SOUNDS_ONLY(MOD_ID + ".filter.6",
                (fileEntry) -> fileEntry.resource != null && fileEntry.fileType == REResourceFile.FileType.OGG),
        TEXT_ONLY(MOD_ID + ".filter.7",
                (fileEntry) -> fileEntry.resource != null && fileEntry.fileType.isRawTextType());

        private final String key;
        private final Predicate<REResourceFile> test;

        REFileFilter(String key, Predicate<REResourceFile> test) {
            this.key = key;
            this.test = test;
        }

        public String getKey() {
            return key;
        }

        public boolean allows(REResourceFile fileEntry) {
            return test.test(fileEntry);
        }

        public REFileFilter next() {
            return switch (this) {
                case ALL_RESOURCES -> ALL_RESOURCES_NO_GENERATED;
                case ALL_RESOURCES_NO_GENERATED -> ONLY_FROM_PACKS_NO_GENERATED;
                case ONLY_FROM_PACKS_NO_GENERATED -> ONLY_TEXTURES;
                case ONLY_TEXTURES -> ONLY_TEXTURE_NO_GENERATED;
                case ONLY_TEXTURE_NO_GENERATED -> ONLY_TEXTURE_FROM_PACKS_NO_GENERATED;
                case ONLY_TEXTURE_FROM_PACKS_NO_GENERATED -> SOUNDS_ONLY;
                case SOUNDS_ONLY -> TEXT_ONLY;
                case TEXT_ONLY -> ALL_RESOURCES;
            };
        }
    }


    public static class REConfigScreen extends Screen {

        private final Screen parent;

        public REConfig tempConfig;

        public REConfigScreen(Screen parent) {
            super(Text.translatable(MOD_ID + ".settings.title"));
            if (parent instanceof REExplorerScreen) {
                this.parent = null;
            } else {
                this.parent = parent;
            }
            tempConfig = getInstance().copy();
        }

        public void reset() {
            this.clearAndInit();
        }

        @Override
        protected void init() {
            super.init();
            this.addDrawableChild(ButtonWidget.builder(
                    Text.translatable("gui.done"),
                    (button) -> {
                        if (!tempConfig.equals(instance)) {
                            setInstance(tempConfig);
                            MinecraftClient.getInstance().reloadResources();
                        }
                        Objects.requireNonNull(client).setScreen(parent);
                    }).dimensions((int) (this.width * 0.7), (int) (this.height * 0.9), (int) (this.width * 0.2), 20).build());
            this.addDrawableChild(ButtonWidget.builder(
                    Text.translatable("dataPack.validation.reset"),
                    (button) -> {
                        tempConfig = new REConfig();
                        this.clearAndInit();
                        //Objects.requireNonNull(client).setScreen(parent);
                    }).dimensions((int) (this.width * 0.4), (int) (this.height * 0.9), (int) (this.width * 0.22), 20).build());
            this.addDrawableChild(ButtonWidget.builder(
                    ScreenTexts.CANCEL,
                    (button) -> {
                        tempConfig = null;
                        Objects.requireNonNull(client).setScreen(parent);
                    }).dimensions((int) (this.width * 0.1), (int) (this.height * 0.9), (int) (this.width * 0.2), 20).build());


            this.addDrawableChild(ButtonWidget.builder(
                            Text.of(Text.translatable(MOD_ID + ".settings.options_button").getString() + (ScreenTexts.onOrOff(tempConfig.showResourcePackButton).getString())),
                            (button) -> {
                                tempConfig.showResourcePackButton = !tempConfig.showResourcePackButton;
                                button.setMessage(Text.of(Text.translatable(MOD_ID + ".settings.options_button").getString() + (ScreenTexts.onOrOff(tempConfig.showResourcePackButton).getString())));
                            }).tooltip(Tooltip.of(Text.translatable(MOD_ID + ".settings.options_button.tooltip")))
                    .dimensions((int) (this.width * 0.5), (int) (this.height * 0.2), (int) (this.width * 0.4), 20).build());
            this.addDrawableChild(ButtonWidget.builder(
                            Text.of(Text.translatable(MOD_ID + ".settings.log_files").getString() + (ScreenTexts.onOrOff(tempConfig.logFullFileTree).getString())),
                            (button) -> {
                                tempConfig.logFullFileTree = !tempConfig.logFullFileTree;
                                button.setMessage(Text.of(Text.translatable(MOD_ID + ".settings.log_files").getString() + (ScreenTexts.onOrOff(tempConfig.logFullFileTree)).getString()));
                            }).tooltip(Tooltip.of(Text.translatable(MOD_ID + ".settings.log_files.tooltip")))
                    .dimensions((int) (this.width * 0.5), (int) (this.height * 0.3), (int) (this.width * 0.4), 20).build());
            this.addDrawableChild(ButtonWidget.builder(
                            Text.translatable(tempConfig.filterMode.key),
                            (button) -> {
                                tempConfig.filterMode = tempConfig.filterMode.next();
                                button.setMessage(Text.translatable(tempConfig.filterMode.key));
                            }).tooltip(Tooltip.of(Text.translatable(MOD_ID + ".filter.tooltip")))
                    .dimensions((int) (this.width * 0.5), (int) (this.height * 0.4), (int) (this.width * 0.4), 20).build());
            this.addDrawableChild(ButtonWidget.builder(
                            Text.of(Text.translatable(MOD_ID + ".settings.fail_toast").getString() + (ScreenTexts.onOrOff(tempConfig.addCauseToReloadFailureToast).getString())),
                            (button) -> {
                                tempConfig.addCauseToReloadFailureToast = !tempConfig.addCauseToReloadFailureToast;
                                button.setMessage(Text.of(Text.translatable(MOD_ID + ".settings.fail_toast").getString() + (ScreenTexts.onOrOff(tempConfig.addCauseToReloadFailureToast)).getString()));
                            }).tooltip(Tooltip.of(Text.translatable(MOD_ID + ".settings.fail_toast.tooltip")))
                    .dimensions((int) (this.width * 0.5), (int) (this.height * 0.5), (int) (this.width * 0.4), 20).build());


            int x = (int) (this.width * 0.1);
            int y = (int) (this.height * 0.15);

            int square = (int) Math.min(this.height * 0.6, this.width * 0.45);
            this.addDrawableChild(new TexturedButtonWidget(
                    x, y, square, square,0,0,square,
                    REExplorer.ICON_FOLDER_OPEN,
                    (button) -> {
                        assert this.client != null;
                        this.client.setScreen(new REExplorerScreen(this));
                    }) {
                {
                    setTooltip(Tooltip.of(Text.translatable(MOD_ID + ".open_tooltip")));
                }

                //override required because textured button widget just doesnt work
                @Override
                public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
                    Identifier identifier = this.isSelected() ? REExplorer.ICON_FOLDER_OPEN : REExplorer.ICON_MOD;
                    context.drawTexture(identifier, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
                }

            });
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            renderBackgroundTexture(context);
            super.render(context, mouseX, mouseY, delta);
        }
    }
}
