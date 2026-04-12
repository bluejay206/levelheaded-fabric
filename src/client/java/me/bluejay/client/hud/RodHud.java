package me.bluejay.client.hud;

import me.bluejay.ModItems;
import me.bluejay.client.ClientTransitCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class RodHud extends SurveyorHud {

    @Override
    protected boolean isHoldingTool(PlayerEntity player) {
        return player.getMainHandStack().isOf(ModItems.ROD);
    }

    @Override
    protected Vec3d getOccupyPoint() {
        return ClientTransitCache.getOccupyVec3d();
    }

    @Override
    protected Vec3d getMovingPosition(MinecraftClient mc, PlayerEntity player) {
        return player.getEyePos();
    }
}