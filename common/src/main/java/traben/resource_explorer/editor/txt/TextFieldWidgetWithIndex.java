package traben.resource_explorer.editor.txt;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

class TextFieldWidgetWithIndex extends TextFieldWidget {
    private final int index;

    private final Text actualLine;
    private final int lineIndex;


    private final int spaceForLineNum;

    public TextFieldWidgetWithIndex(final int index, final int actualLine, final int spaceForLineNum, final int x, final int y, final int width, final int height) {
        super(MinecraftClient.getInstance().textRenderer, x + spaceForLineNum, y, width - spaceForLineNum, height, Text.of(""));
        this.index = index;
        this.actualLine = Text.of(String.valueOf(actualLine + 1));
        this.lineIndex = actualLine;
        this.spaceForLineNum = spaceForLineNum;

        setMaxLength(Integer.MAX_VALUE);
        setDrawsBackground(false);
    }

    public int getActualLineIndex() {
        return lineIndex;
    }

    public int getIndexInDisplayList() {
        return index;
    }

    @Override
    public void renderWidget(final DrawContext context, final int mouseX, final int mouseY, final float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, actualLine, getX() - spaceForLineNum, getY(), -8355712);
    }
}
