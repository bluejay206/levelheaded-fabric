package me.bluejay.levelheaded.core.math;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

/**
 * Server-side scope reach. Uses the server view distance so the ray covers
 * every chunk the server has loaded for the player.
 */
public final class RaycastRange {

    public static final double MAX_BLOCKS = 2048.0;
    public static final double MIN_BLOCKS = 64.0;
    public static final double MARGIN = 16.0;

    private RaycastRange() {}

    public static double server(PlayerEntity player) {
        int chunks = 32;
        if (player != null) {
            World world = player.getWorld();
            if (world instanceof ServerWorld serverWorld && serverWorld.getServer() != null) {
                chunks = serverWorld.getServer().getPlayerManager().getViewDistance();
            }
        }
        return clamp(chunks * 16.0 + MARGIN);
    }

    public static double clamp(double blocks) {
        if (blocks < MIN_BLOCKS) return MIN_BLOCKS;
        if (blocks > MAX_BLOCKS) return MAX_BLOCKS;
        return blocks;
    }
}
