package me.bluejay.levelheaded.client.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Placeholder for a data-collector section. */
public final class SectionScreen extends Screen {

    private final Screen parent;

    public SectionScreen(Screen parent, String title) {
        super(Text.literal(title));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int w = 160;
        int x = (this.width - w) / 2;
        int y = this.height / 2;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> this.client.setScreen(parent))
                .dimensions(x, y, w, 20)
                .build());
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}