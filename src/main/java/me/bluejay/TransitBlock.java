package me.bluejay;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TransitBlock extends Block implements BlockEntityProvider {

    public TransitBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TransitBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player,
                              BlockHitResult hit) {

        if (world.isClient) {
            SurveyorSays.updateClientTransitCache(pos);
            return ActionResult.SUCCESS;
        }

        // Server-side
        ServerTransitManager.setOccupyPos(world.getServer(), pos);

        // Broadcast to all players (multiplayer)
        SurveyorSays.broadcastOccupyPos(world.getServer(), pos);

        player.sendMessage(
                net.minecraft.text.Text.literal("§aTransit occupy point set at "
                        + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()),
                false
        );

        return ActionResult.SUCCESS;
    }
}