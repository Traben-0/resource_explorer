package traben.resource_explorer.editor.txt;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import traben.resource_explorer.editor.ConfirmExportScreen;

import java.util.Objects;

import static traben.resource_explorer.ResourceExplorerClient.MOD_ID;

public class TXTEditorScreen extends Screen {
    private final Screen parent;

    private final TextEditorWidget editorWidget;
    int lastSliderUpdate = 0;
    private final ButtonWidget undoButton = null;

    public TXTEditorScreen(final Screen parent, final Identifier txtToEdit, final String text) throws IllegalArgumentException, NullPointerException {
        super(Text.translatable(MOD_ID + ".png_editor.title"));
        this.parent = parent;

        if (txtToEdit == null) throw new NullPointerException("[PNG Editor] Identifier was null");
        if (text == null) throw new NullPointerException("[PNG Editor] Resource was null");

        editorWidget = new TextEditorWidget(txtToEdit, text, "txt");
    }

    @Override
    public void close() {
        editorWidget.clear();
        super.close();
    }

    @Override
    protected void init() {
        super.init();
        //init lower buttons
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("resource_explorer.png_editor.close"),
                        (button) -> Objects.requireNonNull(client).setScreen(parent))
                .dimensions((int) (this.width * 0.1), (int) (this.height * 0.9), (int) (this.width * 0.2), 20)
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("resource_explorer.png_editor.export_button"),
                        (button) -> Objects.requireNonNull(client).setScreen(new ConfirmExportScreen(this, editorWidget)))
                .dimensions((int) (this.width * 0.6), (int) (this.height * 0.9), (int) (this.width * 0.3), 20)
                .tooltip(Tooltip.of(Text.translatable("resource_explorer.png_editor.export_button.tooltip")))
                .build());


        //init editor
        editorWidget.setDimensions((int) (width * 0.1), (int) (height * 0.1), editorSize());
        this.addDrawableChild(editorWidget);


//        var buttonWidth = 12 + (int) (this.width * 0.225);
//        //fit image button
//        this.addDrawableChild(ButtonWidget.builder(
//                        Text.translatable("resource_explorer.png_editor.center_button"),
//                        (button) -> editorWidget.fitImage())
//                .dimensions(getButtonAreaLeft(), (int) (this.height * 0.1), buttonWidth, 20)
//                .build());
//
//        //eraser button
//        this.addDrawableChild(ButtonWidget.builder(
//                        Text.translatable("resource_explorer.png_editor.eraser_button"),
//                        (button) -> {
//                            colorTool.setColor(0);
//                            updateSliders();
//                        })
//                .dimensions(getButtonAreaLeft(), (int) (this.height * 0.2), buttonWidth, 20)
//                .tooltip(Tooltip.of(Text.translatable("resource_explorer.png_editor.eraser_button.tooltip")))
//                .build());
//
//        //pick color button
//        this.addDrawableChild(ButtonWidget.builder(
//                        Text.translatable("resource_explorer.png_editor.pick_button"),
//                        (button) -> editorWidget.setCtrl())
//                .dimensions(getButtonAreaLeft(), (int) (this.height * 0.3), buttonWidth, 20)
//                .tooltip(Tooltip.of(Text.translatable("resource_explorer.png_editor.pick_button.tooltip")))
//                .build());
//
//
        int secondButtonRowX = getButtonAreaLeft();
        int secondButtonRowWidth = 150;
        //reset button
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("resource_explorer.txt_editor.reset"),
                        (button) -> editorWidget.resetText())
                .dimensions(secondButtonRowX, (int) (this.height * 0.1), secondButtonRowWidth, 20)
                .tooltip(Tooltip.of(Text.translatable("resource_explorer.txt_editor.reset.tooltip")))
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("resource_explorer.txt_editor.clear"),
                        (button) -> editorWidget.clearText())
                .dimensions(secondButtonRowX, (int) (this.height * 0.2), secondButtonRowWidth, 20)
                .tooltip(Tooltip.of(Text.translatable("resource_explorer.txt_editor.clear.tooltip")))
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("resource_explorer.txt_editor.format"),
                        (button) -> editorWidget.reformatText())
                .dimensions(secondButtonRowX, (int) (this.height * 0.3), secondButtonRowWidth, 20)
                .tooltip(Tooltip.of(Text.translatable("resource_explorer.txt_editor.format.tooltip")))
                .build());


    }


    private int getButtonAreaLeft() {
        return editorWidget.getX() + editorWidget.getWidth() + (int) (this.width * 0.05);
    }

    private boolean isMouseOverEditor() {
        return editorWidget.isHovered();
    }

    private int editorSize() {
        return Math.min((int) (width * 0.7), (int) (height * 0.7));
    }


    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackgroundTexture(context);

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        //title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 8, 16777215);

        //image location
        context.drawTextWithShadow(this.textRenderer, Text.of(editorWidget.getOriginalAssetIdentifier().toString()), editorWidget.getX(), editorWidget.getBottom() + 8, Colors.GRAY);


    }


}
