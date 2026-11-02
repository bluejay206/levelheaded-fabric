package me.bluejay.levelheaded.client;

import me.bluejay.levelheaded.ShotDescription;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * Description entry. Java uses keyPressed(int,int,int).
 */
public final class DescriptionScreen extends Screen {

    private TextFieldWidget box;

    public DescriptionScreen() {
        super(Text.literal("Shot Description"));
    }

    @Override
    protected void init() {
        int w = 240;
        int x = (this.width - w) / 2;
        int y = this.height / 2 - 24;

        box = new TextFieldWidget(this.textRenderer, x, y, w, 20, Text.literal("Description"));
        box.setMaxLength(80);
        box.setText(ShotDescription.get());
        this.addDrawableChild(box);
        this.setInitialFocus(box);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Set"), b -> submit())
                .dimensions(x, y + 28, 110, 20)
                .build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Clear"), b -> {
                    ShotDescription.set("");
                    this.close();
                })
                .dimensions(x + 130, y + 28, 110, 20)
                .build());
    }

    private void submit() {
        ShotDescription.set(box.getText());
        this.close();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            submit();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
