package traben.resource_explorer.gui;

//import net.minecraft.client.gui.Drawable;
//import net.minecraft.client.gui.Element;
//import net.minecraft.client.gui.Selectable;
//import net.minecraft.client.gui.screen.Screen;
//import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
//import net.minecraft.client.render.RenderLayer;
//import org.jetbrains.annotations.Nullable;
//
//public class REDrawableFile implements Drawable, Element, Selectable {
//
//
//
//
//    REDrawableFile(int x, int y, int width, int height) {
////        32, height - 55 + 4
//        this.x = x;
//        this.y = y + 32;
//        this.width = width;
//        this.height = height - 55 + 4-32;
//
//    }
//    int x;
//    int y;
//    int width;
//    int height;
//
//
//
//
////    private boolean doRender = true;
//
//    @Override
//    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
////        if(!doRender) return;
//        context.setShaderColor(0.125F, 0.125F, 0.125F, 1.0F);
//        context.drawTexture(Screen.OPTIONS_BACKGROUND_TEXTURE, x, y, width, height, width, height, 32, 32);
//        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//
//        int displayX = x+8;
//        int displayY = y+8;
//        int displaySquare = Math.min(height,width);
//        int displayDim = (int) (displaySquare/1.5);
//
//        if(currentFile == null) return;
//        switch (currentFile.type){
//            case PNG -> {
//                int largestSize = Math.max(currentFile.width, currentFile.height);
//                float sizeScale = ((float)displayDim) / largestSize;
//
//                int displayX2 = (int) (currentFile.width * sizeScale) ;
//                int displayY2 = (int) (currentFile.height * sizeScale);
//
//                context.drawTexture(currentFile.identifier,displayX,displayY, 0,0, displayX2, displayY2, displayX2, displayY2);
//            }
//            case TXT -> {
//
//            }
//            case PROPERTIES -> {
//
//            }
//            case OGG -> {
//
//            }
//            case ZIP -> {
//
//            }
//            case JEM -> {
//
//            }
//            case JPM -> {
//
//            }
//            case OTHER -> {
//
//            }
//            case BLANK -> {
//
//            }
//            case JSON -> {
//
//            }
//        };
//
//        context.fillGradient(RenderLayer.getGuiOverlay(), x, y,x+width,y+4, -16777216, 0, 0);
//        context.fillGradient(RenderLayer.getGuiOverlay(), x,y+height-4,x+width,y+height, 0, -16777216, 0);
//    }
//
//    @Override
//    public void setFocused(boolean focused) {
//
//    }
//
//    @Override
//    public boolean isFocused() {
//        return false;
//    }
//
//    @Override
//    public SelectionType getType() {
//        return SelectionType.NONE;
//    }
//
//    @Override
//    public void appendNarrations(NarrationMessageBuilder builder) {
//
//    }
//}
