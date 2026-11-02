package me.bluejay.levelheaded;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LevelHeaded implements ModInitializer {
    public static final String MOD_ID = "levelheaded";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("LevelHeaded {} initializing for Minecraft 1.21.1", MOD_ID);

        ModItems.register();
        ModBlocks.register();
        ModTabs.register();
        LevelHeadedCommands.register();
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
