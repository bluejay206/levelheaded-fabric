package me.bluejay.levelheaded;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

public final class ModTabs {

    public static final RegistryKey<ItemGroup> LEVELHEADED_TAB_KEY =
            RegistryKey.of(RegistryKeys.ITEM_GROUP, LevelHeaded.id("main"));

    public static final ItemGroup LEVELHEADED_TAB = FabricItemGroup.builder()
            .icon(() -> new ItemStack(ModItems.ROD))
            .displayName(Text.translatable("itemGroup.levelheaded"))
            .entries((params, output) -> {
                output.add(ModItems.ROD);
                output.add(ModItems.SCOPE);
                output.add(ModBlocks.TRANSIT_ITEM);
            })
            .build();

    public static void register() {
        Registry.register(Registries.ITEM_GROUP, LEVELHEADED_TAB_KEY, LEVELHEADED_TAB);
        LevelHeaded.LOGGER.info("Creative tab registered");
    }
}
