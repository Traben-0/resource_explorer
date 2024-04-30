package traben.resource_explorer.explorer.display.resources;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import traben.resource_explorer.explorer.display.ExplorerScreen;
import traben.resource_explorer.explorer.display.resources.entries.CreateResourceEntry;
import traben.resource_explorer.explorer.display.resources.entries.ResourceEntry;

import java.util.Objects;

public class ResourceListWidget extends AlwaysSelectedEntryListWidget<ResourceEntry> {

    public ExplorerScreen explorerScreen;

    public ResourceListWidget(MinecraftClient minecraftClient, ExplorerScreen explorerScreen, int width, int height) {
        super(minecraftClient, width, height - 83, 32/*, height - 55 + 4*/, 36);
        this.centerListVertically = false;
        this.explorerScreen = explorerScreen;
        explorerScreen.getContentOfDirectoryAccordingToSearch().forEach(entry -> {
            entry.setWidget(this);
            addEntry(entry);
        });

        if (!explorerScreen.cumulativePath.equals("assets/")) {
            var entry = new CreateResourceEntry(explorerScreen);
            entry.setWidget(this);
            addEntry(entry);
        }

        //1.20.4
        Objects.requireNonNull(client.textRenderer);
        this.setRenderHeader(true, (int) (9.0F * 1.5F));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void close() {
        this.explorerScreen = null;
        clearEntries();
    }

    public int getRowWidth() {
        return this.width;
    }


    protected int getScrollbarX() {
        return this.getRight() - 6;
    }



    @Override
    protected void drawSelectionHighlight(DrawContext context, int y, int entryWidth, int entryHeight, int borderColor, int fillColor) {
        int i = this.getX() + (this.width - entryWidth) / 2;
        int j = (this.getX() + (this.width + entryWidth) / 2) - 10;
        context.fill(i, y - 2, j, y + entryHeight + 2, borderColor);
        context.fill(i + 1, y - 1, j - 1, y + entryHeight + 1, fillColor);
    }

}
