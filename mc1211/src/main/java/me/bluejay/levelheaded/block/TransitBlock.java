package me.bluejay.levelheaded.block;

import me.bluejay.levelheaded.SurveyState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TransitBlock extends Block {

    public TransitBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos,
                                 PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient()) {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 1.0;
            double z = pos.getZ() + 0.5;
            SurveyState.setOccupy(x, y, z, "Transit @" + pos.toShortString());
            player.sendMessage(Text.literal(
                    String.format("Occupy set: %.2f, %.2f, %.2f", x, y, z)), false);
        }
        return ActionResult.SUCCESS;
    }
}
