package me.bluejay.levelheaded.client;

import net.fabricmc.api.ClientModInitializer;

/**
 * Client entry point.
 * Registers the HUD, client commands, and keybinds.
 */
public final class LevelHeadedClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        SurveyHud.register();
        LevelHeadedClientCommands.register();
        ModKeybinds.register();
    }
}
