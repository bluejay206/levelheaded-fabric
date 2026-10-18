package me.bluejay.levelheaded.block;

import me.bluejay.levelheaded.SurveyState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class TransitBlock extends Block {

    public TransitBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            // Server: set occupy at the center of the block top
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 1.0;   // top of the block
            double z = pos.getZ() + 0.5;

            SurveyState.setOccupy(x, y, z, "Transit @" + pos.toShortString());
            player.sendSystemMessage(
                    Component.literal(String.format("Occupy set: %.2f, %.2f, %.2f", x, y, z)));
        }
        return InteractionResult.SUCCESS;
    }
}
