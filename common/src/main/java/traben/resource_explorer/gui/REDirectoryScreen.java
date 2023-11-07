package traben.resource_explorer.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import org.jetbrains.annotations.Nullable;
import traben.resource_explorer.REConfig;

import java.util.LinkedList;

public class REDirectoryScreen extends Screen {

    @Nullable
    static public REResourceDisplayWidget currentDisplay = null;



    private REResourceListWidget fileList;
    private ButtonWidget doneButton;

    public Screen parent;


    final String cumulativePath;

    public final LinkedList<REResourceEntry> entries;
    public REDirectoryScreen(Screen parent, Text title, LinkedList<REResourceEntry> entries, String cumulativePath) {
        super(title);
        this.cumulativePath = cumulativePath;
        this.entries = entries;
        this.parent = parent;
    }

    protected void init() {
        if(currentDisplay == null) currentDisplay = new REResourceDisplayWidget(client,200, this.height);

        this.fileList = new REResourceListWidget(this.client, this, 200, this.height);
        this.fileList.setLeftPos(this.width / 2 - 4 - 200);
        this.addSelectableChild(this.fileList);

        currentDisplay.setDimensions(width / 2 + 4, 200, this.height);
        this.addSelectableChild(currentDisplay);

        this.doneButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
            this.close();
        }).dimensions(this.width / 2 + 4, this.height - 48, 150, 20).tooltip(Tooltip.of(Text.of("Return to tile screen"))) .build());

        Tooltip warn = Tooltip.of(Text.of("Applying a new filter will close the explorer and reload resources"));

        ButtonWidget apply = this.addDrawableChild(ButtonWidget.builder(Text.of("Apply"), (button) -> {
            this.close();
            REConfig.getInstance().filterMode = filterChoice;
        }).dimensions(this.width / 2 - 4 - 46, this.height - 48, 46, 20).tooltip(warn).build());
        apply.active = false;

        this.addDrawableChild(ButtonWidget.builder(Text.of( REConfig.getInstance().filterMode.getKey()), (button) -> {
            filterChoice = filterChoice.next();
            button.setMessage(Text.of(filterChoice.getKey()));
            apply.active = filterChoice != REConfig.getInstance().filterMode;
        }).dimensions(this.width / 2 - 4 - 200, this.height - 48, 150, 20).tooltip(warn).build());
    }

    private REConfig.REFileFilter filterChoice = REConfig.getInstance().filterMode;

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        this.fileList.render(context, mouseX, mouseY, delta);
        if(currentDisplay != null)
            currentDisplay.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 8, 16777215);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.of(cumulativePath), this.width / 2, 20, Colors.GRAY);
    }

    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(context);
    }

    @Override
    public void close() {
        this.fileList.close();
        super.close();
        currentDisplay = null;
        //reading resources this way has some... affects to the resource system
        //thus a resource reload is required
        MinecraftClient.getInstance().reloadResources();
    }



}
