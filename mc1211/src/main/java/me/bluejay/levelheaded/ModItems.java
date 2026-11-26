package me.bluejay.levelheaded;

import me.bluejay.levelheaded.item.DataCollectorItem;
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

    public static final Item COLLECTOR = Registry.register(
            Registries.ITEM,
            LevelHeaded.id("collector"),
            new DataCollectorItem(new Item.Settings().maxCount(1))
    );

    /** The survey scope is the vanilla spyglass. */
    public static final Item SCOPE = Items.SPYGLASS;

    public static void register() {
        LevelHeaded.LOGGER.info("Items registered (ROD, COLLECTOR, vanilla SPYGLASS as scope)");
    }
}