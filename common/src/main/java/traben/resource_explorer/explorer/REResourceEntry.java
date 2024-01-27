package traben.resource_explorer.explorer;

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
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class REResourceEntry extends AlwaysSelectedEntryListWidget.Entry<REResourceEntry> implements Comparable<REResourceEntry> {

    private final ButtonWidget exportButton;
    protected REResourceListWidget widget = null;

    REResourceEntry() {
        exportButton = ButtonWidget.builder(Text.translatable("resource_explorer.export"), button -> {
                    REExplorer.REExportContext context = new REExplorer.REExportContext();
                    if (isFolder()) context.sendLargeFolderWarning();

                    Util.getIoWorkerExecutor().execute(() -> {
                        this.exportToOutputPack(context);
                        context.showExportToast();
                    });

                    button.active = false;
                }).tooltip(Tooltip.of(Text.translatable("resource_explorer.export.tooltip." + (isFolder() ? "folder" : "file"))))
                .dimensions(0, 0, 42, 15).build();
    }

    protected static Text trimmedTextToWidth(String string) {
        Text text = Text.of(string);
        MinecraftClient client = MinecraftClient.getInstance();
        int i = client.textRenderer.getWidth(text);
        if (i > 157) {
            StringVisitable stringVisitable = StringVisitable.concat(client.textRenderer.trimToWidth(text, 157 - client.textRenderer.getWidth("...")), StringVisitable.plain("..."));
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

    @Override
    public int compareTo(@NotNull REResourceEntry o) {
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

    public void setWidget(REResourceListWidget widget) {
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

    abstract void exportToOutputPack(REExplorer.REExportContext context);


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
        MultilineText multilineText = MultilineText.createFromTexts(MinecraftClient.getInstance().textRenderer, getExtraText(true));

        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, orderedText, x + 32 + 2, y + 1, 16777215);
        multilineText.drawWithShadow(context, x + 32 + 2, y + 12, 10, -8355712);
    }


}
