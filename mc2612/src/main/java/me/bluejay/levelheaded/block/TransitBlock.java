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
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;

        // Point creation is done on the client by UseBlockCallback.
        // This only sets occupy when the method actually runs here.
        if (level.isClientSide()) {
            SurveyState.setOccupy(x, y, z, "transit");
        }
        return InteractionResult.SUCCESS;
    }
}