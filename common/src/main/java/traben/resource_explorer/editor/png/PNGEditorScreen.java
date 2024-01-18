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
import org.jetbrains.annotations.Nullable;
import traben.resource_explorer.explorer.REExplorer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

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

    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);


        context.drawTexture(renderImage.getCurrent(), 0, 0, 0.0F, 0.0F, 32, 32, 32, 32);

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
