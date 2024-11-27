package traben.resource_explorer.explorer.display;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import traben.resource_explorer.REConfig;
import traben.resource_explorer.ResourceExplorerClient;
import traben.resource_explorer.explorer.ExplorerUtils;
import traben.resource_explorer.explorer.display.detail.SingleDisplayWidget;
import traben.resource_explorer.explorer.display.resources.ResourceListWidget;
import traben.resource_explorer.explorer.display.resources.entries.ResourceEntry;
import traben.resource_explorer.explorer.display.resources.entries.ResourceFolderEntry;
import traben.resource_explorer.explorer.stats.ExplorerStats;

import java.util.List;

public class ExplorerScreen extends Screen {

    @Nullable
    static public SingleDisplayWidget currentDisplay = null;

    @Nullable
    static public ExplorerStats currentStats = null;
    static String searchTerm = "";
    private static REConfig.REFileFilter filterChoice = REConfig.getInstance().filterMode;
    @Nullable
    public final ExplorerScreen reParent;
    public final String cumulativePath;
    final TextFieldWidget searchBar = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 200, 20, Text.of(""));
    public ResourceFolderEntry resourceFolder;
    private ResourceListWidget fileList;
    private ButtonWidget searchButton = null;
    public ExplorerScreen(Screen vanillaParent) {
        super(Text.translatable("resource_explorer.title"));
        this.cumulativePath = "assets/";
        assertOptionsBackgroundTextureBeforeSearch();
        this.resourceFolder = new ResourceFolderEntry("assets", ExplorerUtils.getResourceFolderRoot());
        ResourceExplorerClient.setExitScreen(vanillaParent);
        this.reParent = null;
        searchTerm = "";
        filterChoice = REConfig.getInstance().filterMode;
    }

    public ExplorerScreen(@NotNull ExplorerScreen reParent, ResourceFolderEntry resourceFolder, String cumulativePath) {
        super(Text.translatable("resource_explorer.title"));
        this.cumulativePath = cumulativePath;
        this.resourceFolder = resourceFolder;
        this.reParent = reParent;
    }

    public static String getSearchTerm() {
        return searchTerm;
    }

    //this texture can be made invalid by the resource search, ensure it is registered first
    //normally not an issue but at-least 1 mod I know of removes the options background from the options screen
    //meaning it does not get registered before the search breaks textures
    private void assertOptionsBackgroundTextureBeforeSearch() {
        assertTexture(Identifier.of("minecraft:textures/gui/options_background.png"));
        assertTexture(Identifier.of("widget/scroller"));
        assertTexture(Identifier.of("widget/scroller_background"));
        assertTexture(Identifier.of("textures/gui/menu_list_background.png"));
        assertTexture(Identifier.of("textures/gui/inworld_menu_list_background.png"));
        assertTexture(Identifier.of("textures/gui/menu_background.png"));
        assertTexture(Identifier.of("textures/gui/header_separator.png"));
        assertTexture(Identifier.of("textures/gui/footer_separator.png"));
        assertTexture(Identifier.of("textures/gui/inworld_menu_background.png"));
        assertTexture(Identifier.of("textures/gui/inworld_header_separator.png"));
        assertTexture(Identifier.of("textures/gui/inworld_footer_separator.png"));
    }

    private static void assertTexture(Identifier id) {
        NativeImage background = ResourceExplorerClient.getNativeImageElseNull(id);
        if (background == null) return;
        MinecraftClient.getInstance().getTextureManager().registerTexture(id, new NativeImageBackedTexture(background));
    }

    public List<ResourceEntry> getContentOfDirectoryAccordingToSearch() {
        return resourceFolder.getContentViaSearch(searchTerm);
    }

    void doSearch() {
        searchTerm = searchBar.getText();
        this.clearAndInit();
    }

    @Override
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        if (keyCode == 257 && searchBar.isFocused()) {
            doSearch();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(final int keyCode, final int scanCode, final int modifiers) {
        testSearchButtonActive();
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
        testSearchButtonActive();
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void testSearchButtonActive() {
        if (searchButton == null) return;
        searchButton.active = (!searchTerm.equals(searchBar.getText()) || filterChoice != REConfig.getInstance().filterMode);
    }

    protected void init() {
        if (currentDisplay == null) {
            currentDisplay = new SingleDisplayWidget(client, 200, this.height, resourceFolder.getDetailEntryIfRoot());
        }

        this.fileList = new ResourceListWidget(this.client, this, 200, this.height);
        this.fileList.setX(this.width / 2 - 4 - 200);
        this.addSelectableChild(this.fileList);

        currentDisplay.setDimensions(width / 2 + 4, 200, this.height);
        this.addSelectableChild(currentDisplay);

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("resource_explorer.explorer.exit"),
                (button) -> this.close()).dimensions(this.width / 2 + 104, this.height - 24, 98, 20).build());


        //search bar
        searchBar.setPosition(this.width / 2 - 4 - 200, this.height - 48);
        searchBar.setText(searchTerm);
        this.addDrawableChild(searchBar);

        //search button
        searchButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("resource_explorer.explorer.search"), (button) -> {
            REConfig.getInstance().filterMode = filterChoice;
            REConfig.saveConfig();
            doSearch();
        }).dimensions(this.width / 2 - 4 - 46, this.height - 24, 46, 20).build());

        //filter
        this.addDrawableChild(ButtonWidget.builder(Text.translatable(REConfig.getInstance().filterMode.getKey()), (button) -> {
                    filterChoice = filterChoice.next();
                    button.setMessage(Text.translatable(filterChoice.getKey()));
                }).dimensions(this.width / 2 - 4 - 200, this.height - 24, 150, 20)
                .tooltip(Tooltip.of(Text.translatable("resource_explorer.explorer.apply_warn"))).build());

        //settings
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("resource_explorer.explorer.settings"), (button) -> {
            this.close();
            MinecraftClient.getInstance().setScreen(new REConfig.REConfigScreen(this));
        }).dimensions(this.width / 2 + 4, this.height - 24, 98, 20).build());

        //stats
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("resource_explorer.explorer.stats"), (button) -> {
            if (currentStats != null) MinecraftClient.getInstance().setScreen(currentStats.getAsScreen(this));
        }).dimensions(this.width / 2 + 4, this.height - 48, 98, 20).build());
    }


    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        this.fileList.render(context, mouseX, mouseY, delta);
        if (currentDisplay != null)
            currentDisplay.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 8, 16777215);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.of(cumulativePath), this.width / 2, 20, Colors.GRAY);
    }

//1.20.5
//    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
//        this.renderBackgroundTexture(context);
//    }

    @Override
    public void close() {
        this.fileList.close();
        super.close();

        resourceFolder = null;

        ResourceExplorerClient.leaveModScreensAndResourceReload();
    }


}
