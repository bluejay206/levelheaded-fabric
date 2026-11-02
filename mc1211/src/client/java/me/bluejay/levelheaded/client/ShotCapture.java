package me.bluejay.levelheaded.client;

import me.bluejay.levelheaded.ModItems;
import me.bluejay.levelheaded.SurveyState;
import me.bluejay.levelheaded.core.math.SurveyMath;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

/**
 * Captures the reading the HUD is showing when V is pressed.
 */
public final class ShotCapture {

    private ShotCapture() {}

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
            HitResult hit = LevelHeadedClientCommands.raycast();
            if (hit == null || hit.getType() != HitResult.Type.BLOCK) return false;
            var loc = ((BlockHitResult) hit).getBlockPos();
            double x = loc.getX() + 0.5;
            double y = loc.getY() + 0.5;
            double z = loc.getZ() + 0.5;
            SurveyMath.Result result = SurveyState.ENGINE.measure(x, y, z);
            ShotLog.capture("scope", x, y, z, result, "");
            return true;
        }

        if (mode == ClientSurveyMode.HudMode.ROD || (mode == ClientSurveyMode.HudMode.OFF && holdingRod)) {
            Vec3d pos = player.getPos();
            SurveyMath.Result result = SurveyState.ENGINE.measure(pos.x, pos.y, pos.z);
            ShotLog.capture("rod", pos.x, pos.y, pos.z, result, "");
            return true;
        }
        return false;
    }
}
