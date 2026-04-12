package me.bluejay.client;

import me.bluejay.client.hud.SurveyorHud;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class SurveyorKeybinds {

    public static KeyBinding saveShotKey;
    public static KeyBinding toggleHudKey;

    public static void register() {
        // V - Save Shot
        saveShotKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.surveyorsays.save_shot",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "category.surveyorsays.general"
        ));

        // H - Toggle HUD on/off (changed from F to avoid switch hands conflict)
        toggleHudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.surveyorsays.toggle_hud",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "category.surveyorsays.general"
        ));

        // Tick handler for both keys
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // V key - Save current shot
            while (saveShotKey.wasPressed()) {
                if (SurveyorSaysClient.rodHud.getMode() == SurveyorHud.Mode.SHOT) {
                    SurveyorSaysClient.rodHud.saveCurrentShotAndReset();
                } else if (SurveyorSaysClient.scopeHud.getMode() == SurveyorHud.Mode.SHOT) {
                    SurveyorSaysClient.scopeHud.saveCurrentShotAndReset();
                }
            }

            // H key - Toggle entire HUD visibility
            while (toggleHudKey.wasPressed()) {
                SurveyorSaysClient.rodHud.toggleHud();
                SurveyorSaysClient.scopeHud.toggleHud();
            }
        });
    }
}