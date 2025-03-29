package traben.resource_explorer.explorer.display.resources.entries;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import traben.resource_explorer.explorer.ExplorerUtils;
import traben.resource_explorer.explorer.display.detail.entries.DisplayEntry;
import traben.resource_explorer.explorer.display.detail.entries.ExplorerDetailsDisplayEntry;

public class ExplorerDetailsEntry extends ResourceFileEntry {

    private final String[] message;

//    public ExplorerDetailsEntry(final Exception e) {
//        super();
//        StringBuilder builder = new StringBuilder();
//        builder.append(e.getMessage()).append("\n");
//        for (StackTraceElement line : e.getStackTrace()) {
//            builder.append(line.toString()).append("\n");
//        }
//        message = builder.toString().split("\n");
//    }

    public ExplorerDetailsEntry(final String lines) {
        super();
        message = lines.split("\n");
    }

//    public SearchFeedbackEntry(final String... lines) {
//        super();
//        message = lines;
//    }

    @Override
    public int hashCode() {
        return -1;
    }

    @Override
    boolean matchesSearch(final String search) {
        return true;
    }

    @Override
    public Identifier getIcon(final boolean hovered) {
        return ExplorerUtils.ICON_FILE_UNKNOWN;
    }

    @Override
    public Text[] getExtraText(final boolean smallMode) {
        return new Text[]{Text.of("")};
    }

    @Override
    public DisplayEntry wrapEntryAsDetailed() {
        return new ExplorerDetailsDisplayEntry(message);
    }
}
