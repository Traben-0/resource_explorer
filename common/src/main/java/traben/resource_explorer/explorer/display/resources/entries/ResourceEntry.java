package traben.resource_explorer.explorer.display.resources.entries;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import traben.resource_explorer.explorer.ExplorerUtils;
import traben.resource_explorer.explorer.display.resources.ResourceListWidget;

import java.util.List;

public abstract class ResourceEntry extends AlwaysSelectedEntryListWidget.Entry<ResourceEntry> implements Comparable<ResourceEntry> {

    private final ButtonWidget exportButton;
    protected ResourceListWidget widget = null;

    ResourceEntry() {
        exportButton = ButtonWidget.builder(Text.translatable("resource_explorer.export"), button -> {
                    if (ExplorerUtils.canExportToOutputPack()) {
                        ExplorerUtils.REExportContext context = new ExplorerUtils.REExportContext();
                        button.active = !ExplorerUtils.tryExportToOutputPack(() -> {
                            if (isFolder()) context.sendLargeFolderWarning();
                            this.exportToOutputPack(context);
                            context.showExportToast();
                        });
                    }
                }).tooltip(Tooltip.of(Text.translatable("resource_explorer.export.tooltip." + (isFolder() ? "folder" : "file"))))
                .dimensions(0, 0, 42, 15).build();
    }

    protected static Text trimmedTextToWidth(String string) {
        Text text = Text.of(string);
        MinecraftClient client = MinecraftClient.getInstance();
        int i = client.textRenderer.getWidth(text);
        if (i > 150) {
            StringVisitable stringVisitable = StringVisitable.concat(client.textRenderer.trimToWidth(text, 150 - client.textRenderer.getWidth("...")), StringVisitable.plain("..."));
            return Text.of(stringVisitable.getString());
        } else {
            return text;
        }
    }

    @SuppressWarnings("SameParameterValue")
    protected static String trimmedStringToWidth(String string, int width) {
        Text text = Text.of(string);
        MinecraftClient client = MinecraftClient.getInstance();
        int i = client.textRenderer.getWidth(text);
        if (i > width) {
            StringVisitable stringVisitable = StringVisitable.concat(client.textRenderer.trimToWidth(text, width - client.textRenderer.getWidth("...")), StringVisitable.plain("..."));
            return stringVisitable.getString();
        } else {
            return string;
        }
    }

    public abstract boolean isEmpty();

    @Override
    public int compareTo(@NotNull ResourceEntry o) {
        return getDisplayName().compareTo(o.getDisplayName());
    }

    boolean isFolder() {
        return false;
    }

    abstract boolean canExport();

    abstract String getDisplayName();

    abstract OrderedText getDisplayText();

    abstract List<Text> getExtraText(boolean smallMode);

    abstract String toString(int indent);

    abstract Identifier getIcon(boolean hovered);

    Identifier getIcon2OrNull(boolean hovered) {
        return null;
    }

    Identifier getIcon3OrNull(boolean hovered) {
        return null;
    }

    @Override
    public Text getNarration() {
        return Text.of(getDisplayName());
    }

    public void setWidget(ResourceListWidget widget) {
        this.widget = widget;
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (exportButton != null && exportButton.isMouseOver(mouseX, mouseY)) {
            exportButton.onPress();
            return true;
        } else {
            return mouseClickExplorer();
        }
    }

    abstract boolean matchesSearch(final String search);

    abstract boolean mouseClickExplorer();

    abstract void exportToOutputPack(ExplorerUtils.REExportContext context);


    protected String translated(String key) {
        return Text.translatable(key).getString();
    }

    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {

        int entryWidthSmaller = entryWidth - 14;

        if ((MinecraftClient.getInstance().options.getTouchscreen().getValue() || hovered || this.widget.getSelectedOrNull() == this && this.widget.isFocused())) {
            context.fill(x, y, x + entryWidthSmaller, y + 32, -1601138544);
            if (canExport() && hovered) {
                exportButton.setX(x + entryWidthSmaller - 44);
                exportButton.setY(y + 15);
                exportButton.render(context, mouseX, mouseY, tickDelta);
            }
        }

        context.drawTexture(getIcon(hovered), x, y, 0.0F, 0.0F, 32, 32, 32, 32);

        Identifier secondaryIcon = getIcon2OrNull(hovered);
        if (secondaryIcon != null) {
            context.drawTexture(secondaryIcon, x, y, 0.0F, 0.0F, 32, 32, 32, 32);
        }
        Identifier thirdIcon = getIcon3OrNull(hovered);
        if (thirdIcon != null) {
            context.drawTexture(thirdIcon, x, y, 0.0F, 0.0F, 32, 32, 32, 32);
        }
        OrderedText orderedText = getDisplayText();
        MultilineText multilineText = MultilineText.create(MinecraftClient.getInstance().textRenderer, getExtraText(true).toArray(new Text[0]));

        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, orderedText, x + 32 + 2, y + 1, 16777215);
        multilineText.drawWithShadow(context, x + 32 + 2, y + 12, 10, -8355712);
    }


}
