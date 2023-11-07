package traben.resource_explorer.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import org.jetbrains.annotations.Nullable;

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
        }).dimensions(this.width / 2 + 4, this.height - 48, 150, 20).build());
//        this.refresh();

    }

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
        //MinecraftClient.getInstance().setScreen(parent);

        //reading resources this way has some... affects to the resource system
        //thus a resource reload is required
        MinecraftClient.getInstance().reloadResources();
    }


}
