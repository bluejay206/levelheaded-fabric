package me.bluejay.levelheaded;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item ROD = register("rod", new Item(new Item.Settings()));

    // Scope2 is now the only scope
    public static final Item SCOPE2 = register("scope2", new Scope2Item(new Item.Settings()));

    private static Item register(String name, Item item) {
        Identifier id = Identifier.of(LevelHeaded.MOD_ID, name);
        return Registry.register(Registries.ITEM, id, item);
    }

    public static void register() {
        System.out.println("[LevelHeaded] Items registered (ROD + SCOPE2 only)");
    }
}