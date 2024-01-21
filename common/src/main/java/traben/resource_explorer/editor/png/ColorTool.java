package traben.resource_explorer.editor.png;

import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

class ColorTool {
    private final LinkedList<Integer> colorHistory = new LinkedList<>();

    private int current = ColorHelper.Abgr.getAbgr(255, 0, 0, 255);

    ColorTool() {
        colorHistory.add(current);
        saveColorInHistory();
    }

    int getColor() {
        return current;
    }

    int getColorARGB(){
        return getABGRasARGB(current);
    }

    int getABGRasARGB(int ABGR){
        return ColorHelper.Argb.getArgb(
                ColorHelper.Abgr.getAlpha(ABGR),
                ColorHelper.Abgr.getRed(ABGR),
                ColorHelper.Abgr.getGreen(ABGR),
                ColorHelper.Abgr.getBlue(ABGR));
    }

    int getColorRed(){
        return ColorHelper.Abgr.getRed(current);
    }
    int getColorGreen(){
        return ColorHelper.Abgr.getGreen(current);
    }
    int getColorBlue(){
        return ColorHelper.Abgr.getBlue(current);
    }
    int getColorAlpha(){
        return ColorHelper.Abgr.getAlpha(current);
    }
    void setColorRed(int red255){
        setColor(ColorHelper.Abgr.getAbgr(
                getColorAlpha(),
                getColorBlue(),
                getColorGreen(),
                red255));
    }
    void setColorGreen(int green255){
        setColor(ColorHelper.Abgr.getAbgr(
                getColorAlpha(),
                getColorBlue(),
                green255,
                getColorRed()));
    }
    void setColorBlue(int blue255){
        setColor(ColorHelper.Abgr.getAbgr(
                getColorAlpha(),
                blue255,
                getColorGreen(),
                getColorRed()));
    }
    void setColorAlpha(int alpha255){
        setColor(ColorHelper.Abgr.getAbgr(
                alpha255,
                getColorBlue(),
                getColorGreen(),
                getColorRed()));
    }

    void setColor(int color) {
        if(current == color) return;
        current = color;
    }

    void saveColorInHistory(){
        if(getColorAlpha() == 0) return;
        if(!colorHistory.contains(current)) {
            colorHistory.addFirst(current);
            if (colorHistory.size() > 10) colorHistory.removeLast();
        }
    }

    void setFromIndex(int i) {
        if (i < colorHistory.size()) {
            setColor(colorHistory.get(i));
        }
    }

    @Nullable
    Integer getFromIndex(int i){
        if (i < colorHistory.size()) {
            return colorHistory.get(i);
        }
        return null;
    }

    List<Integer> getDisplayList() {
        return colorHistory;
    }
}
