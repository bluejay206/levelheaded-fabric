package me.bluejay;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class TransitRegistry {

    public static final Block TRANSIT_BLOCK = Registry.register(
            Registries.BLOCK,
            Identifier.of(SurveyorSays.MOD_ID, "transit"),
            new TransitBlock(AbstractBlock.Settings.create().strength(2.0f).nonOpaque())
    );

    public static final BlockEntityType<TransitBlockEntity> TRANSIT_BLOCK_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier.of(SurveyorSays.MOD_ID, "transit"),
            BlockEntityType.Builder.create(TransitBlockEntity::new, TRANSIT_BLOCK).build(null)
    );

    public static final Item TRANSIT_BLOCK_ITEM = Registry.register(
            Registries.ITEM,
            Identifier.of(SurveyorSays.MOD_ID, "transit"),
            new BlockItem(TRANSIT_BLOCK, new Item.Settings())
    );

    public static void register() {
        // Simple console output instead of LOGGER to avoid initialization issues
        System.out.println("[SurveyorSays] Transit block, block entity, and item registered");
    }
}