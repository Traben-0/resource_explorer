package traben.resource_explorer.editor.png;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;


public enum BackgroundMode {
    BLACK("resource_explorer.png_editor.background.black") {
        @Override
        void render(DrawContext context, int x, int y, int x2, int y2) {
            renderWhiteOutlineSingleBox(context, x, y, x2, y2);
            renderSolidColor(context, x, y, x2, y2, Colors.BLACK);
        }
    },
    GRAY("resource_explorer.png_editor.background.gray") {
        @Override
        void render(DrawContext context, int x, int y, int x2, int y2) {
            renderWhiteOutlineSingleBox(context, x, y, x2, y2);
            renderSolidColor(context, x, y, x2, y2, Colors.GRAY);
        }
    },
    WHITE("resource_explorer.png_editor.background.white") {
        @Override
        void render(DrawContext context, int x, int y, int x2, int y2) {
            renderWhiteOutlineSingleBox(context, x, y, x2, y2);
        }
    },
    CHECKER("resource_explorer.png_editor.background.checker") {
        @Override
        void render(DrawContext context, int x, int y, int x2, int y2) {
            renderWhiteOutlineSingleBox(context, x, y, x2, y2);
            renderSolidImage(context, x, y, x2, y2, Identifier.of("resource_explorer:textures/editor_checker.png"), 64);

        }
    },
    NONE("resource_explorer.png_editor.background.none") {
        @Override
        void render(DrawContext context, int x, int y, int x2, int y2) {
            renderWhiteOutlineSingleBox(context, x, y, x2, y2);

//            context.setShaderColor(0.25F, 0.25F, 0.25F, 1.0F);
//            renderSolidImage(context, x, y, x2, y2, Identifier.of("textures/block/dirt.png"), 32);
//            context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    };

    private final String nameKey;

    BackgroundMode(String nameKey) {
        this.nameKey = nameKey;
    }

    public String getNameKey() {
        return nameKey;
    }

    protected void renderWhiteOutlineSingleBox(DrawContext context, int x, int y, int x2, int y2) {
        context.fill(x - 2, y - 2, x2 + 2, y2 + 2, Colors.WHITE);
    }

    protected void renderSolidColor(DrawContext context, int x, int y, int x2, int y2, int color) {
        context.fill(x, y, x2, y2, color);
    }

    protected void renderSolidImage(DrawContext context, int x, int y, int x2, int y2, Identifier identifier, int scale) {
        context.drawTexture(identifier, x, y, 0, 0, x2 - x, y2 - y, scale, scale);
    }

    abstract void render(DrawContext context, int x, int y, int x2, int y2);

    public BackgroundMode next() {
        var all = BackgroundMode.values();
        for (int i = 0; i < all.length; i++) {
            if (all[i] == this) {
                var nextI = (i + 1) % all.length;
                return all[nextI];
            }
        }
        return BLACK;
    }
}
