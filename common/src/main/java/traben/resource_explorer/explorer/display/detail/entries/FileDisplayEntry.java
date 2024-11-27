package traben.resource_explorer.explorer.display.detail.entries;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.NotNull;
import traben.resource_explorer.ResourceExplorerClient;
import traben.resource_explorer.editor.png.PNGEditorScreen;
import traben.resource_explorer.editor.txt.TXTEditorScreen;
import traben.resource_explorer.explorer.ExplorerUtils;
import traben.resource_explorer.explorer.display.resources.entries.ResourceFileEntry;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class FileDisplayEntry extends DisplayEntry {
    private final ResourceFileEntry fileEntry;
    private ButtonWidget multiUseButton = null;
    private ButtonWidget editorButton = null;

    public FileDisplayEntry(ResourceFileEntry fileEntry) {
        this.fileEntry = fileEntry;

        //does button need to be initiated?
        if (fileEntry.fileType == ResourceFileEntry.FileType.OGG) {
            SoundPlayer easySound = new SoundPlayer(fileEntry);
            multiUseButton = new ButtonWidget.Builder(Text.translatable("resource_explorer.play_sound"),
                    (button) -> MinecraftClient.getInstance().getSoundManager().play(easySound)
            ).dimensions(0, 0, 150, 20).build();
            multiUseButton.active = fileEntry.resource != null;
        } else if (fileEntry.resource != null && (fileEntry.fileType.isRawTextType() || fileEntry.fileType == ResourceFileEntry.FileType.PNG)) {
            multiUseButton = new ButtonWidget.Builder(Text.translatable("resource_explorer.export_single"),
                    (button) -> {
                        button.active = false;

                        ExplorerUtils.REExportContext context = new ExplorerUtils.REExportContext();
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

        initImageEditorButton();
        initTextEditorButton();
    }

    private void initImageEditorButton() {
        if (fileEntry.fileType == ResourceFileEntry.FileType.PNG) {
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

    private void initTextEditorButton() {
        if (fileEntry.fileType.isRawTextType()) {
            editorButton = new ButtonWidget.Builder(Text.translatable("resource_explorer.edit_txt"),
                    (button) -> {
                        try {
                            MinecraftClient.getInstance().setScreen(
                                    new TXTEditorScreen(
                                            MinecraftClient.getInstance().currentScreen,
                                            fileEntry.identifier,
                                            getRawInputText()));
                        } catch (Exception e) {
                            ResourceExplorerClient.log("edit button failed: " + e.getMessage());
                            button.active = false;
                            button.setMessage(Text.translatable("resource_explorer.edit_txt.fail"));
                        }
                    }
            ).dimensions(0, 0, 150, 20).build();
            editorButton.active = fileEntry.resource != null;
            if (!editorButton.active) {
                editorButton.setMessage(Text.translatable("resource_explorer.edit_txt.fail"));
            }
        }
    }

    private String getRawInputText() {
        if (fileEntry.resource != null) {
            try (InputStream in = fileEntry.resource.getInputStream()) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            } catch (Exception ignored) {
            }
        }
        return null;
    }


    public ResourceFileEntry getFileEntry() {
        return fileEntry;
    }

    public int getEntryHeight() {
        int entryWidth = 178;
        int heightMargin = 100 + (fileEntry.getExtraText(false).length * 11);
        return (int) (heightMargin + switch (fileEntry.fileType) {
            case PNG -> 82 + fileEntry.height * ((entryWidth + 0f) / fileEntry.width);
            case TXT, PROPERTIES, JEM, JPM, JSON -> 106 + fileEntry.getTextLines().count() * 10;
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

        MultilineText extraText = MultilineText.create(MinecraftClient.getInstance().textRenderer, fileEntry.getExtraText(false));
        extraText.drawWithShadow(context, displayX, displayY + offset, 10, -8355712);
        offset += extraText.count() * 11;

        switch (fileEntry.fileType) {
            case PNG -> {
                offset = drawAsImage(context, offset, displaySquareMaximum, displayX, displayY);
                offset = drawWidget(multiUseButton, Text.of("Export:"), context, offset, displayX, displayY, mouseX, mouseY);
                drawWidget(editorButton, Text.of("Edit & Export:"), context, offset, displayX, displayY, mouseX, mouseY);
            }
            case TXT, PROPERTIES, JEM, JPM, JSON -> {
                offset = drawText(Text.of("Text:"), fileEntry.getTextLines(), context, offset, displayX, displayY);
                offset = drawWidget(multiUseButton, Text.of("Export:"), context, offset, displayX, displayY, mouseX, mouseY);
                drawWidget(editorButton, Text.of("Edit & Export:"), context, offset, displayX, displayY, mouseX, mouseY);
            }
            case OTHER -> {
                offset = drawText(Text.of("Text:"), fileEntry.getTextLines(), context, offset, displayX, displayY);
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
        context.fill(displayX - 2, displayY + offset - 2, displayX + displayX2 + 2, displayY + offset + displayY2 + 2, ColorHelper.getArgb(255, 255, 255, 255));
        context.fill(displayX, displayY + offset, displayX + displayX2, displayY + offset + displayY2, -16777216);
        //image
        context.drawTexture(RenderLayer::getGuiTextured, fileEntry.identifier, displayX, displayY + offset, 0, 0, displayX2, displayY2, displayX2, displayY2);

        offset += displayY2;
        return offset;
    }

    @Override
    public int compareTo(@NotNull DisplayEntry o) {
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


}
