package traben.resource_explorer.editor;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public interface ExportableFileContainerAndPreviewer {

    boolean exportAsIdentifier(Identifier identifier);

    Identifier getOriginalAssetIdentifier();

    String assertFileTypeOnEnd(String possiblyEndsWithFilenameAlready);

    void renderSimple(DrawContext context, int x, int y, int x2, int y2);
}
