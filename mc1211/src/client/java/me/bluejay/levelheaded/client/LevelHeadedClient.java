package me.bluejay.levelheaded.client;

import net.fabricmc.api.ClientModInitializer;

public class LevelHeadedClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModKeybinds.register();
        SurveyHud.register();
        LevelHeadedClientCommands.register();
    }
}
