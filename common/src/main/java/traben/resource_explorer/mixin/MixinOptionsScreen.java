package traben.resource_explorer.mixin;


import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import traben.resource_explorer.gui.REDirectoryScreen;
import traben.resource_explorer.ResourceExplorer;


@Mixin(OptionsScreen.class)
public abstract class MixinOptionsScreen extends Screen {




    @SuppressWarnings("unused")
    protected MixinOptionsScreen(Text title) {
        super(title);
    }



    @Inject(method = "init", at = @At(value = "TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void afterAdder(CallbackInfo ci, GridWidget gridWidget, GridWidget.Adder adder) {


        int x = gridWidget.getX() - 16;
        int y = gridWidget.getY() + 128;

        addDrawableChild(new TexturedButtonWidget(
                x,
                y,
                16,
                16,
                new ButtonTextures(ResourceExplorer.ICON_FOLDER, ResourceExplorer.ICON_FOLDER_OPEN),
                (button) -> {
                    assert this.client != null;
                    this.client.setScreen(new REDirectoryScreen(
                            this,
                            Text.of("Resource Explorer"),
                            ResourceExplorer.getResourceFolderRoot(),
                            "assets/"
                    ));
                },
                Text.of("Open Resource Explorer")){
            //override required because textured button widget just doesnt work
            @Override
            public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
                Identifier identifier = this.isSelected() ? ResourceExplorer.ICON_FOLDER_OPEN : ResourceExplorer.ICON_FOLDER;
                context.drawTexture(identifier, this.getX(), this.getY(),0,0, this.width, this.height,16,16);
            }
        });


    }



}