package me.bluejay.levelheaded.client;

import me.bluejay.levelheaded.HudSettings;
import me.bluejay.levelheaded.LevelHeaded;
import me.bluejay.levelheaded.ModItems;
import me.bluejay.levelheaded.SurveyState;
import me.bluejay.levelheaded.core.data.Units;
import me.bluejay.levelheaded.core.math.SurveyMath;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Live survey HUD. Shows when either:
 * <ul>
 *   <li>Client mode is ROD or SCOPE ({@code /lh client hud ...}), or</li>
 *   <li>the player is holding the Rod/Scope items (original behaviour).</li>
 * </ul>
 * Client mode works on third-party SMPs with no custom items.
 */
public final class SurveyHud {

    private static final Identifier LAYER_ID = LevelHeaded.id("survey_hud");

    private SurveyHud() {}

    public static void register() {
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                LAYER_ID,
                SurveyHud::extract
        );
    }

    private static void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;
        if (!SurveyState.hasOccupy()) return;

        Player player = client.player;

        ClientSurveyMode.HudMode clientMode = ClientSurveyMode.hudMode();
        boolean holdingScope = player.getMainHandItem().is(ModItems.SCOPE)
                || player.getOffhandItem().is(ModItems.SCOPE);
        boolean holdingRod = player.getMainHandItem().is(ModItems.ROD)
                || player.getOffhandItem().is(ModItems.ROD);

        double tx, ty, tz;
        String modeLabel;

        if (clientMode == ClientSurveyMode.HudMode.SCOPE || (clientMode == ClientSurveyMode.HudMode.OFF && holdingScope)) {
            HitResult hit = LevelHeadedClientCommands.raycast();
            if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;
            var loc = ((BlockHitResult) hit).getLocation();
            tx = loc.x;
            ty = loc.y;
            tz = loc.z;
            modeLabel = clientMode == ClientSurveyMode.HudMode.SCOPE ? "SCOPE*" : "SCOPE";
        } else if (clientMode == ClientSurveyMode.HudMode.ROD || (clientMode == ClientSurveyMode.HudMode.OFF && holdingRod)) {
            tx = player.getX();
            ty = player.getY();
            tz = player.getZ();
            modeLabel = clientMode == ClientSurveyMode.HudMode.ROD ? "ROD*" : "ROD";
        } else {
            return;
        }

        SurveyMath.Result result = SurveyState.ENGINE.measure(tx, ty, tz);
        Units unit = SurveyState.ENGINE.getUnits();
        String scaleTag = SurveyState.SCALE.hudTag();

        double cutFill = result.cutFill(unit);
        int cutFillColor = cutFill < 0 ? 0xFFFF5555 : 0xFF55FF55;

        String angleLine = HudSettings.angleMode() == HudSettings.AngleMode.BEARING
                ? String.format("Brg %s", result.formatQuadrantBearing())
                : String.format("Az  %s", result.formatAzimuthDMS());

        String[] lines = {
                String.format("[%s][%s]", modeLabel, scaleTag),
                angleLine,
                String.format("VA  %s", result.formatZenithDMS()),
                String.format("Hz  %8.2f%s", result.horizontal(unit), unit.symbol()),
                String.format("ΔN  %8.2f", result.northing(unit)),
                String.format("ΔE  %8.2f", result.easting(unit)),
                String.format("C/F %8.2f%s", cutFill, unit.symbol())
        };

        int lineHeight = client.font.lineHeight + 2;
        int padding = 4;
        int maxWidth = 0;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, client.font.width(line));
        }
        int boxWidth = maxWidth + padding * 2;
        int boxHeight = lines.length * lineHeight + padding * 2;
        int x = 8;
        int y = 8;

        graphics.fill(x, y, x + boxWidth, y + boxHeight, 0x80000000);

        for (int i = 0; i < lines.length; i++) {
            int color = (i == lines.length - 1) ? cutFillColor : 0xFFFFFFFF;
            graphics.text(client.font, lines[i], x + padding, y + padding + i * lineHeight, color, true);
        }
    }
}
