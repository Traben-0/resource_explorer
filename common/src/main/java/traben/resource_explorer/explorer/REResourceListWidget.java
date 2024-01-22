package traben.resource_explorer.explorer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Objects;

public class REResourceListWidget extends AlwaysSelectedEntryListWidget<REResourceEntry> {

    REExplorerScreen screen;

    public REResourceListWidget(MinecraftClient minecraftClient, REExplorerScreen screen, int width, int height) {
        super(minecraftClient, width, height-83, 32/*, height - 55 + 4*/, 36);
        this.centerListVertically = false;
        this.screen = screen;
        screen.entriesInThisDirectory.forEach(entry -> {
            entry.setWidget(this);
            addEntry(entry);
        });

        if(!screen.cumulativePath.equals("assets/")) {
            var entry = new NewFileEntry(screen);
            entry.setWidget(this);
            addEntry(entry);
        }

        //1.20.4
        Objects.requireNonNull(client.textRenderer);
        this.setRenderHeader(true, (int)(9.0F * 1.5F));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void close() {
        this.screen = null;
        clearEntries();
    }

    public int getRowWidth() {
        return this.width;
    }


    protected int getScrollbarPositionX() {
        return this.getRight() - 6;
    }

    @Override
    protected void drawSelectionHighlight(DrawContext context, int y, int entryWidth, int entryHeight, int borderColor, int fillColor) {
        int i = this.getX() + (this.width - entryWidth) / 2;
        int j = (this.getX() + (this.width + entryWidth) / 2) - 10;
        context.fill(i, y - 2, j, y + entryHeight + 2, borderColor);
        context.fill(i + 1, y - 1, j - 1, y + entryHeight + 1, fillColor);
    }

    private static class NewFileEntry extends REResourceEntry {
        private final REExplorerScreen screen;
        NewFileEntry(REExplorerScreen screen){
            this.screen = screen;
        }
        @Override
        boolean canExport() {
            return false;
        }

        @Override
        String getDisplayName() {
            return "create resource";
        }

        @Override
        OrderedText getDisplayText() {
            return Text.of(getDisplayName()).asOrderedText();
        }

        @Override
        List<Text> getExtraText(boolean smallMode) {
            return List.of();
        }

        @Override
        String toString(int indent) {
            return getDisplayName();
        }

        private final Identifier icon = new Identifier("resource_explorer:textures/file_add.png");
        @Override
        Identifier getIcon(boolean hovered) {
            return icon;
        }

        @Override
        boolean mouseClickExplorer() {
            if (REExplorerScreen.currentDisplay != null) {
                REExplorerScreen.currentDisplay.setSelectedEntry(new REResourceDisplayWrapper.CreateFile(screen));
            }
            return true;
        }

        @Override
        void exportToOutputPack(REExplorer.REExportContext context) {

        }
    }
}
