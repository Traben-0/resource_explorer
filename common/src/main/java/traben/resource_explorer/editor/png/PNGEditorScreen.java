package traben.resource_explorer.editor.png;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.resource.Resource;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import traben.resource_explorer.ResourceExplorerClient;

import java.io.IOException;
import java.util.Objects;

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

    public PNGEditorScreen(final Screen parent, final Identifier pngToEdit, final Resource pngResource) throws IOException, NullPointerException {
        super(Text.translatable(MOD_ID + ".png_editor.title"));
        this.parent = parent;

        if (pngToEdit == null) throw new NullPointerException("[PNG Editor] Identifier was null");
        if (pngResource == null) throw new NullPointerException("[PNG Editor] Resource was null");

        editorWidget = new EditorWidget(colorTool, pngToEdit, pngResource);
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
                        Text.translatable("resource_explorer.png_editor.reset"),
                        (button) -> editorWidget.resetImage())
                .dimensions((int) (this.width * 0.35), (int) (this.height * 0.9), (int) (this.width * 0.2), 20)
                .build());
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("resource_explorer.png_editor.export_button"),
                        (button) -> {
                            ResourceExplorerClient.log("saved image = " + editorWidget.saveImage());
                            Objects.requireNonNull(client).setScreen(parent);
                        })
                .dimensions((int) (this.width * 0.6), (int) (this.height * 0.9), (int) (this.width * 0.3), 20)
                .tooltip(Tooltip.of(Text.translatable("resource_explorer.png_editor.export_button.tooltip")))
                .build());


        //init editor
        editorWidget.setBounds(editorSize(), (int) (width * 0.1), (int) (height * 0.1));
        this.addDrawableChild(editorWidget);

        //fit image button
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("resource_explorer.png_editor.center_button"),
                        (button) -> editorWidget.fitImage())
                .dimensions(getButtonAreaLeft(), (int) (this.height * 0.1), (int) (this.width * 0.25) + 10, 20)
                .build());

        //eraser button
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("resource_explorer.png_editor.eraser_button"),
                        (button) -> {
                            colorTool.setColor(0);
                            updateSliders();
                        })
                .dimensions(getButtonAreaLeft(), (int) (this.height * 0.2), (int) (this.width * 0.25) + 10, 20)
                .tooltip(Tooltip.of(Text.translatable("resource_explorer.png_editor.eraser_button.tooltip")))
                .build());

        //pick color button
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("resource_explorer.png_editor.pick_button"),
                        (button) -> editorWidget.setCtrl())
                .dimensions(getButtonAreaLeft(), (int) (this.height * 0.3), (int) (this.width * 0.25) + 10, 20)
                .tooltip(Tooltip.of(Text.translatable("resource_explorer.png_editor.pick_button.tooltip")))
                .build());

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

        //init the color history buttons
        var colorDisplayX = getButtonAreaLeft() + (int) (this.width * 0.3);
        int colorDisplayY = (int) (this.height * 0.12);
        for (int i = 0; i < 10; i++) {
            this.addDrawableChild(new ColorHistoryWidget(colorDisplayX, colorDisplayY, colorTool, i));
            colorDisplayY += 16;
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
        }
        return b;
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

        RenderSystem.enableBlend();
        context.fill(colorDisplayX, colorDisplayY, colorDisplayX2, colorDisplayY2,
                colorTool.getColorARGB());
        RenderSystem.disableBlend();
    }


}
