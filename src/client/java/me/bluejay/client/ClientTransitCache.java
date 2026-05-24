package me.bluejay.client;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ClientTransitCache {

    private static BlockPos occupyPos = BlockPos.ORIGIN;

    public static void setOccupyPos(BlockPos pos) {
        occupyPos = (pos != null) ? pos : BlockPos.ORIGIN;
        System.out.println("[LevelHeaded] Client cache updated → " + occupyPos);
    }

    public static BlockPos getOccupyPos() {
        return occupyPos;
    }

    public static Vec3d getOccupyVec3d() {
        return occupyPos.toCenterPos();
    }

    public static void clear() {
        occupyPos = BlockPos.ORIGIN;
    }
}