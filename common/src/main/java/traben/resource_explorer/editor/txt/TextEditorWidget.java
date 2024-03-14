package traben.resource_explorer.editor.txt;

import com.google.gson.*;
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
import traben.resource_explorer.ResourceExplorerClient;
import traben.resource_explorer.editor.ExportableFileContainerAndPreviewer;
import traben.resource_explorer.explorer.ExplorerUtils;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class TextEditorWidget extends ClickableWidget implements ExportableFileContainerAndPreviewer {


    protected final List<String> currentTextLines = new ArrayList<>();
    private final Identifier identifier;
    private final String initialText;
    private final String fileExtension;
    List<TextFieldWidgetWithIndex> textFields = new ArrayList<>();
    int topLineOfEditor = 0;
    final int textHeight = 12;
    private int textOffset = 0;

    public TextEditorWidget(@NotNull Identifier identifier, @NotNull String initialText, @NotNull String fileExtension) {
        super(0, 0, 1, 1, Text.of(""));


        if (fileExtension.isEmpty()) throw new IllegalArgumentException("fileTypeEnd must not be empty");
        if (!fileExtension.startsWith(".")) fileExtension = "." + fileExtension;

        this.identifier = identifier;
        this.fileExtension = fileExtension;

        if (isJsonFormat()) {
            setText(initialText);
            formatText();
            this.initialText = getText();
            updateTextWidgets();
        } else {

            this.initialText = initialText
                    // format out weird whitespaces for our editor
                    .replace("\r\n", "\n")
                    .replace("\r", "\n")
                    // use spaces in our editor just as a visual preference with the limited horizontal space and
                    // odd horizontal scrolling per line behaviour
                    .replace("\t", " ");
            resetText();
        }


    }

    public boolean isJsonFormat() {
        return ".json".equals(fileExtension) || ".jem".equals(fileExtension) || ".jpm".equals(fileExtension);
    }

    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double amount) {
        // if vertical amount isn't zero tick the top line of the editor up or down clamping the minimum to 0
        if (amount != 0) {
            topLineOfEditor = MathHelper.clamp(topLineOfEditor + (amount > 0 ? -1 : 1), 0, currentTextLines.size() - textFields.size());
            updateTextWidgets();
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
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

    void clearFocusedFields() {
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
                textField.setCursorToStart();//false?
                return textField.mouseClicked(mouseX, mouseY, button);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        var textField = getTextFieldFocused();
        if (textField != null) {
            int displayLine = textField.getIndexInDisplayList();
            switch (keyCode) {
                case 265 -> {//up
                    moveUpLine(displayLine, textField, textField.getCursor());
                    return true;
                }
                case 264 -> {//down
                    moveDownLine(displayLine, textField, textField.getCursor());
                    return true;
                }
                //right will move to the next line if at the end of the text
                case 262 -> {
                    String text = textField.getText();
                    if (textField.getCursor() >= text.length()) {
                        moveDownLine(displayLine, textField, 0);
                        textField.setCursorToStart();//false?
                        return true;
                    }
                }
                //same with left but to the previous line
                case 263 -> {
                    if (textField.getCursor() <= 0) {
                        moveUpLine(displayLine, textField, Integer.MAX_VALUE);
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
                    int actualLine = textField.getActualLineIndex();
                    clearChangedListeners();
                    setTextOfActualTextLine(actualLine, textBeforeCursor);
                    insertNewActualTextLine(actualLine + 1, textAfterCursor);
                    updateTextWidgets();
                    moveDownLine(displayLine, textField, 0);
                    return true;
                }
                //backspace will remove this line if the line is empty
                // it will alternatively append this line to the end of the previous line if not empty
                case 259 -> {
                    String text = textField.getText();
                    int actualLine = textField.getActualLineIndex();
                    if (actualLine > 0) {
                        if (text.isEmpty()) {
                            clearChangedListeners();
                            currentTextLines.remove(actualLine);
                            updateTextWidgets();
                            moveUpLine(displayLine, textField, Integer.MAX_VALUE);
                            return true;
                        } else if (textField.getCursor() <= 0) {
                            String previousLine = currentTextLines.get(actualLine - 1);
                            clearChangedListeners();
                            currentTextLines.set(actualLine - 1, previousLine + text);
                            currentTextLines.remove(actualLine);
                            updateTextWidgets();
                            moveUpLine(displayLine, textField, previousLine.length());
                            return true;
                        }
                    }
                }
                //delete will remove this line if the line is empty
                // or append the next line to the end of this line if not empty
                case 261 -> {
                    String text = textField.getText();
                    int actualLine = textField.getActualLineIndex();
                    if (actualLine < currentTextLines.size() - 1) {
                        if (text.isEmpty()) {
                            clearChangedListeners();
                            currentTextLines.remove(actualLine);
                            updateTextWidgets();
                            textFields.get(displayLine).setFocused(true);
                            return true;
                        } else if (textField.getCursor() >= text.length()) {
                            String nextLine = currentTextLines.get(actualLine + 1);
                            clearChangedListeners();
                            currentTextLines.set(actualLine, text + nextLine);
                            currentTextLines.remove(actualLine + 1);
                            updateTextWidgets();
                            textFields.get(displayLine).setFocused(true);
                            textFields.get(displayLine).setCursor(text.length());//false?
                            return true;
                        }
                    }
                }
                //page down will move the vertical offset down by the text field index length
                case 267 -> {
                    if (topLineOfEditor >= currentTextLines.size() - textFields.size()) return true;
                    topLineOfEditor += textFields.size() - 1;
                    updateTextWidgets();
                    textFields.get(displayLine).setFocused(true);
                    return true;
                }
                //page up will move the vertical offset up by the text field index length
                case 266 -> {
                    if (topLineOfEditor <= 0) return true;
                    topLineOfEditor = Math.max(0, topLineOfEditor - textFields.size() + 1);
                    updateTextWidgets();
                    textFields.get(displayLine).setFocused(true);
                    return true;
                }
                //tab will insert only 1 space as a style choice
                case 258 -> {
                    charTyped(' ', 0);
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
            updateTextWidgets();
            newField = textFields.get(0);
        } else {
            newField = textFields.get(line - 1);
            oldField.setFocused(false);
            oldField.setCursorToStart();//false?
        }
        newField.setFocused(true);
        newField.setCursor(cursor);//false?
    }

    private void moveDownLine(int line, TextFieldWidgetWithIndex oldField, int cursor) {
        TextFieldWidgetWithIndex newField;
        if (line >= textFields.size() - 1) {
            if (topLineOfEditor >= currentTextLines.size() - textFields.size()) return;
            topLineOfEditor++;
            updateTextWidgets();
            newField = textFields.get(textFields.size() - 1);
        } else {
            newField = textFields.get(line + 1);
            oldField.setFocused(false);
            oldField.setCursorToStart();//false?
        }
        newField.setFocused(true);
        newField.setCursor(cursor);//false?
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

    protected void updateTextWidgets() {
        //ensure no remaining pointers in the old text fields
        clearChangedListeners();

        List<TextFieldWidgetWithIndex> textFields = new ArrayList<>();

        //calculate space for line numbers
        int widthTest = Math.max(currentTextLines.size(), 100);
        textOffset = MinecraftClient.getInstance().textRenderer.getWidth(String.valueOf(widthTest)) + 3;

        int amount = this.height / textHeight;
        for (int displayIndex = 0; displayIndex < amount; displayIndex++) {
            textFields.add(getTextFieldWidgetWithIndex(displayIndex));
        }
        this.textFields = textFields;
    }

    @NotNull
    TextFieldWidgetWithIndex getTextFieldWidgetWithIndex(final int displayIndex) {
        if (topLineOfEditor < 0) topLineOfEditor = 0;
        final int lineInActualText = displayIndex + topLineOfEditor;

        TextFieldWidgetWithIndex textField = new TextFieldWidgetWithIndex(displayIndex, lineInActualText, textOffset, getX(), getY() + (displayIndex * textHeight), width, textHeight);

        //set text for the line
        if (currentTextLines.size() > lineInActualText) {
            textField.setText(currentTextLines.get(lineInActualText));
            textField.setCursorToStart();//false?
        } else {
            textField.setText("");
        }

        textField.setChangedListener((string) -> this.setTextOfActualTextLine(lineInActualText, string));
        return textField;
    }

    //a method called when the text is changed
    // uses the int index to apply changes to that line in the currentTextLines list
    public void setTextOfActualTextLine(int index, String newText) {
        //System.out.println("set@"+index+": "+newText);
        if (index < 0) return;
        while (currentTextLines.size() <= index) {
            currentTextLines.add("");
        }
        currentTextLines.set(index, newText);
    }

    //same method as above but this one is called when a new line is inserted shifting all lines after it down
    public void insertNewActualTextLine(int index, String newText) {

        //System.out.println("insert@ "+index+": "+newText);
        if (index < 0) return;
        while (currentTextLines.size() <= index) {
            currentTextLines.add("");
        }
        currentTextLines.add(index, newText);
    }

    //set all text field change listeners to null
    private void clearChangedListeners() {
        for (TextFieldWidget textField : textFields) {
            textField.setChangedListener(null);
        }
    }


    //clears all references to text fields
    void clear() {
        clearChangedListeners();
        textFields.clear();
    }

    @Override
    public boolean exportAsIdentifier(final Identifier identifier) {
        return ExplorerUtils.outputResourceToPackInternal(identifier == null ? this.identifier : identifier, (file) -> {
            try (var writer = new FileWriter(file)) {
                writer.write(getText());
                ResourceExplorerClient.log("Exported file: " + file.getAbsolutePath());
                return true;
            } catch (Exception e) {
                ResourceExplorerClient.log("Failed to export file: " + file.getAbsolutePath() + ",\nbecause of an exception: " + e.getMessage());
                return false;
            }
        });
    }

    public void resetText() {
        setText(initialText);
        updateTextWidgets();
    }

    public void clearText() {
        currentTextLines.clear();
        updateTextWidgets();
    }

    public String getText() {
        StringBuilder concatenatedText = new StringBuilder();
        for (String line : currentTextLines) {
            concatenatedText.append(line).append("\n");
        }
        return concatenatedText.toString().replaceFirst("\n$", "");
    }

    public void setText(String text) {
        currentTextLines.clear();
        currentTextLines.addAll(List.of(text.split("\n")));
    }


    //method to stylise text according to .json formatting
    public void formatText() {
        String input = getText();
        if (input.isBlank()) setText("{\n\n}");
        try {
            Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
            JsonElement el = gson.fromJson(input, JsonElement.class);
            String output = gson.toJson(el);
            if (output != null && !output.isEmpty() && !output.equals(input)) {
                if ("null".equals(output)) output = "{\n\n}";
                setText(output);
                updateTextWidgets();
            }
        } catch (Exception e) {
            ResourceExplorerClient.log("Failed to format text: " + e.getMessage());
        }
    }


    public boolean isValidJson() {
        String input = getText();
        if (input.isEmpty()) return true;
        try {
            JsonElement el = JsonParser.parseString(input);
            if (el != null && !el.isJsonNull()) {
                return true;
            } else {
                ResourceExplorerClient.log("Json parsed into empty result");
                return false;
            }
        } catch (JsonSyntaxException e) {
            ResourceExplorerClient.log("Json was not valid: " + e.getMessage());
        }
        return false;
    }


    public void setDimensions(int x, int y, int widthHeight) {
//        setDimensionsAndPosition((int) (widthHeight * 1.5), widthHeight, x, y);
        setX(x);
        setY(y);
        setWidth((int) (widthHeight * 1.5));
        height=(widthHeight);
        updateTextWidgets();
    }

    @Override
    public Identifier getOriginalAssetIdentifier() {
        return identifier;
    }


//    @Override
//    public String assertFileTypeOnEnd(final String possiblyEndsWithFilenameAlready) {
//        if (isJsonFormat() &&
//                (possiblyEndsWithFilenameAlready.endsWith(".jem")
//                        || possiblyEndsWithFilenameAlready.endsWith(".jpm"))) {
//            return possiblyEndsWithFilenameAlready;
//        }
//        if (possiblyEndsWithFilenameAlready.endsWith(fileExtension)) {
//            return possiblyEndsWithFilenameAlready;
//        }
//        return possiblyEndsWithFilenameAlready + fileExtension;
//    }

    @Override
    public void renderSimple(final DrawContext context, final int x, final int y, final int x2, final int y2) {
        renderButton(context, 0, 0, 0);
    }

    @Override
    protected void renderButton(final DrawContext context, final int mouseX, final int mouseY, final float delta) {
        //render the background as 2 fills one white and inflated by 1 more and one black
        context.fill(getX() - 2, getY() - 2, getRight() + 2, getBottom() + 2, Colors.WHITE);
        context.fill(getX() - 1, getY() - 1, getRight() + 1, getBottom() + 1, Colors.BLACK);


        //render each text field
        for (TextFieldWidget textField : textFields) {
            textField.render(context, mouseX, mouseY, delta);
        }

        //line number separator
        context.fill(getX() + textOffset - 2, getY() - 1, getX() + textOffset - 1, getBottom() + 1, -8355712);

    }

    int getRight() {
        return getX() + getWidth();
    }
    int getBottom() {
        return getY() + getHeight();
    }

    @Override
    protected void appendClickableNarrations(final NarrationMessageBuilder builder) {

    }

    static class DisplayOnly extends TextEditorWidget {

        private long lastTimeScrolled = 0;

        private DisplayOnly(@NotNull final Identifier identifier, @NotNull final String initialText, @NotNull final String fileExtension) {
            super(identifier, initialText, fileExtension);
        }

//        @Override
//        public boolean charTyped(final char chr, final int modifiers) {
//            return false;
//        }

        static DisplayOnly of(TextEditorWidget widget) {
            return new DisplayOnly(widget.identifier, widget.getText(), widget.fileExtension);
        }

        @Override
        public void renderSimple(final DrawContext context, final int x, final int y, final int x2, final int y2) {
            if (x != getX() || y != getY() || x2 != getRight() || y2 != getBottom()) {
//                setDimensionsAndPosition(x2 - x, y2 - y, x, y);
                setX(x);
                setY(y);
                setWidth(x2 - x);
                height=(y2 - y);
                updateTextWidgets();
            }
            if (System.currentTimeMillis() > lastTimeScrolled + 1000) {
                lastTimeScrolled = System.currentTimeMillis();
                topLineOfEditor = (topLineOfEditor + 1) % Math.max(1, currentTextLines.size() - textFields.size() + 1);
                updateTextWidgets();
            }

            super.renderSimple(context, x, y, x2, y2);
        }

//        @Override
//        public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
//            switch (keyCode) {
//                case 265, 264, 262, 263, 267, 266 -> {//navigation buttons only
//                    return super.keyPressed(keyCode, scanCode, modifiers);
//                }
//                default -> {
//                    return false;
//                }
//            }
//        }
//
//        @Override
//        @NotNull TextFieldWidgetWithIndex getTextFieldWidgetWithIndex(final int displayIndex) {
//            var textField = super.getTextFieldWidgetWithIndex(displayIndex);
//            textField.setTextPredicate((string) -> false);
//            textField.setChangedListener(null);
//            return textField;
//        }
    }
}
