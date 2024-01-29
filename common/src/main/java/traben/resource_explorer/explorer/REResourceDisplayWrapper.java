package traben.resource_explorer.explorer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import traben.resource_explorer.ResourceExplorerClient;
import traben.resource_explorer.editor.png.PNGEditorScreen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class REResourceDisplayWrapper extends AlwaysSelectedEntryListWidget.Entry<REResourceDisplayWrapper> implements Comparable<REResourceDisplayWrapper> {

    protected int drawText(MultilineText rawTextData, DrawContext context, int offset, int displayX, int displayY) {
        offset += 11;
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.of("Text:"), displayX, displayY + offset, 16777215);
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

    public static class CreateFile extends REResourceDisplayWrapper {

        final String identifierPrefix;
        private final TextFieldWidget textInput = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 150, 20, Text.of("..."));
        private final TextFieldWidget widthInput = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 150, 20, Text.of("width..."));
        private final TextFieldWidget heightInput = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 150, 20, Text.of("height..."));
        private final ButtonWidget pngButton;
        private final ButtonWidget txtButton;
        private final ButtonWidget createButton;
        final List<ClickableWidget> widgets = new ArrayList<>();

        CreateFile(REExplorerScreen screen) {
            identifierPrefix = screen.cumulativePath
                    .replaceFirst("assets/", "")
                    .replaceFirst("/", ":");
            pngButton = ButtonWidget.builder(Text.translatable("resource_explorer.explorer.png"), (button) -> setPNG())
                    .dimensions(0, 0, 50, 20).build();
            txtButton = ButtonWidget.builder(Text.translatable("resource_explorer.explorer.txt"), (button) -> setTXT())
                    .dimensions(0, 0, 50, 20).build();
            createButton = ButtonWidget.builder(Text.translatable("resource_explorer.explorer.create"), (button) -> {
                boolean png = !getPngButton().active;
                if (png && !textInput.getText().isEmpty()) {
                    Optional<Identifier> validated = Identifier.validate(identifierPrefix + textInput.getText() + ".png").result();
                    var width = getInputWidth();
                    var height = getInputHeight();
                    if (validated.isPresent() && width != null && height != null) {
                        try {
                            MinecraftClient.getInstance().setScreen(new PNGEditorScreen(screen, validated.get(),
                                    () -> ResourceExplorerClient.getEmptyNativeImage(width, height)));
                        } catch (IOException e) {
                            ResourceExplorerClient.log("image resource creation failed");
                        }
                    } else {
                        ResourceExplorerClient.log("image resource creation invalid");
                    }
                }
            }).dimensions(0, 0, 150, 20).build();

            widgets.add(textInput);
            widgets.add(widthInput);
            widgets.add(heightInput);
            widgets.add(pngButton);
            widgets.add(txtButton);
            widgets.add(createButton);
        }

        void setPNG() {
            getPngButton().active = false;
            getTxtButton().active = true;
            setWidthHeight(true);
        }

        void setTXT() {
            getPngButton().active = true;
            getTxtButton().active = false;
            setWidthHeight(false);
        }

        ButtonWidget getPngButton() {
            return pngButton;
        }

        ButtonWidget getTxtButton() {
            return txtButton;
        }

        void setWidthHeight(boolean set) {
            widthInput.active = set;
            widthInput.setEditable(set);
            heightInput.active = set;
            heightInput.setEditable(set);
        }

        Integer getInputWidth() {
            try {
                return Integer.parseInt(widthInput.getText());
            } catch (Exception e) {
                return null;
            }
        }

        Integer getInputHeight() {
            try {
                return Integer.parseInt(heightInput.getText());
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public int compareTo(@NotNull REResourceDisplayWrapper o) {
            return 0;
        }

        @Override
        public Text getNarration() {
            return Text.of("");
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            focusHovered();
            for (ClickableWidget widget : widgets) {
                if (widget.isHovered() && widget.active) {
                    return widget.mouseClicked(mouseX, mouseY, button);
                }
            }
            return false;
        }

        void focusHovered() {
            for (ClickableWidget widget : widgets) {
                widget.setFocused(widget.isHovered());
            }
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            for (ClickableWidget widget : widgets) {
                if (widget.isHovered()) {
                    return widget.mouseReleased(mouseX, mouseY, button);
                }
            }
            return false;
        }


        @Override
        public boolean charTyped(char chr, int modifiers) {
            for (ClickableWidget widget : widgets) {
                if (widget.isFocused()) {
                    return widget.charTyped(chr, modifiers);
                }
            }
            return super.charTyped(chr, modifiers);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            for (ClickableWidget widget : widgets) {
                if (widget.isFocused()) {
                    return widget.keyPressed(keyCode, scanCode, modifiers);
                }
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
            for (ClickableWidget widget : widgets) {
                if (widget.isFocused()) {
                    return widget.keyReleased(keyCode, scanCode, modifiers);
                }
            }
            return super.keyReleased(keyCode, scanCode, modifiers);
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int displayX = x + 8;
            int displayY = y + 8;

            int offset = 0;

            offset = drawWidget(textInput, Text.translatable("resource_explorer.explorer.file_name"), context, offset, displayX, displayY, mouseX, mouseY);

            offset = drawWidget(pngButton, Text.translatable("resource_explorer.explorer.tile_type"), context, offset, displayX, displayY, mouseX, mouseY);
            drawWidgetOnly(txtButton, context, offset - 33, displayX + 68, displayY, mouseX, mouseY);

            if (widthInput.active) {
                offset = drawWidget(widthInput, Text.translatable("resource_explorer.explorer.width"), context, offset, displayX, displayY, mouseX, mouseY);
                offset = drawWidget(heightInput, Text.translatable("resource_explorer.explorer.height"), context, offset, displayX, displayY, mouseX, mouseY);
            } else {
                offset = drawWidgetSubtleText(widthInput, Text.translatable("resource_explorer.explorer.width"), context, offset, displayX, displayY, mouseX, mouseY);
                offset = drawWidgetSubtleText(heightInput, Text.translatable("resource_explorer.explorer.height"), context, offset, displayX, displayY, mouseX, mouseY);
            }

            drawWidget(createButton, Text.translatable("resource_explorer.explorer.create_file"), context, offset, displayX, displayY, mouseX, mouseY);
        }

        @Override
        public String getDisplayName() {
            return "Create new resource";
        }

        @Override
        public int getEntryHeight() {
            return 600;
        }
    }


    public static class File extends REResourceDisplayWrapper {
        private final REResourceFile fileEntry;
        private ButtonWidget multiUseButton = null;
        private ButtonWidget editorButton = null;

        File(REResourceFile fileEntry) {
            this.fileEntry = fileEntry;

            //does button need to be initiated?
            if (fileEntry.fileType == REResourceFile.FileType.OGG) {
                RESound easySound = new RESound(fileEntry);
                multiUseButton = new ButtonWidget.Builder(Text.translatable("resource_explorer.play_sound"),
                        (button) -> MinecraftClient.getInstance().getSoundManager().play(easySound)
                ).dimensions(0, 0, 150, 20).build();
                multiUseButton.active = fileEntry.resource != null;
            } else if (fileEntry.resource != null && (fileEntry.fileType.isRawTextType() || fileEntry.fileType == REResourceFile.FileType.PNG)) {
                multiUseButton = new ButtonWidget.Builder(Text.translatable("resource_explorer.export_single"),
                        (button) -> {
                            button.active = false;

                            REExplorer.REExportContext context = new REExplorer.REExportContext();
                            Util.getIoWorkerExecutor().execute(() -> {
                                fileEntry.exportToOutputPack(context);
                                context.showExportToast();
                                button.setMessage(Text.translatable(
                                        context.getTotalExported() == 1 ?
                                                "resource_explorer.export_single.success" :
                                                "resource_explorer.export_single.fail"
                                ));
                            });


                        }
                ).dimensions(0, 0, 150, 20).tooltip(Tooltip.of(Text.translatable("resource_explorer.export.tooltip.file"))).build();
            }

            if (fileEntry.fileType == REResourceFile.FileType.PNG) {
                editorButton = new ButtonWidget.Builder(Text.translatable("resource_explorer.edit_png"),
                        (button) -> {
                            try {
                                MinecraftClient.getInstance().setScreen(
                                        new PNGEditorScreen(MinecraftClient.getInstance().currentScreen,
                                                fileEntry.identifier, () -> ResourceExplorerClient.getNativeImageElseNull(fileEntry.resource)));
                            } catch (Exception e) {
                                ResourceExplorerClient.log("edit button failed: " + e.getMessage());
                                button.active = false;
                                button.setMessage(Text.translatable("resource_explorer.edit_png.fail"));
                            }
                        }
                ).dimensions(0, 0, 150, 20).build();
                editorButton.active = fileEntry.resource != null;
                if (!editorButton.active) {
                    editorButton.setMessage(Text.translatable("resource_explorer.edit_png.fail"));
                }
            }
        }

        public REResourceFile getFileEntry() {
            return fileEntry;
        }

        public int getEntryHeight() {
            int entryWidth = 178;
            int heightMargin = 100 + (fileEntry.getExtraText(false).size() * 11);
            return (int) (heightMargin + switch (fileEntry.fileType) {
                case PNG -> 82 + fileEntry.height * ((entryWidth + 0f) / fileEntry.width);
                case TXT, PROPERTIES, JEM, JPM, JSON -> 64 + fileEntry.getTextLines().count() * 10;
                case OTHER ->
                        50 + fileEntry.height * ((entryWidth + 0f) / fileEntry.width) + fileEntry.getTextLines().count() * 10;
                case OGG, BLANK, ZIP -> 100;
            });
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (multiUseButton != null && multiUseButton.active && multiUseButton.isMouseOver(mouseX, mouseY)) {
                multiUseButton.onPress();
            }
            if (editorButton != null && editorButton.active && editorButton.isMouseOver(mouseX, mouseY)) {
                editorButton.onPress();
            }
            return false;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            //super.render(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);


            int displayX = x + 8;
            int displayY = y + 8;
            int displaySquareMaximum = Math.min(entryHeight, entryWidth) - 22;

            int offset = 0;

            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.of("Resource path:"), displayX, displayY + offset, 16777215);
            offset += 11;

            MultilineText identifierText = MultilineText.create(MinecraftClient.getInstance().textRenderer, Text.of("§o" + fileEntry.identifier), entryWidth - 20);
            identifierText.drawWithShadow(context, displayX + 4, displayY + offset, 11, -8355712);
            offset += 11 + identifierText.count() * 11;

            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.of("Details:"), displayX, displayY + offset, 16777215);
            offset += 11;

            MultilineText extraText = MultilineText.createFromTexts(MinecraftClient.getInstance().textRenderer, fileEntry.getExtraText(false));
            extraText.drawWithShadow(context, displayX, displayY + offset, 10, -8355712);
            offset += extraText.count() * 11;

            switch (fileEntry.fileType) {
                case PNG -> {
                    offset = drawAsImage(context, offset, displaySquareMaximum, displayX, displayY);
                    offset = drawWidget(multiUseButton, Text.of("Export:"), context, offset, displayX, displayY, mouseX, mouseY);
                    drawWidget(editorButton, Text.of("Edit:"), context, offset, displayX, displayY, mouseX, mouseY);
                }
                case TXT, PROPERTIES, JEM, JPM, JSON -> {
                    offset = drawText(fileEntry.getTextLines(), context, offset, displayX, displayY);
                    drawWidget(multiUseButton, Text.of("Export:"), context, offset, displayX, displayY, mouseX, mouseY);
                }
                case OTHER -> {
                    offset = drawText(fileEntry.getTextLines(), context, offset, displayX, displayY);
                    drawAsImage(context, offset, displaySquareMaximum, displayX, displayY);
                }
                case OGG ->
                        drawWidget(multiUseButton, Text.of("Sound:"), context, offset, displayX, displayY, mouseX, mouseY);
            }
        }

        private int drawAsImage(DrawContext context, int offset, int displaySquareMaximum, int displayX, int displayY) {
            float sizeScale = ((float) displaySquareMaximum) / fileEntry.width;

            int displayX2 = (int) (fileEntry.width * sizeScale);
            int displayY2 = (int) (fileEntry.height * sizeScale);

            //title
            offset += 11;
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.of("Image:"), displayX, displayY + offset, 16777215);
            offset += 13;

            //outline
            context.fill(displayX - 2, displayY + offset - 2, displayX + displayX2 + 2, displayY + offset + displayY2 + 2, ColorHelper.Argb.getArgb(255, 255, 255, 255));
            context.fill(displayX, displayY + offset, displayX + displayX2, displayY + offset + displayY2, -16777216);
            //image
            context.drawTexture(fileEntry.identifier, displayX, displayY + offset, 0, 0, displayX2, displayY2, displayX2, displayY2);

            offset += displayY2;
            return offset;
        }

        @Override
        public int compareTo(@NotNull REResourceDisplayWrapper o) {
            return 0;
//            return fileEntry.getDisplayName().compareTo(o.fileEntry.getDisplayName());
        }

        @Override
        public Text getNarration() {
            return Text.of(fileEntry.getDisplayName());
        }

        @Override
        public String getDisplayName() {
            return getFileEntry().getDisplayName();
        }


        private static class RESound implements SoundInstance {

            private final String id;
            private final Sound sound;

            RESound(REResourceFile fileEntry) {
                id = "re_" + fileEntry.getDisplayName() + "2";
                sound = new Sound("re_" + fileEntry.getDisplayName(), (a) -> 1, (a) -> 1, 1, Sound.RegistrationType.FILE, true, true, 1) {
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
                return new WeightedSoundSet(getId(), "wat");
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
        }
    }
}
