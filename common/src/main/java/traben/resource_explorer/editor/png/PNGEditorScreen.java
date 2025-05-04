package traben.resource_explorer.editor.png;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import traben.resource_explorer.editor.ConfirmExportScreen;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Supplier;

import static traben.resource_explorer.ResourceExplorerClient.MOD_ID;

public class PNGEditorScreen extends Screen {
    final ColorTool colorTool = new ColorTool();
    private final Screen parent;
    private final ColorSliderWidget greenSlider = new ColorSliderWidget(Text.of("green: "),
            (value) -> colorTool.setColorGreen((int) (value * 255)));
    private final ColorSliderWidget redSlider = new ColorSliderWidget(Text.of("red: "),
            (value) -> colorTool.setColorRed((int) (value * 255)));
    private final ColorSliderWidget blueSlider = new ColorSliderWidget(Text.of("blue: "),
            (value) -> colorTool.setColorBlue((int) (value * 255)));
    private final ColorSliderWidget alphaSlider = new ColorSliderWidget(Text.of("alpha: "),
            (value) -> colorTool.setColorAlpha((int) (value * 255)));
    private final EditorWidget editorWidget;
    int lastSliderUpdate = 0;
    private ButtonWidget undoButton = null;

    public PNGEditorScreen(final Screen parent, final Identifier pngToEdit, final Supplier<NativeImage> supplier) throws IOException, NullPointerException {
        super(Text.translatable(MOD_ID + ".png_editor.title"));
        this.parent = parent;

        if (pngToEdit == null) throw new NullPointerException("[PNG Editor] Identifier was null");
        if (supplier == null) throw new NullPointerException("[PNG Editor] Resource was null");

        editorWidget = new EditorWidget(colorTool, pngToEdit, supplier);
    }

    @Override
    public void close() {
        editorWidget.close();
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
        editorWidget.setBounds(editorSize(), (int) (width * 0.1), (int) (height * 0.1));
        this.addDrawableChild(editorWidget);


        var buttonWidth = 12 + (int) (this.width * 0.225);
        //fit image button
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("resource_explorer.png_editor.center_button"),
                        (button) -> editorWidget.fitImage())
                .dimensions(getButtonAreaLeft(), (int) (this.height * 0.1), buttonWidth, 20)
                .build());

        //eraser button
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("resource_explorer.png_editor.eraser_button"),
                        (button) -> {
                            colorTool.setColor(0);
                            updateSliders();
                        })
                .dimensions(getButtonAreaLeft(), (int) (this.height * 0.2), buttonWidth, 20)
                .tooltip(Tooltip.of(Text.translatable("resource_explorer.png_editor.eraser_button.tooltip")))
                .build());

        //pick color button
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("resource_explorer.png_editor.pick_button"),
                        (button) -> editorWidget.setCtrl())
                .dimensions(getButtonAreaLeft(), (int) (this.height * 0.3), buttonWidth, 20)
                .tooltip(Tooltip.of(Text.translatable("resource_explorer.png_editor.pick_button.tooltip")))
                .build());


        int secondButtonRowX = getButtonAreaLeft() + buttonWidth + 4;
        int secondButtonRowWidth = (int) (buttonWidth / 1.5);
        //reset button
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("resource_explorer.png_editor.reset"),
                        (button) -> {
                            editorWidget.clearUndoHistory();
                            undoButton.active = false;
                            editorWidget.resetImage();
                        })
                .dimensions(secondButtonRowX, (int) (this.height * 0.1), secondButtonRowWidth, 20)
                .tooltip(Tooltip.of(Text.translatable("resource_explorer.png_editor.reset.tooltip")))
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("resource_explorer.png_editor.clear"),
                        (button) -> {
                            editorWidget.clearUndoHistory();
                            undoButton.active = false;
                            editorWidget.clearImage();
                        })
                .dimensions(secondButtonRowX, (int) (this.height * 0.2), secondButtonRowWidth, 20)
                .tooltip(Tooltip.of(Text.translatable("resource_explorer.png_editor.clear.tooltip")))
                .build());

        undoButton = ButtonWidget.builder(
                        Text.translatable("resource_explorer.png_editor.undo"),
                        (button) -> {
                            editorWidget.undoLastPixel();
                            button.active = editorWidget.canUndo();
                        })
                .dimensions(secondButtonRowX, (int) (this.height * 0.3), secondButtonRowWidth, 20)
                .tooltip(Tooltip.of(Text.translatable("resource_explorer.png_editor.undo.tooltip")))
                .build();
        this.addDrawableChild(undoButton);
        undoButton.active = editorWidget.canUndo();

        //init color sliders
        redSlider.setDimensionsAndPosition((int) (this.width * 0.2), 20,
                getButtonAreaLeft(), (int) (this.height * 0.4));
        greenSlider.setDimensionsAndPosition((int) (this.width * 0.2), 20,
                getButtonAreaLeft(), (int) (this.height * 0.5));
        blueSlider.setDimensionsAndPosition((int) (this.width * 0.2), 20,
                getButtonAreaLeft(), (int) (this.height * 0.6));
        alphaSlider.setDimensionsAndPosition((int) (this.width * 0.2), 20,
                getButtonAreaLeft(), (int) (this.height * 0.7));

        this.addDrawableChild(redSlider);
        this.addDrawableChild(greenSlider);
        this.addDrawableChild(blueSlider);
        this.addDrawableChild(alphaSlider);

        updateSliders();

        //background button
        this.addDrawableChild(ButtonWidget.builder(
                        Text.of(Text.translatable("resource_explorer.png_editor.background").getString() +
                                ": " + editorWidget.getBackgroundText().getString()),
                        (button) -> {
                            editorWidget.nextBackground();
                            button.setMessage(Text.of(Text.translatable("resource_explorer.png_editor.background").getString() +
                                    ": " + editorWidget.getBackgroundText().getString()));
                        })
                .dimensions(getButtonAreaLeft(), (int) (this.height * 0.8), buttonWidth + 4 + secondButtonRowWidth, 20)
                .build());

        //init the color history buttons
        var colorDisplayX = secondButtonRowX + 1;
        int xOffset = 0;
        int count = 0;
        for (int j = 0; j < 5; j++) {
            int colorDisplayY = (int) (this.height * 0.4);
            int countPlus6 = count + 6;
            while (count < countPlus6) {
                this.addDrawableChild(new ColorHistoryWidget(colorDisplayX + xOffset, colorDisplayY, colorTool, count));
                colorDisplayY += 16;
                count++;
            }
            xOffset += 16;
        }
    }


    private void updateSliders() {
        if (colorTool.getColor() != lastSliderUpdate) {
            lastSliderUpdate = colorTool.getColor();
            redSlider.setValue255(colorTool.getColorRed());
            greenSlider.setValue255(colorTool.getColorGreen());
            blueSlider.setValue255(colorTool.getColorBlue());
            alphaSlider.setValue255(colorTool.getColorAlpha());
        }
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean b = super.mouseClicked(mouseX, mouseY, button);
        if (isMouseOverEditor()) {
            updateSliders();
            if (undoButton != null) {
                undoButton.active = editorWidget.canUndo();
            }
        }
        return b;
    }
//1.20.5
//    @Override
//    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
//        renderBackgroundTexture(context);
//
//    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        //title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 8, 16777215);

        //image location
        context.drawTextWithShadow(this.textRenderer, Text.of(editorWidget.getImageIdentifier().toString()), editorWidget.getX(), editorWidget.getBottom() + 8, Colors.GRAY);

        //color display
        renderCurrentColorDisplay(context);

        //cursor
        if (isMouseOverEditor()) {
            PointerIcon pointer;
            if (Screen.hasShiftDown()) {
                pointer = PointerIcon.HAND_MOVE;
            } else if (Screen.hasControlDown() || editorWidget.isCtrl()) {
                pointer = PointerIcon.PICK;
            } else if (colorTool.getColorAlpha() == 0) {
                pointer = PointerIcon.ERASER;
            } else {
                pointer = PointerIcon.BRUSH;
            }
            pointer.render(context, mouseX, mouseY, colorTool.getColor());
        }
    }

    private void renderCurrentColorDisplay(DrawContext context) {
        var colorDisplayX = getButtonAreaLeft() + (int) (this.width * 0.225);
        var colorDisplayY = (int) (this.height * 0.4);
        var colorDisplayX2 = colorDisplayX + 10;
        var colorDisplayY2 = colorDisplayY + (int) (this.height * 0.3) + 20;

        context.fill(colorDisplayX - 1, colorDisplayY - 1, colorDisplayX2 + 1, colorDisplayY2 + 1,
                Colors.WHITE);
        context.fill(colorDisplayX, colorDisplayY, colorDisplayX2, colorDisplayY2,
                Colors.BLACK);

//        RenderSystem.enableBlend();

        context.fill(colorDisplayX, colorDisplayY, colorDisplayX2, colorDisplayY2,
                colorTool.getColorARGB());
//        RenderSystem.disableBlend();
    }


    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Screen.hasControlDown() && keyCode == 90) {//z
            editorWidget.undoLastPixel();
            undoButton.active = editorWidget.canUndo();
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
