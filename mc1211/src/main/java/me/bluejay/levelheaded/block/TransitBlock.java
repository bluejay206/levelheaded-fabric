package me.bluejay.levelheaded.block;

import me.bluejay.levelheaded.PointLists;
import me.bluejay.levelheaded.SurveyState;
import me.bluejay.levelheaded.core.data.ControlPoint;
import me.bluejay.levelheaded.core.data.PointKind;
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
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;

        if (world.isClient()) {
            String id = PointLists.STORE.nextId();
            String desc = "TRANSIT#" + id;
            PointLists.STORE.put(new ControlPoint(
                    id, PointKind.CONTROL, -z, x, y, desc));
            SurveyState.setOccupy(x, y, z, desc);
            PointLists.changed();
            player.sendMessage(Text.literal(
                    String.format("Occupied %s  %.2f, %.2f, %.2f", desc, x, y, z)), false);
        }
        return ActionResult.SUCCESS;
    }
}