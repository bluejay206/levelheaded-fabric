package me.bluejay.levelheaded.client;

import me.bluejay.levelheaded.ModItems;
import me.bluejay.levelheaded.SurveyState;
import me.bluejay.levelheaded.core.math.SurveyMath;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class ShotCapture {

    private ShotCapture() {}

    public static boolean captureLive() {
        var player = net.minecraft.client.Minecraft.getInstance().player;
        if (player == null || !SurveyState.hasOccupy()) return false;

        ClientSurveyMode.HudMode mode = ClientSurveyMode.hudMode();
        boolean holdingScope = player.getMainHandItem().is(ModItems.SCOPE)
                || player.getOffhandItem().is(ModItems.SCOPE);
        boolean holdingRod = player.getMainHandItem().is(ModItems.ROD)
                || player.getOffhandItem().is(ModItems.ROD);

        if (mode == ClientSurveyMode.HudMode.SCOPE || (mode == ClientSurveyMode.HudMode.OFF && holdingScope)) {
            return captureScope(player);
        }
        if (mode == ClientSurveyMode.HudMode.ROD || (mode == ClientSurveyMode.HudMode.OFF && holdingRod)) {
            return captureRod(player);
        }
        // HUD not active: do not save.
        return false;
    }

    private static boolean captureScope(Player player) {
        HitResult hit = LevelHeadedClientCommands.raycast();
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) return false;
        Vec3 loc = ((BlockHitResult) hit).getLocation();
        SurveyMath.Result result = SurveyState.ENGINE.scaledResult(loc.x, loc.y, loc.z);
        ShotLog.capture("SCOPE", loc.x, loc.y, loc.z, result, "use");
        return true;
    }

    private static boolean captureRod(Player player) {
        Vec3 pos = player.position();
        SurveyMath.Result result = SurveyState.ENGINE.scaledResult(pos.x, pos.y, pos.z);
        ShotLog.capture("ROD", pos.x, pos.y, pos.z, result, "use");
        return true;
    }
}