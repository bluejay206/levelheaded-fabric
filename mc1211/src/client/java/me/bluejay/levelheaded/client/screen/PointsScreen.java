package me.bluejay.levelheaded.client.screen;

import me.bluejay.levelheaded.core.data.ControlPoint;
import me.bluejay.levelheaded.core.data.PointKind;
import me.bluejay.levelheaded.core.data.PointStore;
import me.bluejay.levelheaded.SurveyState;
import me.bluejay.levelheaded.client.JobStore;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.TreeSet;

/**
 * Single point list. Purpose of a point lives in its description.
 * Shift extends a range, Ctrl toggles, Ctrl+A selects all.
 * Mouse wheel scrolls the view and does not move the selection.
 */
public final class PointsScreen extends Screen {

    private static final int ROW = 12;
    private static final int TOP = 36;
    private static final int BOTTOM_PAD = 36;
    private static final int LIST_W = 280;

    private final Screen parent;
    private final PointStore store;
    private int scroll;
    private int selected;
    private int anchor;
    private boolean dragging;
    private final TreeSet<Integer> multi = new TreeSet<>();

    public PointsScreen(Screen parent, PointKind kind, PointStore store) {
        super(Text.literal("Points"));
        this.parent = parent;
        this.store = store;
    }

    @Override
    protected void init() {
        int x = this.width / 2 - 120;
        int y = this.height - 28;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Back"),
                        b -> this.client.setScreen(parent))
                .dimensions(x, y, 70, 20)
                .build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Occupy"),
                        b -> occupySelected())
                .dimensions(x + 76, y, 70, 20)
                .build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Delete"),
                        b -> deleteSelected())
                .dimensions(x + 152, y, 70, 20)
                .build());
    }

    private List<ControlPoint> visible() {
        return store.list();
    }

    private int rowsThatFit() {
        int h = this.height - TOP - BOTTOM_PAD;
        return Math.max(1, h / ROW);
    }

    private int maxScroll() {
        return Math.max(0, visible().size() - rowsThatFit());
    }

    private int listX() {
        return this.width / 2 - 140;
    }

    private int trackX() {
        return this.width / 2 + 150;
    }

    private void select(int index) {
        int size = visible().size();
        if (size == 0) {
            selected = 0;
            scroll = 0;
            return;
        }
        selected = Math.max(0, Math.min(size - 1, index));
        if (selected < scroll) scroll = selected;
        int rows = rowsThatFit();
        if (selected >= scroll + rows) scroll = selected - rows + 1;
        scroll = Math.max(0, Math.min(maxScroll(), scroll));
    }

    private void selectOnly(int index) {
        select(index);
        anchor = selected;
        multi.clear();
        multi.add(selected);
    }

    private void toggle(int index) {
        int size = visible().size();
        if (size == 0) return;
        index = Math.max(0, Math.min(size - 1, index));
        if (multi.contains(index)) {
            multi.remove(index);
            if (selected == index && !multi.isEmpty()) {
                selected = multi.first();
            }
        } else {
            multi.add(index);
            selected = index;
            anchor = index;
        }
        // keep the row on screen, but do not clear the rest of the selection
        if (index < scroll) scroll = index;
        int rows = rowsThatFit();
        if (index >= scroll + rows) scroll = index - rows + 1;
        scroll = Math.max(0, Math.min(maxScroll(), scroll));
    }

    private void selectRange(int index) {
        select(index);
        multi.clear();
        int lo = Math.min(anchor, selected);
        int hi = Math.max(anchor, selected);
        for (int i = lo; i <= hi; i++) multi.add(i);
    }

    private void selectAll() {
        multi.clear();
        int size = visible().size();
        for (int i = 0; i < size; i++) multi.add(i);
        if (!multi.isEmpty()) {
            anchor = multi.first();
            selected = multi.last();
            select(selected);
        }
    }

    private boolean isSelected(int index) {
        return multi.contains(index);
    }

    private void occupySelected() {
        List<ControlPoint> list = visible();
        if (list.isEmpty()) return;
        ControlPoint p = list.get(Math.min(selected, list.size() - 1));
        SurveyState.setOccupy(p.easting(), p.elevation(), -p.northing(), p.id());
        JobStore.save();
        if (this.client != null && this.client.inGameHud != null) {
            this.client.inGameHud.setOverlayMessage(
                    Text.literal("Occupied " + p.id()), false);
        }
    }

    private void deleteSelected() {
        List<ControlPoint> list = visible();
        if (list.isEmpty() || multi.isEmpty()) return;
        for (int i : multi.descendingSet()) {
            if (i >= 0 && i < list.size()) store.remove(list.get(i).id());
        }
        JobStore.save();
        multi.clear();
        selectOnly(Math.min(selected, Math.max(0, visible().size() - 1)));
    }

    private boolean onScrollbar(double mouseX, double mouseY) {
        if (visible().size() <= rowsThatFit()) return false;
        int trackY = TOP;
        int trackH = rowsThatFit() * ROW;
        int tx = trackX();
        return mouseX >= tx - 2 && mouseX <= tx + 6
                && mouseY >= trackY && mouseY <= trackY + trackH;
    }

    private void scrollFromTrack(double mouseY) {
        int total = visible().size();
        int rows = rowsThatFit();
        if (total <= rows) return;
        int trackY = TOP;
        int trackH = rows * ROW;
        double t = (mouseY - trackY) / Math.max(1, trackH);
        t = Math.max(0, Math.min(1, t));
        scroll = (int) Math.round(t * maxScroll());
        scroll = Math.max(0, Math.min(maxScroll(), scroll));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        boolean shift = hasShiftDown();
        boolean ctrl = hasControlDown();
        if (onScrollbar(mouseX, mouseY)) {
            dragging = true;
            scrollFromTrack(mouseY);
            return true;
        }
        int x = listX();
        if (mouseX >= x && mouseX < x + LIST_W && mouseY >= TOP) {
            int row = (int) ((mouseY - TOP) / ROW);
            if (row >= 0 && row < rowsThatFit() && scroll + row < visible().size()) {
                int index = scroll + row;
                if (shift) selectRange(index);
                else if (ctrl) toggle(index);
                else selectOnly(index);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            scrollFromTrack(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean shift = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
        boolean ctrl = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        if (ctrl && keyCode == GLFW.GLFW_KEY_A) {
            selectAll();
            return true;
        }
        Integer next = null;
        if (keyCode == GLFW.GLFW_KEY_UP) next = selected - 1;
        else if (keyCode == GLFW.GLFW_KEY_DOWN) next = selected + 1;
        else if (keyCode == GLFW.GLFW_KEY_HOME) next = 0;
        else if (keyCode == GLFW.GLFW_KEY_END) next = visible().size() - 1;
        else if (keyCode == GLFW.GLFW_KEY_PAGE_UP) next = selected - rowsThatFit();
        else if (keyCode == GLFW.GLFW_KEY_PAGE_DOWN) next = selected + rowsThatFit();
        if (next != null) {
            if (shift) selectRange(next);
            else selectOnly(next);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (vertical > 0) {
            scroll = Math.max(0, scroll - 1);
        } else if (vertical < 0) {
            scroll = Math.min(maxScroll(), scroll + 1);
        }
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float partialTick) {
        super.render(context, mouseX, mouseY, partialTick);
        List<ControlPoint> list = visible();
        int x = listX();
        int rows = rowsThatFit();
        context.drawText(this.textRenderer, list.size() + " points  (" + multi.size() + " selected)",
                x, 20, 0xFFA0A0A0, false);
        if (list.isEmpty()) {
            context.drawText(this.textRenderer, "No points. Press V to save the live shot.",
                    x, TOP, 0xFF808080, false);
            return;
        }
        for (int shown = 0; shown < rows && scroll + shown < list.size(); shown++) {
            int i = scroll + shown;
            ControlPoint p = list.get(i);
            String line = String.format("%s  N %.2f  E %.2f  Z %.2f  %s",
                    p.id(), p.northing(), p.easting(), p.elevation(), p.description());
            int rowY = TOP + shown * ROW;
            if (isSelected(i)) {
                context.fill(x - 2, rowY - 1, x + LIST_W, rowY + ROW - 1, 0xC0406090);
            }
            int color = isSelected(i) ? 0xFFFFFFFF : 0xFFC0C0C0;
            context.drawText(this.textRenderer, line, x, rowY, color, false);
        }
        drawScrollbar(context, rows, list.size());
    }

    private void drawScrollbar(DrawContext context, int rows, int total) {
        if (total <= rows) return;
        int tx = trackX();
        int trackY = TOP;
        int trackH = rows * ROW;
        context.fill(tx, trackY, tx + 4, trackY + trackH, 0x40000000);
        int thumbH = Math.max(8, trackH * rows / total);
        int thumbY = trackY + (trackH - thumbH) * scroll / Math.max(1, maxScroll());
        context.fill(tx, thumbY, tx + 4, thumbY + thumbH, 0xFFA0A0A0);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}