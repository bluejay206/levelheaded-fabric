package me.bluejay.client.hud;

import me.bluejay.ModItems;
import me.bluejay.client.ClientTransitCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class ScopeHud extends SurveyorHud {

    @Override
    protected boolean isHoldingTool(PlayerEntity player) {
        return player.getMainHandStack().isOf(ModItems.SCOPE);
    }

    @Override
    protected Vec3d getOccupyPoint() {
        return ClientTransitCache.getOccupyVec3d();
    }

    @Override
    protected Vec3d getMovingPosition(MinecraftClient mc, PlayerEntity player) {
        Vec3d eyePos = player.getEyePos();
        Vec3d direction = player.getRotationVector();

        // Long-range raycast (1000 blocks)
        BlockHitResult hit = mc.world.raycast(new RaycastContext(
                eyePos,
                eyePos.add(direction.multiply(1000.0)),
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                player
        ));

        if (hit.getType() == HitResult.Type.BLOCK) {
            return hit.getPos();
        }

        return null;   // Out of range or hitting sky
    }

    @Override
    public void requestUpdate() {
        // Only enter SHOT mode if we have a valid target
        Vec3d moving = getMovingPosition(MinecraftClient.getInstance(), MinecraftClient.getInstance().player);
        if (moving == null) {
            return;   // Do not lock into SHOT mode when out of range
        }
        super.requestUpdate();
    }
}