package me.bluejay.levelheaded.client;

/**
 * Client-only survey mode. Independent of items/blocks so the mod works
 * on third-party SMPs that do not have LevelHeaded installed.
 */
public final class ClientSurveyMode {

    public enum HudMode {
        OFF,
        ROD,    // continuous: player position vs occupy
        SCOPE   // continuous: raycast target vs occupy
    }

    private static HudMode hudMode = HudMode.OFF;

    private ClientSurveyMode() {}

    public static HudMode hudMode() {
        return hudMode;
    }

    public static void setHudMode(HudMode mode) {
        hudMode = mode == null ? HudMode.OFF : mode;
    }

    public static boolean isActive() {
        return hudMode != HudMode.OFF;
    }
}
