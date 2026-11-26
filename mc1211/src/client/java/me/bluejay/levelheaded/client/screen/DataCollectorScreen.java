package me.bluejay.levelheaded.client.screen;

import me.bluejay.levelheaded.PointLists;
import me.bluejay.levelheaded.core.data.PointKind;
import me.bluejay.levelheaded.core.data.PointStore;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Common entry for survey configuration and the single point list.
 * Purpose of a point lives in its description, not in a separate list.
 */
public final class DataCollectorScreen extends Screen {

    private static final int BTN_W = 200;
    private static final int BTN_H = 20;
    private static final int GAP = 6;



    public DataCollectorScreen() {
        super(Text.literal("Data Collector"));
    }

    @Override
    protected void init() {
        int x = (this.width - BTN_W) / 2;
        int y = this.height / 2 - 30;

        add(x, y, "Configuration", () -> this.client.setScreen(new ConfigScreen(this)));
        add(x, y + (BTN_H + GAP), "Points", () ->
                this.client.setScreen(new PointsScreen(this, PointKind.TOPO, PointLists.STORE)));
        add(x, y + 2 * (BTN_H + GAP), "Job", () ->
                this.client.setScreen(new JobScreen(this)));

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Close"), b -> this.close())
                .dimensions(x, y + 3 * (BTN_H + GAP) + 8, BTN_W, BTN_H)
                .build());
    }

    private void add(int x, int y, String label, Runnable action) {
        this.addDrawableChild(ButtonWidget.builder(Text.literal(label), b -> action.run())
                .dimensions(x, y, BTN_W, BTN_H)
                .build());
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}