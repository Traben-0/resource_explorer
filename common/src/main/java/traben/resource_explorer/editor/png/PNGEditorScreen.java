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
    protected void init() {
        super.init();
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.back"),
                        (button) -> Objects.requireNonNull(client).setScreen(parent))
                .dimensions((int) (this.width * 0.1), (int) (this.height * 0.9), (int) (this.width * 0.2), 20)
                .build());
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.done"),
                        (button) ->{
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

        if(isMouseOverEditor(mouseX, mouseY)) {
            System.out.println("drag=" + deltaX + ", " + deltaY);
            renderXOffset -= deltaX / renderScale;
            renderYOffset -= deltaY / renderScale;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    double renderXOffset = 0;
    double renderYOffset = 0;

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        //scrolls by 1.0      positive direction is in
        if(verticalAmount != 0 && isMouseOverEditor(mouseX, mouseY)){
            System.out.println("scroll=" + verticalAmount);
            renderScale *= verticalAmount > 0 ? 1.1 : 0.9;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private double renderScale = 1.0;

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        //test editor basic functionality
        lastClickX = mouseX;
        lastClickY = mouseY;
        return super.mouseClicked(mouseX,mouseY,button);
    }

    private double lastClickX = -1;
    private double lastClickY = -1;

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(mouseX == lastClickX && mouseY == lastClickY && isMouseOverEditor(mouseX, mouseY)) {
            //then we have clicked and released meaning we want to draw
            var imageX = getInImageXOfMouseX(mouseX);
            var imageY = getInImageYOfMouseY(mouseY);
            System.out.println("try click x,y=" + imageX+", "+imageY);
            if(imageX != Integer.MAX_VALUE && imageY != Integer.MAX_VALUE) {

                int imageXWrap = imageX % image.getWidth();
                int imageYWrap = imageY % image.getHeight();
                System.out.println("try wrap x,y=" + imageXWrap+", "+imageYWrap);

                int setX = imageXWrap >= 0 ? imageXWrap : image.getWidth()+ imageXWrap;
                int setY = imageYWrap >= 0 ? imageYWrap : image.getHeight()+ imageYWrap;
                System.out.println("try set x,y=" + setX+", "+setY);

                if (setX < image.getWidth() && setY < image.getHeight() && setX >= 0 && setY >= 0) {
                    Random rand = new Random();
                    image.setColor(setX, setY,
                            ColorHelper.Argb.getArgb(255,
                                    rand.nextInt(255),
                                    rand.nextInt(255),
                                    rand.nextInt(255)));
                    updateRenderedImage();
                    return true;
                }
                System.out.println("image click failed with x,y=" + setX+", "+setY);
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean isMouseOverEditor(double mouseX, double mouseY){
        return isMouseXOverEditor(mouseX) && isMouseYOverEditor(mouseY);
    }

    private boolean isMouseXOverEditor(double mouseX){
        return mouseX > editorLeft && mouseX < editorRight;
    }

    private boolean isMouseYOverEditor( double mouseY){
        return mouseY > editorTop && mouseY < editorBottom;
    }


    private boolean isShiftHeld = false;
    private boolean isCtrlHeld = false;

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        //lshift = 42?
        //rshift = 54?
        //lctrl  = 29?
        //rctrl  = 157?
        //KeyBinding?
        if(keyCode == 42 || keyCode == 54) isShiftHeld = true;
        if(keyCode == 29 || keyCode == 157) isCtrlHeld = true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if(keyCode == 42 || keyCode == 54) isShiftHeld = false;
        if(keyCode == 29 || keyCode == 157) isCtrlHeld = false;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        setEditorValues();
    }
    private void setEditorValues(){
        int editorSquareMeasure = Math.min((int) (width *0.7),(int) (height *0.7));

        fitImage(editorSquareMeasure);

        editorLeft = (int) (width *0.1);
        editorTop = (int) (height *0.1);
        editorRight = editorLeft + editorSquareMeasure;
        editorBottom = editorTop + editorSquareMeasure;
    }

    private void fitImage(int editorSquare){
        double max = Math.max(image.getWidth(),image.getHeight());
        uOffset=0;
        vOffset=0;
        renderScale = editorSquare / max;
    }

    private int editorLeft = 0;
    private int editorTop = 0;
    private int editorRight = 0;
    private int editorBottom = 0;

    private int getInImageXOfMouseX(double mouseX){
        if(isMouseXOverEditor(mouseX)){
            var fromEditorLeft = mouseX - editorLeft;
            var fromImageLeft = fromEditorLeft + uOffset;
            var asWidthPercentage = fromImageLeft / imageRenderWidth;
            //if(asWidthPercentage < 1 && asWidthPercentage >= 0){
            var result = asWidthPercentage * image.getWidth();
            return (int) (result < 0 ? result -1 : result);
            //}
        }
        return Integer.MAX_VALUE;
    }

    private int getInImageYOfMouseY(double mouseY){
        if(isMouseYOverEditor(mouseY)){
            var fromEditorTop = mouseY - editorTop;
            var fromImageTop = fromEditorTop + vOffset;
            var asHeightPercentage = fromImageTop / imageRenderHeight;
//            if(asHeightPercentage < 1 && asHeightPercentage >= 0){
            var result = asHeightPercentage * image.getHeight();
            return (int) (result < 0 ? result -1 : result);
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
        context.fill(editorLeft-2,editorTop-2,editorRight+2,editorBottom+2,
                ColorHelper.Argb.getArgb(255, 255, 255, 255));
        context.fill(editorLeft,editorTop,editorRight,editorBottom,
                ColorHelper.Argb.getArgb(255, 0, 0, 0)/*-16777216*/);

        //image itself render
        context.drawTexture(renderImage.getCurrent(),
                editorLeft,editorTop, uOffset, vOffset,
                editorRight - editorLeft,editorBottom - editorTop, imageRenderWidth, imageRenderHeight);



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
        MinecraftClient.getInstance().getTextureManager().registerTexture(
                renderImage.newCurrent(),
                new NativeImageBackedTexture(image));
    }

    private boolean saveImage(){
        return REExplorer.outputResourceToPackInternal(imageIdentifier,(file)->{
            try {
                image.writeTo(file);
                return true;
            } catch (IOException e) {
                return false;
            }
        });
    }


    private static class RollingIdentifier {

        private Identifier current = null;

        Identifier getCurrent() {
            return current == null ? newCurrent() : current;
        }

        Identifier newCurrent() {
            current = new Identifier("resource_explorer", "png_editor/" + System.currentTimeMillis());
            return current;
        }

    }
}
