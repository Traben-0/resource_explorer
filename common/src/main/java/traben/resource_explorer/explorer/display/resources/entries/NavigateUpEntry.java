package traben.resource_explorer.explorer.display.resources.entries;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import traben.resource_explorer.explorer.ExplorerUtils;

public class NavigateUpEntry extends ResourceFolderEntry {


    public NavigateUpEntry(String folderName) {
        super(folderName);
    }

    @Override
    public boolean mouseClickExplorer() {
        Screen parent = this.widget.explorerScreen.reParent;
        MinecraftClient.getInstance().setScreen(parent);
        // this.widget.screen.close();
        return false;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public Identifier getIcon(boolean hovered) {
        return hovered ? ExplorerUtils.ICON_FOLDER_UP_SELECTED : ExplorerUtils.ICON_FOLDER_UP;
    }

    @Override
    public Text[] getExtraText(boolean ignored) {
        return new Text[]{Text.of("§8§o move up directory")};
    }

    @Override
    protected boolean canExport() {
        return false;
    }

}
