package me.bluejay.levelheaded;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ModTabs {

    public static final ResourceKey<CreativeModeTab> LEVELHEADED_TAB_KEY =
            ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), LevelHeaded.id("main"));

    public static final CreativeModeTab LEVELHEADED_TAB = FabricCreativeModeTab.builder()
            .icon(() -> new ItemStack(ModItems.ROD))
            .title(Component.translatable("itemGroup.levelheaded"))
            .displayItems((params, output) -> {
                output.accept(ModItems.ROD);
                output.accept(ModItems.SCOPE);
                output.accept(ModBlocks.TRANSIT_ITEM);
            })
            .build();

    public static void register() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, LEVELHEADED_TAB_KEY, LEVELHEADED_TAB);
        LevelHeaded.LOGGER.info("Creative tab registered");
    }
}
