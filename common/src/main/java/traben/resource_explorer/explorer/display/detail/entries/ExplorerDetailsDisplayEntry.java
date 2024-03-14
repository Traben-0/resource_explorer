package traben.resource_explorer.explorer.display.detail.entries;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import traben.resource_explorer.ResourceExplorerClient;

import java.util.ArrayList;
import java.util.List;

public class ExplorerDetailsDisplayEntry extends DisplayEntry {

    private final MultilineText reason;

    private final ButtonWidget logButton;

    public ExplorerDetailsDisplayEntry(String[] reasonSource) {
        List<Text> textsSplitByLine = new ArrayList<>();
        for (String line : reasonSource) {
            textsSplitByLine.add(Text.of(line));
        }
        reason = MultilineText.createFromTexts(MinecraftClient.getInstance().textRenderer, textsSplitByLine);
        logButton = new ButtonWidget.Builder(Text.translatable("resource_explorer.explorer.log_feedback"),
                (button) -> {
                    StringBuilder out = new StringBuilder();
                    for (String line : reasonSource) {
                        out.append("\n").append(line);
                    }
                    ResourceExplorerClient.log(out.toString());
                    button.active = false;
                }
        ).dimensions(0, 0, 150, 20).build();
    }

    @Override
    public String getDisplayName() {
        return Text.translatable("resource_explorer.explorer.feedback.title").getString();
    }

    @Override
    public int getEntryHeight() {
        return reason.count() * 11 + 60;
    }

    @Override
    public int compareTo(@NotNull final DisplayEntry o) {
        return 0;
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (logButton.isHovered() && logButton.active) {
            logButton.onClick((int) mouseX, (int) mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(final DrawContext context, final int index, final int y, final int x, final int entryWidth, final int entryHeight, final int mouseX, final int mouseY, final boolean hovered, final float tickDelta) {
        int offset = drawWidgetOnly(logButton, context, 0, x + 8, y + 8, mouseX, mouseY);
        drawText(Text.translatable("resource_explorer.explorer.feedback.info"), reason, context, offset, x + 8, y + 8);
    }
}
