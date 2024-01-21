package traben.resource_explorer.editor.png;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;

class ColorHistoryWidget extends ClickableWidget {

    private final ColorTool colorSource;
    private final int index;

    public ColorHistoryWidget(int x, int y, ColorTool colorTool, int index) {
        super(x, y, 10, 10, Text.of(""));
        colorSource = colorTool;
        this.index = index;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        Integer color = colorSource.getFromIndex(index);
        if (color != null) {
            //render outline
            context.fill(getX() - 1, getY() - 1, getX() + width + 1, getY() + height + 1,
                    isHovered() ?
                            ColorHelper.Argb.getArgb(255, 255, 255, 255) :
                            ColorHelper.Argb.getArgb(255, 0, 0, 0));
            //render backfill
            context.fill(getX(), getY(), getX() + width, getY() + height,
                    ColorHelper.Argb.getArgb(255, 0, 0, 0));

            //render color
            RenderSystem.enableBlend();
            context.fill(getX(), getY(), getX() + width, getY() + height, colorSource.getABGRasARGB(color));
            RenderSystem.disableBlend();
        }
    }


    @Override
    public void onClick(double mouseX, double mouseY) {
        colorSource.setFromIndex(index);
        super.onClick(mouseX, mouseY);
    }


    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }
}
