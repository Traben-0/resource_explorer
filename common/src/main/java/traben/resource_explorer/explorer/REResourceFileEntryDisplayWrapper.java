package traben.resource_explorer.explorer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class REResourceFileEntryDisplayWrapper extends AlwaysSelectedEntryListWidget.Entry<REResourceFileEntryDisplayWrapper> implements Comparable<REResourceFileEntryDisplayWrapper> {

    public REResourceFileEntry getFileEntry() {
        return fileEntry;
    }

    private final REResourceFileEntry fileEntry;
    REResourceFileEntryDisplayWrapper(REResourceFileEntry fileEntry){
        this.fileEntry = fileEntry;

        //does button need to be initiated?
        if(fileEntry.fileType == REResourceFileEntry.FileType.OGG){
            RESound easySound = new RESound(fileEntry);
            multiUseButton = new ButtonWidget.Builder(Text.translatable("resource_explorer.play_sound"),
                    (button) -> MinecraftClient.getInstance().getSoundManager().play(easySound)
            ).dimensions(0, 0, 150, 20).build();
            multiUseButton.active = fileEntry.resource != null;
        } else if (fileEntry.resource != null && (fileEntry.fileType.isRawTextType() || fileEntry.fileType == REResourceFileEntry.FileType.PNG)) {
            multiUseButton = new ButtonWidget.Builder(Text.translatable("resource_explorer.export_single"),
                    (button) -> {
                        button.active = false;

                        REExplorer.REExportContext context = new REExplorer.REExportContext();
                        fileEntry.exportToOutputPack(context);
                        context.showExportToast();
                        button.setMessage(Text.translatable(
                                context.getTotatExported() == 1 ?
                                        "resource_explorer.export_single.success" :
                                        "resource_explorer.export_single.fail"
                        ));
                    }
            ).dimensions(0, 0, 150, 20).tooltip(Tooltip.of(Text.translatable("resource_explorer.export.tooltip"))).build();
        }
    }

    public int getEntryHeight(){
        int entryWidth = 178;
        int heightMargin = 100 + (fileEntry.getExtraText(false).size() * 11);
        return (int) (heightMargin + switch (fileEntry.fileType){
                    case PNG -> 40+  fileEntry.height*((entryWidth+0f)/fileEntry.width);
                    case TXT, PROPERTIES, JEM, JPM , JSON -> 64+  fileEntry.getTextLines().count() * 10;
                    case OGG -> 100;
                    case ZIP -> 100;
                    case OTHER -> 50+ fileEntry.height*((entryWidth+0f)/fileEntry.width) + fileEntry.getTextLines().count() * 10;
                    case BLANK -> 100;
                });
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(multiUseButton != null && multiUseButton.active && multiUseButton.isMouseOver(mouseX,mouseY)){
            multiUseButton.onPress();
        }
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

        switch (fileEntry.fileType){
            case PNG -> {
                offset = drawAsImage(context, offset,displaySquareMaximum, displayX, displayY);
                drawButton(Text.of("Export:"), context, offset, displayX, displayY, mouseX, mouseY);
            }
            case TXT, PROPERTIES, JEM, JPM , JSON -> {
                offset = drawAsText(context, offset, displayX, displayY);
                drawButton(Text.of("Export:"), context, offset, displayX, displayY, mouseX, mouseY);
            }
            case OTHER -> {
                offset = drawAsText(context, offset, displayX, displayY);
                drawAsImage(context, offset,displaySquareMaximum, displayX, displayY);
            }
            case OGG -> drawButton(Text.of("Sound:"), context, offset, displayX, displayY, mouseX, mouseY);
        };
    }


    private int drawAsImage(DrawContext context ,int offset, int displaySquareMaximum, int displayX, int displayY){
        float sizeScale = ((float)displaySquareMaximum) / fileEntry.width;

        int displayX2 = (int) (fileEntry.width * sizeScale) ;
        int displayY2 = (int) (fileEntry.height * sizeScale);

        //title
        offset += 11;
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.of("Image:"), displayX, displayY+offset, 16777215);
        offset += 13;

        //outline
        context.fill(displayX-2,displayY+offset-2, displayX+displayX2+2,displayY+offset+displayY2+2, ColorHelper.Argb.getArgb(255,255,255,255));
        context.fill(displayX,displayY+offset, displayX+displayX2,displayY+offset+displayY2, -16777216);
        //image
        context.drawTexture(fileEntry.identifier,displayX,displayY+offset, 0,0, displayX2, displayY2, displayX2, displayY2);

        offset += displayY2;
        return offset;
    }
    private int drawAsText(DrawContext context ,int offset, int displayX, int displayY){
        offset += 11;
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.of("Text:"), displayX, displayY+offset, 16777215);
        offset += 11;

        MultilineText rawTextData = fileEntry.getTextLines();
        rawTextData.drawWithShadow(context, displayX, displayY + offset, 10, -8355712);
        offset += rawTextData.count()*10;
        return offset;
    }

    private int drawButton(Text text ,DrawContext context ,int offset, int displayX, int displayY, int mouseX, int mouseY){
        if(multiUseButton != null) {
            offset += 11;
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, text, displayX, displayY+offset, 16777215);
            offset += 11;
            multiUseButton.setX(displayX);
            multiUseButton.setY(displayY + offset);
            multiUseButton.render(context, mouseX, mouseY, 0);
            offset += 20;
        }
        return offset ;
    }

    private ButtonWidget multiUseButton = null;

    @Override
    public int compareTo(@NotNull REResourceFileEntryDisplayWrapper o) {
        return fileEntry.getDisplayName().compareTo(o.fileEntry.getDisplayName());
    }

    @Override
    public Text getNarration() {
        return Text.of(fileEntry.getDisplayName());
    }



    private static class RESound implements SoundInstance{

        private final String id;
        private final Sound sound;
        RESound(REResourceFileEntry fileEntry){
            id ="re_"+fileEntry.getDisplayName()+"2";
            sound = new Sound("re_"+fileEntry.getDisplayName(),(a)->1,(a)->1,1, Sound.RegistrationType.FILE,true,true,1){
                @Override
                public Identifier getLocation() {
                    return fileEntry.identifier;
                }
            };
        }

        @Override
        public Identifier getId() {
            return new Identifier(id);
        }

        @Nullable
        @Override
        public WeightedSoundSet getSoundSet(SoundManager soundManager) {
            return new WeightedSoundSet(getId(),"wat");
        }

        @Override
        public Sound getSound() {
            return sound;
        }

        @Override
        public SoundCategory getCategory() {
            return SoundCategory.MASTER;
        }

        @Override
        public boolean isRepeatable() {
            return false;
        }

        @Override
        public boolean isRelative() {
            return false;
        }

        @Override
        public int getRepeatDelay() {
            return 0;
        }

        @Override
        public float getVolume() {
            return 1;
        }

        @Override
        public float getPitch() {
            return 1;
        }

        @Override
        public double getX() {
            return 0;
        }

        @Override
        public double getY() {
            return 0;
        }

        @Override
        public double getZ() {
            return 0;
        }

        @Override
        public AttenuationType getAttenuationType() {
            return AttenuationType.NONE;
        }
    };
}
