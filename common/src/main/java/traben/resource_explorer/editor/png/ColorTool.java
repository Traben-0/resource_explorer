package traben.resource_explorer.editor.png;

import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;

class ColorTool {
    private final LinkedList<Integer> colorHistory = new LinkedList<>();

    private int current = ColorHelper.Abgr.getAbgr(255, 0, 0, 255);

    ColorTool() {
        colorHistory.add(current);
        initHistory(
                ColorHelper.Abgr.getAbgr(255, 0, 0, 255),
                ColorHelper.Abgr.getAbgr(255, 0, 255, 0),
                ColorHelper.Abgr.getAbgr(255, 255, 0, 0),
                ColorHelper.Abgr.getAbgr(255, 0, 0, 0),
                ColorHelper.Abgr.getAbgr(255, 255, 255, 255),
                ColorHelper.Abgr.getAbgr(255, 0, 255, 255),
                ColorHelper.Abgr.getAbgr(255, 255, 255, 0),
                ColorHelper.Abgr.getAbgr(255, 255, 0, 255),
                ColorHelper.Abgr.getAbgr(255, 192, 192, 192),
                ColorHelper.Abgr.getAbgr(255, 128, 128, 128),
                ColorHelper.Abgr.getAbgr(255, 64, 64, 64),
                ColorHelper.Abgr.getAbgr(255, 0, 0, 128),
                ColorHelper.Abgr.getAbgr(255, 0, 128, 128),
                ColorHelper.Abgr.getAbgr(255, 0, 128, 0),
                ColorHelper.Abgr.getAbgr(255, 128, 0, 128),
                ColorHelper.Abgr.getAbgr(255, 128, 128, 0),
                ColorHelper.Abgr.getAbgr(255, 128, 0, 0));
    }

    void initHistory(int... colors) {
        for (int color : colors) {
            colorHistory.add(color);
        }
    }

    int getColor() {
        return current;
    }

    void setColor(int color) {
        if (current == color) return;
        current = color;
    }

    int getColorARGB() {
        return getABGRasARGB(current);
    }

    int getABGRasARGB(int ABGR) {
        return ColorHelper.Argb.getArgb(
                ColorHelper.Abgr.getAlpha(ABGR),
                ColorHelper.Abgr.getRed(ABGR),
                ColorHelper.Abgr.getGreen(ABGR),
                ColorHelper.Abgr.getBlue(ABGR));
    }

    int getColorRed() {
        return ColorHelper.Abgr.getRed(current);
    }

    void setColorRed(int red255) {
        setColor(ColorHelper.Abgr.getAbgr(
                getColorAlpha(),
                getColorBlue(),
                getColorGreen(),
                red255));
    }

    int getColorGreen() {
        return ColorHelper.Abgr.getGreen(current);
    }

    void setColorGreen(int green255) {
        setColor(ColorHelper.Abgr.getAbgr(
                getColorAlpha(),
                getColorBlue(),
                green255,
                getColorRed()));
    }

    int getColorBlue() {
        return ColorHelper.Abgr.getBlue(current);
    }

    void setColorBlue(int blue255) {
        setColor(ColorHelper.Abgr.getAbgr(
                getColorAlpha(),
                blue255,
                getColorGreen(),
                getColorRed()));
    }

    int getColorAlpha() {
        return ColorHelper.Abgr.getAlpha(current);
    }

    void setColorAlpha(int alpha255) {
        setColor(ColorHelper.Abgr.getAbgr(
                alpha255,
                getColorBlue(),
                getColorGreen(),
                getColorRed()));
    }

    void saveColorInHistory() {
        if (getColorAlpha() == 0) return;
        if (!colorHistory.contains(current)) {
            colorHistory.addFirst(current);
            if (colorHistory.size() > 30) colorHistory.removeLast();
        }
    }

    void setFromIndex(int i) {
        if (i < colorHistory.size()) {
            setColor(colorHistory.get(i));
        }
    }

    @Nullable
    Integer getFromIndex(int i) {
        if (i < colorHistory.size()) {
            return colorHistory.get(i);
        }
        return null;
    }

    int blendOver(int underColor) {

        if (ColorHelper.Abgr.getAlpha(underColor) == 0) return getColor();

        var underR = ColorHelper.Abgr.getRed(underColor);
        var underG = ColorHelper.Abgr.getGreen(underColor);
        var underB = ColorHelper.Abgr.getBlue(underColor);

        int alpha = getColorAlpha();
        double alphaDelta = -alpha / 255f;

        int redDifference = (int) Math.round((underR - getColorRed()) * alphaDelta);
        int greenDifference = (int) Math.round((underG - getColorGreen()) * alphaDelta);
        int blueDifference = (int) Math.round((underB - getColorBlue()) * alphaDelta);

        return ColorHelper.Abgr.getAbgr(
                MathHelper.clamp(ColorHelper.Abgr.getAlpha(underColor) + alpha, 0, 255),
                MathHelper.clamp(underB + blueDifference, 0, 255),
                MathHelper.clamp(underG + greenDifference, 0, 255),
                MathHelper.clamp(underR + redDifference, 0, 255)
        );
    }
}
