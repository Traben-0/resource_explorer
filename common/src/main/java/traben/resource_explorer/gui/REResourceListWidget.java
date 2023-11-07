package traben.resource_explorer.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;

public class REResourceListWidget extends AlwaysSelectedEntryListWidget<REResourceEntry> {

    final REDirectoryScreen screen;

    public REResourceListWidget(MinecraftClient minecraftClient, REDirectoryScreen screen, int width, int height) {
        super(minecraftClient, width, height, 32, height - 55 + 4, 36);
        this.centerListVertically = false;
        this.screen = screen;
        screen.entries.forEach(entry -> {
            entry.setWidget(this);
            addEntry(entry);
        });
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


}
