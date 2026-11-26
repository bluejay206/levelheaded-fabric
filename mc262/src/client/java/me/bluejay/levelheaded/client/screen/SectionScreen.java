package me.bluejay.levelheaded.client.screen;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Placeholder for a data-collector section. */
public final class SectionScreen extends Screen {

    private final Screen parent;

    public SectionScreen(Screen parent, String title) {
        super(Component.literal(title));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int w = 160;
        int x = (this.width - w) / 2;
        int y = this.height / 2;
        this.addRenderableWidget(Button.builder(Component.literal("Back"), b -> this.minecraft.gui.setScreen(parent))
                .bounds(x, y, w, 20)
                .build());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}