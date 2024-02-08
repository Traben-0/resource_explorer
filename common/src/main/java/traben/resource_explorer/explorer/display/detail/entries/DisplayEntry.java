package traben.resource_explorer.explorer.display.detail.entries;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

public abstract class DisplayEntry extends AlwaysSelectedEntryListWidget.Entry<DisplayEntry> implements Comparable<DisplayEntry> {

    protected int drawText(Text title, MultilineText rawTextData, DrawContext context, int offset, int displayX, int displayY) {
        offset += 11;
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, title, displayX, displayY + offset, 16777215);
        offset += 11;
        rawTextData.drawWithShadow(context, displayX, displayY + offset, 10, -8355712);
        offset += rawTextData.count() * 10;
        return offset;
    }

    protected int drawWidget(ClickableWidget widget, Text text, DrawContext context, int offset, int displayX, int displayY, int mouseX, int mouseY) {
        return drawWidgetColoredText(widget, text, context, offset, displayX, displayY, mouseX, mouseY, 16777215);
    }

    protected int drawWidgetSubtleText(ClickableWidget widget, Text text, DrawContext context, int offset, int displayX, int displayY, int mouseX, int mouseY) {
        return drawWidgetColoredText(widget, text, context, offset, displayX, displayY, mouseX, mouseY, Colors.GRAY);
    }

    protected int drawWidgetColoredText(ClickableWidget widget, Text text, DrawContext context, int offset, int displayX, int displayY, int mouseX, int mouseY, int color) {
        if (widget != null) {
            offset += 11;
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, text, displayX, displayY + offset, color);
            return drawWidgetOnly(widget, context, offset, displayX, displayY, mouseX, mouseY);
        }
        return offset;
    }

    protected int drawWidgetOnly(ClickableWidget widget, DrawContext context, int offset, int displayX, int displayY, int mouseX, int mouseY) {
        if (widget != null) {
            offset += 11;
            widget.setX(displayX);
            widget.setY(displayY + offset);
            widget.render(context, mouseX, mouseY, 0);
            offset += 20;
        }
        return offset;
    }


    public abstract String getDisplayName();

    public abstract int getEntryHeight();

    @Override
    public Text getNarration() {
        return Text.of(getDisplayName());
    }
}
