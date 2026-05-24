package me.bluejay.client.hud;

import me.bluejay.levelheaded.ModItems;
import me.bluejay.client.levelheaded.ClientTransitCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class ScopeHud extends SurveyorHud {

    @Override
    protected boolean isHoldingTool(PlayerEntity player) {
        return player.getMainHandStack().isOf(ModItems.SCOPE2);
    }

    @Override
    protected Vec3d getOccupyPoint() {
        return ClientTransitCache.getOccupyVec3d();
    }

    @Override
    protected Vec3d getMovingPosition(MinecraftClient mc, PlayerEntity player) {
        Vec3d eyePos = player.getEyePos();
        Vec3d direction = player.getRotationVector();

        var hit = mc.world.raycast(new RaycastContext(
                eyePos,
                eyePos.add(direction.multiply(1000.0)),
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                player
        ));

        // CRITICAL: Only use hit position if we actually hit a block
        // Otherwise return null → triggers "Out of range" in parent HUD
        if (hit.getType() == net.minecraft.util.hit.HitResult.Type.BLOCK) {
            return hit.getPos();
        } else {
            return null;   // Sky / void / no hit → Out of range
        }
    }
}