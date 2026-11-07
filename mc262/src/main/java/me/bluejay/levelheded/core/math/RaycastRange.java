package me.bluejay.levelheaded.core.math;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Server-side scope reach. Uses the server view distance so the ray covers
 * every chunk the server has loaded for the player. Vanilla max is 32 chunks
 * (512 blocks); the ceiling is higher so raised view-distance still works.
 * The ray still misses beyond loaded chunks.
 */
public final class RaycastRange {

    /** Absolute ceiling (blocks). 128 chunks. */
    public static final double MAX_BLOCKS = 2048.0;
    /** Floor so a bad read never collapses the scope. */
    public static final double MIN_BLOCKS = 64.0;
    /** Extra blocks so the far edge of the last loaded chunk is reachable. */
    public static final double MARGIN = 16.0;

    private RaycastRange() {}

    /** Server view distance in blocks, plus margin, clamped. */
    public static double server(Player player) {
        int chunks = 32;
        if (player != null) {
            Level level = player.level();
            if (level instanceof ServerLevel serverLevel && serverLevel.getServer() != null) {
                chunks = serverLevel.getServer().getPlayerList().getViewDistance();
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