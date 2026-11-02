package me.bluejay.levelheaded;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModItems {

    public static final Item ROD = Registry.register(
            Registries.ITEM,
            LevelHeaded.id("rod"),
            new Item(new Item.Settings().maxCount(1))
    );

    /** The survey scope is the vanilla spyglass. */
    public static final Item SCOPE = Items.SPYGLASS;

    public static void register() {
        LevelHeaded.LOGGER.info("Items registered (ROD, vanilla SPYGLASS as scope)");
    }
}
