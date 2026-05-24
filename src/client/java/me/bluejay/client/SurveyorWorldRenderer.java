package me.bluejay.client;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.util.math.Vec3d;

public class SurveyorWorldRenderer {

    private static boolean renderCursor = false;
    private static Vec3d liveTarget = null;
    private static Vec3d frozenShot = null;

    public static void register() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(SurveyorWorldRenderer::onAfterTranslucent);
        System.out.println("[LevelHeaded] SurveyorWorldRenderer registered (3-axis cursor DISABLED to prevent native LWJGL crash)");
    }

    public static void setLiveTarget(Vec3d position) {
        liveTarget = position;
        renderCursor = position != null;
    }

    public static void setFrozenShot(Vec3d position) {
        frozenShot = position;
        renderCursor = position != null;
    }

    private static void onAfterTranslucent(WorldRenderContext context) {
        // NO-OP — cursor rendering disabled to achieve stable launch
        // All HUD, Scope spyglass zoom, keybinds (H/V/F), CSV export, and transit still work
    }

    public static void close() {
        liveTarget = null;
        frozenShot = null;
    }
}