package me.bluejay.levelheaded.client;

import me.bluejay.levelheaded.SurveyInput;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class ModKeybinds {

    /** Yarn 1.21.1 uses a plain String category, not KeyBinding.Category. */
    public static final String CATEGORY = "key.categories.levelheaded";

    public static KeyBinding ZOOM;
    public static KeyBinding SAVE_SHOT;
    public static KeyBinding SET_DESCRIPTION;

    private ModKeybinds() {}

    public static void register() {
        ZOOM = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.levelheaded.zoom",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                CATEGORY
        ));
        SAVE_SHOT = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.levelheaded.save_shot",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                CATEGORY
        ));
        SET_DESCRIPTION = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.levelheaded.description",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                CATEGORY
        ));

        SurveyInput.setListener(ModKeybinds::onAction);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            if (SET_DESCRIPTION != null) {
                while (SET_DESCRIPTION.wasPressed()) {
                    SurveyInput.fire(SurveyInput.Action.SET_DESCRIPTION);
                }
            }
            if (SAVE_SHOT == null) return;
            while (SAVE_SHOT.wasPressed()) {
                SurveyInput.fire(SurveyInput.Action.SAVE_SHOT);
            }
        });
    }

    private static void onAction(SurveyInput.Action action) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        switch (action) {
            case SET_DESCRIPTION -> {
                if (client.currentScreen == null) {
                    client.setScreen(new DescriptionScreen());
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

    private static void overlay(MinecraftClient client, String message) {
        if (client.inGameHud != null) {
            client.inGameHud.setOverlayMessage(Text.literal(message), false);
        }
    }
}