package me.bluejay.levelheaded.client;

import me.bluejay.levelheaded.PointLists;
import me.bluejay.levelheaded.ShotDescription;
import me.bluejay.levelheaded.ShotRate;
import me.bluejay.levelheaded.SurveyInput;
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

    /** Yarn 1.21.1 uses a plain String category, not KeyBinding.Category. */
    public static final String CATEGORY = "key.categories.levelheaded";

    public static KeyBinding SET_DESCRIPTION;
    public static KeyBinding SAVE_POINT;
    public static KeyBinding TOGGLE_MARKERS;

    private ModKeybinds() {}

    public static void register() {
        SET_DESCRIPTION = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.levelheaded.description",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                CATEGORY
        ));
        SAVE_POINT = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.levelheaded.save_point",
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

        SurveyInput.setListener(ModKeybinds::onAction);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            if (SET_DESCRIPTION != null) {
                while (SET_DESCRIPTION.wasPressed()) {
                    SurveyInput.fire(SurveyInput.Action.SET_DESCRIPTION);
                }
            }

            if (TOGGLE_MARKERS != null) {
                while (TOGGLE_MARKERS.wasPressed()) {
                    PointMarkers.toggle();
                }
            }
            PointMarkers.tick(client);

            if (SAVE_POINT != null) {
                while (SAVE_POINT.wasPressed()) { /* discard */ }
                if (SAVE_POINT.isPressed() && rateAllows(client)) {
                    SurveyInput.fire(SurveyInput.Action.SAVE_POINT);
                }
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
            case SAVE_POINT -> saveToStore(client);
        }
    }

    private static void saveToStore(MinecraftClient client) {
        if (!ShotCapture.captureLive()) {
            overlay(client, "Invalid shot, not saved.");
            return;
        }
        ShotLog.Shot s = ShotLog.last();
        if (s == null) return;
        String id = PointLists.STORE.nextId();
        // Same absolute coordinates the CSV uses: N = -Z, E = X, Z = Y.
        double northing = -s.toZ();
        double easting = s.toX();
        double elevation = s.toY();
        String desc = ShotDescription.get();
        if (desc.isEmpty()) desc = s.kind();
        PointLists.STORE.put(new ControlPoint(id, PointKind.TOPO, northing, easting, elevation, desc));
        JobStore.save();
        overlay(client, "Saved point " + id + " → " + PointKind.TOPO);
    }

    private static boolean rateAllows(MinecraftClient client) {
        if (client.world == null) return true;
        float partial = client.getRenderTickCounter().getTickDelta(false);
        double now = client.world.getTime() + partial;
        return ShotRate.allow(now);
    }

    private static void overlay(MinecraftClient client, String message) {
        if (client.inGameHud != null) {
            client.inGameHud.setOverlayMessage(Text.literal(message), false);
        }
    }
}