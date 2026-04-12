package me.bluejay;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item ROD = register("rod", new Item(new Item.Settings()));
    public static final Item SCOPE = register("scope", new Item(new Item.Settings()));

    private static Item register(String name, Item item) {
        Identifier id = Identifier.of(SurveyorSays.MOD_ID, name);
        return Registry.register(Registries.ITEM, id, item);
    }

    public static void register() {
        // Use direct System.out for registration message to avoid LOGGER dependency issues
        System.out.println("[SurveyorSays] Items registered");
    }
}