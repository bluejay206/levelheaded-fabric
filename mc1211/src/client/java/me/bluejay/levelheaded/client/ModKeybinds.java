package me.bluejay.levelheaded.client;

import me.bluejay.levelheaded.PointLists;
import me.bluejay.levelheaded.ShotDescription;
import me.bluejay.levelheaded.core.data.ControlPoint;
import me.bluejay.levelheaded.core.data.PointKind;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class ModKeybinds {
    public static final String CATEGORY = "key.categories.levelheaded";
    public static KeyBinding SAVE_SHOT;
    public static KeyBinding TOGGLE_MARKERS;

    private ModKeybinds() {}

    public static void register() {
        SAVE_SHOT = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.levelheaded.save_shot",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                CATEGORY
        ));
        TOGGLE_MARKERS = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.levelheaded.markers",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                CATEGORY
        ));

        PointMarkers.registerRender();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            while (SAVE_SHOT.wasPressed()) {
                saveToStore(client);
            }
            while (TOGGLE_MARKERS.wasPressed()) {
                PointMarkers.toggle();
            }
            PointMarkers.tick(client);
        });
    }

    private static void saveToStore(MinecraftClient client) {
        if (!ShotCapture.captureLive()) {
            overlay(client, "Invalid shot, not saved.");
            return;
        }
        ShotLog.Shot s = ShotLog.last();
        if (s == null) return;
        String id = PointLists.STORE.nextId();
        double northing = -s.toZ();
        double easting = s.toX();
        double elevation = s.toY();
        String desc = ShotDescription.current();
        if (desc == null || desc.isBlank()) {
            desc = s.kind().equalsIgnoreCase("ROD") ? "rod" : "scope";
        }
        PointLists.STORE.put(new ControlPoint(id, PointKind.TOPO, northing, easting, elevation, desc));
        ShotLog.saveLast();
        overlay(client, "Shot saved.");
    }

    private static void overlay(MinecraftClient client, String message) {
        if (client.inGameHud != null) {
            client.inGameHud.setOverlayMessage(Text.literal(message), false);
        }
    }
}
