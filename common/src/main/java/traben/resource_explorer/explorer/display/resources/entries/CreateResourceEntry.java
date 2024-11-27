package traben.resource_explorer.explorer.display.resources.entries;

import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import traben.resource_explorer.explorer.ExplorerUtils;
import traben.resource_explorer.explorer.display.ExplorerScreen;
import traben.resource_explorer.explorer.display.detail.entries.CreateResourceDisplayEntry;

public class CreateResourceEntry extends ResourceEntry {
    private final ExplorerScreen screen;
    private final Identifier icon = Identifier.of("resource_explorer:textures/file_add.png");

    public CreateResourceEntry(ExplorerScreen screen) {
        this.screen = screen;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    protected boolean canExport() {
        return false;
    }

    @Override
    String getDisplayName() {
        return "create resource";
    }

    @Override
    OrderedText getDisplayText() {
        return Text.of(getDisplayName()).asOrderedText();
    }

    @Override
    Text[] getExtraText(boolean smallMode) {
        return new Text[0];
    }

    @Override
    String toString(int indent) {
        return getDisplayName();
    }

    @Override
    Identifier getIcon(boolean hovered) {
        return icon;
    }

    @Override
    boolean matchesSearch(final String search) {
        throw new UnsupportedOperationException("NewFileEntry shouldn't be searched");
    }

    @Override
    boolean mouseClickExplorer() {
        if (ExplorerScreen.currentDisplay != null) {
            ExplorerScreen.currentDisplay.setSelectedEntry(new CreateResourceDisplayEntry(screen));
        }
        return true;
    }

    @Override
    void exportToOutputPack(ExplorerUtils.REExportContext context) {

    }
}
