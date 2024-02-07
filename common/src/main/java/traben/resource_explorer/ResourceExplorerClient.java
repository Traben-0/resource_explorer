package traben.resource_explorer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import traben.resource_explorer.explorer.display.ExplorerScreen;

import java.io.InputStream;
import java.util.Optional;

public class ResourceExplorerClient {
    public static final String MOD_ID = "resource_explorer";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static Screen explorerExit = null;

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
    public static NativeImage getNativeImageElseNull(final Identifier identifier) {
        if (identifier == null) return null;
        Optional<Resource> resource = MinecraftClient.getInstance().getResourceManager().getResource(identifier);
        return resource.map(ResourceExplorerClient::getNativeImageElseNull).orElse(null);
    }

    @Nullable
    public static NativeImage getNativeImageElseNull(final Resource pngResource) {
        if (pngResource == null) return null;
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

    public static void setExitScreen(Screen explorerExit2) {
        explorerExit = explorerExit2;
    }

    public static void leaveModScreensAndResourceReload() {

        if (ExplorerScreen.currentDisplay != null)
            ExplorerScreen.currentDisplay.close();
        ExplorerScreen.currentDisplay = null;
        ExplorerScreen.currentStats = null;

        //reading resources has some... affects to minecraft's resource system
        //thus a resource reload is required to fix everything up
        MinecraftClient.getInstance().setScreen(explorerExit);
        MinecraftClient.getInstance().reloadResources();
        if (explorerExit instanceof REConfig.REConfigScreen configScreen) {
            configScreen.tempConfig.filterMode = REConfig.getInstance().filterMode;
            configScreen.reset();
        }
        explorerExit = null;
    }

}
