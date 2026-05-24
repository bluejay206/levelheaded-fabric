package me.bluejay.levelheaded;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class TransitBlockEntity extends BlockEntity {

    public TransitBlockEntity(BlockPos pos, BlockState state) {
        super(TransitRegistry.TRANSIT_BLOCK_ENTITY, pos, state);
    }
}