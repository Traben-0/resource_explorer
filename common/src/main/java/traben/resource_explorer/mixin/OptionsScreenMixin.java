package traben.resource_explorer.mixin;


import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.resource_explorer.REConfig;
import traben.resource_explorer.explorer.ExplorerUtils;
import traben.resource_explorer.explorer.display.ExplorerScreen;

import java.util.concurrent.atomic.AtomicIntegerArray;

import static traben.resource_explorer.ResourceExplorerClient.MOD_ID;


@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {


    @Shadow
    @Final
    private ThreePartsLayoutWidget layout;

    @SuppressWarnings("unused")
    protected OptionsScreenMixin(Text title) {
        super(title);
    }


    @Unique
    private int re$resourcePackX = 0;
    @Unique
    private int re$resourcePackY = 0;

    @Unique
    private TexturedButtonWidget re$resourcePackButton = null;

    @Inject(method = "refreshWidgetPositions", at = @At(value = "TAIL"))
    private void re$afterAdder(final CallbackInfo ci) {
        if (REConfig.getInstance().showResourcePackButton) {
            var test = Text.translatable("options.resourcepack");
            children().forEach((w)->{
                if (w instanceof ButtonWidget button && test.equals(button.getMessage())){
                    this.re$resourcePackX = button.getX();
                    this.re$resourcePackY = button.getY();
                }
            });

            AtomicIntegerArray gridWidget = new AtomicIntegerArray(2);
            layout.forEachElement(element->{
                if(element instanceof GridWidget grid){
                    gridWidget.set(0, grid.getX());
                    gridWidget.set(1, grid.getY());
                }
            });

            int x;
            int y;

            if (gridWidget.get(0) != 0 || gridWidget.get(1) != 0) {
                x = re$resourcePackX == 0 ? gridWidget.get(0) - 16 : re$resourcePackX - 20;
                y = re$resourcePackY == 0 ? gridWidget.get(1) + 96 : re$resourcePackY;
            } else {
                x = 4;
                y = 4;
            }

            if (re$resourcePackButton != null) {
                re$resourcePackButton.setX(x);
                re$resourcePackButton.setY(y);
            } else {
                re$resourcePackButton = addDrawableChild(new TexturedButtonWidget(
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
                        context.drawTexture(RenderLayer::getGuiTextured, identifier, this.getX(), this.getY(), 0, 0, this.width, this.height, 16, 16);
                    }
                });
            }
        }
    }
}