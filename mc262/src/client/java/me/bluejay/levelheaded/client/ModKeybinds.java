package me.bluejay.levelheaded.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.bluejay.levelheaded.*;
import me.bluejay.levelheaded.core.data.ControlPoint;
import me.bluejay.levelheaded.core.data.PointKind;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * Java binding for {@link SurveyInput}. Each action is one rebindable key.
 * Bedrock binds the same actions to a key or controller button.
 *
 * 26.2 still uses GLFW ({@code KEYSYM} + {@code GLFW.GLFW_KEY_*}).
 */
public final class ModKeybinds {

    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            LevelHeaded.id("assets/levelheaded")
    );

    public static KeyMapping SET_DESCRIPTION;
    public static KeyMapping TOGGLE_MARKERS;
    public static KeyMapping SAVE_POINT;

    private ModKeybinds() {}

    public static void register() {

        SET_DESCRIPTION = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.levelheaded.description",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                CATEGORY
        ));
        TOGGLE_MARKERS = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.levelheaded.markers",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                CATEGORY
        ));
        SAVE_POINT = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.levelheaded.save_point",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
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
            if (TOGGLE_MARKERS != null) {
                while (TOGGLE_MARKERS.consumeClick()) {
                    PointMarkers.toggle();
                }
            }
            PointMarkers.tick(client);
            if (SAVE_POINT != null) {
                while (SAVE_POINT.consumeClick()) { /* discard */ }
                if (SAVE_POINT.isDown() && rateAllows(client)) {
                    SurveyInput.fire(SurveyInput.Action.SAVE_POINT);
                }
            }
        });
    }

    private static void onAction(SurveyInput.Action action) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        switch (action) {
            case SET_DESCRIPTION -> {
                if (client.gui.screen() == null) {
                    client.gui.setScreen(new DescriptionScreen());
                }
            }
            case SAVE_POINT -> saveToStore(client);
        }
    }

    private static void saveToStore(Minecraft client) {
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
        String desc = ShotDescription.get();
        if (desc.isEmpty()) desc = s.kind();
        PointLists.STORE.put(new ControlPoint(id, PointKind.TOPO, northing, easting, elevation, desc));
        JobStore.save();
        overlay(client, "Saved point " + id + " → " + PointKind.TOPO);
    }

    private static boolean rateAllows(Minecraft client) {
        if (client.level == null) return true;
        float partial = 0f;
        if (client.getDeltaTracker() != null) {
            partial = client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        }
        double now = client.level.getGameTime() + partial;
        return ShotRate.allow(now);
    }

    private static void overlay(Minecraft client, String message) {
        if (client.gui != null && client.gui.hud != null) {
            client.gui.hud.setOverlayMessage(Component.literal(message), false);
        }
    }
}