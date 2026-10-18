package me.bluejay.levelheaded;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class ModItems {

    public static final Item ROD = register("rod", new Item(new Item.Properties()
            .setId(key("rod"))
            .stacksTo(1)));

    /** The survey scope is the vanilla spyglass. No custom item, no mixin. */
    public static final Item SCOPE = Items.SPYGLASS;

    private static Item register(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, LevelHeaded.id(name), item);
    }

    private static ResourceKey<Item> key(String name) {
        return ResourceKey.create(Registries.ITEM, LevelHeaded.id(name));
    }

    public static void register() {
        // Static fields above force registration on class load.
        // Creative tab is handled by ModTabs.
        LevelHeaded.LOGGER.info("Items registered (ROD, vanilla SPYGLASS as scope)");
    }
}
