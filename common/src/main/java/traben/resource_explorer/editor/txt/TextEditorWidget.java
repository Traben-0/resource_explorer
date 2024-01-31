package traben.resource_explorer.editor.txt;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import traben.resource_explorer.editor.ExportableFileContainerAndPreviewer;
import traben.resource_explorer.explorer.REExplorer;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class TextEditorWidget extends ClickableWidget implements ExportableFileContainerAndPreviewer {


    private final Identifier identifier;
    private final String initialText;
    private final String fileTypeEnd;

    private final List<String> currentTextLines = new ArrayList<>();
    List<TextFieldWidgetWithIndex> textFields = new ArrayList<>();
    int topLineOfEditor = 0;

    public TextEditorWidget(@NotNull Identifier identifier, @NotNull String initialText, @NotNull String fileTypeEnd) {
        super(0, 0, 1, 1, Text.of(""));


        if (fileTypeEnd.isEmpty()) throw new IllegalArgumentException("fileTypeEnd must not be empty");
        if (!fileTypeEnd.startsWith(".")) fileTypeEnd = "." + fileTypeEnd;

        this.identifier = identifier;
        this.initialText = initialText;
        this.fileTypeEnd = fileTypeEnd;

        resetText();
    }

    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double horizontalAmount, final double verticalAmount) {
        // if vertical amount isn't zero tick the top line of the editor up or down clamping the minimum to 0
        if (verticalAmount != 0) {
            topLineOfEditor = MathHelper.clamp(topLineOfEditor + (verticalAmount > 0 ? -1 : 1), 0, currentTextLines.size() - textFields.size());
            initTextWidgets();
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    // method to get the text field given a screen space mouse y
    // returning null if the mouse is not over a text field
    @Nullable
    private TextFieldWidgetWithIndex getTextField(double mouseY) {
        double relativeMouseY = mouseY - getY();
        int textFieldIndex = (int) (relativeMouseY / textHeight);
        if (textFieldIndex < textFields.size()) {
            var field = textFields.get(textFieldIndex);
            clearFocusedFields();
            field.setFocused(true);
            return field;
        }
        return null;
    }

    @Nullable
    private TextFieldWidgetWithIndex getTextFieldFocused() {
        for (TextFieldWidgetWithIndex textField : textFields) {
            if (textField.isFocused()) return textField;
        }
        return null;
    }

    private void clearFocusedFields() {
        for (TextFieldWidget textField : textFields) {
            textField.setFocused(false);
        }
    }

    @Override
    public boolean isFocused() {
        return getTextFieldFocused() != null;
    }


    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (isHovered()) {
            var textField = getTextField(mouseY);
            if (textField != null) {
                setFocused(true);
                textField.setCursorToStart(false);
                return textField.mouseClicked(mouseX, mouseY, button);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        var textField = getTextFieldFocused();
        if (textField != null) {
            int line = textField.getIndexInDisplayList();
            switch (keyCode) {
                case 265 -> {//up
                    moveUpLine(line, textField, textField.getCursor());
                    return true;
                }
                case 264 -> {//down
                    moveDownLine(line, textField, textField.getCursor());
                    return true;
                }
                //right will move to the next line if at the end of the text
                case 262 -> {
                    String text = textField.getText();
                    if (textField.getCursor() >= text.length()) {
                        moveDownLine(line, textField, 0);
                        textField.setCursorToStart(false);
                        return true;
                    }
                }
                //same with left but to the previous line
                case 263 -> {
                    if (textField.getCursor() <= 0) {
                        moveUpLine(line, textField, Integer.MAX_VALUE);
                        return true;
                    }
                }
                //enter
                case 257 -> {
                    //insert a new line in the next index but also remove any text after the current cursor and put it in the new line
                    String text = textField.getText();
                    int cursor = textField.getCursor();
                    String textBeforeCursor = text.substring(0, cursor);
                    String textAfterCursor = text.substring(cursor);
                    onTextChange(line, textBeforeCursor);
                    insertNewTextLineAfter(line, textAfterCursor);
                    initTextWidgets();
                    moveDownLine(line, textField, 0);
                    return true;
                }
                //backspace will remove this line if the line is empty
                case 259 -> {
                    String text = textField.getText();
                    if (text.isEmpty() && line > 0) {
                        currentTextLines.remove(line);
                        initTextWidgets();
                        moveUpLine(line, textField, Integer.MAX_VALUE);
                        return true;
                    }
                }
                //delete will remove this line if the line is empty
                case 261 -> {
                    String text = textField.getText();
                    if (text.isEmpty() && line < currentTextLines.size() - 1) {
                        currentTextLines.remove(line);
                        initTextWidgets();
                        textFields.get(line).setFocused(true);
                        return true;
                    }
                }
                //page down will move the vertical offset down by the text field index length
                case 267 -> {
                    if (topLineOfEditor >= currentTextLines.size() - textFields.size()) return true;
                    topLineOfEditor += textFields.size();
                    initTextWidgets();
                    textFields.get(line).setFocused(true);
                    return true;
                }
                //page up will move the vertical offset up by the text field index length
                case 266 -> {
                    if (topLineOfEditor <= 0) return true;
                    topLineOfEditor = Math.max(0, topLineOfEditor - textFields.size());
                    initTextWidgets();
                    textFields.get(line).setFocused(true);
                    return true;
                }
            }
            return textField.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void moveUpLine(int line, TextFieldWidgetWithIndex oldField, int cursor) {
        TextFieldWidgetWithIndex newField;
        if (line <= 0) {
            if (topLineOfEditor <= 0) return;
            topLineOfEditor = Math.max(0, topLineOfEditor - 1);
            initTextWidgets();
            newField = textFields.get(0);
        } else {
            newField = textFields.get(line - 1);
            oldField.setFocused(false);
            oldField.setCursorToStart(false);
        }
        newField.setFocused(true);
        newField.setCursor(cursor, false);
    }

    private void moveDownLine(int line, TextFieldWidgetWithIndex oldField, int cursor) {
        TextFieldWidgetWithIndex newField;
        if (line >= textFields.size() - 1) {
            if (topLineOfEditor >= currentTextLines.size() - textFields.size()) return;
            topLineOfEditor++;
            initTextWidgets();
            newField = textFields.get(textFields.size() - 1);
        } else {
            newField = textFields.get(line + 1);
            oldField.setFocused(false);
            oldField.setCursorToStart(false);
        }
        newField.setFocused(true);
        newField.setCursor(cursor, false);
    }


    @Override
    public boolean charTyped(final char chr, final int modifiers) {
        var textField = getTextFieldFocused();
        if (textField != null) {
            return textField.charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyReleased(final int keyCode, final int scanCode, final int modifiers) {
        var textField = getTextFieldFocused();
        if (textField != null) {
            setFocused(true);
            return textField.keyReleased(keyCode, scanCode, modifiers);
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    int textHeight = 12;


    private void initTextWidgets() {
        List<TextFieldWidgetWithIndex> textFields = new ArrayList<>();

        int widthTest = Math.max(currentTextLines.size(),100);
        int spaceForLineNumber = MinecraftClient.getInstance().textRenderer.getWidth(  widthTest + "|");

        int amount = this.height / textHeight;
        for (int displayIndex = 0; displayIndex < amount; displayIndex++) {
            textFields.add(prepTextField(displayIndex, spaceForLineNumber));
        }
        this.textFields = textFields;
    }

    @NotNull
    private TextFieldWidgetWithIndex prepTextField(final int displayIndex, final int spaceForLineNumber) {
        final int lineInActualText = displayIndex + topLineOfEditor;

        TextFieldWidgetWithIndex textField = new TextFieldWidgetWithIndex(displayIndex, lineInActualText, spaceForLineNumber, getX(), getY() + (displayIndex * textHeight), width, textHeight);

        //set text for the line
        if (currentTextLines.size() > lineInActualText) {
            textField.setText(currentTextLines.get(lineInActualText));
            textField.setCursorToStart(false);
        } else {
            textField.setText("");
        }

        textField.setChangedListener((string) -> this.onTextChange(lineInActualText, string));
        return textField;
    }

    //a method called when the text is changed
    // uses the int index to apply changes to that line in the currentTextLines list
    public void onTextChange(int index, String newText) {
        if (index < 0) return;
        while (currentTextLines.size() < index) {
            currentTextLines.add("");
        }
        currentTextLines.set(index, newText);
    }

    //same method as above but this one is called when a new line is inserted shifting all lines after it down
    public void insertNewTextLineAfter(int index, String newText) {
        if (index < 0) return;
        while (currentTextLines.size() < index+1) {
            currentTextLines.add("");
        }
        currentTextLines.add(index + 1, newText);
    }


    //clears all references to text fields
    void clear() {
        textFields.clear();
    }

    @Override
    public boolean exportAsIdentifier(final Identifier identifier) {
        return REExplorer.outputResourceToPackInternal(identifier == null ? this.identifier : identifier, (file) -> {
            try (var writer = new FileWriter(file)) {
                writer.write(getText());
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    public void resetText() {
        setText(initialText);
        initTextWidgets();
    }

    public void clearText() {
        currentTextLines.clear();
        initTextWidgets();
    }

    public String getText() {
        StringBuilder concatenatedText = new StringBuilder();
        for (String line : currentTextLines) {
            concatenatedText.append(line).append("\n");
        }
        return concatenatedText.toString().replaceFirst("\n$", "");
    }

    public void setText(String text) {
        currentTextLines.addAll(List.of(text.split("\n")));
    }

    public void reformatText() {
        String ogText = getText();
        String text = ogText;
        text = text.replaceAll("\r\n", "\n");
        text = text.replaceAll("\n\n+", "\n\n");
        text = text.replaceAll(" +", " ");
        if (!text.contentEquals(ogText)) {
            setText(text);
        }
        initTextWidgets();
    }

    // a variation of setDimensions but with width and height params being one value
    public void setDimensions(int x, int y, int widthHeight) {
        setDimensionsAndPosition((int) (widthHeight * 1.5), widthHeight, x, y);
        initTextWidgets();
    }

    @Override
    public Identifier getOriginalAssetIdentifier() {
        return identifier;
    }


    @Override
    public String assertFileTypeOnEnd(final String possiblyEndsWithFilenameAlready) {
        if (possiblyEndsWithFilenameAlready.endsWith(fileTypeEnd)) {
            return possiblyEndsWithFilenameAlready;
        }
        return possiblyEndsWithFilenameAlready + fileTypeEnd;
    }

    @Override
    public void renderSimple(final DrawContext context, final int x, final int y, final int x2, final int y2) {
        renderWidget(context, 0, 0, 0);
    }

    @Override
    protected void renderWidget(final DrawContext context, final int mouseX, final int mouseY, final float delta) {
        //render the background as 2 fills one white and inflated by 1 and one black
        context.fill(getX() - 1, getY() - 1, getRight() + 1, getBottom() + 1, Colors.WHITE);
        context.fill(getX(), getY(), getRight(), getBottom(), Colors.BLACK);

        //render each text field
        for (TextFieldWidget textField : textFields) {
            textField.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    protected void appendClickableNarrations(final NarrationMessageBuilder builder) {

    }
}
