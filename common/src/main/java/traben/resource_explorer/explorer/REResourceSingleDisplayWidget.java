package traben.resource_explorer.explorer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import traben.resource_explorer.mixin.EntryListWidgetAccessor;

public class REResourceSingleDisplayWidget extends AlwaysSelectedEntryListWidget<REResourceFileDisplayWrapper> {


    private Text title = null;


    public REResourceSingleDisplayWidget(MinecraftClient minecraftClient, int width, int height) {
        super(minecraftClient, width, height, 32, height - 55 + 4, 32);
        this.centerListVertically = false;
    }

    void setSelectedFile(@Nullable REResourceFileDisplayWrapper newFile) {
        setScrollAmount(0);
        clearEntries();
        if (newFile != null) {
            ((EntryListWidgetAccessor) this).setItemHeight(newFile.getEntryHeight());
            title = Text.of(newFile.getFileEntry().getDisplayName());
            setRenderHeader(true, 10);
        } else {
            ((EntryListWidgetAccessor) this).setItemHeight(32);
            setRenderHeader(false, 0);
            title = null;
        }
        addEntry(newFile);

    }

    public void setDimensions(int x, int width, int height) {
        this.width = width;
        this.height = height;
        this.top = 32;
        this.bottom = height - 55 + 4;
        this.left = x;
        this.right = x + width;
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void close() {
        clearEntries();
    }

    public int getRowWidth() {
        return this.width;
    }

    protected int getScrollbarPositionX() {
        return this.right - 6;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        this.setScrollAmount(this.getScrollAmount() - amount * 18);
        return true;
    }



    @Override
    protected void renderHeader(DrawContext context, int x, int y) {
        if (title != null) {
            Text text = Text.empty().append(this.title).formatted(Formatting.UNDERLINE, Formatting.BOLD);
            context.drawText(this.client.textRenderer, text, x + this.width / 2 - this.client.textRenderer.getWidth(text) / 2, Math.min(this.top + 3, y), 16777215, false);
        }
    }

}
