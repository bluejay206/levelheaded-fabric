package me.bluejay.levelheaded.client;

import me.bluejay.levelheaded.core.math.RaycastRange;
import net.minecraft.client.MinecraftClient;

public final class ClientRaycastRange {

    private ClientRaycastRange() {}

    public static double current() {
        MinecraftClient mc = MinecraftClient.getInstance();
        int chunks = 32;
        try {
            if (mc.options != null) {
                chunks = mc.options.getViewDistance().getValue();
            }
        } catch (Throwable ignored) {
            chunks = 32;
        }
        return RaycastRange.clamp(chunks * 16.0 + RaycastRange.MARGIN);
    }
}
