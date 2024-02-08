package traben.resource_explorer.editor.png;

import net.minecraft.util.Identifier;

import java.util.Random;

class RollingIdentifier {

    private Identifier current = new Identifier("resource_explorer", "png_editor/" + System.currentTimeMillis());
    private Identifier next = null;

    Identifier getCurrent() {
        return current;
    }

    Identifier getNext() {
        next = new Identifier("resource_explorer", "png_editor/" + System.currentTimeMillis());
        while (next.equals(current)) {
            next = new Identifier("resource_explorer", "png_editor/" + System.currentTimeMillis() + "/" + new Random().nextInt());
        }
        return next;
    }

    void confirmNext() {
        current = next == null ? getNext() : next;
    }

}
