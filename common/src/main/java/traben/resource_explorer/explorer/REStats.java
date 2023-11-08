package traben.resource_explorer.explorer;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.Objects;

import static traben.resource_explorer.ResourceExplorerClient.MOD_ID;

public class REStats {


    final int totalResources;
    final int totalFileResources;
    int totalAllowedResources = 0;
    int totalAllowedFileResources = 0;
    int totalTextureResources = 0;
    int totalTextureFileResources = 0;
    int folderCount = 0;

    Object2IntArrayMap<REResourceFileEntry.FileType> totalPerFileType = new Object2IntArrayMap<>(){{defRetValue=0;}};
    Object2IntArrayMap<String> totalPerNameSpace = new Object2IntArrayMap<>(){{defRetValue=0;}};
    Object2IntArrayMap<String> totalTexturesPerNameSpace = new Object2IntArrayMap<>(){{defRetValue=0;}};
    Object2IntArrayMap<String> totalPerResourcepack = new Object2IntArrayMap<>(){{defRetValue=0;}};
    Object2IntArrayMap<String> totalTexturesPerResourcepack = new Object2IntArrayMap<>(){{defRetValue=0;}};

    REStats(int totalResources, int totalFileResources){
        this.totalResources = totalResources;
        this.totalFileResources = totalFileResources;
    }

    void incrementMap(Object2IntArrayMap<String> map,String key){
        map.put(key,map.getInt(key)+1);
    }


    void addEntryStatistic(REResourceFileEntry entry, boolean allowedByFilter){
        boolean isFile = entry.resource != null;
        boolean isTexture = entry.fileType == REResourceFileEntry.FileType.PNG;

        totalPerFileType.put(entry.fileType,totalPerFileType.getInt(entry.fileType)+1);

        if(isTexture){
            totalTextureResources++;
            if(isFile){
                totalTextureFileResources++;
            }
        }

        incrementMap(totalPerNameSpace, entry.identifier.getNamespace());
        if(isTexture)
            incrementMap(totalTexturesPerNameSpace, entry.identifier.getNamespace());

        if(isFile) {
            incrementMap(totalPerResourcepack, entry.resource.getResourcePackName());
            if (isTexture)
                incrementMap(totalTexturesPerResourcepack, entry.resource.getResourcePackName());
        }

        if(allowedByFilter){
            totalAllowedResources++;
            if(isFile)
                totalAllowedFileResources++;

        }
    }

    REStatsScreen getAsScreen(Screen parent){
        return new REStatsScreen(parent,this);
    }

    private static class REStatsScreen extends Screen {

        private final Screen parent;

        private final REStats stats;
        public REStatsScreen(Screen parent, REStats stats) {
            super(Text.translatable(MOD_ID+".stats.title"));
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
            int offset = 0;
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.of("shsfdghdf"), 0, 0, 16777215);
            offset += 11;

            MultilineText identifierText = MultilineText.create(MinecraftClient.getInstance().textRenderer, Text.of("dfhf"),0);
            identifierText.drawWithShadow(context, 0, 0, 11, -8355712);
            offset += 11+ identifierText.count()*11;
            //todo draw stats
        }
    }

}
