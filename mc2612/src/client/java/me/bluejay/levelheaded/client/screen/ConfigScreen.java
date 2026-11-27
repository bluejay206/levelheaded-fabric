package me.bluejay.levelheaded.client.screen;

import me.bluejay.levelheaded.HudSettings;
import me.bluejay.levelheaded.ShotRate;
import me.bluejay.levelheaded.SurveyState;
import me.bluejay.levelheaded.core.data.DistanceScale;
import me.bluejay.levelheaded.core.data.Units;
import me.bluejay.levelheaded.core.math.projection.Projections;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for the {@code /lh} settings: units, scale, projection, HUD angle,
 * and the shot-rate limiter. Content scrolls; the Back button stays pinned.
 */
public final class ConfigScreen extends Screen {

    private final Screen parent;
    private EditBox groundFactor;
    private EditBox rateBox;
    private Button status;
    private final List<AbstractWidget> content = new ArrayList<>();
    private final List<Integer> baseY = new ArrayList<>();
    private int scrollY;
    private int contentHeight;

    public ConfigScreen(Screen parent) {
        super(Component.literal("Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        content.clear();
        baseY.clear();
        int x = this.width / 2 - 160;
        int y = 28;
        int w = 70;
        int h = 20;
        int gap = 4;

        addLabel(x, y, "Units");
        y += 16;
        add(x, y, w, h, "m", () -> setUnits(Units.METERS));
        add(x + (w + gap), y, w, h, "ft", () -> setUnits(Units.FEET));
        add(x + 2 * (w + gap), y, w, h, "ch", () -> setUnits(Units.CHAINS));
        add(x + 3 * (w + gap), y, w, h, "mi", () -> setUnits(Units.MILES));

        y += h + 14;
        addLabel(x, y, "Scale");
        y += 16;
        add(x, y, w, h, "Grid", () -> { SurveyState.SCALE.setGrid(); refresh(); });
        add(x + (w + gap), y, w + 10, h, "Geodesic", () -> { SurveyState.SCALE.setGeodesic(); refresh(); });
        add(x + 2 * (w + gap) + 10, y, w, h, "Ground", this::applyGround);

        y += h + 8;
        groundFactor = new EditBox(this.font, x, y, 80, h, Component.literal("Factor"));
        groundFactor.setValue(String.format("%.4f", SurveyState.SCALE.factor()));
        addContent(groundFactor, y);
        add(x + 86, y, 70, h, "Set", this::applyGround);

        y += h + 14;
        addLabel(x, y, "Projection");
        y += 16;
        add(x, y, 100, h, "WebMercator", () -> { Projections.setById("webmercator"); refresh(); });
        add(x + 104, y, 110, h, "Equirectangular", () -> { Projections.setById("equirectangular"); refresh(); });
        add(x + 218, y, 90, h, "EqualEarth", () -> { Projections.setById("equalearth"); refresh(); });

        y += h + 14;
        addLabel(x, y, "HUD angle");
        y += 16;
        add(x, y, 90, h, "Azimuth", () -> {
            HudSettings.setAngleMode(HudSettings.AngleMode.AZIMUTH);
            refresh();
        });
        add(x + 94, y, 90, h, "Bearing", () -> {
            HudSettings.setAngleMode(HudSettings.AngleMode.BEARING);
            refresh();
        });
        add(x + 188, y, 70, h, "Toggle", () -> {
            HudSettings.toggleAngle();
            refresh();
        });

        y += h + 14;
        addLabel(x, y, "Shot rate (Hz, 0 = unlimited)");
        y += 16;
        rateBox = new EditBox(this.font, x, y, 60, h, Component.literal("Hz"));
        rateBox.setValue(Integer.toString(ShotRate.rateHz()));
        addContent(rateBox, y);
        add(x + 66, y, 50, h, "Set", this::applyRate);
        add(x + 120, y, 70, h, "Unlimited", () -> {
            ShotRate.setRateHz(0);
            rateBox.setValue("0");
            refresh();
        });

        y += h + 16;
        status = Button.builder(Component.literal(statusText()), b -> {})
                .bounds(x, y, 320, h)
                .build();
        status.active = false;
        addContent(status, y);

        contentHeight = y + h + 8;
        applyScroll();

        this.addRenderableWidget(Button.builder(Component.literal("Back"),
                        b -> this.minecraft.gui.setScreen(parent))
                .bounds(this.width / 2 - 50, this.height - 28, 100, 20)
                .build());
    }

    private void add(int x, int y, int w, int h, String label, Runnable action) {
        Button b = Button.builder(Component.literal(label), btn -> action.run())
                .bounds(x, y, w, h)
                .build();
        addContent(b, y);
    }

    private void addLabel(int x, int y, String text) {
        Button caption = Button.builder(Component.literal(text), b -> {})
                .bounds(x, y, 220, 20)
                .build();
        caption.active = false;
        addContent(caption, y);
    }

    private void addContent(AbstractWidget widget, int naturalY) {
        content.add(widget);
        baseY.add(naturalY);
        this.addRenderableWidget(widget);
    }

    private int maxScroll() {
        return Math.max(0, contentHeight - (this.height - 40));
    }

    private void applyScroll() {
        scrollY = Math.max(0, Math.min(maxScroll(), scrollY));
        int viewTop = 20;
        int viewBottom = this.height - 36;
        for (int i = 0; i < content.size(); i++) {
            AbstractWidget w = content.get(i);
            int y = baseY.get(i) - scrollY;
            boolean onScreen = y >= viewTop && y + w.getHeight() <= viewBottom;
            w.visible = onScreen;
            if (onScreen) w.setY(y);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        scrollY -= (int) (vertical * 12);
        applyScroll();
        return true;
    }

    private void setUnits(Units units) {
        SurveyState.ENGINE.setUnits(units);
        refresh();
    }

    private void applyGround() {
        try {
            double f = Double.parseDouble(groundFactor.getValue().trim());
            SurveyState.SCALE.setGround(f);
        } catch (NumberFormatException ignored) {
            // leave scale unchanged
        }
        refresh();
    }

    private void applyRate() {
        try {
            ShotRate.setRateHz(Integer.parseInt(rateBox.getValue().trim()));
        } catch (NumberFormatException ignored) {
            // leave rate unchanged
        }
        refresh();
    }

    private void refresh() {
        if (status != null) {
            status.setMessage(Component.literal(statusText()));
        }
    }

    private String statusText() {
        DistanceScale s = SurveyState.SCALE;
        return SurveyState.ENGINE.getUnits().symbol()
                + " | " + s.statusLine()
                + " | " + Projections.current().id()
                + " | " + HudSettings.statusLine()
                + " | " + ShotRate.statusLine();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}