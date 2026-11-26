package me.bluejay.levelheaded.client.screen;

import me.bluejay.levelheaded.core.data.ControlPoint;
import me.bluejay.levelheaded.core.data.PointKind;
import me.bluejay.levelheaded.core.data.PointStore;
import me.bluejay.levelheaded.SurveyState;
import me.bluejay.levelheaded.client.JobStore;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.TreeSet;

/**
 * Single point list. Purpose of a point lives in its description.
 * Shift extends a range, Ctrl toggles, Ctrl+A selects all.
 * Mouse wheel scrolls the view and does not move the selection.
 * mc262 uses event.key(); mc263 uses event.shortcutKey(). See keyPressed note.
 */
public final class PointsScreen extends Screen {

    private static final int ROW = 12;
    private static final int TOP = 36;
    private static final int BOTTOM_PAD = 36;
    private static final int LIST_W = 280;

    // GLFW (26.2) and SDL (26.3).
    private static final int KEY_UP = 1073741906;
    private static final int KEY_DOWN = 1073741905;
    private static final int KEY_HOME = 1073741898;
    private static final int KEY_END = 1073741901;
    private static final int KEY_PAGE_UP = 1073741899;
    private static final int KEY_PAGE_DOWN = 1073741902;
    private static final int KEY_A = 97;
    private static final int MOD_SHIFT = 1;
    private static final int MOD_CTRL = 2;

    private final Screen parent;
    private final PointStore store;
    private int scroll;
    private int selected;
    private int anchor;
    private boolean dragging;
    private final TreeSet<Integer> multi = new TreeSet<>();

    public PointsScreen(Screen parent, PointKind kind, PointStore store) {
        super(Component.literal("Points"));
        this.parent = parent;
        this.store = store;
    }

    @Override
    protected void init() {
        int x = this.width / 2 - 120;
        int y = this.height - 28;
        this.addRenderableWidget(Button.builder(Component.literal("Back"),
                        b -> this.minecraft.gui.setScreen(parent))
                .bounds(x, y, 70, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("Occupy"),
                        b -> occupySelected())
                .bounds(x + 76, y, 70, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("Delete"),
                        b -> deleteSelected())
                .bounds(x + 152, y, 70, 20)
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
        if (this.minecraft.player != null) {
            this.minecraft.player.sendSystemMessage(Component.literal("Occupied " + p.id()));
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
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) return true;
        int mods = event.modifiers();
        boolean shift = (mods & 0x0003) != 0 || (mods & 1) != 0;
        boolean ctrl = (mods & 0x00C0) != 0 || (mods & 2) != 0 || (mods & 8) != 0;
        double mouseX = event.x();
        double mouseY = event.y();
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
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (dragging) {
            scrollFromTrack(event.y());
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        dragging = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int code = event.shortcutKey();
        boolean shift = (event.modifiers() & 1) != 0;
        boolean ctrl = (event.modifiers() & 2) != 0;
        if (ctrl && code == KEY_A) {
            selectAll();
            return true;
        }
        Integer next = null;
        if (code == KEY_UP) next = selected - 1;
        else if (code == KEY_DOWN) next = selected + 1;
        else if (code == KEY_HOME) next = 0;
        else if (code == KEY_END) next = visible().size() - 1;
        else if (code == KEY_PAGE_UP) next = selected - rowsThatFit();
        else if (code == KEY_PAGE_DOWN) next = selected + rowsThatFit();
        if (next != null) {
            if (shift) selectRange(next);
            else selectOnly(next);
            return true;
        }
        return super.keyPressed(event);
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
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        List<ControlPoint> list = visible();
        int x = listX();
        int rows = rowsThatFit();
        graphics.text(this.font, list.size() + " points  (" + multi.size() + " selected)",
                x, 20, 0xFFA0A0A0, false);
        if (list.isEmpty()) {
            graphics.text(this.font, "No points. Press V to save the live shot.",
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
                graphics.fill(x - 2, rowY - 1, x + LIST_W, rowY + ROW - 1, 0xC0406090);
            }
            int color = isSelected(i) ? 0xFFFFFFFF : 0xFFC0C0C0;
            graphics.text(this.font, line, x, rowY, color, false);
        }
        drawScrollbar(graphics, rows, list.size());
    }

    private void drawScrollbar(GuiGraphicsExtractor graphics, int rows, int total) {
        if (total <= rows) return;
        int tx = trackX();
        int trackY = TOP;
        int trackH = rows * ROW;
        graphics.fill(tx, trackY, tx + 4, trackY + trackH, 0x40000000);
        int thumbH = Math.max(8, trackH * rows / total);
        int thumbY = trackY + (trackH - thumbH) * scroll / Math.max(1, maxScroll());
        graphics.fill(tx, thumbY, tx + 4, thumbY + thumbH, 0xFFA0A0A0);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}