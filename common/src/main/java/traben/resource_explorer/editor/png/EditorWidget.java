package traben.resource_explorer.editor.png;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.jetbrains.annotations.NotNull;
import traben.resource_explorer.ResourceExplorerClient;
import traben.resource_explorer.explorer.REExplorer;

import java.io.IOException;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.function.Supplier;

class EditorWidget extends ClickableWidget {

    private final ColorTool colorSource;
    private final RollingIdentifier renderImage = new RollingIdentifier();
    private final Supplier<NativeImage> imageSource;
    private final Identifier imageIdentifier;
    private final Stack<ImmutableTriple<Integer, Integer, Integer>> undoHistory = new Stack<>();
    double renderXOffset = 0;
    double renderYOffset = 0;
    private int imageRenderWidth = 0;
    private int imageRenderHeight = 0;
    private int uOffset = 0;
    private int vOffset = 0;
    private boolean overrideCtrl = false;
    private double renderScale = 1.0;
    private int lastClickX = Integer.MAX_VALUE;
    private int lastClickY = Integer.MAX_VALUE;
    @NotNull
    private NativeImage image;
    private int backgroundColor = Colors.BLACK;


    public EditorWidget(ColorTool colorSource, Identifier identifier, final Supplier<NativeImage> supplier) throws IOException {
        super(0, 0, 0, 0, Text.of(""));
        this.colorSource = colorSource;
        this.imageIdentifier = identifier;
        this.imageSource = supplier;

        var image = supplier.get();
        if (image == null) throw new IOException("[PNG Editor] Image could not be loaded: " + identifier);

        this.image = image;
        fitImage();
        updateRenderedImage();
    }

    public Identifier getImageIdentifier() {
        return imageIdentifier;
    }

    void close() {
        image.close();
    }

    void updateRenderedImage() {
        NativeImage closableImage = new NativeImage(image.getWidth(), image.getHeight(), true);
        closableImage.copyFrom(image);

        MinecraftClient.getInstance().getTextureManager().registerTexture(
                renderImage.getNext(), new NativeImageBackedTexture(closableImage));
        renderImage.confirmNext();

        closableImage.close();
    }

    void resetImage() {
        setImageFromSupplier(imageSource);
        clearUndoHistory();
    }

    void clearImage() {
        setImageFromSupplier(() -> ResourceExplorerClient.getEmptyNativeImage(image.getWidth(), image.getHeight()));
        clearUndoHistory();
    }

    void setImageFromSupplier(Supplier<NativeImage> supplier) {
        NativeImage newImage = supplier.get();
        if (newImage == null) {
            ResourceExplorerClient.log("[PNG Editor] Image could not be set: " + imageIdentifier);
        } else {
            var oldImg = image;
            image = newImage;
            oldImg.close();
            updateRenderedImage();
        }
    }

    void saveImage() {
        Util.getIoWorkerExecutor().execute(() ->
            REExplorer.outputResourceToPackInternal(imageIdentifier, (file) -> {
                try {
                    image.writeTo(file);
                    return true;
                } catch (IOException e) {
                    return false;
                }
            }));

    }

    void fitImage() {
        double max = Math.max(image.getWidth(), image.getHeight());
        renderXOffset = 0;
        renderYOffset = 0;
        renderScale = height / max;
    }

    void setCtrl() {
        overrideCtrl = true;
    }

    boolean isCtrl() {
        return overrideCtrl;
    }

    public void setBounds(int size, int x, int y) {
        super.setDimensionsAndPosition(size, size, x, y);
        fitImage();
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // image editor bounds
        context.fill(getX() - 2, getY() - 2, getRight() + 2, getBottom() + 2, Colors.WHITE);
        context.fill(getX(), getY(), getRight(), getBottom(), backgroundColor);

        //image itself render
        renderNonTilingImageInEditor(context);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        //it's an image...
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
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

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount != 0) {
            renderScale *= verticalAmount > 0 ? 1.11111 : 0.9;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!Screen.hasShiftDown()) {
            if (Screen.hasControlDown() || overrideCtrl) {
                return pickPixel(mouseX, mouseY);
            } else {
                return paintPixel(mouseX, mouseY);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        //reset some context values used in drag and pixel picking
        lastClickX = Integer.MAX_VALUE;
        lastClickY = Integer.MAX_VALUE;
        overrideCtrl = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean paintPixel(double mouseX, double mouseY) {
        return pixelAction(mouseX, mouseY, (x, y) -> {
            colorSource.saveColorInHistory();
            int oldColor = image.getColor(x, y);
            if (colorSource.getColorAlpha() == 255 || colorSource.getColorAlpha() == 0) {
                //solid pixel or eraser
                image.setColor(x, y, colorSource.getColor());
            } else {
                //blend
                image.setColor(x, y, colorSource.blendOver(oldColor));
            }
            updateRenderedImage();
            undoHistory.push(ImmutableTriple.of(x, y, oldColor));
            return true;
        }, () -> {
            backgroundColor = colorSource.getColorARGB();
            return true;
        });
    }

    void clearUndoHistory() {
        undoHistory.clear();
    }

    boolean canUndo() {
        return !undoHistory.isEmpty();
    }

    void undoLastPixel() {
        var lastAction = undoHistory.pop();
        try {
            image.setColor(lastAction.left, lastAction.middle, lastAction.right);
            updateRenderedImage();
        } catch (Exception e) {
            //return to stack
            undoHistory.push(lastAction);
            ResourceExplorerClient.log("Undo action failed: " + e.getMessage());
        }
    }

    private boolean pickPixel(double mouseX, double mouseY) {
        return pixelAction(mouseX, mouseY, (x, y) -> {
            colorSource.saveColorInHistory();
            colorSource.setColor(image.getColor(x, y));
            //flatten transparency to black transparency, good habit for png compression
            if (colorSource.getColorAlpha() == 0) colorSource.setColor(0);
            return true;
        }, () -> {
            colorSource.setColor(backgroundColor);
            //flip rb
            colorSource.setColor(colorSource.getColorARGB());
            return true;
        });
    }

    private boolean pixelAction(double mouseX, double mouseY, BiFunction<Integer, Integer,
            Boolean> insideImageAction, Supplier<Boolean> backgroundAction) {
        int imageX = getInImageXOfMouseX(mouseX);
        int imageY = getInImageYOfMouseY(mouseY);

        //cancel repeated drag clicks on the same pixel
        if (imageX == lastClickX && imageY == lastClickY) return false;
        lastClickX = imageX;
        lastClickY = imageY;

        try {
            if (imageX < image.getWidth() && imageY < image.getHeight() && imageX >= 0 && imageY >= 0) {
                return insideImageAction.apply(imageX, imageY);
            } else if(isHovered()){
                return backgroundAction.get();
            }
        } catch (Exception e) {
            ResourceExplorerClient.log(e.getMessage());
            return false;
        }
        return false;
    }

    private int getInImageXOfMouseX(double mouseX) {
        var fromEditorLeft = mouseX - getX();
        var fromImageLeft = fromEditorLeft + uOffset;
        var asWidthPercentage = fromImageLeft / imageRenderWidth;
        var result = asWidthPercentage * image.getWidth();
        return (int) (result < 0 ? result - 1 : result);
    }

    private int getInImageYOfMouseY(double mouseY) {
        var fromEditorTop = mouseY - getY();
        var fromImageTop = fromEditorTop + vOffset;
        var asHeightPercentage = fromImageTop / imageRenderHeight;
        var result = asHeightPercentage * image.getHeight();
        return (int) (result < 0 ? result - 1 : result);
    }

    private void renderNonTilingImageInEditor(DrawContext context) {
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

            //handle top left render bound behaviour
            //image bounds start
            int imageBoxX = MathHelper.clamp((getX() - uOffset), getX(), getRight());
            int imageBoxY = MathHelper.clamp((getY() - vOffset), getY(), getBottom());
            //uv start
            int imageU2 = Math.max(uOffset, 0);
            int imageV2 = Math.max(vOffset, 0);

            //handle bottom right render bound behaviour
            int renderWidthMax = MathHelper.clamp(width + uOffset, 0, width);
            int renderHeightMax = MathHelper.clamp(height + vOffset, 0, height);
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
                    getX() + 6, getY() + 6, 16777215);
        }
    }
}
