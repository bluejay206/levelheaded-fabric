package me.bluejay.levelheaded.client.screen;

import me.bluejay.levelheaded.PointLists;
import me.bluejay.levelheaded.core.data.PointKind;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Common entry for survey configuration, the point list, and the job.
 * Purpose of a point lives in its description, not in a separate list.
 */
public final class DataCollectorScreen extends Screen {

    private static final int BTN_W = 200;
    private static final int BTN_H = 20;
    private static final int GAP = 6;

    public DataCollectorScreen() {
        super(Component.literal("Data Collector"));
    }

    @Override
    protected void init() {
        int x = (this.width - BTN_W) / 2;
        int y = this.height / 2 - 40;

        add(x, y, "Configuration", () -> this.minecraft.gui.setScreen(new ConfigScreen(this)));
        add(x, y + (BTN_H + GAP), "Points", () ->
                this.minecraft.gui.setScreen(new PointsScreen(this, PointKind.TOPO, PointLists.STORE)));
        add(x, y + 2 * (BTN_H + GAP), "Job", () ->
                this.minecraft.gui.setScreen(new JobScreen(this)));

        this.addRenderableWidget(Button.builder(Component.literal("Close"), b -> this.onClose())
                .bounds(x, y + 3 * (BTN_H + GAP) + 8, BTN_W, BTN_H)
                .build());
    }

    private void add(int x, int y, String label, Runnable action) {
        this.addRenderableWidget(Button.builder(Component.literal(label), b -> action.run())
                .bounds(x, y, BTN_W, BTN_H)
                .build());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}