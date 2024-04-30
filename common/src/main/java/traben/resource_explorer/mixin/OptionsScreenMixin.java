package traben.resource_explorer.mixin;


import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import traben.resource_explorer.REConfig;
import traben.resource_explorer.explorer.ExplorerUtils;
import traben.resource_explorer.explorer.display.ExplorerScreen;

import static traben.resource_explorer.ResourceExplorerClient.MOD_ID;


@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {


    @SuppressWarnings("unused")
    protected OptionsScreenMixin(Text title) {
        super(title);
    }


    @Unique
    private int re$resourcePackX =0;
    @Unique
    private int re$resourcePackY =0;

    @Inject(method = "init", at = @At(value = "TAIL"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void re$afterAdder(final CallbackInfo ci, final DirectionalLayoutWidget directionalLayoutWidget, final DirectionalLayoutWidget directionalLayoutWidget2, final GridWidget gridWidget, final GridWidget.Adder adder) {
        if (REConfig.getInstance().showResourcePackButton) {
            var test = Text.translatable("options.resourcepack");
            children().forEach((w)->{
                if (w instanceof ButtonWidget button && test.equals(button.getMessage())){
                    this.re$resourcePackX = button.getX();
                    this.re$resourcePackY = button.getY();
//                    System.out.println("found pack button: "+re$resourcePackX+", "+re$resourcePackY);
                }
            });


            //fallbacks
            int x = re$resourcePackX == 0 ? gridWidget.getX() - 16 : re$resourcePackX - 20;
            int y = re$resourcePackY == 0 ? gridWidget.getY() + 96 : re$resourcePackY;
            addDrawableChild(new TexturedButtonWidget(
                    x,
                    y,
                    16,
                    16,
                    new ButtonTextures(ExplorerUtils.ICON_FOLDER, ExplorerUtils.ICON_FOLDER_OPEN),
                    (button) -> {
                        assert this.client != null;
                        this.client.setScreen(new ExplorerScreen(this));
                    },
                    Text.translatable(MOD_ID + ".open_tooltip")) {
                {
                    setTooltip(Tooltip.of(Text.translatable(MOD_ID + ".open_tooltip")));
                }

                //override required because textured button widget just doesnt work
                @Override
                public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
                    Identifier identifier = this.isSelected() ? ExplorerUtils.ICON_FOLDER_OPEN : ExplorerUtils.ICON_FOLDER;
                    context.drawTexture(identifier, this.getX(), this.getY(), 0, 0, this.width, this.height, 16, 16);
                }
            });
        }
    }
}