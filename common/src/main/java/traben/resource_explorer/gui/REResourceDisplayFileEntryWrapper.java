package traben.resource_explorer.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.NotNull;

public class REResourceDisplayFileEntryWrapper extends AlwaysSelectedEntryListWidget.Entry<REResourceDisplayFileEntryWrapper> implements Comparable<REResourceDisplayFileEntryWrapper> {

    public REResourceFileEntry getFileEntry() {
        return fileEntry;
    }

    private final REResourceFileEntry fileEntry;
    REResourceDisplayFileEntryWrapper(REResourceFileEntry fileEntry){
        this.fileEntry = fileEntry;
    }

    public int getEntryHeight(){
        int entryWidth = 178;
        int heightMargin = 100 + (fileEntry.getExtraText(false).size() * 11);
        return (int) (heightMargin + switch (fileEntry.type){
                    case PNG ->  fileEntry.height*((entryWidth+0f)/fileEntry.width);
                    case TXT, PROPERTIES, JEM, JPM , JSON ->  fileEntry.getTextLines().count() * 10;
                    case OGG -> 100;
                    case ZIP -> 100;
                    case OTHER -> 50+ fileEntry.height*((entryWidth+0f)/fileEntry.width) + fileEntry.getTextLines().count() * 10;
                    case BLANK -> 100;
                });
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        //super.render(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);


        int displayX = x+8;
        int displayY = y+8;
        int displaySquareMaximum = Math.min(entryHeight,entryWidth)-22;

        int offset = 0;

        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.of("Resource path:"), displayX, displayY+offset, 16777215);
        offset += 11;

        MultilineText identifierText = MultilineText.create(MinecraftClient.getInstance().textRenderer, Text.of("§o"+fileEntry.identifier),entryWidth-20);
        identifierText.drawWithShadow(context, displayX+4, displayY+offset, 11, -8355712);
        offset += 11+ identifierText.count()*11;

        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.of("Details:"), displayX, displayY+offset, 16777215);
        offset += 11;

        MultilineText extraText = MultilineText.createFromTexts(MinecraftClient.getInstance().textRenderer, fileEntry.getExtraText(false));
        extraText.drawWithShadow(context, displayX, displayY + offset, 10, -8355712);
        offset += extraText.count()*11;

        switch (fileEntry.type){
            case PNG -> offset = drawImage(context, offset,displaySquareMaximum, displayX, displayY);
            case TXT, PROPERTIES, JEM, JPM , JSON -> offset = drawText(context, offset, displayX, displayY);
            case OTHER -> {
                offset = drawText(context, offset, displayX, displayY);
                offset = drawImage(context, offset,displaySquareMaximum, displayX, displayY);
            }
        };
    }


    private int drawImage(DrawContext context ,int offset, int displaySquareMaximum, int displayX, int displayY){
        float sizeScale = ((float)displaySquareMaximum) / fileEntry.width;

        int displayX2 = (int) (fileEntry.width * sizeScale) ;
        int displayY2 = (int) (fileEntry.height * sizeScale);


        offset += 11;
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.of("Image:"), displayX, displayY+offset, 16777215);
        offset += 13;

        context.fill(displayX-2,displayY+offset-2, displayX+displayX2+2,displayY+offset+displayY2+2, ColorHelper.Argb.getArgb(255,255,255,255));
        context.fill(displayX,displayY+offset, displayX+displayX2,displayY+offset+displayY2, -16777216);
        //context.fill(displayX,displayY+offset, displayX2, displayY2, 0);

        context.drawTexture(fileEntry.identifier,displayX,displayY+offset, 0,0, displayX2, displayY2, displayX2, displayY2);

        return offset;
    }
    private int drawText(DrawContext context ,int offset, int displayX, int displayY){
        offset += 11;
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.of("Text:"), displayX, displayY+offset, 16777215);
        offset += 11;

        MultilineText rawTextData = fileEntry.getTextLines();
        rawTextData.drawWithShadow(context, displayX, displayY + offset, 10, -8355712);
        offset += rawTextData.count()*10;
        return offset;
    }

    @Override
    public int compareTo(@NotNull REResourceDisplayFileEntryWrapper o) {
        return fileEntry.getDisplayName().compareTo(o.fileEntry.getDisplayName());
    }

    @Override
    public Text getNarration() {
        return Text.of(fileEntry.getDisplayName());
    }
}
