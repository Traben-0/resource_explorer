package traben.resource_explorer.explorer.display.detail;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import traben.resource_explorer.explorer.display.detail.entries.DisplayEntry;
import traben.resource_explorer.mixin.accessors.EntryListWidgetAccessor;

import java.util.Objects;

public class SingleDisplayWidget extends AlwaysSelectedEntryListWidget<DisplayEntry> {


    private Text title = null;

    public SingleDisplayWidget(MinecraftClient minecraftClient, int width, int height, @Nullable DisplayEntry initialDisplay) {
        super(minecraftClient, width, height - 83, 32/*, height - 55 + 4*/, 32);
        this.centerListVertically = false;
        //1.20.4
        Objects.requireNonNull(client.textRenderer);
        this.setRenderHeader(true, (int) (9.0F * 1.5F));

        if (initialDisplay != null) {
            setSelectedEntry(initialDisplay);
        }
    }

    public void setSelectedEntry(@Nullable DisplayEntry newFile) {
        setScrollAmount(0);
        clearEntries();
        if (newFile != null) {
            ((EntryListWidgetAccessor) this).setItemHeight(newFile.getEntryHeight());
            title = Text.of(newFile.getDisplayName());
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
        this.height = height - 83;
        this.setY(32);
        //this.bottom = height - 55 + 4;
        this.setX(x);
//        this.right = x + width;
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
        return this.getRight() - 6;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.setScrollAmount(this.getScrollAmount() - verticalAmount * 18);
        return true;
    }

    @Override
    public void setFocused(@Nullable Element focused) {
        super.setFocused(focused);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (getEntryCount() > 0) {
            return getEntry(0).charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (getEntryCount() > 0) {
            return getEntry(0).keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (getEntryCount() > 0) {
            return getEntry(0).keyReleased(keyCode, scanCode, modifiers);
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    protected void drawSelectionHighlight(DrawContext context, int y, int entryWidth, int entryHeight, int borderColor, int fillColor) {
//        super.drawSelectionHighlight(context, y, entryWidth, entryHeight, borderColor, fillColor);
    }

    @Override
    protected void renderHeader(DrawContext context, int x, int y) {
        if (title != null) {
            Text text = Text.empty().append(this.title).formatted(Formatting.UNDERLINE, Formatting.BOLD);
            context.drawText(this.client.textRenderer, text, x + this.width / 2 - this.client.textRenderer.getWidth(text) / 2, Math.min(this.getY() + 3, y), 16777215, false);
        }
    }

}
