package me.bluejay.levelheaded.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.bluejay.levelheaded.ShotDescription;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

/**
 * Java form for the {@code set_description} action. Enter or Set stores
 * the text; Escape cancels. Bedrock uses its own form and writes the
 * same {@link ShotDescription}.
 *
 * 26.3 uses SDL scancodes ({@link InputConstants}), not GLFW.
 */
public final class DescriptionScreen extends Screen {

    private EditBox box;

    public DescriptionScreen() {
        super(Component.literal("Shot Description"));
    }

    @Override
    protected void init() {
        int w = 240;
        int x = (this.width - w) / 2;
        int y = this.height / 2 - 24;

        box = new EditBox(this.font, x, y, w, 20, Component.literal("Description"));
        box.setMaxLength(80);
        box.setValue(ShotDescription.get());
        this.addRenderableWidget(box);
        this.setInitialFocus(box);

        this.addRenderableWidget(Button.builder(Component.literal("Set"), b -> submit())
                .bounds(x, y + 28, 110, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("Clear"), b -> {
                    ShotDescription.set("");
                    this.onClose();
                })
                .bounds(x + 130, y + 28, 110, 20)
                .build());
    }

    private void submit() {
        ShotDescription.set(box.getValue());
        this.onClose();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_RETURN
                || event.key() == InputConstants.KEY_NUMPADENTER) {
            submit();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}