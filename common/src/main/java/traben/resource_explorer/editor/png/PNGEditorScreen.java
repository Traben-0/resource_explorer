package traben.resource_explorer.editor.png;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.Resource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import traben.resource_explorer.explorer.REExplorer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Random;

import static traben.resource_explorer.ResourceExplorerClient.MOD_ID;

public class PNGEditorScreen extends Screen {
    private final Screen parent;

    private final Identifier imageIdentifier;
    private final NativeImage image;

    private final RollingIdentifier renderImage;


    public PNGEditorScreen(final Screen parent, final Identifier pngToEdit, final Resource pngResource) throws IOException, NullPointerException {
        super(Text.translatable(MOD_ID + ".png_editor.title"));
        this.parent = parent;

        if (pngToEdit == null) throw new NullPointerException("[PNG Editor] Identifier was null");
        this.imageIdentifier = pngToEdit;

        if (pngResource == null) throw new NullPointerException("[PNG Editor] Resource was null");
        image = initImage(pngResource);

        if (image == null) throw new IOException("[PNG Editor] Image could not be loaded: " + imageIdentifier);


        renderImage = new RollingIdentifier();
        updateRenderedImage();

    }

    @Override
    public void close() {
        if(image!= null) image.close();
        super.close();
    }

    @Override
    protected void init() {
        super.init();
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.back"),
                        (button) -> Objects.requireNonNull(client).setScreen(parent))
                .dimensions((int) (this.width * 0.1), (int) (this.height * 0.9), (int) (this.width * 0.2), 20)
                .build());
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.done"),
                        (button) -> {
                            System.out.println("saved image = " + saveImage());
                            Objects.requireNonNull(client).setScreen(parent);
                        })
                .dimensions((int) (this.width * 0.7), (int) (this.height * 0.9), (int) (this.width * 0.2), 20)
                .build());
        setEditorValues();
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        //increments one direction at a time by 0.5
        if (isMouseOverEditor(mouseX, mouseY)) {
            if (Screen.hasShiftDown()) {
                //System.out.println("drag=" + deltaX + ", " + deltaY);
                renderXOffset -= deltaX / renderScale;
                renderYOffset -= deltaY / renderScale;

//                renderXOffset = renderXOffset % image.getWidth();
//                renderYOffset = renderYOffset % image.getHeight();

                System.out.println("drag="+renderXOffset+", "+renderYOffset);
                return true;
            } else {
                return paintPixel(mouseX, mouseY);
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    double renderXOffset = 0;
    double renderYOffset = 0;

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        //scrolls by 1.0      positive direction is in
        if (verticalAmount != 0 && isMouseOverEditor(mouseX, mouseY)) {
            //System.out.println("scroll=" + verticalAmount);

            //scale
            double scaleChange = verticalAmount > 0 ? 1.111111111 : 0.9;
            renderScale *= scaleChange;

        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private double renderScale = 1.0;

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        //test editor basic functionality

        if (!Screen.hasShiftDown() && isMouseOverEditor(mouseX, mouseY)) {
            return paintPixel(mouseX, mouseY);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private int lastClickX = Integer.MAX_VALUE;
    private int lastClickY = Integer.MAX_VALUE;

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        lastClickX = Integer.MAX_VALUE;
        lastClickY = Integer.MAX_VALUE;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean paintPixel(double mouseX, double mouseY) {
        int imageX = getInImageXOfMouseX(mouseX);
        int imageY = getInImageYOfMouseY(mouseY);

        //cancel repeated drag clicks
        if(imageX == lastClickX && imageY == lastClickY) return false;

        lastClickX = imageX;
        lastClickY = imageY;

        System.out.println("try click x,y=" + imageX + ", " + imageY);
        if (imageX != Integer.MAX_VALUE
                && imageY != Integer.MAX_VALUE) {
            try {
                //int imageXWrap = imageX % image.getWidth();
                //int imageYWrap = imageY % image.getHeight();
               // System.out.println("try wrap x,y=" + imageXWrap + ", " + imageYWrap);

                //int setX = imageXWrap >= 0 ? imageXWrap : image.getWidth() + imageXWrap;
                //int setY = imageYWrap >= 0 ? imageYWrap : image.getHeight() + imageYWrap;
                //System.out.println("try set x,y=" + setX + ", " + setY);

                if (imageX < image.getWidth() && imageY < image.getHeight() && imageX >= 0 && imageY >= 0) {
                    Random rand = new Random();
                    image.setColor(imageX, imageY,
                            ColorHelper.Argb.getArgb(255,
                                    rand.nextInt(255),
                                    rand.nextInt(255),
                                    rand.nextInt(255)));
                    updateRenderedImage();
                    return true;
                }
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
            System.out.println("image click failed.");
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






    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        setEditorValues();
    }

    private void setEditorValues() {
        int editorSquareMeasure = Math.min((int) (width * 0.7), (int) (height * 0.7));

        fitImage(editorSquareMeasure);

        editorLeft = (int) (width * 0.1);
        editorTop = (int) (height * 0.1);
        editorRight = editorLeft + editorSquareMeasure;
        editorBottom = editorTop + editorSquareMeasure;
    }

    private void fitImage(int editorSquare) {
        double max = Math.max(image.getWidth(), image.getHeight());
        uOffset = 0;
        vOffset = 0;
        renderScale = editorSquare / max;
    }

    private int editorLeft = 0;
    private int editorTop = 0;
    private int editorRight = 0;
    private int editorBottom = 0;

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


    private int imageRenderWidth = 0;
    private int imageRenderHeight = 0;

    private int uOffset = 0;
    private int vOffset = 0;

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackgroundTexture(context);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        //image scaling
        imageRenderWidth = (int) (image.getWidth() * renderScale);
        imageRenderHeight = (int) (image.getHeight() * renderScale);

        //image offsets
        uOffset = (int) (renderXOffset * renderScale);
        vOffset = (int) (renderYOffset * renderScale);


        // image editor render
        context.fill(editorLeft - 2, editorTop - 2, editorRight + 2, editorBottom + 2,
                ColorHelper.Argb.getArgb(255, 255, 255, 255));
        context.fill(editorLeft, editorTop, editorRight, editorBottom,
                ColorHelper.Argb.getArgb(255, 0, 0, 0)/*-16777216*/);

        //image itself render
        try {
//            context.drawTexture(renderImage.getCurrent(),
//                    editorLeft, editorTop, uOffset, vOffset,
//                    editorRight - editorLeft, editorBottom - editorTop, imageRenderWidth, imageRenderHeight);


            int imageBoxX = editorLeft;
            int imageBoxY =editorTop;

            int imageBoxWidth =editorRight - editorLeft;
            int imageBoxHeight =editorBottom - editorTop;
            int imageBoxWidth2 = MathHelper.clamp((int) ((image.getWidth() - renderXOffset) * renderScale),0,imageBoxWidth);
            int imageBoxHeight2 = MathHelper.clamp((int) ((image.getHeight() - renderYOffset) * renderScale),0,imageBoxHeight);

            int imageU2 = uOffset;//Math.min(uOffset, 0);
            int imageV2 = vOffset;//Math.min(vOffset, 0);

            int imageRenderWidth2 = imageRenderWidth;
            int imageRenderHeight2 = imageRenderHeight;


            context.drawTexture(renderImage.getCurrent(),
                    imageBoxX, imageBoxY, imageU2, imageV2,
                    imageBoxWidth2, imageBoxHeight2, imageRenderWidth2, imageRenderHeight2);

        }catch(Exception e){
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.of("image broken..."),
                    editorLeft+6, editorTop+6, 16777215);
        }




//        context.drawTexture(getIcon(hovered), x, y, 0.0F, 0.0F, 32, 32, 32, 32);
//        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, orderedText, x + 32 + 2, y + 1, 16777215);
//        multilineText.drawWithShadow(context, x + 32 + 2, y + 12, 10, -8355712);

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
        NativeImage closableImage = new NativeImage(image.getWidth(),image.getHeight(),true);
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


    private static class RollingIdentifier {

        private Identifier current = new Identifier("resource_explorer", "png_editor/" + System.currentTimeMillis());
        private Identifier next = null;

        Identifier getCurrent() {
            return current;
        }

        Identifier getNext() {
            next = new Identifier("resource_explorer", "png_editor/" + System.currentTimeMillis());
            while(next.equals(current)){
                next = new Identifier("resource_explorer", "png_editor/" + System.currentTimeMillis()+"/"+new Random().nextInt());
            }
            return next;
        }

        void confirmNext(){
            current = next == null? getNext() : next;
        }

    }

    private enum PointerIcon{
        NONE(new Identifier("a")),
        BRUSH(new Identifier("b")),
        HAND_MOVE(new Identifier("c")),
        ERASER(new Identifier("d"));

        final Identifier identifier;

        PointerIcon(Identifier identifier){
            this.identifier = identifier;
        }

        private static PointerIcon current= NONE;

        static PointerIcon getCurrent() {
            return current;
        }

        static void setCurrent(PointerIcon newCurrent) {
            current = Objects.requireNonNullElse(newCurrent, NONE);
        }
    }
}
