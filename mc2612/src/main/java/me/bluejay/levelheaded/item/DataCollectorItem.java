package me.bluejay.levelheaded.item;

import net.minecraft.world.item.Item;

/**
 * Field data collector. Right-click is handled on the client
 * so this class stays free of client imports.
 */
public final class DataCollectorItem extends Item {

    public DataCollectorItem(Properties properties) {
        super(properties);
    }
}