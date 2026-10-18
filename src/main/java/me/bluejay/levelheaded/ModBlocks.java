package me.bluejay.levelheaded;

import me.bluejay.levelheaded.block.TransitBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class ModBlocks {

    public static final Block TRANSIT = register("transit",
            new TransitBlock(BlockBehaviour.Properties.of()
                    .setId(blockKey("transit"))
                    .mapColor(MapColor.METAL)
                    .strength(2.5f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    // Full-cube culling was hiding the top face of the block below.
                    .noOcclusion()));

    public static final Item TRANSIT_ITEM = registerBlockItem("transit", TRANSIT);

    private static Block register(String name, Block block) {
        return Registry.register(BuiltInRegistries.BLOCK, LevelHeaded.id(name), block);
    }

    private static Item registerBlockItem(String name, Block block) {
        return Registry.register(
                BuiltInRegistries.ITEM,
                LevelHeaded.id(name),
                new BlockItem(block, new Item.Properties().setId(itemKey(name)))
        );
    }

    private static ResourceKey<Block> blockKey(String name) {
        return ResourceKey.create(Registries.BLOCK, LevelHeaded.id(name));
    }

    private static ResourceKey<Item> itemKey(String name) {
        return ResourceKey.create(Registries.ITEM, LevelHeaded.id(name));
    }

    public static void register() {
        LevelHeaded.LOGGER.info("Blocks registered (TRANSIT)");
    }
}
