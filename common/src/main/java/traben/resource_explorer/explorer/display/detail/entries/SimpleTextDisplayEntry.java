package traben.resource_explorer.explorer.display.detail.entries;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SimpleTextDisplayEntry extends DisplayEntry {

    private final MultilineText text;
    private final String title;

    public static final SimpleTextDisplayEntry exportWaitMessage = new SimpleTextDisplayEntry(
            Text.translatable("resource_explorer.export.wait.title").getString(),
            Text.translatable("resource_explorer.export.wait.1").getString(),
            Text.translatable("resource_explorer.export.wait.2").getString(),
            Text.translatable("resource_explorer.export.wait.3").getString());

    public SimpleTextDisplayEntry(String title, String... reasonSource) {
        List<Text> textsSplitByLine = new ArrayList<>();
        for (String line : reasonSource) {
            textsSplitByLine.add(Text.of(line));
        }
        this.title = title;
        text = MultilineText.create(MinecraftClient.getInstance().textRenderer, textsSplitByLine.toArray(new Text[0]));
    }

    @Override
    public String getDisplayName() {
        return title;
    }

    @Override
    public int getEntryHeight() {
        return text.count() * 11 + 60;
    }

    @Override
    public int compareTo(@NotNull final DisplayEntry o) {
        return 0;
    }


    @Override
    public void render(final DrawContext context, final int index, final int y, final int x, final int entryWidth, final int entryHeight, final int mouseX, final int mouseY, final boolean hovered, final float tickDelta) {
        drawText(Text.translatable("resource_explorer.explorer.feedback.info"), text, context, 0, x + 8, y + 8);
    }

}
