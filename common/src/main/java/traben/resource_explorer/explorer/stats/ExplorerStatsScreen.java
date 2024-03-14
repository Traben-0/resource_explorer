package traben.resource_explorer.explorer.stats;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static traben.resource_explorer.ResourceExplorerClient.MOD_ID;

public class ExplorerStatsScreen extends Screen {

    private final Screen parent;

    private final ExplorerStats stats;

    public ExplorerStatsScreen(Screen parent, ExplorerStats stats) {
        super(Text.translatable(MOD_ID + ".stats.title"));
        this.stats = stats;
        this.parent = parent;

    }

    @Override
    protected void init() {
        super.init();
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.done"),
                        (button) -> Objects.requireNonNull(client).setScreen(parent))
                .dimensions((int) (this.width * 0.7), (int) (this.height * 0.9), (int) (this.width * 0.2), 20)
                .build());

    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        StatDisplayUtil statText = getStatDisplayUtil(context, (float) mouseY);

        statText.renderSubtitle("resource_explorer.explorer.stats.totals",
                Text.translatable("resource_explorer.explorer.stats.all").getString(),
                Text.translatable("resource_explorer.explorer.stats.files").getString());

        statText.renderValue("resource_explorer.explorer.stats.resources", stats.totalResources, stats.totalFileResources);
        statText.renderValue("resource_explorer.explorer.stats.all_textures", stats.totalTextureResources, stats.totalTextureFileResources);
        //statText.renderValue("resource_explorer.explorer.stats.post_filter", stats.totalAllowedResources, stats.totalAllowedFileResources);

        statText.renderSubtitle("resource_explorer.explorer.stats.filetype",
                Text.translatable("resource_explorer.explorer.stats.all").getString(), null);
        stats.totalPerFileType.forEach((k, v) -> statText.renderValue(k.name(), v, -1));

        statText.renderSubtitle("resource_explorer.explorer.stats.packs",
                Text.translatable("resource_explorer.explorer.stats.all").getString(),
                Text.translatable("resource_explorer.explorer.stats.textures").getString());
        stats.totalPerResourcepack.forEach((k, v) -> statText.renderValue(k, v, stats.totalTexturesPerResourcepack.getInt(k)));

        statText.renderSubtitle("resource_explorer.explorer.stats.namespace",
                Text.translatable("resource_explorer.explorer.stats.all").getString(),
                Text.translatable("resource_explorer.explorer.stats.textures").getString());
        stats.totalPerNameSpace.forEach((k, v) -> statText.renderValue(k, v, stats.totalTexturesPerNameSpace.getInt(k)));
    }

    @NotNull
    private StatDisplayUtil getStatDisplayUtil(final DrawContext context, final float mouseY) {
        int offset;
        int projectedHeight = 110 + stats.totalPerFileType.size() * 11 + stats.totalPerNameSpace.size() * 11 + stats.totalPerResourcepack.size() * 11;
        if (projectedHeight > this.height * 0.7) {
            int offsetMax = (int) (projectedHeight - this.height * 0.8) + 64;
            float mouseScroll = (mouseY / this.height) * 1.15f;
            offset = (int) (this.height * 0.25 - (mouseScroll > 1 ? 1 : mouseScroll) * offsetMax);
        } else {
            offset = (int) (this.height * 0.15);
        }

        return new StatDisplayUtil(context, MinecraftClient.getInstance().textRenderer, this.width, this.height, offset);
    }

    private static class StatDisplayUtil {
        final int min;
        final int max;
        final private TextRenderer renderer;
        final private DrawContext context;
        final private int width;
        int offset;

        StatDisplayUtil(DrawContext context, TextRenderer renderer, int width, int height, int offset) {
            this.renderer = renderer;
            this.context = context;
            this.width = width;
            this.min = (int) (height * 0.05);
            this.max = (int) (height * 0.85);
            this.offset = offset;
        }

        private boolean inRenderRange() {
            return offset >= min && offset <= max;
        }

        void renderValue(String valueName, int value, int valueFile) {
            if (inRenderRange()) {
                context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.translatable(valueName), width / 7, offset, 11184810);

                if (value != -1) {
                    int renderLeftOffset = renderer.getWidth(value + " ");
                    context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.of(value + " "), (int) (width * 0.75) - renderLeftOffset, offset, 16777215);
                    if (valueFile != -1) {
                        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.of("| " + valueFile), (int) (width * 0.75), offset, 16777215);
                    }
                }
            }
            offset += 11;
        }

        @SuppressWarnings("SameParameterValue")
        void renderSubtitle(String text, @Nullable String dataType1, @Nullable String dataType2) {
            offset += 11;
            if (inRenderRange()) {
                context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.of("§l" + Text.translatable(text).getString()), width / 8, offset, 16777215);
                if (dataType1 != null) {
                    int renderLeftOffset = renderer.getWidth(dataType1 + " ");
                    context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.translatable(dataType1), (int) (width * 0.75) - renderLeftOffset, offset, 16777215);
                    if (dataType2 != null) {
                        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.of("| " + Text.translatable(dataType2).getString()), (int) (width * 0.75), offset, 16777215);
                    }
                }
            }
            offset += 11;
        }
    }
}
