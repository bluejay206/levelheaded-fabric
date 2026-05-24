package me.bluejay.client;

import me.bluejay.levelheaded.ModItems;
import me.bluejay.client.hud.DescriptionInputScreen;
import me.bluejay.client.hud.SurveyorHud;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class SurveyorKeybinds {

    public static KeyBinding saveShotKey;
    public static KeyBinding toggleHudKey;
    public static KeyBinding toggleShotKey;
    public static KeyBinding stickyDescriptionKey;

    public static void register() {
        saveShotKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.levelheaded.save_shot",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "category.levelheaded.general"
        ));

        toggleHudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.levelheaded.toggle_hud",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "category.levelheaded.general"
        ));

        toggleShotKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.levelheaded.toggle_shot",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.levelheaded.general"
        ));

        stickyDescriptionKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.levelheaded.sticky_description",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "category.levelheaded.general"
        ));

        System.out.println("[LevelHeaded KEYBINDS] Keybinds registered successfully (V/H/R/K)");

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // V - Save shot
            if (saveShotKey.wasPressed()) {
                SurveyorHud.staticSaveCurrentShotAndReset();
            }

            // H - Toggle HUD
            if (toggleHudKey.wasPressed()) {
                SurveyorHud.staticToggleHud();
            }

            // R - Toggle SHOT mode
            if (toggleShotKey.wasPressed()) {
                boolean holdingRod = LevelHeadedClient.rodHud.isHoldingToolPublic(client.player);
                boolean holdingScope2 = client.player.getMainHandStack().isOf(ModItems.SCOPE2);

                if (holdingRod || holdingScope2) {
                    if (holdingRod) {
                        LevelHeadedClient.rodHud.requestUpdate();
                    } else {
                        LevelHeadedClient.scopeHud.requestUpdate();
                    }
                    SurveyorHud.toggleMode();
                }
            }

            // K - Sticky Description (only in SHOT mode)
            if (stickyDescriptionKey.wasPressed()) {
                if (SurveyorHud.getMode() == SurveyorHud.Mode.SHOT) {
                    MinecraftClient.getInstance().setScreen(new DescriptionInputScreen());
                }
            }
        });
    }
}