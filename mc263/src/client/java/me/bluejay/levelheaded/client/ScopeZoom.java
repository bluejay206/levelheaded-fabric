package me.bluejay.levelheaded.client;

import me.bluejay.levelheaded.ModItems;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public final class ScopeZoom {
    private static final float ZOOM_SCALE = 0.25f; // ~4×
    private static float currentScale = 1.0f;
    private static boolean zooming = false;

    private ScopeZoom() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                zooming = false;
                currentScale = 1.0f;
                return;
            }
            Player p = client.player;
            boolean holdingScope = p.getMainHandItem().is(ModItems.SCOPE)
                    || p.getOffhandItem().is(ModItems.SCOPE);
            zooming = holdingScope && ModKeybinds.ZOOM.isDown();
            float target = zooming ? ZOOM_SCALE : 1.0f;
            currentScale = Mth.lerp(0.35f, currentScale, target);
        });
    }

    public static float getFovMultiplier() {
        return currentScale;
    }
}