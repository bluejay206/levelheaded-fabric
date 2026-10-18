package me.bluejay.levelheaded.client;

import me.bluejay.levelheaded.core.math.RaycastRange;
import net.minecraft.client.Minecraft;

/**
 * Client scope reach: the player's effective render distance in blocks,
 * plus a margin so the far edge of the last visible chunk is hit.
 */
public final class ClientRaycastRange {

    private ClientRaycastRange() {}

    public static double current() {
        Minecraft mc = Minecraft.getInstance();
        int chunks = 32;
        try {
            if (mc.options != null) {
                chunks = mc.options.getEffectiveRenderDistance();
            }
        } catch (Throwable ignored) {
            try {
                chunks = mc.options.renderDistance().get();
            } catch (Throwable ignored2) {
                chunks = 32;
            }
        }
        return RaycastRange.clamp(chunks * 16.0 + RaycastRange.MARGIN);
    }
}
