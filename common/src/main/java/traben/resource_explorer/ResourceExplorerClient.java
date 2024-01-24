package traben.resource_explorer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import traben.resource_explorer.explorer.REExplorerScreen;

import java.io.InputStream;

public class ResourceExplorerClient {
    public static final String MOD_ID = "resource_explorer";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        log("loaded.");
    }

    public static void log(Object message) {
        LOGGER.info("[resource_explorer]: " + message.toString());
    }

    @SuppressWarnings("unused")
    public static void logWarn(Object message) {
        LOGGER.warn("[resource_explorer]: " + message.toString());
    }

    public static void logError(Object message) {
        LOGGER.error("[resource_explorer]: " + message.toString());
    }

    @Nullable
    public static NativeImage getNativeImageElseNull(final Resource pngResource) {
        NativeImage img;
        try {
            InputStream in = pngResource.getInputStream();
            try {
                img = NativeImage.read(in);
                in.close();
                return img;
            } catch (Exception e) {
                in.close();
                ResourceExplorerClient.log(e.getMessage());
                return null;
            }
        } catch (Exception e) {
            ResourceExplorerClient.log(e.getMessage());
        }
        return null;
    }

    @NotNull
    public static NativeImage getEmptyNativeImage(int width, int height) {
        var img = new NativeImage(width, height, false);
        img.fillRect(0, 0, width, height, 0);
        return img;
    }

    private static Screen explorerExit = null;

    public static void setExplorerExit(Screen explorerExit2){
        explorerExit = explorerExit2;
    }

    public static void leaveEditorAndResourceReload(){
        if (REExplorerScreen.currentDisplay != null)
            REExplorerScreen.currentDisplay.close();
        REExplorerScreen.currentDisplay = null;
        REExplorerScreen.currentStats = null;

        //reading resources this way has some... affects to the resource system
        //thus a resource reload is required
        MinecraftClient.getInstance().setScreen(explorerExit);
        MinecraftClient.getInstance().reloadResources();
        if (explorerExit instanceof REConfig.REConfigScreen configScreen) {
            configScreen.tempConfig.filterMode = REConfig.getInstance().filterMode;
            configScreen.reset();
        }
        explorerExit = null;
    }

}
