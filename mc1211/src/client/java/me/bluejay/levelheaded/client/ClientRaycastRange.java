package me.bluejay.levelheaded.client;

import net.minecraft.client.MinecraftClient;

/**
 * Client scope reach: the player's effective render distance in blocks,
 * plus a margin so the far edge of the last visible chunk is hit.
 */
public final class ClientRaycastRange {

    private static final double MAX_BLOCKS = 2048.0;
    private static final double MIN_BLOCKS = 64.0;
    private static final double MARGIN = 16.0;

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
        return clamp(chunks * 16.0 + MARGIN);
    }

    private static double clamp(double blocks) {
        if (blocks < MIN_BLOCKS) return MIN_BLOCKS;
        if (blocks > MAX_BLOCKS) return MAX_BLOCKS;
        return blocks;
    }
}