package me.bluejay.levelheaded;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LevelHeaded implements ModInitializer {
    public static final String MOD_ID = "levelheaded";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("LevelHeaded {} initializing for Minecraft 26.2", MOD_ID);

        ModItems.register();
        ModBlocks.register();
        ModTabs.register();
        LevelHeadedCommands.register();
        // No UseItemCallback: right-click must stay free for the Scope spyglass.
        // Shots are saved with V (ModKeybinds → ShotCapture.captureLive).
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
