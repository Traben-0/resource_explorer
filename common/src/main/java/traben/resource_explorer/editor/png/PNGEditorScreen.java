package traben.resource_explorer.editor.png;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.Resource;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import traben.resource_explorer.ResourceExplorerClient;
import traben.resource_explorer.explorer.REExplorer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.function.BiFunction;

import static traben.resource_explorer.ResourceExplorerClient.MOD_ID;

public class PNGEditorScreen extends Screen {
    private final Screen parent;
    private final Identifier imageIdentifier;
    private NativeImage image;
    private final RollingIdentifier renderImage;
    double renderXOffset = 0;
    double renderYOffset = 0;
    private boolean overrideCtrl = false;
    private double renderScale = 1.0;
    private int lastClickX = Integer.MAX_VALUE;
    private int lastClickY = Integer.MAX_VALUE;
    private final ColorTool colorTool = new ColorTool();
    private final ColorSliderWidget greenSlider = new ColorSliderWidget(Text.of("green: "),
            (value) -> colorTool.setColorGreen((int) (value * 255)));
    private final ColorSliderWidget redSlider = new ColorSliderWidget(Text.of("red: "),
            (value) -> colorTool.setColorRed((int) (value * 255)));
    private final ColorSliderWidget blueSlider = new ColorSliderWidget(Text.of("blue: "),
            (value) -> colorTool.setColorBlue((int) (value * 255)));
    private final ColorSliderWidget alphaSlider = new ColorSliderWidget(Text.of("alpha: "),
            (value) -> colorTool.setColorAlpha((int) (value * 255)));
    private int editorLeft = 0;
    private int editorTop = 0;
    private int editorRight = 0;
    private int editorBottom = 0;
    private int imageRenderWidth = 0;
    private int imageRenderHeight = 0;
    private int uOffset = 0;
    private int vOffset = 0;

    private final Resource imageSource;

    public PNGEditorScreen(final Screen parent, final Identifier pngToEdit, final Resource pngResource) throws IOException, NullPointerException {
        super(Text.translatable(MOD_ID + ".png_editor.title"));
        this.parent = parent;
        imageSource = pngResource;

        if (pngToEdit == null) throw new NullPointerException("[PNG Editor] Identifier was null");
        this.imageIdentifier = pngToEdit;

        if (imageSource == null) throw new NullPointerException("[PNG Editor] Resource was null");
        image = initImage(imageSource);

        if (image == null) throw new IOException("[PNG Editor] Image could not be loaded: " + imageIdentifier);

        renderImage = new RollingIdentifier();
        updateRenderedImage();
    }

    @Override
    public void close() {
        if (image != null) image.close();
        super.close();
    }

    @Override
    protected void init() {
        super.init();
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("resource_explorer.png_editor.close"),
                        (button) -> Objects.requireNonNull(client).setScreen(parent))
                .dimensions((int) (this.width * 0.1), (int) (this.height * 0.9), (int) (this.width * 0.2), 20)
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("resource_explorer.png_editor.reset"),
                        (button) -> {
                            NativeImage newImage = initImage(imageSource);
                            if (newImage == null){
                                ResourceExplorerClient.log("[PNG Editor] Image could not be reset: " + imageIdentifier);
                            }else{
                                var oldImg = image;
                                image = newImage;
                                oldImg.close();
                                updateRenderedImage();
                            }
                        })
                .dimensions((int) (this.width * 0.1), (int) (this.height * 0.9), (int) (this.width * 0.2), 20)
                .build());
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("resource_explorer.png_editor.export_button"),
                        (button) -> {
                            ResourceExplorerClient.log("saved image = " + saveImage());
                            Objects.requireNonNull(client).setScreen(parent);
                        })
                .dimensions((int) (this.width * 0.6), (int) (this.height * 0.9), (int) (this.width * 0.3), 20)
                .tooltip(Tooltip.of(Text.translatable("resource_explorer.png_editor.export_button.tooltip")))
                .build());

        //init editor positions
        setEditorValues();

        //fit image button
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("resource_explorer.png_editor.center_button"),
                        (button) -> fitImage())
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
                        (button) -> overrideCtrl = true)
                .dimensions(getButtonAreaLeft(), (int) (this.height * 0.3), (int) (this.width * 0.25) + 10, 20)
                .tooltip(Tooltip.of(Text.translatable("resource_explorer.png_editor.pick_button.tooltip")))
                .build());

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

        var colorDisplayX = getButtonAreaLeft() + (int) (this.width * 0.3);
        int colorDisplayY = (int) (this.height * 0.12);
        for (int i = 0; i < 10; i++) {
            this.addDrawableChild(new ColorHistoryWidget(colorDisplayX, colorDisplayY, colorTool, i));
            colorDisplayY += 16;
        }

    }

    private void updateSliders() {
        redSlider.setValue255(colorTool.getColorRed());
        greenSlider.setValue255(colorTool.getColorGreen());
        blueSlider.setValue255(colorTool.getColorBlue());
        alphaSlider.setValue255(colorTool.getColorAlpha());
    }

    private int getButtonAreaLeft(){
        return editorRight + (int) (this.width * 0.05);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        //increments one direction at a time by 0.5
        if (isMouseOverEditor(mouseX, mouseY)) {
            if (Screen.hasShiftDown()) {
                renderXOffset -= deltaX / renderScale;
                renderYOffset -= deltaY / renderScale;
                return true;
            } else {
                if (Screen.hasControlDown() || overrideCtrl) {
                    return pickPixel(mouseX, mouseY);
                } else {
                    return paintPixel(mouseX, mouseY);
                }
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        //scrolls by 1.0      positive direction is in
        if (verticalAmount != 0 && isMouseOverEditor(mouseX, mouseY)) {
            //scale
            double scaleChange = verticalAmount > 0 ? 1.111111111 : 0.9;
            renderScale *= scaleChange;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        //test editor basic functionality

        if (isMouseOverEditor(mouseX, mouseY)) {
            if (!Screen.hasShiftDown()) {
                if (Screen.hasControlDown() || overrideCtrl) {
                    return pickPixel(mouseX, mouseY);
                } else {
                    return paintPixel(mouseX, mouseY);
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        lastClickX = Integer.MAX_VALUE;
        lastClickY = Integer.MAX_VALUE;
        if (isMouseOverEditor(mouseX, mouseY))
            overrideCtrl = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean paintPixel(double mouseX, double mouseY) {
        return pixelAction(mouseX, mouseY, (x, y) -> {
            colorTool.saveColorInHistory();
            image.setColor(x, y, colorTool.getColor());
            updateRenderedImage();
            return true;
        });
    }

    private boolean pickPixel(double mouseX, double mouseY) {
        return pixelAction(mouseX, mouseY, (x, y) -> {
            colorTool.saveColorInHistory();
            colorTool.setColor( image.getColor(x, y));
            //flatten transparency to black transparency
            if (colorTool.getColorAlpha() == 0) colorTool.setColor(0);
            updateSliders();
            return true;
        });
    }

    private boolean pixelAction(double mouseX, double mouseY, BiFunction<Integer, Integer, Boolean> insideImageAction) {
        int imageX = getInImageXOfMouseX(mouseX);
        int imageY = getInImageYOfMouseY(mouseY);

        //cancel repeated drag clicks
        if (imageX == lastClickX && imageY == lastClickY) return false;
        lastClickX = imageX;
        lastClickY = imageY;
        if (imageX != Integer.MAX_VALUE
                && imageY != Integer.MAX_VALUE) {
            try {
                if (imageX < image.getWidth() && imageY < image.getHeight() && imageX >= 0 && imageY >= 0) {
                    return insideImageAction.apply(imageX, imageY);
                } else {
                    return false;
                }
            } catch (Exception e) {
                ResourceExplorerClient.log(e.getMessage());
            }
        }
        return false;
    }

    private boolean isMouseOverEditor(double mouseX, double mouseY) {
        return isMouseXOverEditor(mouseX) && isMouseYOverEditor(mouseY);
    }

    private boolean isMouseXOverEditor(double mouseX) {
        return mouseX > editorLeft && mouseX < editorRight;
    }

    private boolean isMouseYOverEditor(double mouseY) {
        return mouseY > editorTop && mouseY < editorBottom;
    }

    private void setEditorValues() {
        int editorSquareMeasure = editorSquareMeasure();

        fitImage(editorSquareMeasure);

        editorLeft = (int) (width * 0.1);
        editorTop = (int) (height * 0.1);
        editorRight = editorLeft + editorSquareMeasure;
        editorBottom = editorTop + editorSquareMeasure;
    }

    private int editorSquareMeasure() {
        return Math.min((int) (width * 0.7), (int) (height * 0.7));
    }

    private void fitImage() {
        fitImage(editorSquareMeasure());
    }

    private void fitImage(int editorSquare) {
        double max = Math.max(image.getWidth(), image.getHeight());
        renderXOffset = 0;
        renderYOffset = 0;
        renderScale = editorSquare / max;
    }

    private int getInImageXOfMouseX(double mouseX) {
        if (isMouseXOverEditor(mouseX)) {
            var fromEditorLeft = mouseX - editorLeft;
            var fromImageLeft = fromEditorLeft + uOffset;
            var asWidthPercentage = fromImageLeft / imageRenderWidth;
            //if(asWidthPercentage < 1 && asWidthPercentage >= 0){
            var result = asWidthPercentage * image.getWidth();
            return (int) (result < 0 ? result - 1 : result);
            //}
        }
        return Integer.MAX_VALUE;
    }

    private int getInImageYOfMouseY(double mouseY) {
        if (isMouseYOverEditor(mouseY)) {
            var fromEditorTop = mouseY - editorTop;
            var fromImageTop = fromEditorTop + vOffset;
            var asHeightPercentage = fromImageTop / imageRenderHeight;
//            if(asHeightPercentage < 1 && asHeightPercentage >= 0){
            var result = asHeightPercentage * image.getHeight();
            return (int) (result < 0 ? result - 1 : result);
//            }
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackgroundTexture(context);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 8, 16777215);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.of(imageIdentifier.toString()), this.width / 2, editorBottom+6, Colors.GRAY);


        // image editor bounds
        context.fill(editorLeft - 2, editorTop - 2, editorRight + 2, editorBottom + 2,
                ColorHelper.Argb.getArgb(255, 255, 255, 255));
        context.fill(editorLeft, editorTop, editorRight, editorBottom,
                ColorHelper.Argb.getArgb(255, 0, 0, 0)/*-16777216*/);

        //image itself render
        renderImageInEditor(context);

        //color display
        renderCurrentColorDisplay(context);

        if (isMouseOverEditor(mouseX, mouseY)) {
            PointerIcon pointer;
            if (Screen.hasShiftDown()) {
                pointer = PointerIcon.HAND_MOVE;
            } else if (Screen.hasControlDown() || overrideCtrl) {
                pointer = PointerIcon.PICK;
            } else if (colorTool.getColorAlpha() == 0) {
                pointer = PointerIcon.ERASER;
            } else {
                pointer = PointerIcon.BRUSH;
            }
            pointer.render(context, mouseX, mouseY, colorTool.getColor());
        }


//        context.drawTexture(getIcon(hovered), x, y, 0.0F, 0.0F, 32, 32, 32, 32);
//        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, orderedText, x + 32 + 2, y + 1, 16777215);
//        multilineText.drawWithShadow(context, x + 32 + 2, y + 12, 10, -8355712);

    }



    private void renderImageInEditor(DrawContext context){
        try {
            //infinite tiling render
//            context.drawTexture(renderImage.getCurrent(),
//                    editorLeft, editorTop, uOffset, vOffset,
//                    editorRight - editorLeft, editorBottom - editorTop, imageRenderWidth, imageRenderHeight);
            //image scaling
            imageRenderWidth = (int) (image.getWidth() * renderScale);
            imageRenderHeight = (int) (image.getHeight() * renderScale);

            //image offsets
            uOffset = (int) (renderXOffset * renderScale);
            vOffset = (int) (renderYOffset * renderScale);

            //bounds of display area
            int imageBoxWidth = editorRight - editorLeft;
            int imageBoxHeight = editorBottom - editorTop;

            //handle top left bound behaviour
            //image bounds start
            int imageBoxX = MathHelper.clamp((editorLeft - uOffset), editorLeft, editorRight);
            int imageBoxY = MathHelper.clamp((editorTop - vOffset), editorTop, editorBottom);
            //uv start
            int imageU2 = Math.max(uOffset, 0);
            int imageV2 = Math.max(vOffset, 0);

            //handle bottom right bound behaviour
            int renderWidthMax = MathHelper.clamp(imageBoxWidth + uOffset, 0, imageBoxWidth);
            int renderHeightMax = MathHelper.clamp(imageBoxHeight + vOffset, 0, imageBoxHeight);
            int imageWidth2 = MathHelper.clamp(imageRenderWidth - imageU2, 0, renderWidthMax);
            int imageHeight2 = MathHelper.clamp(imageRenderHeight - imageV2, 0, renderHeightMax);

            //allow transparency then render
            RenderSystem.enableBlend();
            context.drawTexture(renderImage.getCurrent(),
                    imageBoxX, imageBoxY, imageU2, imageV2,
                    imageWidth2, imageHeight2, imageRenderWidth, imageRenderHeight);
            RenderSystem.disableBlend();

        } catch (Exception e) {
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.of("image broken..."),
                    editorLeft + 6, editorTop + 6, 16777215);
        }
    }

    private void renderCurrentColorDisplay(DrawContext context){
        var colorDisplayX = getButtonAreaLeft() + (int) (this.width * 0.225);
        var colorDisplayY = (int) (this.height * 0.4);
        var colorDisplayX2 = colorDisplayX + 10;
        var colorDisplayY2 = colorDisplayY + (int) (this.height * 0.3) + 20;

        context.fill(colorDisplayX - 1, colorDisplayY - 1, colorDisplayX2 + 1, colorDisplayY2 + 1,
                ColorHelper.Argb.getArgb(255, 255, 255, 255));
        context.fill(colorDisplayX, colorDisplayY, colorDisplayX2, colorDisplayY2,
                colorTool.getColorARGB());
    }



    @Nullable
    public NativeImage initImage(final Resource pngResource) {
        NativeImage img;
        try {
            InputStream in = pngResource.getInputStream();
            try {
                img = NativeImage.read(in);
                in.close();
                return img;
            } catch (Exception e) {
                in.close();
                e.printStackTrace();
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updateRenderedImage() {
        NativeImage closableImage = new NativeImage(image.getWidth(), image.getHeight(), true);
        closableImage.copyFrom(image);

        MinecraftClient.getInstance().getTextureManager().registerTexture(
                renderImage.getNext(), new NativeImageBackedTexture(closableImage));
        renderImage.confirmNext();

        closableImage.close();
    }

    private boolean saveImage() {
        return REExplorer.outputResourceToPackInternal(imageIdentifier, (file) -> {
            try {
                image.writeTo(file);
                return true;
            } catch (IOException e) {
                return false;
            }
        });
    }


}
