package traben.resource_explorer.editor.png;

import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;

class ColorTool {
    private final LinkedList<Integer> colorHistory = new LinkedList<>();

    private int current = ColorHelper.getArgb(255, 255, 0, 0);

    ColorTool() {
        colorHistory.add(current);
        initHistory(
                ColorHelper.getArgb(255, 255, 0, 0),
                ColorHelper.getArgb(255, 0, 255, 0),
                ColorHelper.getArgb(255, 0, 0, 255),
                ColorHelper.getArgb(255, 0, 0, 0),
                ColorHelper.getArgb(255, 255, 255, 255),
                ColorHelper.getArgb(255, 255, 255, 0),
                ColorHelper.getArgb(255, 0, 255, 255),
                ColorHelper.getArgb(255, 255, 0, 255),
                ColorHelper.getArgb(255, 192, 192, 192),
                ColorHelper.getArgb(255, 128, 128, 128),
                ColorHelper.getArgb(255, 64, 64, 64),
                ColorHelper.getArgb(255, 128, 0, 0),
                ColorHelper.getArgb(255, 128, 128, 0),
                ColorHelper.getArgb(255, 0, 128, 0),
                ColorHelper.getArgb(255, 128, 0, 128),
                ColorHelper.getArgb(255, 0, 128, 128),
                ColorHelper.getArgb(255, 0, 0, 128));



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
        return current;
    }



    int getColorRed() {
        return ColorHelper.getRed(current);
    }

    void setColorRed(int red255) {
        setColor(ColorHelper.getArgb(
                getColorAlpha(),
                red255,
                getColorGreen(),
                getColorBlue()));
    }

    int getColorGreen() {
        return ColorHelper.getGreen(current);
    }

    void setColorGreen(int green255) {
        setColor(ColorHelper.getArgb(
                getColorAlpha(),
                getColorRed(),
                green255,
                getColorBlue()
                ));
    }

    int getColorBlue() {
        return ColorHelper.getBlue(current);
    }

    void setColorBlue(int blue255) {
        setColor(ColorHelper.getArgb(
                getColorAlpha(),
                getColorRed(),
                getColorGreen(),
                blue255));
    }

    int getColorAlpha() {
        return ColorHelper.getAlpha(current);
    }

    void setColorAlpha(int alpha255) {
        setColor(ColorHelper.getArgb(
                alpha255,
                getColorRed(),
                getColorGreen(),
                getColorBlue()));
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

        if (ColorHelper.getAlpha(underColor) == 0) return getColor();

        var underR = ColorHelper.getRed(underColor);
        var underG = ColorHelper.getGreen(underColor);
        var underB = ColorHelper.getBlue(underColor);

        int alpha = getColorAlpha();
        double alphaDelta = -alpha / 255f;

        int redDifference = (int) Math.round((underR - getColorRed()) * alphaDelta);
        int greenDifference = (int) Math.round((underG - getColorGreen()) * alphaDelta);
        int blueDifference = (int) Math.round((underB - getColorBlue()) * alphaDelta);

        return ColorHelper.getArgb(
                MathHelper.clamp(ColorHelper.getAlpha(underColor) + alpha, 0, 255),
                MathHelper.clamp(underR + redDifference, 0, 255),
                MathHelper.clamp(underG + greenDifference, 0, 255),
                MathHelper.clamp(underB + blueDifference, 0, 255)
        );
    }
}
