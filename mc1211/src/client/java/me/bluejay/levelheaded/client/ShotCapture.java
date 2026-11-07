package me.bluejay.levelheaded.client;

import me.bluejay.levelheaded.ModItems;
import me.bluejay.levelheaded.SurveyState;
import me.bluejay.levelheaded.core.math.SurveyMath;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

/**
 * Captures the reading the HUD is showing when V is pressed.
 * Does not listen for right-click, so the Scope spyglass use is left alone.
 */
public final class ShotCapture {

    private ShotCapture() {}

    /** Same target the HUD is showing right now. Returns false if nothing to log. */
    public static boolean captureLive() {
        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity player = mc.player;
        if (player == null || !SurveyState.hasOccupy()) return false;

        ClientSurveyMode.HudMode mode = ClientSurveyMode.hudMode();
        boolean holdingScope = player.getMainHandStack().isOf(ModItems.SCOPE)
                || player.getOffHandStack().isOf(ModItems.SCOPE);
        boolean holdingRod = player.getMainHandStack().isOf(ModItems.ROD)
                || player.getOffHandStack().isOf(ModItems.ROD);

        if (mode == ClientSurveyMode.HudMode.SCOPE || (mode == ClientSurveyMode.HudMode.OFF && holdingScope)) {
            return captureScope(player);
        }
        if (mode == ClientSurveyMode.HudMode.ROD || (mode == ClientSurveyMode.HudMode.OFF && holdingRod)) {
            return captureRod(player);
        }
        // No item and HUD off: still log the rod reading (player vs occupy).
        return captureRod(player);
    }

    private static boolean captureScope(PlayerEntity player) {
        HitResult hit = LevelHeadedClientCommands.raycast();
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) return false;
        // Exact raycast hit (fine-grained), not block center.
        Vec3d loc = hit.getPos();
        SurveyMath.Result result = SurveyState.ENGINE.scaledResult(loc.x, loc.y, loc.z);
        ShotLog.capture("SCOPE", loc.x, loc.y, loc.z, result, "use");
        return true;
    }

    private static boolean captureRod(PlayerEntity player) {
        Vec3d pos = player.getPos();
        SurveyMath.Result result = SurveyState.ENGINE.scaledResult(pos.x, pos.y, pos.z);
        ShotLog.capture("ROD", pos.x, pos.y, pos.z, result, "use");
        return true;
    }
}