package me.bluejay.client.hud;

import me.bluejay.client.data.SurveyPointManager;
import me.bluejay.math.SurveyMath;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public abstract class SurveyorHud {

    protected static final int BG_COLOR       = 0x1A000000;
    protected static final int TEXT_COLOR     = 0xCCFFFFFF;
    protected static final int TRACKING_COLOR = 0xCC00FF88;
    protected static final int SCOPE_COLOR    = 0xCC44CCFF;
    protected static final int SHOT_COLOR     = 0xCCFFAA00;
    protected static final int CUT_COLOR      = 0xCCFF5555;
    protected static final int FILL_COLOR     = 0xCC55FF55;

    protected static final int LINE_HEIGHT = 12;
    protected static final int PAD_X       = 6;
    protected static final int PAD_Y       = 4;
    protected static final int BG_WIDTH    = 210;

    protected SurveyMath.SurveyResult cachedResult = null;
    protected Vec3d frozenPos = null;
    protected Mode mode = Mode.LIVE;
    protected boolean hudEnabled = true;   // F key toggles entire HUD

    public enum Mode { LIVE, SHOT }

    public void toggleHud() {
        hudEnabled = !hudEnabled;
    }

    public void clearCache() {
        if (mode != Mode.SHOT) {
            cachedResult = null;
            frozenPos = null;
        }
    }

    public void requestUpdate() {
        if (mode == Mode.LIVE) {
            mode = Mode.SHOT;
            Vec3d occupy = getOccupyPoint();
            Vec3d moving = getMovingPosition(MinecraftClient.getInstance(), MinecraftClient.getInstance().player);
            if (occupy != null && moving != null) {
                cachedResult = SurveyMath.compute(occupy.x, occupy.y, occupy.z,
                        moving.x, moving.y, moving.z,
                        MinecraftClient.getInstance().world);
                frozenPos = moving;
            }
        } else {
            mode = Mode.LIVE;
            cachedResult = null;
            frozenPos = null;
        }
    }

    public void saveCurrentShotAndReset() {
        if (mode != Mode.SHOT || cachedResult == null) return;

        Vec3d shotPos = (frozenPos != null)
                ? frozenPos
                : getMovingPosition(MinecraftClient.getInstance(), MinecraftClient.getInstance().player);

        if (shotPos == null) return;

        String source = (this instanceof RodHud) ? "Rod Shot" : "Scope Shot";

        SurveyPointManager.addPoint(getOccupyPoint(), shotPos, cachedResult, source);
        me.bluejay.client.SurveyorSaysClient.showShotMessage(source);

        mode = Mode.LIVE;
        cachedResult = null;
        frozenPos = null;
    }

    public final void render(DrawContext context, RenderTickCounter tickCounter) {
        if (!hudEnabled) return;   // Completely hide HUD when toggled off

        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity player = mc.player;
        if (player == null || mc.world == null) return;

        if (!isHoldingTool(player)) {
            clearCache();
            return;
        }

        Vec3d occupy = getOccupyPoint();
        if (occupy == null || occupy.lengthSquared() < 0.01) {
            renderNoTransit(context, mc);
            return;
        }

        Vec3d movingPos = getMovingPosition(mc, player);
        if (movingPos == null) {
            renderNoValidTarget(context, mc);
            return;
        }

        updateResult(occupy, movingPos, mc.world);

        if (cachedResult == null) {
            renderNoTransit(context, mc);
        } else {
            renderHud(context, mc, cachedResult, movingPos);
        }
    }

    protected abstract boolean isHoldingTool(PlayerEntity player);
    protected abstract Vec3d getOccupyPoint();
    protected abstract Vec3d getMovingPosition(MinecraftClient mc, PlayerEntity player);

    protected void updateResult(Vec3d occupy, Vec3d movingPos, net.minecraft.world.World level) {
        if (mode == Mode.LIVE) {
            cachedResult = SurveyMath.compute(occupy.x, occupy.y, occupy.z,
                    movingPos.x, movingPos.y, movingPos.z, level);
            frozenPos = null;
        }
    }

    protected void renderHud(DrawContext context, MinecraftClient mc,
                             SurveyMath.SurveyResult r, Vec3d movingPos) {

        int x = 10;
        int y = 10;

        int linesShown = 7;
        int totalHeight = linesShown * LINE_HEIGHT + PAD_Y * 2;

        context.fill(x - PAD_X, y - PAD_Y,
                x - PAD_X + BG_WIDTH, y - PAD_Y + totalHeight, BG_COLOR);

        String modeLabel = getModeLabel();
        int modeColor = getModeColor();

        boolean isValid = isValidResult(r);

        String bgText = isValid ? r.bearingDMS() : "Out of range";
        String azText = isValid ? r.azimuthDMS() : "Out of range";
        String zaText = isValid ? r.zenithAngleDMS() : "Out of range";
        String sdText = isValid ? String.format("%.2f blks", r.slopeDistance()) : "∞";
        String hdText = isValid ? String.format("%.2f blks", r.horizontalDistance()) : "∞";

        Vec3d displayPos = (mode == Mode.SHOT && frozenPos != null) ? frozenPos : movingPos;

        String deltaN = isValid ? String.format("%+.2f", r.relativeNorthing()) : "—";
        String deltaE = isValid ? String.format("%+.2f", r.relativeEasting()) : "—";

        String cutFillText = "—";
        int cutFillColor = TEXT_COLOR;
        if (isValid) {
            cutFillText = r.cutFillString(displayPos.y) + " blks";
            cutFillColor = getCutFillColor(cutFillText);
        }

        String[] lines = {
                modeLabel,
                " Bg: " + bgText,
                " Az: " + azText,
                " ZA: " + zaText,
                " SD: " + sdText + "  HD: " + hdText,
                " ΔN: " + deltaN + "  ΔE: " + deltaE,
                " C/F: " + cutFillText
        };

        for (int i = 0; i < lines.length; i++) {
            int color = (i == 0) ? modeColor
                    : (i == 6) ? cutFillColor
                    : TEXT_COLOR;

            context.drawText(mc.textRenderer, lines[i], x, y + i * LINE_HEIGHT, color, false);
        }
    }

    private int getCutFillColor(String cutFillStr) {
        if (cutFillStr.startsWith("C-")) return CUT_COLOR;
        if (cutFillStr.startsWith("F+")) return FILL_COLOR;
        return TEXT_COLOR;
    }

    protected void renderNoValidTarget(DrawContext context, MinecraftClient mc) {
        context.fill(4, 6, 190, 28, BG_COLOR);
        context.drawText(mc.textRenderer, "§e[ SurveyorSays ] §7Out of range", 10, 10, TEXT_COLOR, false);
    }

    protected void renderNoTransit(DrawContext context, MinecraftClient mc) {
        context.fill(4, 6, 190, 28, BG_COLOR);
        context.drawText(mc.textRenderer, "§e[ SurveyorSays ] §7No Transit Set", 10, 10, TEXT_COLOR, false);
    }

    private boolean isValidResult(SurveyMath.SurveyResult r) {
        return r != null && r.slopeDistance() < 10000.0 && !Double.isNaN(r.slopeDistance());
    }

    protected String getModeLabel() {
        boolean isLive = (mode == Mode.LIVE);
        int nextShot = SurveyPointManager.getNextShotNumber();

        if (this instanceof RodHud) {
            return isLive ? "§a[ TRACKING ]" : "§6[ SHOT #" + nextShot + " — Press V to Save ]";
        } else {
            return isLive ? "§b[ SCOPE MODE ]" : "§6[ SHOT #" + nextShot + " — Press V to Save ]";
        }
    }

    protected int getModeColor() {
        boolean isLive = (mode == Mode.LIVE);
        if (this instanceof RodHud) {
            return isLive ? TRACKING_COLOR : SHOT_COLOR;
        } else {
            return isLive ? SCOPE_COLOR : SHOT_COLOR;
        }
    }

    public Mode getMode() {
        return mode;
    }
}