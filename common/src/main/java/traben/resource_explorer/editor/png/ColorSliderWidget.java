package traben.resource_explorer.editor.png;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

class ColorSliderWidget extends SliderWidget {

    private final Text message;
    private final Consumer<Double> setter;

    public ColorSliderWidget(Text text, Consumer<Double> setter) {
        super(0, 0, 1, 1, text, 1);
        message = text;
        this.setter = setter;
    }

    @Override
    protected void updateMessage() {
        this.setMessage(Text.of(message.getString() + (int) (value * 255)));
    }

    @Override
    protected void applyValue() {
        setter.accept(value);
    }

    public void setValue255(int value255) {
        value = value255 / 255D;
        updateMessage();
    }

    public void setDimensionsAndPosition(int width, int height, int x, int y) {
        this.width = width;
        this.height = height;
        this.setX(x);
        this.setY(y);
    }
}
