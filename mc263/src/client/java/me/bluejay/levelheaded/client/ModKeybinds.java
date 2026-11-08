package me.bluejay.levelheaded.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.bluejay.levelheaded.LevelHeaded;
import me.bluejay.levelheaded.SurveyInput;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Java binding for {@link SurveyInput}. Each action is one rebindable key.
 * Bedrock binds the same actions to a key or controller button.
 *
 * 26.3 uses SDL scancodes ({@link InputConstants}), not GLFW.
 */
public final class ModKeybinds {

    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            LevelHeaded.id("assets/levelheaded")
    );

    public static KeyMapping ZOOM;
    public static KeyMapping SAVE_SHOT;
    public static KeyMapping SET_DESCRIPTION;

    private ModKeybinds() {}

    public static void register() {
        ZOOM = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.levelheaded.zoom",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_Z,
                CATEGORY
        ));
        SAVE_SHOT = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.levelheaded.save_shot",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_V,
                CATEGORY
        ));
        SET_DESCRIPTION = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.levelheaded.description",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_G,
                CATEGORY
        ));

        SurveyInput.setListener(ModKeybinds::onAction);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            if (SET_DESCRIPTION != null) {
                while (SET_DESCRIPTION.consumeClick()) {
                    SurveyInput.fire(SurveyInput.Action.SET_DESCRIPTION);
                }
            }
            if (SAVE_SHOT == null) return;
            while (SAVE_SHOT.consumeClick()) {
                SurveyInput.fire(SurveyInput.Action.SAVE_SHOT);
            }
        });
    }

    /** Java handler for the shared actions. */
    private static void onAction(SurveyInput.Action action) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        switch (action) {
            case SET_DESCRIPTION -> {
                if (client.gui.screen() == null) {
                    client.gui.setScreen(new DescriptionScreen());
                }
            }
            case SAVE_SHOT -> {
                if (!ShotCapture.captureLive()) {
                    overlay(client, "No occupy set. Set occupy before saving.");
                    return;
                }
                ShotLog.saveLast();
            }
        }
    }

    private static void overlay(Minecraft client, String message) {
        if (client.gui != null && client.gui.hud != null) {
            client.gui.hud.setOverlayMessage(Component.literal(message), false);
        }
    }
}