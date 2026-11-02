package me.bluejay.levelheaded;

import me.bluejay.levelheaded.block.TransitBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;

public final class ModBlocks {

    public static final Block TRANSIT = Registry.register(
            Registries.BLOCK,
            LevelHeaded.id("transit"),
            new TransitBlock(AbstractBlock.Settings.create()
                    .mapColor(MapColor.IRON_GRAY)
                    .strength(2.0f, 6.0f)
                    .sounds(BlockSoundGroup.METAL)
                    .requiresTool()
                    .nonOpaque())
    );

    public static final Item TRANSIT_ITEM = Registry.register(
            Registries.ITEM,
            LevelHeaded.id("transit"),
            new BlockItem(TRANSIT, new Item.Settings())
    );

    public static void register() {
        LevelHeaded.LOGGER.info("Blocks registered (TRANSIT)");
    }
}
