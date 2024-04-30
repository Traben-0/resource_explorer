package traben.resource_explorer.editor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import traben.resource_explorer.ResourceExplorerClient;

import java.util.Objects;

public class ConfirmExportScreen extends Screen {

    private Screen parent;

    private ExportableFileContainerAndPreviewer fileData;

    public ConfirmExportScreen(Screen parent, ExportableFileContainerAndPreviewer fileData) {
        super(Text.translatable("resource_explorer.png_editor.export.title"));
        this.parent = parent;
        this.fileData = fileData;
    }


    @Override
    protected void init() {
        super.init();
        //init lower buttons
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.back"),
                        (button) -> {
                            clearChildren();
                            Objects.requireNonNull(client).setScreen(parent);
                            parent = null;
                            fileData = null;
                        })
                .dimensions((int) (this.width * 0.1), (int) (this.height * 0.9), (int) (this.width * 0.2), 20)
                .build());

        var txt = new TextFieldWidget(MinecraftClient.getInstance().textRenderer,
                (int) (this.width * 0.61), (int) (this.height * 0.2), (int) (this.width * 0.35), 20,
                Text.of(fileData.getOriginalAssetIdentifier()));
        txt.setMaxLength(256);
        txt.setText(fileData.getOriginalAssetIdentifier().toString());

        this.addDrawableChild(txt);
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("resource_explorer.png_editor.export_button"),
                        (button) -> {
//                            var id = Identifier.validate(fileData.assertFileTypeOnEnd(txt.getText())).result();
                            var id = Identifier.validate(txt.getText()).result();
                            if (id.isPresent()) {
                                if (fileData.exportAsIdentifier(id.get())) {
                                    MinecraftClient.getInstance().getTextureManager().destroyTexture(id.get());
                                    ResourceExplorerClient.leaveModScreensAndResourceReload();
                                    button.active = false;
                                } else {
                                    button.setMessage(Text.of("resource_explorer.png_editor.export_button.fail"));
                                }
                            } else if (txt.getText().isBlank()) {
                                if (fileData.exportAsIdentifier(fileData.getOriginalAssetIdentifier())) {
                                    MinecraftClient.getInstance().getTextureManager().destroyTexture(fileData.getOriginalAssetIdentifier());
                                    ResourceExplorerClient.leaveModScreensAndResourceReload();
                                    button.active = false;
                                } else {
                                    button.setMessage(Text.of("resource_explorer.png_editor.export_button.fail"));
                                }
                            } else {
                                button.setMessage(Text.of("resource_explorer.png_editor.export_button.fail"));
                            }
                        })
                .dimensions((int) (this.width * 0.61), (int) (this.height * 0.3), (int) (this.width * 0.2), 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (fileData != null) {
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.translatable("resource_explorer.png_editor.export_save")
                    , (int) (this.width * 0.61), (int) (this.height * 0.1), Colors.WHITE);

            String[] lines = Text.translatable("resource_explorer.png_editor.export_button.warn").getString().split("\n");
            int heightLines = (int) (this.height * 0.4);
            for (String line : lines) {
                context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.of(line)
                        , (int) (this.width * 0.61), heightLines, Colors.LIGHT_GRAY);
                heightLines += 11;
            }

            int size = Math.min((int) (width * 0.6), (int) (height * 0.7));
            int x = (int) (width * 0.05);
            int y = (int) (height * 0.1);

            fileData.renderSimple(context, x, y, x + size, y + size);
        }
    }
//1.20.5
//    @Override
//    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
//        renderBackgroundTexture(context);
//    }
}
