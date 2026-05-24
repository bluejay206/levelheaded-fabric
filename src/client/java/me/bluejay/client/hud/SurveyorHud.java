package me.bluejay.client.hud;

import me.bluejay.client.ClientTransitCache;
import me.bluejay.client.data.SurveyPoint;
import me.bluejay.client.data.SurveyPointManager;
import me.bluejay.levelheaded.math.SurveyMath;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.RaycastContext;

@Environment(EnvType.CLIENT)
public abstract class SurveyorHud {

    protected static final int BG_COLOR       = 0x1A000000;
    protected static final int TEXT_COLOR     = 0xCCFFFFFF;
    protected static final int TRACKING_COLOR = 0xCC00FF88;
    protected static final int SCOPE_COLOR    = 0xCC44CCFF;
    protected static final int ZOOM_COLOR     = 0xCCFFAA00;
    protected static final int SHOT_COLOR     = 0xCCFFAA00;
    protected static final int CUT_COLOR      = 0xCCFF5555;
    protected static final int FILL_COLOR     = 0xCC55FF55;

    protected static final int LINE_HEIGHT = 12;
    protected static final int PAD_X       = 6;
    protected static final int PAD_Y       = 4;
    protected static final int BG_WIDTH    = 235;

    // Shared static state
    protected static SurveyMath.SurveyResult cachedResult = null;
    protected static Vec3d frozenPos = null;
    protected static Mode mode = Mode.LIVE;
    protected static boolean hudEnabled = true;

    // Sticky Description
    protected static String stickyDescription = "";

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
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        Vec3d occupy = getOccupyPoint();
        if (occupy == null || occupy.lengthSquared() < 0.01) {
            cachedResult = null;
            return;
        }

        if (mode == Mode.SHOT && frozenPos != null) {
            return;
        }

        Vec3d moving = getMovingPosition(mc, mc.player);
        if (moving == null) {
            cachedResult = null;
            return;
        }

        double distance = occupy.distanceTo(moving);
        if (distance > 10000.0) {
            cachedResult = null;
            return;
        }

        cachedResult = SurveyMath.compute(occupy.x, occupy.y, occupy.z,
                moving.x, moving.y, moving.z, mc.world);

        if (mode != Mode.SHOT) {
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
        String desc = stickyDescription.isEmpty() ? "Survey Shot" : stickyDescription;

        SurveyPointManager.addPoint(getOccupyPoint(), shotPos, cachedResult, source, desc);

        MinecraftClient.getInstance().player.sendMessage(
                Text.literal("§a" + source + " saved! " + desc + " (Shot #" + SurveyPointManager.getNextShotNumber() + ")"),
                false
        );

        mode = Mode.LIVE;
        cachedResult = null;
        frozenPos = null;
    }

    public final void render(DrawContext context, RenderTickCounter tickCounter) {
        if (!hudEnabled) return;

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

        Vec3d displayPos = (mode == Mode.SHOT && frozenPos != null)
                ? frozenPos
                : getMovingPosition(mc, player);

        if (displayPos == null) {
            renderNoValidTarget(context, mc);
            return;
        }

        updateResult(occupy, displayPos, mc.world);

        if (cachedResult == null) {
            renderNoTransit(context, mc);
        } else {
            renderHud(context, mc, cachedResult, displayPos);
        }
    }

    protected abstract boolean isHoldingTool(PlayerEntity player);
    protected abstract Vec3d getOccupyPoint();
    protected abstract Vec3d getMovingPosition(MinecraftClient mc, PlayerEntity player);

    public boolean isHoldingToolPublic(PlayerEntity player) {
        return isHoldingTool(player);
    }

    protected void updateResult(Vec3d occupy, Vec3d movingPos, net.minecraft.world.World level) {
        requestUpdate();
    }

    protected void renderHud(DrawContext context, MinecraftClient mc,
                             SurveyMath.SurveyResult r, Vec3d movingPos) {

        int x = 10;
        int y = 10;

        int linesShown = (mode == Mode.SHOT) ? 8 : 7;
        int totalHeight = linesShown * LINE_HEIGHT + PAD_Y * 2;

        context.fill(x - PAD_X, y - PAD_Y,
                x - PAD_X + BG_WIDTH, y - PAD_Y + totalHeight, BG_COLOR);

        SurveyPoint.Unit unit = SurveyPoint.getCurrentUnit();
        String unitStr = unit.symbol;

        boolean isValid = isValidResult(r);

        String modeLabel = getModeLabel() + " §7(" + unit.name().toLowerCase() + ")";

        String bgText = isValid ? r.bearingDMS() : "Out of range";
        String azText = isValid ? r.azimuthDMS() : "Out of range";
        String zaText = isValid ? r.zenithAngleDMS() : "Out of range";

        double scale = unit.scale;
        String sdText = isValid ? String.format("%.2f %s", r.slopeDistance() * scale, unitStr) : "∞";
        String hdText = isValid ? String.format("%.2f %s", r.horizontalDistance() * scale, unitStr) : "∞";

        String deltaN = isValid ? String.format("%+.2f %s", r.relativeNorthing() * scale, unitStr) : "—";
        String deltaE = isValid ? String.format("%+.2f %s", r.relativeEasting() * scale, unitStr) : "—";

        String cutFillText = "—";
        int cutFillColor = TEXT_COLOR;
        if (isValid) {
            double diffMeters = movingPos.y - r.transitElevation();
            double diff = diffMeters * scale;
            if (Math.abs(diff) < 0.01) {
                cutFillText = "0.00 " + unitStr;
            } else {
                cutFillText = diff < 0
                        ? String.format("C-%.2f %s", Math.abs(diff), unitStr)
                        : String.format("F+%.2f %s", diff, unitStr);
            }
            cutFillColor = getCutFillColor(cutFillText);
        }

        String descText = stickyDescription.isEmpty() ? "Survey Shot" : stickyDescription;

        // === NEW: Player Heading (current facing direction) ===
        float yaw = mc.player != null ? mc.player.getYaw() : 0;
        int heading = (int) ((yaw + 180) % 360 + 360) % 360;  // Normalize to 0-359, Corrected to standard compass (0° = North)



        String[] lines = {
                modeLabel,
                " Bearing: " + bgText,
                " Azimuth: " + azText,
                " Zenith: " + zaText,
                " Slope Dist: " + sdText + "  Horiz. Dist: " + hdText,
                " ΔN: " + deltaN + "  ΔE: " + deltaE,
                " C/F: " + cutFillText,
        };

        if (mode == Mode.SHOT) {
            lines = appendDescriptionLine(lines, " Desc: " + descText);
            lines = appendDescriptionLine(lines, " §7(Press K to change Description)");
        }

        for (int i = 0; i < lines.length; i++) {
            int color = (i == 0) ? getModeColor()
                    : (i == 6) ? cutFillColor
                    : TEXT_COLOR;

            // Draw main line
            context.drawText(mc.textRenderer, lines[i], x, y + i * LINE_HEIGHT, color, false);

            // Draw Heading on the same line as Bg (first data line)
            if (i == 1) {
                String headingStr = "Heading: " + heading + "°";
                int headingX = x + BG_WIDTH - PAD_X - mc.textRenderer.getWidth(headingStr) - 4;
                context.drawText(mc.textRenderer, headingStr, headingX, y + i * LINE_HEIGHT, TEXT_COLOR, false);
            }
            // Right-justified Zoom on second line (Azimuth) when using Scope2
            if (i == 2 && isScope2Zooming(mc)) {
                double zoom = me.bluejay.client.LevelHeadedClient.getScope2ZoomFactor();
                String zoomStr = String.format("Zoom: %.0fx", zoom);
                int zoomX = x + BG_WIDTH - PAD_X - mc.textRenderer.getWidth(zoomStr) - 4;
                context.drawText(mc.textRenderer, zoomStr, zoomX, y + i * LINE_HEIGHT, ZOOM_COLOR, false);
            }
        }
    }

    private boolean isScope2Zooming(MinecraftClient mc) {
        return mc.player != null &&
                mc.player.getMainHandStack().isOf(me.bluejay.levelheaded.ModItems.SCOPE2) &&
                mc.options.useKey.isPressed();
    }

    private String[] appendDescriptionLine(String[] original, String descLine) {
        String[] newLines = new String[original.length + 1];
        System.arraycopy(original, 0, newLines, 0, original.length);
        newLines[original.length] = descLine;
        return newLines;
    }

    private int getCutFillColor(String cutFillStr) {
        if (cutFillStr.startsWith("C-")) return CUT_COLOR;
        if (cutFillStr.startsWith("F+")) return FILL_COLOR;
        return TEXT_COLOR;
    }

    protected void renderNoValidTarget(DrawContext context, MinecraftClient mc) {
        context.fill(4, 6, 225, 28, BG_COLOR);
        context.drawText(mc.textRenderer, "§e[ LevelHeaded ] §7Out of range", 10, 10, TEXT_COLOR, false);
    }

    protected void renderNoTransit(DrawContext context, MinecraftClient mc) {
        context.fill(4, 6, 225, 28, BG_COLOR);
        context.drawText(mc.textRenderer, "§e[ LevelHeaded ] §7No Transit Set", 10, 10, TEXT_COLOR, false);
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

    public static Mode getMode() {
        return mode;
    }

    public static void staticToggleHud() {
        hudEnabled = !hudEnabled;
    }

    public static void staticSaveCurrentShotAndReset() {
        if (mode != Mode.SHOT || cachedResult == null) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        Vec3d shotPos = null;
        String source = "Scope Shot";
        if (mc.player != null) {
            if (new RodHud().isHoldingToolPublic(mc.player)) {
                shotPos = new RodHud().getMovingPosition(mc, mc.player);
                source = "Rod Shot";
            } else if (new ScopeHud().isHoldingToolPublic(mc.player)) {
                shotPos = new ScopeHud().getMovingPosition(mc, mc.player);
                source = "Scope Shot";
            }
        }
        if (shotPos == null) shotPos = frozenPos;

        if (shotPos == null) return;

        String desc = stickyDescription.isEmpty() ? "Survey Shot" : stickyDescription;

        SurveyPointManager.addPoint(ClientTransitCache.getOccupyVec3d(), shotPos, cachedResult, source, desc);

        mc.player.sendMessage(
                Text.literal("§a" + source + " saved! " + desc + " (Shot #" + SurveyPointManager.getNextShotNumber() + ")"),
                false
        );

        mode = Mode.LIVE;
        cachedResult = null;
        frozenPos = null;
    }

    public static void toggleMode() {
        if (mode == Mode.LIVE) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null) {
                if (new RodHud().isHoldingToolPublic(mc.player)) {
                    frozenPos = new RodHud().getMovingPosition(mc, mc.player);
                } else if (new ScopeHud().isHoldingToolPublic(mc.player)) {
                    frozenPos = new ScopeHud().getMovingPosition(mc, mc.player);
                }
            }
            mode = Mode.SHOT;
        } else {
            mode = Mode.LIVE;
            frozenPos = null;
        }
    }

    public static String getStickyDescription() {
        return stickyDescription;
    }

    public static void setStickyDescription(String desc) {
        stickyDescription = (desc == null) ? "" : desc.trim();
    }
}