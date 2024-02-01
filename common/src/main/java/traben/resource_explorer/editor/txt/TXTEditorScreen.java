package traben.resource_explorer.editor.txt;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import traben.resource_explorer.editor.ConfirmExportScreen;

import java.util.Objects;

import static traben.resource_explorer.ResourceExplorerClient.MOD_ID;

public class TXTEditorScreen extends Screen {
    private final Screen parent;

    private final TextEditorWidget editorWidget;
    private ButtonWidget validationButton = null;

    public TXTEditorScreen(final Screen parent, final Identifier txtToEdit, final String text) throws IllegalArgumentException, NullPointerException {
        this(parent, txtToEdit, text, null);
    }

    public TXTEditorScreen(final Screen parent, final Identifier txtToEdit, final String text, @Nullable String fileExtension) throws IllegalArgumentException, NullPointerException {
        super(Text.translatable(MOD_ID + ".txt_editor.title"));
        this.parent = parent;

        if (txtToEdit == null) throw new NullPointerException("[TXT Editor] Identifier was null");
        if (text == null) throw new NullPointerException("[TXT Editor] Resource was null");

        // if file extension is null try to extract the extension from the identifier.tostring()
        if (fileExtension == null || fileExtension.isEmpty()) {
            var identifierString = txtToEdit.toString();
            var lastDot = identifierString.lastIndexOf('.');
            if (lastDot == -1)
                throw new IllegalArgumentException("[TXT Editor] File extension was missing and could not be extracted from the identifier");
            fileExtension = identifierString.substring(lastDot);
        }

        editorWidget = new TextEditorWidget(txtToEdit, text, fileExtension);
    }

    @Override
    public void close() {
        editorWidget.clear();
        super.close();
    }

    @Override
    protected void init() {
        super.init();
        //init lower buttons
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("resource_explorer.png_editor.close"),
                        (button) -> Objects.requireNonNull(client).setScreen(parent))
                .dimensions((int) (this.width * 0.1), (int) (this.height * 0.9), (int) (this.width * 0.2), 20)
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("resource_explorer.png_editor.export_button"),
                        (button) -> Objects.requireNonNull(client).setScreen(
                                new ConfirmExportScreen(this, TextEditorWidget.DisplayOnly.of(editorWidget))))
                .dimensions((int) (this.width * 0.6), (int) (this.height * 0.9), (int) (this.width * 0.3), 20)
                .tooltip(Tooltip.of(Text.translatable("resource_explorer.png_editor.export_button.tooltip")))
                .build());


        //init editor
        editorWidget.setDimensions((int) (width * 0.1), (int) (height * 0.1), editorSize());
        this.addDrawableChild(editorWidget);


        int secondButtonRowX = getButtonAreaLeft();
        int secondButtonRowWidth = 100;
        //reset button
        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("resource_explorer.txt_editor.reset"),
                        (button) -> {
                            clearValidationButton();
                            editorWidget.resetText();
                        })
                .dimensions(secondButtonRowX, (int) (this.height * 0.1), secondButtonRowWidth, 20)
                .tooltip(Tooltip.of(Text.translatable("resource_explorer.txt_editor.reset.tooltip")))
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                        Text.translatable("resource_explorer.txt_editor.clear"),
                        (button) -> {
                            clearValidationButton();
                            editorWidget.clearText();
                        })
                .dimensions(secondButtonRowX, (int) (this.height * 0.2), secondButtonRowWidth, 20)
                .tooltip(Tooltip.of(Text.translatable("resource_explorer.txt_editor.clear.tooltip")))
                .build());

        if (editorWidget.isJsonFormat()) {

            validationButton = this.addDrawableChild(ButtonWidget.builder(
                            Text.translatable("resource_explorer.txt_editor.validator"),
                            (button) -> {
                                if (editorWidget.isValidJson()) {
                                    button.setMessage(Text.translatable("resource_explorer.txt_editor.validator.valid"));
                                } else {
                                    button.setMessage(Text.translatable("resource_explorer.txt_editor.validator.invalid"));
                                }
                            })
                    .dimensions(secondButtonRowX, (int) (this.height * 0.5), secondButtonRowWidth, 20)
                    .tooltip(Tooltip.of(Text.translatable("resource_explorer.txt_editor.validator.tooltip")))
                    .build());

            this.addDrawableChild(ButtonWidget.builder(
                            Text.translatable("resource_explorer.txt_editor.format"),
                            (button) -> {
                                //set validation button to show outcome
                                validationButton.onPress();
                                editorWidget.formatText();
                            })
                    .dimensions(secondButtonRowX, (int) (this.height * 0.4), secondButtonRowWidth, 20)
                    .tooltip(Tooltip.of(Text.translatable("resource_explorer.txt_editor.format.tooltip")))
                    .build());

        } else {
            validationButton = null;
        }

    }

    private void clearValidationButton() {
        if (validationButton != null)
            validationButton.setMessage(Text.translatable("resource_explorer.txt_editor.validator"));
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (!editorWidget.isHovered()) editorWidget.clearFocusedFields();
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        var result = super.keyPressed(keyCode, scanCode, modifiers);
        if (result && editorWidget.isFocused()) {
            clearValidationButton();
        }
        return result;
    }

    @Override
    public boolean charTyped(final char chr, final int modifiers) {
        var result = super.charTyped(chr, modifiers);
        if (result && editorWidget.isFocused()) {
            clearValidationButton();
        }
        return result;
    }

    private int getButtonAreaLeft() {
        return editorWidget.getX() + editorWidget.getWidth() + (int) (this.width * 0.04);
    }

    private int editorSize() {
        return Math.min((int) (width * 0.7), (int) (height * 0.7));
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackgroundTexture(context);

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        //title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 8, 16777215);

        //file location
        context.drawTextWithShadow(this.textRenderer, Text.of(editorWidget.getOriginalAssetIdentifier().toString()), editorWidget.getX(), editorWidget.getBottom() + 8, Colors.GRAY);
    }


}
