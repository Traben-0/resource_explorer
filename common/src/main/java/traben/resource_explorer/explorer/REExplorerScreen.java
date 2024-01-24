package traben.resource_explorer.explorer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import traben.resource_explorer.REConfig;
import traben.resource_explorer.ResourceExplorerClient;

import java.util.LinkedList;

import static traben.resource_explorer.ResourceExplorerClient.MOD_ID;

public class REExplorerScreen extends Screen {

    @Nullable
    static public REResourceSingleDisplayWidget currentDisplay = null;

    @Nullable
    static public REStats currentStats = null;
    @Nullable
    public final REExplorerScreen reParent;
    public final LinkedList<REResourceEntry> entriesInThisDirectory;
    public final String cumulativePath;
    private REResourceListWidget fileList;
    private REConfig.REFileFilter filterChoice = REConfig.getInstance().filterMode;

    public REExplorerScreen(Screen vanillaParent) {
        super(Text.translatable(MOD_ID + ".title"));
        this.cumulativePath = "assets/";
        this.entriesInThisDirectory = REExplorer.getResourceFolderRoot();
        ResourceExplorerClient.setExplorerExit(vanillaParent);
        this.reParent = null;
    }

    public REExplorerScreen(@NotNull REExplorerScreen reParent, LinkedList<REResourceEntry> entries, String cumulativePath) {
        super(Text.translatable(MOD_ID + ".title"));
        this.cumulativePath = cumulativePath;
        this.entriesInThisDirectory = entries;
        this.reParent = reParent;
    }

    protected void init() {
        if (currentDisplay == null) currentDisplay = new REResourceSingleDisplayWidget(client, 200, this.height);

        this.fileList = new REResourceListWidget(this.client, this, 200, this.height);
        this.fileList.setX(this.width / 2 - 4 - 200);
        this.addSelectableChild(this.fileList);

        currentDisplay.setDimensions(width / 2 + 4, 200, this.height);
        this.addSelectableChild(currentDisplay);

        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE,
                (button) -> this.close()).dimensions(this.width / 2 + 4, this.height - 48, 150, 20).build());

        Tooltip warn = Tooltip.of(Text.translatable(MOD_ID + ".explorer.apply_warn"));

        ButtonWidget apply = this.addDrawableChild(ButtonWidget.builder(Text.translatable(MOD_ID + ".explorer.apply"), (button) -> {
            this.close();
            REConfig.getInstance().filterMode = filterChoice;
            REConfig.saveConfig();
        }).dimensions(this.width / 2 - 4 - 46, this.height - 48, 46, 20).tooltip(warn).build());
        apply.active = false;

        this.addDrawableChild(ButtonWidget.builder(Text.translatable(REConfig.getInstance().filterMode.getKey()), (button) -> {
            filterChoice = filterChoice.next();
            button.setMessage(Text.translatable(filterChoice.getKey()));
            apply.active = filterChoice != REConfig.getInstance().filterMode;
        }).dimensions(this.width / 2 - 4 - 200, this.height - 48, 150, 20).tooltip(warn).build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable(MOD_ID + ".explorer.settings"), (button) -> {
            this.close();
            MinecraftClient.getInstance().setScreen(new REConfig.REConfigScreen(null));
        }).dimensions(this.width / 2 - 4 - 200, this.height - 24, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable(MOD_ID + ".explorer.stats"), (button) -> {
            if (currentStats != null) MinecraftClient.getInstance().setScreen(currentStats.getAsScreen(this));
        }).dimensions(this.width / 2 - 4 - 46, this.height - 24, 46, 20).build());
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        this.fileList.render(context, mouseX, mouseY, delta);
        if (currentDisplay != null)
            currentDisplay.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 8, 16777215);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.of(cumulativePath), this.width / 2, 20, Colors.GRAY);
    }

    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(context);
    }

    @Override
    public void close() {
        this.fileList.close();
        super.close();

        entriesInThisDirectory.clear();

        ResourceExplorerClient.leaveEditorAndResourceReload();
    }


}
