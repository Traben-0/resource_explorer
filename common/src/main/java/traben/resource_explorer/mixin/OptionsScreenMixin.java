package traben.resource_explorer.mixin;


import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import traben.resource_explorer.REConfig;
import traben.resource_explorer.gui.REDirectoryScreen;
import traben.resource_explorer.ResourceExplorer;

import static traben.resource_explorer.ResourceExplorer.MOD_ID;


@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {

    @SuppressWarnings("unused")
    protected OptionsScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At(value = "TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void afterAdder(CallbackInfo ci, GridWidget gridWidget, GridWidget.Adder adder) {
        if(REConfig.getInstance().showResourcePackButton) {
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
                                this,null,
                                ResourceExplorer.getResourceFolderRoot(),
                                "assets/"
                        ));
                    },
                    Text.translatable(MOD_ID+".open_tooltip")) {
                {
                    setTooltip(Tooltip.of(Text.translatable(MOD_ID+".open_tooltip")));
                }
                //override required because textured button widget just doesnt work
                @Override
                public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
                    Identifier identifier = this.isSelected() ? ResourceExplorer.ICON_FOLDER_OPEN : ResourceExplorer.ICON_FOLDER;
                    context.drawTexture(identifier, this.getX(), this.getY(), 0, 0, this.width, this.height, 16, 16);
                }
            });
        }
    }
}