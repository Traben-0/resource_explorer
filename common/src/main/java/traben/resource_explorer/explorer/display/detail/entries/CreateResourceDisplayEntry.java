package traben.resource_explorer.explorer.display.detail.entries;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import traben.resource_explorer.ResourceExplorerClient;
import traben.resource_explorer.editor.png.PNGEditorScreen;
import traben.resource_explorer.editor.txt.TXTEditorScreen;
import traben.resource_explorer.explorer.display.ExplorerScreen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class CreateResourceDisplayEntry extends DisplayEntry {

    final String identifierPrefix;
    final List<ClickableWidget> widgets = new ArrayList<>();
    private final TextFieldWidget textInput = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 150, 20, Text.of("..."));
    private final TextFieldWidget widthInput = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 150, 20, Text.of("width..."));
    private final TextFieldWidget heightInput = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 150, 20, Text.of("height..."));
    private final ButtonWidget pngButton;
    private final ButtonWidget txtButton;
    private final ButtonWidget jsonButton;
    private final ButtonWidget propertiesButton;
    private final ButtonWidget createButton;

    public CreateResourceDisplayEntry(ExplorerScreen screen) {
        identifierPrefix = screen.cumulativePath
                .replaceFirst("assets/", "")
                .replaceFirst("/", ":");
        pngButton = ButtonWidget.builder(Text.of("png"), (button) -> setPNG())
                .dimensions(0, 0, 50, 20).build();
        txtButton = ButtonWidget.builder(Text.of("txt"), (button) -> setTXT())
                .dimensions(0, 0, 50, 20).build();
        jsonButton = ButtonWidget.builder(Text.of("json"), (button) -> setJSON())
                .dimensions(0, 0, 50, 20).build();
        propertiesButton = ButtonWidget.builder(Text.of("properties"), (button) -> setPROPERTIES())
                .dimensions(0, 0, 50, 20).build();
        createButton = ButtonWidget.builder(Text.translatable("resource_explorer.explorer.create"), (button) -> {
            if (!textInput.getText().isEmpty()) {
                String txtExtension = getTxtExtensionChoice();
                try {
                    if (txtExtension == null) {
                        openPNGEditorScreen(screen, (str) -> button.setMessage(Text.translatable(str)));
                    } else {
                        openTXTEditorScreen(screen, txtExtension, (str) -> button.setMessage(Text.translatable(str)));
                    }
                } catch (Exception e) {
                    ResourceExplorerClient.log("create file failed: " + e.getMessage());
                    button.active = false;
                    button.setMessage(Text.translatable("resource_explorer.explorer.create.fail2"));
                }
            } else {
                button.setMessage(Text.translatable("resource_explorer.explorer.create.fail"));
            }

        }).dimensions(0, 0, 150, 20).build();

        widgets.add(textInput);
        widgets.add(widthInput);
        widgets.add(heightInput);
        widgets.add(pngButton);
        widgets.add(txtButton);
        widgets.add(jsonButton);
        widgets.add(propertiesButton);
        widgets.add(createButton);
    }

    void openTXTEditorScreen(Screen parent, String txtExtension, Consumer<String> setMessage) {
        Optional<Identifier> validated = Identifier.validate(identifierPrefix + textInput.getText() + txtExtension).result();
        if (validated.isPresent()) {
            MinecraftClient.getInstance().setScreen(new TXTEditorScreen(parent, validated.get(),
                    ".json".equals(txtExtension) ? "{\n\n}" : "", txtExtension));
        } else {
            ResourceExplorerClient.log("text resource creation invalid");
            setMessage.accept("resource_explorer.explorer.create.fail_name");
        }
    }

    void openPNGEditorScreen(Screen parent, Consumer<String> setMessage) {
        Optional<Identifier> validated = Identifier.validate(identifierPrefix + textInput.getText() + ".png").result();
        var width = getInputWidth();
        var height = getInputHeight();
        if (validated.isEmpty()) {
            setMessage.accept("resource_explorer.explorer.create.fail_name");
        } else if (width == null) {
            setMessage.accept("resource_explorer.explorer.create.fail_width");
        } else if (height == null) {
            setMessage.accept("resource_explorer.explorer.create.fail_height");
        } else {
            try {
                MinecraftClient.getInstance().setScreen(new PNGEditorScreen(parent, validated.get(),
                        () -> ResourceExplorerClient.getEmptyNativeImage(width, height)));
            } catch (IOException e) {
                ResourceExplorerClient.log("image resource creation failed: " + e.getMessage());
                setMessage.accept("resource_explorer.explorer.create.fail2");
            }
        }
    }

    void setPNG() {
        setAllButtonsActiveAndWidthHeightFalse();
        getPngButton().active = false;
        setWidthHeight(true);
    }

    void setTXT() {
        setAllButtonsActiveAndWidthHeightFalse();
        getTxtButton().active = false;
    }

    void setJSON() {
        setAllButtonsActiveAndWidthHeightFalse();
        getJsonButton().active = false;
    }

    void setPROPERTIES() {
        setAllButtonsActiveAndWidthHeightFalse();
        getPropertiesButton().active = false;
    }

    private void setAllButtonsActiveAndWidthHeightFalse() {
        getPngButton().active = true;
        getTxtButton().active = true;
        getJsonButton().active = true;
        getPropertiesButton().active = true;
        setWidthHeight(false);
    }

    String getTxtExtensionChoice() {
        if (!getTxtButton().active) {
            return ".txt";
        } else if (!getJsonButton().active) {
            return ".json";
        } else if (!getPropertiesButton().active) {
            return ".properties";
        }
        return null;
    }

    ButtonWidget getPngButton() {
        return pngButton;
    }

    ButtonWidget getTxtButton() {
        return txtButton;
    }

    ButtonWidget getJsonButton() {
        return jsonButton;
    }

    ButtonWidget getPropertiesButton() {
        return propertiesButton;
    }

    void setWidthHeight(boolean set) {
        widthInput.active = set;
        widthInput.setEditable(set);
        heightInput.active = set;
        heightInput.setEditable(set);
    }

    Integer getInputWidth() {
        try {
            return Integer.parseInt(widthInput.getText());
        } catch (Exception e) {
            return null;
        }
    }

    Integer getInputHeight() {
        try {
            return Integer.parseInt(heightInput.getText());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public int compareTo(@NotNull DisplayEntry o) {
        return 0;
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        focusHovered();
        for (ClickableWidget widget : widgets) {
            if (widget.isHovered() && widget.active) {
                return widget.mouseClicked(mouseX, mouseY, button);
            }
        }
        return false;
    }

    void focusHovered() {
        for (ClickableWidget widget : widgets) {
            widget.setFocused(widget.isHovered());
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (ClickableWidget widget : widgets) {
            if (widget.isHovered()) {
                return widget.mouseReleased(mouseX, mouseY, button);
            }
        }
        return false;
    }


    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (ClickableWidget widget : widgets) {
            if (widget.isFocused()) {
                return widget.charTyped(chr, modifiers);
            }
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (ClickableWidget widget : widgets) {
            if (widget.isFocused()) {
                return widget.keyPressed(keyCode, scanCode, modifiers);
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (ClickableWidget widget : widgets) {
            if (widget.isFocused()) {
                return widget.keyReleased(keyCode, scanCode, modifiers);
            }
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        int displayX = x + 8;
        int displayY = y + 8;

        int offset = 0;

        offset = drawWidget(textInput, Text.translatable("resource_explorer.explorer.file_name"), context, offset, displayX, displayY, mouseX, mouseY);

        offset = drawWidget(pngButton, Text.translatable("resource_explorer.explorer.tile_type"), context, offset, displayX, displayY, mouseX, mouseY);
        drawWidgetOnly(txtButton, context, offset - 33, displayX + 68, displayY, mouseX, mouseY);

        drawWidgetOnly(jsonButton, context, offset, displayX, displayY, mouseX, mouseY);
        drawWidgetOnly(propertiesButton, context, offset, displayX + 68, displayY, mouseX, mouseY);
        offset += 33;

        if (widthInput.active) {
            offset = drawWidget(widthInput, Text.translatable("resource_explorer.explorer.width"), context, offset, displayX, displayY, mouseX, mouseY);
            offset = drawWidget(heightInput, Text.translatable("resource_explorer.explorer.height"), context, offset, displayX, displayY, mouseX, mouseY);
        } else {
            offset = drawWidgetSubtleText(widthInput, Text.translatable("resource_explorer.explorer.width"), context, offset, displayX, displayY, mouseX, mouseY);
            offset = drawWidgetSubtleText(heightInput, Text.translatable("resource_explorer.explorer.height"), context, offset, displayX, displayY, mouseX, mouseY);
        }

        drawWidget(createButton, Text.translatable("resource_explorer.explorer.create_file"), context, offset, displayX, displayY, mouseX, mouseY);
    }

    @Override
    public String getDisplayName() {
        return "Create new resource";
    }

    @Override
    public int getEntryHeight() {
        return 600;
    }
}
