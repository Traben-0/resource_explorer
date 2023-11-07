package traben.resource_explorer.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class REResourceEntry extends AlwaysSelectedEntryListWidget.Entry<REResourceEntry> implements Comparable<REResourceEntry> {

    @Override
    public int compareTo(@NotNull REResourceEntry o) {
        return getDisplayName().compareTo(o.getDisplayName());
    }

    abstract String getDisplayName();
    abstract OrderedText getDisplayText();
    abstract List<Text> getExtraText(boolean smallMode);

    abstract String toString(int indent);

    abstract Identifier getIcon(boolean hovered);

    Identifier getIcon2OrNull(boolean hovered){return null;}
    Identifier getIcon3OrNull(boolean hovered){return null;}

    @Override
    public Text getNarration() {
        return Text.of(getDisplayName());
    }

    protected REResourceListWidget widget = null;
    public void setWidget(REResourceListWidget widget){
        this.widget = widget;
    }

    private boolean isSelectable(){return true;}

    @Override
    public abstract boolean mouseClicked(double mouseX, double mouseY, int button);

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

    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
//        ResourcePackCompatibility resourcePackCompatibility = this.pack.getCompatibility();
//        if (!resourcePackCompatibility.isCompatible()) {
//            context.fill(x - 1, y - 1, x + entryWidth - 3, y + entryHeight + 1, -8978432);
//        }

        if (this.isSelectable() && ((Boolean) MinecraftClient.getInstance().options.getTouchscreen().getValue() || hovered || this.widget.getSelectedOrNull() == this && this.widget.isFocused())) {
            context.fill(x, y, x + entryWidth, y + 32, -1601138544);
//            int i = mouseX - x;
//            int j = mouseY - y;
//            if (!this.pack.getCompatibility().isCompatible()) {
//                orderedText = this.incompatibleText;
//                multilineText = this.compatibilityNotificationText;
//            }

//                if (i < 32) {
//                    context.drawGuiTexture(SELECT_HIGHLIGHTED_TEXTURE, x, y, 32, 32);
//                } else {
//                    context.drawGuiTexture(SELECT_TEXTURE, x, y, 32, 32);
//                }

        }
        context.drawTexture(getIcon(hovered), x, y, 0.0F, 0.0F, 32, 32, 32, 32);
        Identifier secondaryIcon = getIcon2OrNull(hovered);
        if(secondaryIcon != null) {
            context.drawTexture(secondaryIcon, x, y, 0.0F, 0.0F, 32, 32, 32, 32);
        }
        Identifier thirdIcon = getIcon3OrNull(hovered);
        if(thirdIcon != null) {
            context.drawTexture(thirdIcon, x, y, 0.0F, 0.0F, 32, 32, 32, 32);
        }
        OrderedText orderedText = getDisplayText();
        MultilineText multilineText = MultilineText.createFromTexts(MinecraftClient.getInstance().textRenderer, getExtraText(true));


        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, orderedText, x + 32 + 2, y + 1, 16777215);
        multilineText.drawWithShadow(context, x + 32 + 2, y + 12, 10, -8355712);
    }

}
