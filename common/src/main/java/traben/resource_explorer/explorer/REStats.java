package traben.resource_explorer.explorer;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static traben.resource_explorer.ResourceExplorerClient.MOD_ID;

public class REStats {


    int totalResources = 0;
    int totalFileResources = 0;
    int totalAllowedResources = 0;
    int totalAllowedFileResources = 0;
    int totalTextureResources = 0;
    int totalTextureFileResources = 0;
    int folderCount = 0;

    Object2IntArrayMap<REResourceFileEntry.FileType> totalPerFileType = new Object2IntArrayMap<>() {{
        defRetValue = 0;
    }};
    Object2IntArrayMap<String> totalPerNameSpace = new Object2IntArrayMap<>() {{
        defRetValue = 0;
    }};
    Object2IntArrayMap<String> totalTexturesPerNameSpace = new Object2IntArrayMap<>() {{
        defRetValue = 0;
    }};
    Object2IntArrayMap<String> totalPerResourcepack = new Object2IntArrayMap<>() {{
        defRetValue = 0;
    }};
    Object2IntArrayMap<String> totalTexturesPerResourcepack = new Object2IntArrayMap<>() {{
        defRetValue = 0;
    }};

    REStats() {
    }

    void incrementMap(Object2IntArrayMap<String> map, String key) {
        map.put(key, map.getInt(key) + 1);
    }


    void addEntryStatistic(REResourceFileEntry entry, boolean allowedByFilter) {
        boolean isFile = entry.resource != null;
        boolean isTexture = entry.fileType == REResourceFileEntry.FileType.PNG;

        //top level
        totalResources++;
        if (isFile)
            totalFileResources++;

        //filtered
        if (allowedByFilter) {
            totalAllowedResources++;
            if (isFile)
                totalAllowedFileResources++;

            //per file type
            totalPerFileType.put(entry.fileType, totalPerFileType.getInt(entry.fileType) + 1);

            //textures only
            if (isTexture) {
                totalTextureResources++;
                if (isFile) {
                    totalTextureFileResources++;
                }
            }

            //by namespace
            incrementMap(totalPerNameSpace, entry.identifier.getNamespace());
            if (isTexture)
                incrementMap(totalTexturesPerNameSpace, entry.identifier.getNamespace());

            //by resourcepack
            if (isFile) {
                incrementMap(totalPerResourcepack, entry.resource.getResourcePackName());
                if (isTexture)
                    incrementMap(totalTexturesPerResourcepack, entry.resource.getResourcePackName());
            }




        }
    }

    REStatsScreen getAsScreen(Screen parent) {
        return new REStatsScreen(parent, this);
    }

    private static class REStatsScreen extends Screen {

        private final Screen parent;

        private final REStats stats;

        public REStatsScreen(Screen parent, REStats stats) {
            super(Text.translatable(MOD_ID + ".stats.title"));
            this.stats = stats;
            this.parent = parent;

        }

        @Override
        protected void init() {
            super.init();
            this.addDrawableChild(ButtonWidget.builder(
                    Text.translatable("gui.done"),
                    (button) -> {
                        Objects.requireNonNull(client).setScreen(parent);
                    }).dimensions((int) (this.width * 0.7), (int) (this.height * 0.9), (int) (this.width * 0.2), 20).build());

        }


        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);

            int offset;
            int projectedHeight = 110+stats.totalPerFileType.size()*11+stats.totalPerNameSpace.size()*11+stats.totalPerResourcepack.size()*11;
            if(projectedHeight > this.height*0.7){
                int offsetMax = (int) (projectedHeight - this.height*0.8)+ 64;
                float mouseScroll = (((float)mouseY)/this.height ) *1.15f;
                offset = (int) (this.height*0.25 - (mouseScroll > 1 ? 1 : mouseScroll )*offsetMax);
            }else{
                offset = (int) (this.height*0.15);
            }

            TextDisplayUtil outText = new TextDisplayUtil(context,MinecraftClient.getInstance().textRenderer, this.width, this.height, offset);

            outText.renderSubtitle("resource_explorer.explorer.stats.totals",Text.translatable("resource_explorer.explorer.stats.all").getString(), Text.translatable("resource_explorer.explorer.stats.files").getString() );

            outText.renderValue("resource_explorer.explorer.stats.resources", stats.totalResources, stats.totalFileResources);
            outText.renderValue("resource_explorer.explorer.stats.all_textures", stats.totalTextureResources, stats.totalTextureFileResources);
            outText.renderValue("resource_explorer.explorer.stats.post_filter", stats.totalAllowedResources, stats.totalAllowedFileResources);
            outText.renderSubtitle("resource_explorer.explorer.stats.filetype",Text.translatable("resource_explorer.explorer.stats.all").getString(),null);
            stats.totalPerFileType.forEach((k,v)->{
                outText.renderValue(k.name(), v, -1);
            });
            outText.renderSubtitle("resource_explorer.explorer.stats.namespace",Text.translatable("resource_explorer.explorer.stats.all").getString(),Text.translatable("resource_explorer.explorer.stats.textures").getString());
            stats.totalPerNameSpace.forEach((k,v)->{
                outText.renderValue(k, v, stats.totalTexturesPerNameSpace.getInt(k));
            });
            outText.renderSubtitle("resource_explorer.explorer.stats.packs",Text.translatable("resource_explorer.explorer.stats.all").getString(),Text.translatable("resource_explorer.explorer.stats.textures").getString());
            stats.totalPerResourcepack.forEach((k,v)->{
                outText.renderValue(k, v, stats.totalTexturesPerResourcepack.getInt(k));
            });
        }

        private static class TextDisplayUtil{
            int offset;
            int min;
            int max;
            final private TextRenderer renderer;
            final private DrawContext context;
            final private int width;
            TextDisplayUtil(DrawContext context,TextRenderer renderer, int width, int height, int offset){
                this.renderer = renderer;
                this.context = context;
                this.width = width;
                this.min = (int) (height*0.05);
                this.max = (int) (height*0.85);
                this.offset = offset;
            }

            private boolean inRenderRange(){
                return offset >= min && offset <= max;
            }
            void renderValue(String valueName, int value, int valueFile){
                if(inRenderRange()) {
                    context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.translatable(valueName), width / 7, offset, 11184810);

                    if (value != -1) {
                        int renderLeftOffset = renderer.getWidth(value + " ");
                        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.of(value + " "), (int) (width * 0.75)-renderLeftOffset, offset, 16777215);
                        if (valueFile != -1) {
                            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,Text.of("| " + valueFile), (int) (width * 0.75), offset, 16777215);
                        }
                    }
                }
                offset += 11;
            }
            @SuppressWarnings("SameParameterValue")
            void renderSubtitle(String text, @Nullable String dataType1, @Nullable String dataType2){
                offset += 11;
                if(inRenderRange()) {
                    context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.of("§l" + Text.translatable(text).getString()), width / 8, offset, 16777215);
                    if (dataType1 != null) {
                        int renderLeftOffset = renderer.getWidth(dataType1 + " ");
                        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.translatable(dataType1), (int) (width * 0.75)-renderLeftOffset, offset, 16777215);
                        if (dataType2 != null) {
                            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,Text.of("| " + Text.translatable(dataType2).getString()), (int) (width * 0.75), offset, 16777215);
                        }
                    }
                }
                offset += 11;
            }
        }
    }

}
